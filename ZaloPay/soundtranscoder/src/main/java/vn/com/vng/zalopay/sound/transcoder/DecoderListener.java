package vn.com.vng.zalopay.sound.transcoder;

/**
 * Created by huuhoa on 6/7/16.
 * Decoder listener interface
 */
public interface DecoderListener {
    void didDetectData(byte[] data);

    void errorDetectData();
}
