package vn.com.vng.zalopay.sound.transcoder;

/**
 * Created by huuhoa on 6/7/16.
 * Transcoder - decoder - native wrapper
 * Create JNI wrapper from command line:
 * ```
 * $ javac vn/com/vng/zalopay/sound/transcoder/Decoder.java
 * $ javah -o ../jni/SoundDecoder.h -jni vn.com.vng.zalopay.sound.transcoder.Decoder
 * ```
 */
public class Decoder {

    private long nativeHandle;
    private final DecoderListener listener;

    public Decoder(DecoderListener listener) {
        this.listener = listener;
    }

    public native long initializeDecoder();
    public native long processBuffer(byte[] buffer);
    public native long releaseDecoder();

    public void onErrorDetectData() {
        if (listener == null) {
            return;
        }

        listener.errorDetectData();
    }

    public void onDidDetectData(byte[] data) {
        if (listener == null) {
            return;
        }

        listener.didDetectData(data);
    }
}
