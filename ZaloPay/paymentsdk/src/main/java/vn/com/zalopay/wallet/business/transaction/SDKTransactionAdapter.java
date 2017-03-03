package vn.com.zalopay.wallet.business.transaction;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.ISDKTransaction;


public class SDKTransactionAdapter extends SingletonBase {
    private static SDKTransactionAdapter _object;
    protected ISDKTransaction mSDKTransaction;

    public SDKTransactionAdapter() {
        super();
    }

    public static SDKTransactionAdapter shared() {
        if (SDKTransactionAdapter._object == null) {
            SDKTransactionAdapter._object = new SDKTransactionAdapter();

            SDKTransactionAdapter._object.getTransaction();
        }

        return SDKTransactionAdapter._object;
    }

    public ISDKTransaction getTransaction() {
        if (GlobalData.isLinkCardChannel()) {
            mSDKTransaction = SDKMapCard.shared();
        } else {
            mSDKTransaction = SDKPayment.shared();
        }

        return mSDKTransaction;
    }

    public void startTransaction(AdapterBase pAdapter) throws Exception {
        if (mSDKTransaction == null) {
            throw new Exception("mSDKTransaction=null");
        }
        mSDKTransaction.doSubmit(pAdapter);
    }

    public void getTransactionStatus(AdapterBase pAdapter, String pZmpTransID, boolean pCheckData, String pMessage) throws Exception {
        if (mSDKTransaction == null) {
            throw new Exception("mSDKTransaction=null");
        }
        mSDKTransaction.getStatus(pAdapter, pZmpTransID, pCheckData, pMessage);
    }

    public void authenPayer(AdapterBase pAdapter, String pTransID, String authenType, String authenValue) throws Exception {
        if (mSDKTransaction == null) {
            throw new Exception("mSDKTransaction=null");
        }
        mSDKTransaction.authenPayer(pAdapter, pTransID, authenType, authenValue);
    }
}
