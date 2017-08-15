package vn.com.zalopay.wallet.entity.config;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.entity.config.DynamicEditText;

public class DynamicViewGroup {
    @SerializedName("EditText")
    public List<DynamicEditText> EditText;
    @SerializedName("View")
    public Map<String, Boolean> View;
}
