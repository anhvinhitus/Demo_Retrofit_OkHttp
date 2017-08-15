package vn.com.zalopay.wallet.business.entity.staticconfig;

import com.google.gson.annotations.SerializedName;

import vn.com.zalopay.wallet.constants.KeyboardType;

public class DKeyBoardConfig {
    @SerializedName("view")
    public String view;
    @SerializedName("bankcode")
    public String bankcode;
    @SerializedName("type")
    @KeyboardType
    public int type;
}
