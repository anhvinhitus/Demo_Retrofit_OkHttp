package vn.com.zalopay.wallet.transaction.behavior.authenpayer;

import vn.com.zalopay.wallet.api.task.AuthenPayerTask;
import vn.com.zalopay.wallet.api.task.BaseTask;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IAuthenPayer;

public class CAuthenPayer implements IAuthenPayer {
    @Override
    public void authenPayer(UserInfo userInfo, String pTransID, String authenType, String authenValue) {
        BaseTask atmAuthenPayerTask = new AuthenPayerTask(userInfo, pTransID, authenType, authenValue);
        atmAuthenPayerTask.makeRequest();
    }
}
