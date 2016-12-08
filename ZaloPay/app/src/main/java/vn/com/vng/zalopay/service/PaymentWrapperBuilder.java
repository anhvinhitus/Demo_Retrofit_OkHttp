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

    public PaymentWrapperBuilder setShowNotificationLinkCard(boolean showNotificationLinkCard) {
        mShowNotificationLinkCard = showNotificationLinkCard;
        return this;
    }

    public PaymentWrapper build() {
//        if (mBalanceRepository == null) {
//            throw new IllegalArgumentException("BalanceRepository should not be null");
//        }
//
//        if (mZaloPayRepository == null) {
//            throw new IllegalArgumentException("ZaloPayRepository should not be null");
//        }
//
//        if (mTransactionRepository == null) {
//            throw new IllegalArgumentException("TransactionRepository should not be null");
//        }
//
//        if (mViewListener == null) {
//            throw new IllegalArgumentException("ViewListener should not be null");
//        }
//
//        if (mResponseListener == null) {
//            throw new IllegalArgumentException("ResponseListener should not be null");
//        }
//
        return new PaymentWrapper(mBalanceRepository, mZaloPayRepository, mTransactionRepository,
                mResponseListener, mRedirectListener, mShowNotificationLinkCard);
    }
}