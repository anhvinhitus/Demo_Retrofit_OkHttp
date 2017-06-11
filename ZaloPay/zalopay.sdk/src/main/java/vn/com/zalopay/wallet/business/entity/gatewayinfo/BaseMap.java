package vn.com.zalopay.wallet.business.entity.gatewayinfo;


public abstract class BaseMap {
    public String bankcode;

    public abstract String getKey();

    public abstract String getFirstNumber();

    public abstract void setFirstNumber(String pFirstNumber);

    public abstract String getLastNumber();

    public abstract void setLastNumber(String pLastNumber);

    public abstract boolean isValid();
}
