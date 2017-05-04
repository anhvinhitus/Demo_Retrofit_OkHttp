package vn.com.zalopay.wallet.message;

public class SdkResourceInitMessage {
    public boolean success;
    public String message;

    public SdkResourceInitMessage(boolean pSuccess, String pMessage) {
        success = pSuccess;
        message = pMessage;
    }

    public SdkResourceInitMessage(boolean pSuccess) {
        success = pSuccess;
        message = null;
    }
}
