package vn.com.vng.zalopay.scanners.ui;

/**
 * Created by huuhoa on 6/1/16.
 * Interface for handling NFC content
 */
public interface NfcView {
    void onReceiveString(String content);
    void onInitDone(boolean isEnable, String status);
}
