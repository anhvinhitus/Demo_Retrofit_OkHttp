package vn.com.zalopay.wallet.transaction.behavior.interfaces;

import vn.com.zalopay.wallet.entity.UserInfo;

public interface IAuthenPayer {
    void authenPayer(UserInfo pUserInfo, String pTransID, String authenType, String authenValue);
}
