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
//    private boolean recording = false;
    private boolean onForeground = false;


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
            return super.onStartCommand(null, flags, startId);
        }

        int commandType = intent.getIntExtra(Constants.COMMANDTYPE, 0);
        if (commandType == 0) {
            return super.onStartCommand(intent, flags, startId);
        }

        switch (commandType) {
            case Constants.STATE_START_RECORDING:
                Timber.d("RecordService STATE_START_RECORDING");
                if (mThreadRecord == null || !mThreadRecord.isAlive()) {
                    startService();
                    startRecording(intent);
                }
                break;
            case Constants.STATE_STOP_RECORDING:
                Timber.d("RecordService STATE_STOP_RECORDING");
                if (mThreadRecord != null && mThreadRecord.isAlive()) {
                    stopService();
                }
                break;
            default:
                Timber.e("Unknown command [%d] passed to RecordService", commandType);
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * in case it is impossible to mAudioRecord
     */
    private void terminateAndEraseFile() {
        Log.d(TAG, "RecordService terminateAndEraseFile");
        stopAndReleaseRecorder();
//        recording = false;
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
            if (mThreadRecord != null && mThreadRecord.isAlive()) {
                mThreadRecord.interrupt();
                mThreadRecord.join();
            }
            mThreadRecord = null;
        } catch (InterruptedException e) {
            Timber.e(e, "Error when releasing");
        } finally {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        mThreadRecord = new Thread(new RecordingThread(fileName), "AudioRecorder Thread");
        mThreadRecord.start();
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

    private class RecordingThread implements Runnable {
        private BufferedOutputStream mBufferedOutputStream = null;
        private AudioRecord mAudioRecord;
        private byte audioDataBuffer[];
        public final String fileName;

        public RecordingThread(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            if (!initialize()) {
                return;
            }

            mAudioRecord.startRecording();

            boolean isGood = true;
            while (!Thread.currentThread().isInterrupted() && isGood) {
                isGood = writeAudioDataToFile();
            }

            cleanup();
        }

        private boolean initialize() {
            Timber.d("RecordService startRecording");
            try {
                mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName));
            } catch (IOException e) {
                Timber.e(e, "Exception in create new file");
                return false;
            } catch (Exception e) {
                Timber.e(e, "Generic Exception");
                return false;
            }

            // buffer size in bytes
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                bufferSize = SAMPLE_RATE * 2;
            }

            audioDataBuffer = new byte[bufferSize];

            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Timber.e("Audio Record can't initialize!");
                return false;
            }

            return true;
        }

        private boolean writeAudioDataToFile() {
            // gets the voice output from microphone to byte format
            Timber.d("read audio data.......time: %s", System.currentTimeMillis());
            int result = mAudioRecord.read(audioDataBuffer, 0, audioDataBuffer.length);
            if (result > 0) {
                try {
                    Timber.d("write data ngon roi heheehehehe");
//                    audioTrack.write(audioData, 0, audioData.length);

                    // // writes the data to file from buffer
                    // // stores the voice buffer
                    mBufferedOutputStream.write(audioDataBuffer, 0, audioDataBuffer.length);
                } catch (IOException e) {
                    Timber.e(e, "exception in writing data");
                }
            } else if (result == AudioRecord.ERROR_INVALID_OPERATION) {
                Timber.d("Invalid operation error");
                return false;
            } else if (result == AudioRecord.ERROR_BAD_VALUE) {
                Timber.e("Bad value error");
                return false;
            } else if (result == AudioRecord.ERROR) {
                Timber.e("Unknown error");
                return false;
            }

            return true;
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                break;
//            }

        }

        private void cleanup() {
            Timber.d("About to cleanup record thread");
            try {
//                recording = false;
                mBufferedOutputStream.close();
                Timber.v("about to stop audio record");
                mAudioRecord.stop();

            } catch (IOException e) {
                Timber.e(e, "Cannot stop");
            } catch (IllegalStateException e) {
                Timber.e(e, "wrong call sequence");
                mAudioRecord = null;
            } finally {
                if (mAudioRecord != null) {
                    mAudioRecord.release();
                }
                mAudioRecord = null;
//                mThreadRecord = null;
            }
        }
    }
}

