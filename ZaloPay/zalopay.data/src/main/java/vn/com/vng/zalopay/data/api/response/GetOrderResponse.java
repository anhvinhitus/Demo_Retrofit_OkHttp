package vn.com.vng.zalopay.data.api.response;

/**
 * Created by Longlv on 5/8/16.
 * *
 */
public class GetOrderResponse extends BaseResponse {
    public long appid;
    public String zptranstoken;
    public String apptransid;
    public String appuser;
    public long apptime;
    public String embeddata;
    public String item;
    public long amount;
    public String description;
    public String payoption;
    public String mac;

    public long getAppid() {
        return appid;
    }

    public String getZptranstoken() {
        return zptranstoken;
    }

    public void setAppid(long appid) {
        this.appid = appid;
    }

    public void setZptranstoken(String zptranstoken) {
        this.zptranstoken = zptranstoken;
    }
}
