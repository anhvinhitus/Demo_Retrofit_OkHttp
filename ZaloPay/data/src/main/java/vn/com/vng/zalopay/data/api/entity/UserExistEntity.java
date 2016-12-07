package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hieuvm on 11/25/16.
 * Kiểm tra tồn tại của user từ zaloid
 * Không sử dụng cho view
 */

public class UserExistEntity {

    @SerializedName("zaloid")
    public String zaloid;
    @SerializedName("userid")
    public String userid;
    @SerializedName("phonenumber")
    public long phonenumber;
    @SerializedName("zalopayname")
    public String zalopayname;
    @SerializedName("status")
    public long status;
}
