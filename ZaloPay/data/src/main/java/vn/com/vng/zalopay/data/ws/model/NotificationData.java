package vn.com.vng.zalopay.data.ws.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.Enums;

/**
 * Created by AnhHieu on 6/20/16.
 */
public class NotificationData extends Event {

    @SerializedName("transid")
    public long transid;

    @SerializedName("appid")
    public long appid;

    @SerializedName("timestamp")
    public long timestamp;

    @SerializedName("message")
    public String message;

    @SerializedName("embeddata")
    public NotificationEmbedData embeddata;

    @SerializedName("transtype")
    public long transtype;

    @SerializedName("notificationtype")
    public long notificationtype;

    @SerializedName("userid")
    public String userid;

    @SerializedName("destuserid")
    public String destuserid;

    @SerializedName("area")
    public long area;

    @Expose(deserialize = false, serialize = false)
    public long notificationstate;

    @Expose(deserialize = false, serialize = false)
    public long notificationId;

    public boolean isRead() {
        return notificationstate == Enums.NotificationState.READ.getId();
    }

    public JsonObject getEmbeddata() {
        if (embeddata != null) {
            return embeddata.object;
        } else {
            return null;
        }
    }

    public void setEmbeddata(JsonObject embeddata) {
        this.embeddata = new NotificationEmbedData(embeddata);
    }

}
