package vn.com.zalopay.wallet.event;

public class SdkDownloadResourceMessage {
    public boolean success;
    public String message;

    public SdkDownloadResourceMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
