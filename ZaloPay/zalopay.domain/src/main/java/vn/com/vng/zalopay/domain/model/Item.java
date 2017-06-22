package vn.com.vng.zalopay.domain.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by chucvv on 6/22/17.
 */

public class Item {
    @SerializedName("transtype")
    public int mTranstype;
    @SerializedName("ext")
    public String mExt;

    public Item(int transtype, String ext) {
        this.mTranstype = transtype;
        this.mExt = ext;
    }

    public static String tranferExtFormat() {
        return "Người nhận:%s";
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
