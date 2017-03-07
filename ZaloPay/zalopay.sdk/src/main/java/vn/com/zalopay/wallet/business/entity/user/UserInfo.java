package vn.com.zalopay.wallet.business.entity.user;

import android.text.TextUtils;

public class UserInfo {
    public String userName;//account name from zalo
    public String zaloPayName;//accout name from zalopay.
    public String phoneNumber;
    public String zaloPayUserId;
    public String zaloUserId;
    public String accessToken;
    public int level;
    public long balance;
    public String userProfile;

    public boolean isUserInfoValid() {
        return !TextUtils.isEmpty(zaloPayUserId) && !TextUtils.isEmpty(accessToken);
    }

    public boolean isUserProfileValid() {
        return !TextUtils.isEmpty(userProfile);
    }
}