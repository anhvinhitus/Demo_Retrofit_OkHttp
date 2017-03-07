package vn.com.vng.zalopay.domain.model;

/**
 * Created by AnhHieu on 5/24/16.
 */

public class MerchantUserInfo {

    public long appId;
    public String displayname;
    public String birthdate;
    public long usergender;
    public String muid;
    public String maccesstoken;

    public MerchantUserInfo() {
    }

    public MerchantUserInfo(long appId) {
        this.appId = appId;
    }


}
