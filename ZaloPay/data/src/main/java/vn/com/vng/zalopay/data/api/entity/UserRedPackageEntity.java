package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hieuvm on 11/25/16.
 * Để submit bundle lên server
 * Không sử dụng cho view
 */

public class UserRedPackageEntity {

    @SerializedName("zaloPayID")
    public String zaloPayID;

    @SerializedName("zaloID")
    public String zaloID;

    @SerializedName("zaloName")
    public String zaloName;

    @SerializedName("avatar")
    public String avatar;
}
