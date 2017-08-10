package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by datnt10 on 8/10/17.
 */

public class ZPCGetByPhone extends AbstractData {
    @SerializedName("userid")
    public String userId;
    @SerializedName("displayname")
    public String displayName;
    @SerializedName("phonenumber")
    public long phoneNumber;
    @SerializedName("zaloid")
    public long zaloId;
    @SerializedName("avatar")
    public String avatar;
    @SerializedName("zalopayname")
    public String zalopayName;
    @SerializedName("identity")
    public String identity;
    @SerializedName("profilelevel")
    public int profileLevel;
    @SerializedName("returncode")
    public int returnCode;
    @SerializedName("returnmessage")
    public String returnMessage;
    @SerializedName("accesstoken")
    public String accessToken;
}
