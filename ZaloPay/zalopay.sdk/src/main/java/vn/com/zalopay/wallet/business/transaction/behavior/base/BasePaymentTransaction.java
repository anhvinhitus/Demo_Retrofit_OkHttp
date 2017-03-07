package vn.com.zalopay.wallet.business.transaction.behavior.base;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IAuthenPayer;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IDoSubmit;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IGetTransactionStatus;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.ISDKTransaction;

public abstract class BasePaymentTransaction extends SingletonBase implements ISDKTransaction {
    protected IDoSubmit mDoSubmit;

    protected IGetTransactionStatus mGetTransactionStatus;

    protected IAuthenPayer mAuthenPayer;

    protected BasePaymentTransaction setDoSubmitInterface(IDoSubmit pDoSubmit) {
        this.mDoSubmit = pDoSubmit;

        return this;
    }

    protected BasePaymentTransaction setGetStatusInterface(IGetTransactionStatus pGetStatus) {
        this.mGetTransactionStatus = pGetStatus;

        return this;
    }

    protected BasePaymentTransaction setAuthenPayerInferface(IAuthenPayer pAuthenPayer) {
        this.mAuthenPayer = pAuthenPayer;

        return this;
    }

    @Override
    public void doSubmit(AdapterBase pAdapter) {
        this.mDoSubmit.doSubmit(pAdapter);
    }

    @Override
    public void getStatus(AdapterBase pAdapter, String pTransID, boolean pCheckData, String pMessage) {
        this.mGetTransactionStatus.getStatus(pAdapter, pTransID, pCheckData, pMessage);
    }

    @Override
    public void authenPayer(AdapterBase pAdapter, String pTransID, String authenType, String authenValue) {
        mAuthenPayer.authenPayer(pAdapter, pTransID, authenType, authenValue);
    }
}
