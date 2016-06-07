package vn.com.vng.zalopay.sound.transcoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by longlv on 05/06/2016.
 * Sound record service
 */
public class RecordService {
    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final boolean RECORDER_SAVE_FILE = false;

    private String fileName = "";

    private Thread mThreadRecord;
    private RecordingThread mRecordingThread;

    public boolean start(String fileName, DecoderListener decoderListener) {
        Timber.d("RecordService STATE_START_RECORDING");
        if (mThreadRecord != null && mThreadRecord.isAlive()) {
            Timber.d("RecordService skipped due to existing recording thread is running");
            return false;
        }

        this.fileName = fileName;
        startRecording(decoderListener);

        Timber.d("RecordService Started");
        return true;
    }

    public boolean stop() {
        Timber.d("RecordService STATE_STOP_RECORDING");
        if (mThreadRecord == null || !mThreadRecord.isAlive()) {
            Timber.d("RecordService skipped due to there is no existing recording thread");
            return false;
        }

        stopService();
        Timber.d("RecordService Stopped");
        return true;
    }

    private void stopService() {
        Timber.d("RecordService stopService");
        stopAndReleaseRecorder();
        if (RECORDER_SAVE_FILE) {
            copyWaveFile(getTempFilename(), fileName);
            Timber.i("Wav was recorded to %s", fileName);
            deleteFile();
        }
    }

    private void deleteFile() {
        Timber.v("RecordService deleteFile");
        String fileName = getTempFilename();
        if (fileName == null) {
            return;
        }
        Timber.d("FileHelper deleteFile " + fileName);
        try {
            File file = new File(fileName);

            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            Timber.e(e, "Exception");
        }
    }

    private void stopAndReleaseRecorder() {
        Timber.v("request to stop and release recorder");
        try {
            if (mThreadRecord != null && mThreadRecord.isAlive()) {
                mThreadRecord.interrupt();
                mThreadRecord.join();
            }
            mThreadRecord = null;
            mRecordingThread = null;
        } catch (InterruptedException e) {
            Timber.e(e, "Error when releasing");
        } finally {
            mThreadRecord = null;
        }
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

    private void startRecording(DecoderListener decoderListener) {
        Timber.d("RecordService startRecording");
        mRecordingThread = new RecordingThread(decoderListener);
        mThreadRecord = new Thread(mRecordingThread, "AudioRecorder Thread");
        mThreadRecord.start();
    }

    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    public void reset() {
        if (mRecordingThread != null) {
            mRecordingThread.resetTranscoder();
        }
    }

    private class RecordingThread implements Runnable {
        private FileOutputStream outputStream = null;
        private AudioRecord mAudioRecord;
        private byte audioDataBuffer[];
        private int bufferSize = 0;
        private Decoder transcoderDecode;

        public RecordingThread(DecoderListener decoderListener) {
            transcoderDecode = new Decoder(decoderListener);
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
            if (RECORDER_SAVE_FILE) {
                try {
                    outputStream = new FileOutputStream(getTempFilename());
                } catch (IOException e) {
                    Timber.e(e, "Exception in create new file");
                    return false;
                } catch (Exception e) {
                    Timber.e(e, "Generic Exception");
                    return false;
                }
            }

            // buffer size in bytes
            bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING);

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                bufferSize = RECORDER_SAMPLERATE * 2;
            }

            audioDataBuffer = new byte[bufferSize];

            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    bufferSize);

            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Timber.e("Audio Record can't initialize!");
                return false;
            }

            try {
                transcoderDecode.initializeDecoder();
            } catch (UnsatisfiedLinkError e) {
                Timber.e(e, "Exception in initializing transcoder");
                return false;
            }
            return true;
        }

        private boolean writeAudioDataToFile() {
            // gets the voice output from microphone to byte format
//            Timber.d("read audio data.......time: %s", System.currentTimeMillis());
            int result = mAudioRecord.read(audioDataBuffer, 0, bufferSize);
            if (result > 0) {
                try {
//                    Timber.d("got audio data: %d-%d", audioDataBuffer.length, result);
//                    audioTrack.write(audioData, 0, audioData.length);

                    // // writes the data to file from buffer
                    // // stores the voice buffer
                    if (RECORDER_SAVE_FILE) {
                        outputStream.write(audioDataBuffer);
                    }

                    try {
                        long processBuffer = transcoderDecode.processBuffer(audioDataBuffer);
                        Timber.i("processBuffer: %d", processBuffer);
                    } catch (UnsatisfiedLinkError e) {
                        Timber.e(e, "Error in JNI cal processBuffer");
                    }

                } catch (IOException e) {
                    Timber.e(e, "exception in writing data");
                } catch (Exception e) {
                    Timber.e(e, "exception in processing buffer");
                    return false;
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
                if (RECORDER_SAVE_FILE) {
                    outputStream.close();
                }

                Timber.v("about to stop audio record");
                mAudioRecord.stop();

                try {
                    transcoderDecode.releaseDecoder();
                    transcoderDecode = null;
                } catch (UnsatisfiedLinkError e) {
                    Timber.e(e, "Error in JNI");
                }
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

        public void resetTranscoder() {
            if (transcoderDecode != null) {
                transcoderDecode.releaseDecoder();
                transcoderDecode.initializeDecoder();
            }
        }
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = (RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1: 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[8000];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Timber.d("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }


    // Load library
    static {
        try {
            Timber.i("Loading transcoder service");
            System.loadLibrary("transcoder");
            Timber.i("DONE Loading transcoder service");
        } catch (Throwable ex) {
            Timber.e("Wrapper", "Error loading transcoder", ex);
        }
    }
}

