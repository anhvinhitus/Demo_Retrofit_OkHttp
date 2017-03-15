package vn.com.vng.zalopay.data.cache.model;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "NOTIFICATION_GD".
 */
@Entity(indexes = {
    @Index(value = "mtaid, mtuid", unique = true)
})
public class NotificationGD {

    @Id(autoincrement = true)
    public Long id;
    public Long transid;
    public Long appid;
    public Long timestamp;
    public String message;
    public String userid;
    public String destuserid;
    public Long area;
    public Long notificationstate;
    public Long notificationtype;
    public Long mtaid;
    public Long mtuid;
    public String embeddata;


}