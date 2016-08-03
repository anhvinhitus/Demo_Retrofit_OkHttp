package vn.com.vng.zalopay.data.ws.model;

/**
 * Created by AnhHieu on 6/16/16.
 */
public class Event {

    private int msgType;
    private long mtaid;
    private long mtuid;
    private int sourceid;
    private boolean hasData;

    public Event(int msgType) {
        this.msgType = msgType;
    }

    public Event() {
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public long getMtaid() {
        return mtaid;
    }

    public void setMtaid(long mtaid) {
        this.mtaid = mtaid;
    }

    public long getMtuid() {
        return mtuid;
    }

    public void setMtuid(long mtuid) {
        this.mtuid = mtuid;
    }

    public int getSourceid() {
        return sourceid;
    }

    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    public boolean hasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }
}
