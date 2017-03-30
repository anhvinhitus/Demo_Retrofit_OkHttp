package vn.com.zalopay.wallet.business.transaction.behavior.authenpayer;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IAuthenPayer;
import vn.com.zalopay.wallet.datasource.task.AuthenMapCardTask;
import vn.com.zalopay.wallet.datasource.task.BaseTask;

public class CAuthenPayerMapCard implements IAuthenPayer {
    @Override
    public void authenPayer(AdapterBase pAdapter, String pTransID, String authenType, String authenValue) {
        BaseTask authenMapCardTask = new AuthenMapCardTask(pAdapter, pTransID, authenType, authenValue);
        authenMapCardTask.makeRequest();
    }
}
