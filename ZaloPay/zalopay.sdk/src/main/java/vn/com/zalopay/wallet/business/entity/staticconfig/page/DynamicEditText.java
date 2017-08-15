package vn.com.zalopay.wallet.business.entity.staticconfig.page;

import com.google.gson.annotations.SerializedName;

public class DynamicEditText {
    @SerializedName("id")
    public String id;
    @SerializedName("errMess")
    public String errMess;
    @SerializedName("pattern")
    public boolean pattern = false;
}
