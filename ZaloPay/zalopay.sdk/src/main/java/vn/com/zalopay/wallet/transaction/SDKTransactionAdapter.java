package vn.com.zalopay.wallet.transaction;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.business.entity.base.PaymentCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.ISDKTransaction;


public class SDKTransactionAdapter extends SingletonBase {
    private static SDKTransactionAdapter _object;
    protected ISDKTransaction mSDKTransaction;
    protected WeakReference<AbstractWorkFlow> mAdapter;

    public SDKTransactionAdapter() {
        super();
    }

    public static SDKTransactionAdapter shared() {
        if (SDKTransactionAdapter._object == null) {
            SDKTransactionAdapter._object = new SDKTransactionAdapter();
        }
        return SDKTransactionAdapter._object;
    }

    public SDKTransactionAdapter setAdapter(AbstractWorkFlow adapterBase) {
        mAdapter = new WeakReference<>(adapterBase);
        if (mAdapter.get().getPaymentInfoHelper().isLinkTrans()) {
            mSDKTransaction = SDKMapCard.shared();
        } else {
            mSDKTransaction = SDKPayment.shared();
        }
        return this;
    }

    public void startTransaction(int channelId, UserInfo userInfo, PaymentCard card, PaymentInfoHelper paymentInfoHelper) throws Exception {
        if (mSDKTransaction == null) {
            throw new Exception("mSDKTransaction null");
        }
        mSDKTransaction.doSubmit(channelId, userInfo, card, paymentInfoHelper);
    }

    public void getTransactionStatus(String pZmpTransID, boolean pCheckData, String pMessage) throws Exception {
        if (mSDKTransaction == null || mAdapter.get() == null) {
            throw new Exception("mSDKTransaction null or adapter null");
        }
        mSDKTransaction.getStatus(mAdapter.get(), pZmpTransID, pCheckData, pMessage);
    }

    public void authenPayer(UserInfo pUserInfo, String pTransID, String authenType, String authenValue) throws Exception {
        if (mSDKTransaction == null) {
            throw new Exception("mSDKTransaction null");
        }
        mSDKTransaction.authenPayer(pUserInfo, pTransID, authenType, authenValue);
    }
}
