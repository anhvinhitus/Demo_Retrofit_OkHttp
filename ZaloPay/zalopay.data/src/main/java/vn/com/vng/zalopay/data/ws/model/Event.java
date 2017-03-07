package vn.com.vng.zalopay.data.ws.model;

/**
 * Created by AnhHieu on 6/16/16.
 */
public class Event {

    public int msgType;

    public long mtaid;

    public long mtuid;

    public int sourceid;
    
    public boolean hasData;

    public Event(int msgType) {
        this.msgType = msgType;
    }

    public Event() {
    }
}
