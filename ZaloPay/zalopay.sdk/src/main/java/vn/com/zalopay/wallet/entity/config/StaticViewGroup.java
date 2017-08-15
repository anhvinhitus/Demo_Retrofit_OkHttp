package vn.com.zalopay.wallet.entity.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.zalopay.wallet.entity.config.StaticView;

public class StaticViewGroup {
    @SerializedName("ImageView")
    public List<StaticView> ImageView;
    @SerializedName("TextView")
    public List<StaticView> TextView;
    @SerializedName("EditText")
    public List<StaticView> EditText;
}
