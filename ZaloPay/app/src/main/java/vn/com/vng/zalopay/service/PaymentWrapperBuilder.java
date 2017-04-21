package vn.com.vng.zalopay.service;

import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

public class PaymentWrapperBuilder {
    private BalanceStore.Repository mBalanceRepository;
    private ZaloPayRepository mZaloPayRepository;
    private TransactionStore.Repository mTransactionRepository;
    private PaymentWrapper.IResponseListener mResponseListener;
    private PaymentWrapper.IRedirectListener mRedirectListener = null;
    private PaymentWrapper.ILinkCardListener mLinkCardListener = null;
    private boolean mShowNotificationLinkCard = true;

    public PaymentWrapperBuilder setBalanceRepository(BalanceStore.Repository balanceRepository) {
        mBalanceRepository = balanceRepository;
        return this;
    }

    public PaymentWrapperBuilder setZaloPayRepository(ZaloPayRepository zaloPayRepository) {
        mZaloPayRepository = zaloPayRepository;
        return this;
    }

    public PaymentWrapperBuilder setTransactionRepository(TransactionStore.Repository transactionRepository) {
        mTransactionRepository = transactionRepository;
        return this;
    }

    public PaymentWrapperBuilder setResponseListener(PaymentWrapper.IResponseListener responseListener) {
        mResponseListener = responseListener;
        return this;
    }

    public PaymentWrapperBuilder setRedirectListener(PaymentWrapper.IRedirectListener redirectListener) {
        mRedirectListener = redirectListener;
        return this;
    }

    public PaymentWrapperBuilder setLinkCardListener(PaymentWrapper.ILinkCardListener linkCardListener) {
        mLinkCardListener = linkCardListener;
        return this;
    }

    public PaymentWrapperBuilder setShowNotificationLinkCard(boolean showNotificationLinkCard) {
        mShowNotificationLinkCard = showNotificationLinkCard;
        return this;
    }

    public PaymentWrapper build() {
        return new PaymentWrapper(mBalanceRepository, mZaloPayRepository, mTransactionRepository,
                mResponseListener, mRedirectListener, mLinkCardListener, mShowNotificationLinkCard);
    }
}