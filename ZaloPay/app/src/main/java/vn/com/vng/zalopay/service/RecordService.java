package vn.com.vng.zalopay.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.utils.FileUtil;

/**
 * Created by longlv on 05/06/2016.
 * Sound record service
 */
public class RecordService extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private static final int SAMPLE_RATE = 44100;

    private String fileName = "";
    private boolean recording = false;
    private boolean onForeground = false;

    private BufferedOutputStream mBufferedOutputStream = null;
    private AudioRecord mAudioRecord;

    private Thread mThreadRecord;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("RecordService onStartCommand");
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        int commandType = intent.getIntExtra("commandType", 0);
        if (commandType == 0) {
            return super.onStartCommand(intent, flags, startId);
        }

        if (commandType == Constants.RECORDING_ENABLED) {
            Timber.d("RecordService RECORDING_ENABLED");
            if (!recording) {
                commandType = Constants.STATE_START_RECORDING;
            }
        } else if (commandType == Constants.RECORDING_DISABLED) {
            Timber.d("RecordService RECORDING_DISABLED");
            if (recording) {
                commandType = Constants.STATE_STOP_RECORDING;
            }
        }

        if (commandType == Constants.STATE_START_RECORDING) {
            Timber.d("RecordService STATE_START_RECORDING");
            if (!recording) {
                recording = true;
                startService();
                startRecording(intent);
            }
        } else if (commandType == Constants.STATE_STOP_RECORDING) {
            Timber.d("RecordService STATE_STOP_RECORDING");
            if (recording) {
                recording = false;
                stopService();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * in case it is impossible to mAudioRecord
     */
    private void terminateAndEraseFile() {
        Log.d(TAG, "RecordService terminateAndEraseFile");
        stopAndReleaseRecorder();
        recording = false;
        deleteFile();
    }

    private void stopService() {
        Log.d(TAG, "RecordService stopService");
        stopForeground(true);
        onForeground = false;
        this.stopSelf();
    }

    private void deleteFile() {
        Timber.v("RecordService deleteFile");
        FileUtil.deleteFile(fileName);
        fileName = null;
    }

    private void stopAndReleaseRecorder() {
        Timber.v("request to stop and release recorder");
        try {
            recording = false;
            if (mBufferedOutputStream != null) {
                mBufferedOutputStream.close();
            }
            mThreadRecord = null;
            if (mAudioRecord != null) {
                mAudioRecord.stop();
            }
        } catch (IOException e) {
            Timber.e(e, "Error when releasing");
        } finally {
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
            mAudioRecord = null;
            mThreadRecord = null;
        }
    }

    @Override
    public void onDestroy() {
        Timber.v("begin onDestroy");
        stopAndReleaseRecorder();
        stopService();
        super.onDestroy();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void startRecording(Intent intent) {
        Timber.d("RecordService startRecording");
        try {
            if (intent != null) {
                fileName = intent.getStringExtra(Constants.RECORDNAME);
            }
            Timber.d("RecordService startRecording fileName: %s", fileName);
            if (TextUtils.isEmpty(fileName)) {
                fileName = FileUtil.getFilename("Record_" + String.valueOf(System.currentTimeMillis()));
            }
            Timber.d("fileName: %s", fileName);
            mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // buffer size in bytes
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "Audio Record can't initialize!");
            return;
        }

        mAudioRecord.startRecording();

        mThreadRecord = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        mThreadRecord.start();
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte

        // buffer size in bytes
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        byte audioData[] = new byte[bufferSize];
        FileOutputStream os = null;
        try {
            fileName = FileUtil.getFilename("Record_" + String.valueOf(System.currentTimeMillis()));
            os = new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        AudioTrack audioTrack = new AudioTrack(
//                AudioManager.STREAM_MUSIC,
//                SAMPLE_RATE,
//                AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT,
//                bufferSize,
//                AudioTrack.MODE_STREAM);
//
//        audioTrack.setPositionNotificationPeriod(SAMPLE_RATE / 30); // 30 times per second
//        audioTrack.play();

        while (recording) {
            // gets the voice output from microphone to byte format
            Timber.d("read audio data.......time: %s", System.currentTimeMillis());
            int result = mAudioRecord.read(audioData, 0, bufferSize);
            if (result > 0) {
                try {
                    Timber.d("write data ngon roi heheehehehe");
//                    audioTrack.write(audioData, 0, audioData.length);

                    // // writes the data to file from buffer
                    // // stores the voice buffer
                    os.write(audioData, 0, audioData.length);
                } catch (IOException e) {
                    Timber.e(e, "exception in writing data");
                }
            } else if (result == AudioRecord.ERROR_INVALID_OPERATION) {
                Timber.d("Invalid operation error");
                break;
            } else if (result == AudioRecord.ERROR_BAD_VALUE) {
                Timber.e("Bad value error");
                break;
            } else if (result == AudioRecord.ERROR) {
                Timber.e("Unknown error");
                break;
            }

//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                break;
//            }
        }
        try {
            recording = false;
            os.close();
            Timber.v("about to stop audio record");
            mAudioRecord.stop();

        } catch (IOException e) {
            Timber.e(e, "Cannot stop");
        } finally {
            mAudioRecord.release();
            mAudioRecord = null;
            mThreadRecord = null;
        }
    }

    private void startService() {
        if (!onForeground) {
            Timber.d(TAG, "RecordService startService");
            Intent intent = new Intent(this, MainActivity.class);
            // intent.setAction(Intent.ACTION_VIEW);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getBaseContext(), 0, intent, 0);

            Notification notification = new NotificationCompat.Builder(
                    getBaseContext())
                    .setContentTitle(this.getString(R.string.recording))
                    .setTicker(this.getString(R.string.recording_ticker))
                    .setContentText(this.getString(R.string.recording_text))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent).setOngoing(true)
                    .getNotification();

            notification.flags = Notification.FLAG_NO_CLEAR;

            startForeground(1337, notification);
            onForeground = true;
        }
    }
}

