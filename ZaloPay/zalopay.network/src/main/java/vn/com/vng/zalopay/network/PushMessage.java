package vn.com.vng.zalopay.network;

/**
 * Created by AnhHieu on 6/16/16.
 */
public class PushMessage {

    public int msgType;

    public long mtaid;

    public long mtuid;

    public int sourceid;
    
    public boolean hasData;

    public PushMessage(int msgType) {
        this.msgType = msgType;
    }

    public PushMessage() {
    }
}
