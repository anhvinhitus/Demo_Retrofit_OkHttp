package vn.com.vng.zalopay.data.cache.model;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "MERCHANT_USER".
 */
@Entity
public class MerchantUser {

    @Id
    public long appid;
    public String mUid;
    public String mAccessToken;
    public String displayName;
    public String avatar;
    public String birthday;
    public Integer gender;

    @Generated
    public MerchantUser() {
    }

    public MerchantUser(long appid) {
        this.appid = appid;
    }

    @Generated
    public MerchantUser(long appid, String mUid, String mAccessToken, String displayName, String avatar, String birthday, Integer gender) {
        this.appid = appid;
        this.mUid = mUid;
        this.mAccessToken = mAccessToken;
        this.displayName = displayName;
        this.avatar = avatar;
        this.birthday = birthday;
        this.gender = gender;
    }

}
