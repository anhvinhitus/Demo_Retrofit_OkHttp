package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hieuvm on 11/25/16.
 * Kiểm tra tồn tại của user từ zaloid
 * Không sử dụng cho view
 */

public class ZaloPayUserEntity {

    @SerializedName("zaloid")
    public String zaloid;
    @SerializedName("userid")
    public String userid;
    @SerializedName("phonenumber")
    public long phonenumber;
    @SerializedName("zalopayname")
    public String zalopayname;
    @SerializedName("status")
    public long status; // = 1 exist

    @SerializedName("displayName")
    public String displayName;

    @SerializedName("avatar")
    public String avatar;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZaloPayUserEntity entity = (ZaloPayUserEntity) o;

        if (zaloid != null ? !zaloid.equals(entity.zaloid) : entity.zaloid != null) {
            return false;
        }

        return userid != null ? userid.equals(entity.userid) : entity.userid == null;
    }
}
