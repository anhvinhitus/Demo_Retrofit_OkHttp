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
    public int appid;

    @SerializedName("timestamp")
    public long timestamp;

    @SerializedName("message")
    public String message;

    @SerializedName("embeddata")
    public NotificationEmbedData embeddata;

    @SerializedName("transtype")
    public int transtype;

    @SerializedName("notificationtype")
    public int notificationtype;

    @SerializedName("userid")
    public String userid;

    @SerializedName("destuserid")
    public String destuserid;

    @Expose(deserialize = false, serialize = false)
    public int notificationstate;

    @Expose(deserialize = false, serialize = false)
    public long notificationId;


    public long getTransid() {
        return transid;
    }

    public void setTransid(long transid) {
        this.transid = transid;
    }

    public int getAppid() {
        return appid;
    }

    public void setAppid(int appid) {
        this.appid = appid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDestuserid() {
        return destuserid;
    }

    public void setDestuserid(String destuserid) {
        this.destuserid = destuserid;
    }

    public boolean isRead() {
        return notificationstate == Enums.NotificationState.READ.getId();
    }

    public void setNotificationState(int state) {
        this.notificationstate = state;
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

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    public int getNotificationType() {
        return notificationtype;
    }

    public void setNotificationtype(int notificationtype) {
        this.notificationtype = notificationtype;
    }

    public long getPackageid() {
        return getLongValue("packageid");
    }

    public long getBundleid() {
        return getLongValue("bundleid");
    }

    public String getAvatar() {
        return getStringValue("avatar");
    }

    public String getName() {
        return getStringValue("name");
    }

    public String getLiximessage() {
        return getStringValue("liximessage");
    }

    private String getStringValue(String propertyName) {
        String value = "";
        try {
            if (embeddata.object.has(propertyName)) {
                value = embeddata.object.get(propertyName).getAsString();
            }
        } catch (Exception e) {
            Timber.w(e, "exception while getting value for property: %s", propertyName);
        }
        return value;
    }

    private long getLongValue(String propertyName) {
        long value = -1;
        try {
            if (embeddata.object.has(propertyName)) {
                value = embeddata.object.get(propertyName).getAsLong();
            }
        } catch (Exception e) {
            Timber.w(e, "exception while getting value for property: %s", propertyName);
        }
        return value;
    }


}
