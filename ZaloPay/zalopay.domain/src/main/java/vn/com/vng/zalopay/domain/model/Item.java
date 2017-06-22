package vn.com.vng.zalopay.domain.model;

import com.google.gson.Gson;

/**
 * Created by chucvv on 6/22/17.
 */

public class Item {
    public int transtype;
    public String ext;

    public Item(int transtype, String ext) {
        this.transtype = transtype;
        this.ext = ext;
    }

    public static String tranferExtFormat() {
        return "Người nhận:%s";
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
