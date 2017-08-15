package vn.com.zalopay.wallet.business.entity.staticconfig.page;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class DynamicViewGroup {
    @SerializedName("EditText")
    public List<DynamicEditText> EditText;
    @SerializedName("View")
    public Map<String, Boolean> View;
}
