package vn.com.zalopay.wallet.business.transaction.behavior.interfaces;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;

public interface IAuthenPayer {
    void authenPayer(AdapterBase pAdapter, String pTransID, String authenType, String authenValue);
}
