package vn.com.zalopay.wallet.transaction.behavior.base;

import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.business.entity.base.PaymentCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IAuthenPayer;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IDoSubmit;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IGetTransactionStatus;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.ISDKTransaction;

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
    public void doSubmit(int channelId, UserInfo userInfo, PaymentCard card, PaymentInfoHelper paymentInfoHelper) {
        this.mDoSubmit.doSubmit(channelId, userInfo, card, paymentInfoHelper);
    }

    @Override
    public void getStatus(AbstractWorkFlow pAdapter, String pTransID, boolean pCheckData, String pMessage) {
        this.mGetTransactionStatus.getStatus(pAdapter, pTransID, pCheckData, pMessage);
    }

    @Override
    public void authenPayer(UserInfo pUserInfo, String pTransID, String authenType, String authenValue) {
        mAuthenPayer.authenPayer(pUserInfo, pTransID, authenType, authenValue);
    }
}
