package vn.com.zalopay.wallet.business.entity.gatewayinfo;


import com.google.gson.annotations.SerializedName;

public abstract class BaseMap {

    @SerializedName("bankcode")
    public String bankcode;

    @SerializedName("displayorder")
    public int displayorder;

    public abstract String getKey();

    public abstract String getFirstNumber();

    public abstract void setFirstNumber(String pFirstNumber);

    public abstract String getLastNumber();

    public abstract void setLastNumber(String pLastNumber);

    public abstract boolean isValid();
}
