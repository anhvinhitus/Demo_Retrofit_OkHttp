package vn.com.zalopay.wallet.transaction.behavior.authenpayer;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IAuthenPayer;
import vn.com.zalopay.wallet.api.task.AuthenPayerTask;
import vn.com.zalopay.wallet.api.task.BaseTask;

public class CAuthenPayer implements IAuthenPayer {
    @Override
    public void authenPayer(AdapterBase pAdapter, String pTransID, String authenType, String authenValue) {
        BaseTask atmAuthenPayerTask = new AuthenPayerTask(pAdapter, pTransID, authenType, authenValue);
        atmAuthenPayerTask.makeRequest();
    }
}
