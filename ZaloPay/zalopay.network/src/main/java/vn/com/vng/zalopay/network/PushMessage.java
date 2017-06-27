package vn.com.vng.zalopay.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 6/16/16.
 */
public class PushMessage {

    @SerializedName("msgType")
    public int msgType;

    @SerializedName("mtaid")
    public long mtaid;

    @SerializedName("mtuid")
    public long mtuid;

    @SerializedName("sourceid")
    public int sourceid;

    @SerializedName("hasData")
    public boolean hasData;

    @SerializedName("usrid")
    public long usrid;

    public PushMessage(int msgType) {
        this.msgType = msgType;
    }

    public PushMessage() {
    }
}
