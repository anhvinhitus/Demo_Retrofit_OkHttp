package vn.com.vng.zalopay.data.ws.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    public JsonObject embeddata;

    @SerializedName("transtype")
    public int transtype;

    @SerializedName("notificationtype")
    public int notificationtype;

    @SerializedName("userid")
    public String userid;

    @SerializedName("destuserid")
    public String destuserid;

    @SerializedName("bundleid")
    public long bundleid;

    @SerializedName("packageid")
    public long packageid;

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
        return embeddata;
    }

    public void setEmbeddata(JsonObject embeddata) {
        this.embeddata = embeddata;
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

    public long getBundleid() {
        return bundleid;
    }

    public long getPackageid() {
        return packageid;
    }

    public void setBundleid(long bundleid) {
        this.bundleid = bundleid;
    }

    public void setPackageid(long packageid) {
        this.packageid = packageid;
    }
}
