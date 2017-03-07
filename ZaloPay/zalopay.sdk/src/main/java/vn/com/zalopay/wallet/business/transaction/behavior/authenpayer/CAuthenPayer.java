package vn.com.zalopay.wallet.business.transaction.behavior.authenpayer;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IAuthenPayer;
import vn.com.zalopay.wallet.datasource.request.AuthenPayer;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;

public class CAuthenPayer implements IAuthenPayer {
    @Override
    public void authenPayer(AdapterBase pAdapter, String pTransID, String authenType, String authenValue) {

        BaseRequest atmAuthenPayerTask = new AuthenPayer(pAdapter, pTransID, authenType, authenValue);
        atmAuthenPayerTask.makeRequest();
    }
}
