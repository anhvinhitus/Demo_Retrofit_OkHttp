package vn.com.zalopay.wallet.transaction.behavior.authenpayer;

import vn.com.zalopay.wallet.api.task.AuthenMapCardTask;
import vn.com.zalopay.wallet.api.task.BaseTask;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IAuthenPayer;

public class CAuthenPayerMapCard implements IAuthenPayer {
    @Override
    public void authenPayer(UserInfo pUserInfo, String pTransID, String authenType, String authenValue) {
        BaseTask authenMapCardTask = new AuthenMapCardTask(pUserInfo, pTransID, authenType, authenValue);
        authenMapCardTask.makeRequest();
    }
}
