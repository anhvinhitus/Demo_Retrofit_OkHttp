package vn.com.zalopay.wallet.business.entity.user;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class UserInfo {
    public String zalo_name;//account name from zalo
    public String zalopay_name;//accout name from zalopay.
    public String phonenumber;
    public String zalopay_userid;
    public String zalo_userid;
    public String accesstoken;
    public int level;
    public long balance;

    /**
     * user level,map table
     * list contain policy for allowing pay which channel by which level.
     */
    public String profile;
    public String avatar; //link http avatar on zalo, show in tranfer money result screen

    public boolean isValid() {
        return !TextUtils.isEmpty(zalopay_userid) && !TextUtils.isEmpty(accesstoken);
    }

    /***
     * check soft token
     * if has new accesstoken, must notify to app to update new token to cache again
     * @param pResponse
     * @return
     */
    public boolean checkForUpdateAccessTokenToApp(BaseResponse pResponse) {
        if (pResponse == null || TextUtils.isEmpty(pResponse.accesstoken)) {
            return false;
        }

        Timber.d("old token = %s", accesstoken);
        Timber.d("new token = %s", pResponse.accesstoken);
        if (GlobalData.getPaymentListener() == null || accesstoken.equals(pResponse.accesstoken)) {
            return false;
        }

        GlobalData.getPaymentListener().onUpdateAccessToken(pResponse.accesstoken);
        accesstoken = pResponse.accesstoken;
        return true;
    }
}