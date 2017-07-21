package vn.com.zalopay.wallet.transaction.behavior.interfaces;

import vn.com.zalopay.wallet.business.entity.user.UserInfo;

public interface IAuthenPayer {
    void authenPayer(UserInfo pUserInfo, String pTransID, String authenType, String authenValue);
}
