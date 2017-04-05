package vn.com.zalopay.wallet.message;

public class UpVersionMessage extends BaseEventMessage {
    public boolean forceupdate;
    public String message;
    public String version;
}
