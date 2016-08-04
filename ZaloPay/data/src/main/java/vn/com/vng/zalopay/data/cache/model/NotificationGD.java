package vn.com.vng.zalopay.data.cache.model;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "NOTIFICATION_GD".
 */
public class NotificationGD {

    private Long id;
    private Long transid;
    private Integer appid;
    private Long timestamp;
    private String message;
    private String userid;
    private String destuserid;
    private Boolean read;
    private Integer notificationtype;
    private Long mtaid;
    private Long mtuid;
    private String embeddata;

    public NotificationGD() {
    }

    public NotificationGD(Long id) {
        this.id = id;
    }

    public NotificationGD(Long id, Long transid, Integer appid, Long timestamp, String message, String userid, String destuserid, Boolean read, Integer notificationtype, Long mtaid, Long mtuid, String embeddata) {
        this.id = id;
        this.transid = transid;
        this.appid = appid;
        this.timestamp = timestamp;
        this.message = message;
        this.userid = userid;
        this.destuserid = destuserid;
        this.read = read;
        this.notificationtype = notificationtype;
        this.mtaid = mtaid;
        this.mtuid = mtuid;
        this.embeddata = embeddata;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransid() {
        return transid;
    }

    public void setTransid(Long transid) {
        this.transid = transid;
    }

    public Integer getAppid() {
        return appid;
    }

    public void setAppid(Integer appid) {
        this.appid = appid;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
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

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Integer getNotificationtype() {
        return notificationtype;
    }

    public void setNotificationtype(Integer notificationtype) {
        this.notificationtype = notificationtype;
    }

    public Long getMtaid() {
        return mtaid;
    }

    public void setMtaid(Long mtaid) {
        this.mtaid = mtaid;
    }

    public Long getMtuid() {
        return mtuid;
    }

    public void setMtuid(Long mtuid) {
        this.mtuid = mtuid;
    }

    public String getEmbeddata() {
        return embeddata;
    }

    public void setEmbeddata(String embeddata) {
        this.embeddata = embeddata;
    }

}
