package vn.com.zalopay.wallet.business.entity.gatewayinfo;


public class DAppInfo {
    public String appname = null;
    public long appid = Long.MIN_VALUE;
    public int status = -1;
    public int viewresulttype = 1;

    public boolean isAllow() {
        return status == 1;
    }
}
