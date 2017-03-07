package vn.com.zalopay.wallet.business.transaction.behavior.authenpayer;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IAuthenPayer;
import vn.com.zalopay.wallet.datasource.request.AuthenMapCard;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;

public class CAuthenPayerMapCard implements IAuthenPayer {
    @Override
    public void authenPayer(AdapterBase pAdapter, String pTransID, String authenType, String authenValue) {
        BaseRequest authenMapCardTask = new AuthenMapCard(pAdapter, pTransID, authenType, authenValue);
        authenMapCardTask.makeRequest();
    }
}
