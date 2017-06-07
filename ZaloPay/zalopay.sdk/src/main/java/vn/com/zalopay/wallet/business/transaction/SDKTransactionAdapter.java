package vn.com.zalopay.wallet.business.transaction;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.ISDKTransaction;


public class SDKTransactionAdapter extends SingletonBase {
    private static SDKTransactionAdapter _object;
    protected ISDKTransaction mSDKTransaction;
    protected WeakReference<AdapterBase> mAdapter;

    public SDKTransactionAdapter() {
        super();
    }

    public static SDKTransactionAdapter shared() {
        if (SDKTransactionAdapter._object == null) {
            SDKTransactionAdapter._object = new SDKTransactionAdapter();
        }
        return SDKTransactionAdapter._object;
    }

    public SDKTransactionAdapter setAdapter(AdapterBase adapterBase) {
        mAdapter = new WeakReference<>(adapterBase);
        if (mAdapter.get().getPaymentInfoHelper().isLinkCardChannel()) {
            mSDKTransaction = SDKMapCard.shared();
        } else {
            mSDKTransaction = SDKPayment.shared();
        }
        return this;
    }

    public void startTransaction() throws Exception {
        if (mSDKTransaction == null || mAdapter.get() == null) {
            throw new Exception("mSDKTransaction null or adpater null");
        }
        mSDKTransaction.doSubmit(mAdapter.get());
    }

    public void getTransactionStatus(String pZmpTransID, boolean pCheckData, String pMessage) throws Exception {
        if (mSDKTransaction == null || mAdapter.get() == null) {
            throw new Exception("mSDKTransaction null or adpater null");
        }
        mSDKTransaction.getStatus(mAdapter.get(), pZmpTransID, pCheckData, pMessage);
    }

    public void authenPayer(String pTransID, String authenType, String authenValue) throws Exception {
        if (mSDKTransaction == null || mAdapter.get() == null) {
            throw new Exception("mSDKTransaction null or adpater null");
        }
        mSDKTransaction.authenPayer(mAdapter.get(), pTransID, authenType, authenValue);
    }
}
