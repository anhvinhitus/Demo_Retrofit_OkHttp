package vn.com.vng.zalopay.data.ws.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import timber.log.Timber;

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
    public boolean read;

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
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public JsonObject getEmbeddata() {
        if (embeddata.object != null) {
            return embeddata.object;
        } else {
            return null;
        }
    }

    public void setEmbeddata(JsonObject embeddata) {
        this.embeddata = new NotificationEmbedData(embeddata);
    }

    public long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    public int getTransType() {
        return transtype;
    }

    public int getNotificationType() {
        return notificationtype;
    }

    public void setNotificationtype(int notificationtype) {
        this.notificationtype = notificationtype;
    }

    public long getPackageid() {
        long packetId = -1;
        try {
            if (embeddata.object.has("packageid")) {
                packetId = embeddata.object.get("packageid").getAsLong();
            }
        } catch (Exception e) {
            Timber.w(e, "exception : ");
        }
        return packetId;
    }

    public long getBundleid() {
        long bundleid = -1;
        try {
            if (embeddata.object.has("bundleid")) {
                bundleid = embeddata.object.get("bundleid").getAsLong();
            }
        } catch (Exception e) {
            Timber.w(e, "exception : ");
        }
        return bundleid;
    }

    public String getAvatar() {
        String avatar = "";
        try {
            if (embeddata.object.has("avatar")) {
                avatar = embeddata.object.get("avatar").getAsString();
            }
        } catch (Exception e) {
            Timber.w(e, "exception : ");
        }
        return avatar;
    }

    public String getName() {
        String name = "";
        try {
            if (embeddata.object.has("name")) {
                name = embeddata.object.get("name").getAsString();
            }
        } catch (Exception e) {
            Timber.w(e, "exception : ");
        }
        return name;
    }

    public String getLiximessage() {
        String liximessage = "";
        try {
            if (embeddata.object.has("liximessage")) {
                liximessage = embeddata.object.get("liximessage").getAsString();
            }
        } catch (Exception e) {
            Timber.w(e, "exception : ");
        }
        return liximessage;
    }
}
