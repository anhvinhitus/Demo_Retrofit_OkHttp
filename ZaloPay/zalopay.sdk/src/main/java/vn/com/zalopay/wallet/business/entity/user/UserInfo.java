package vn.com.zalopay.wallet.business.entity.user;

import android.text.TextUtils;

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.constants.TransactionType;

public class UserInfo {
    public String zalo_name;//account name from zalo
    public String zalopay_name;//accout name from zalopay.
    public String phonenumber;
    public String zalopay_userid;
    public String zalo_userid;
    public String accesstoken;
    public int level;
    public long balance;
    /***
     * user level,map table
     * list contain policy for allowing pay which channel by which level.
     */
    public String profile;
    public String avatar; //link http avatar on zalo, show in tranfer money result screen

    public boolean isValid() {
        return !TextUtils.isEmpty(zalopay_userid) && !TextUtils.isEmpty(accesstoken);
    }

    public boolean isProfileValid() {
        return !TextUtils.isEmpty(profile);
    }

    /***
     * check whether user is allowed payment this channel.
     */
    public int getPermissionByChannelMap(int pChannelID, @TransactionType int pTranstype) {
        if (!isProfileValid()) {
            return Constants.LEVELMAP_INVALID;
        }
        ListUserProfile userProfile = GsonUtils.fromJsonString(profile, ListUserProfile.class);
        if (userProfile == null) {
            return Constants.LEVELMAP_INVALID;
        }
        UserProfile allowProfile = null;
        for (int i = 0; i < userProfile.profilelevelpermisssion.size(); i++) {
            UserProfile mapProfile = userProfile.profilelevelpermisssion.get(i);
            if (mapProfile.pmcid == pChannelID && mapProfile.transtype == pTranstype) {
                allowProfile = mapProfile;
                break;
            }
        }
        return (allowProfile != null && allowProfile.allow) ? Constants.LEVELMAP_ALLOW : Constants.LEVELMAP_BAN;
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
        Log.d("checkForUpdateAccessTokenToApp", "old token = " + accesstoken);
        Log.d("checkForUpdateAccessTokenToApp", "new token = " + pResponse.accesstoken);
        if (GlobalData.getPaymentListener() != null && !accesstoken.equals(pResponse.accesstoken)) {
            //callback to app to update new access token
            GlobalData.getPaymentListener().onUpdateAccessToken(pResponse.accesstoken);
            accesstoken = pResponse.accesstoken;
            return true;
        }
        return false;
    }
}