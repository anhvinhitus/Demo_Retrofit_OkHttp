package vn.com.zalopay.wallet.entity.config;

import com.google.gson.annotations.SerializedName;

public class DynamicEditText {
    @SerializedName("id")
    public String id;
    @SerializedName("errMess")
    public String errMess;
    @SerializedName("pattern")
    public boolean pattern = false;
}
