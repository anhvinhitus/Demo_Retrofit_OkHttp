package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

public class UserEntity {

    @SerializedName("uid")
    public long uid;

    @SerializedName("server_ts")
    public long server_ts;

    @SerializedName("session")
    public String session;

}
