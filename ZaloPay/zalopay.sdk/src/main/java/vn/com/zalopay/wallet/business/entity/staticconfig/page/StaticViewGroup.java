package vn.com.zalopay.wallet.business.entity.staticconfig.page;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StaticViewGroup {
    @SerializedName("ImageView")
    public List<StaticView> ImageView;
    @SerializedName("TextView")
    public List<StaticView> TextView;
    @SerializedName("EditText")
    public List<StaticView> EditText;
}
