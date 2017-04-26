package vn.com.zalopay.wallet.business.entity.gatewayinfo;


public abstract class DBaseMap {
    public String bankcode;

    public abstract String getCardKey();

    public abstract String getFirstNumber();

    public abstract void setFirstNumber(String pFirstNumber);

    public abstract String getLastNumber();

    public abstract void setLastNumber(String pLastNumber);

    public abstract boolean isValid();
}
