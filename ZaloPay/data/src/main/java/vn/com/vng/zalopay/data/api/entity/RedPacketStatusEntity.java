package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hieuvm on 1/4/17.
 * *
 */

public class RedPacketStatusEntity {
    @SerializedName("packageID")
    public long packageID;
    @SerializedName("status")
    public long status;
}
