package vn.com.zalopay.wallet.message;

public class SdkUpVersionMessage {
    public boolean forceupdate;
    public String message;
    public String version;

    public SdkUpVersionMessage(boolean pForceUpdate, String pMessage, String pVersion) {
        forceupdate = pForceUpdate;
        message = pMessage;
        version = pVersion;
    }
}