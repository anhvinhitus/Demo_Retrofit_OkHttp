package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.react.redpacket.RedPacketPayListener;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 19/07/2016.
 * This is payment service that RedPacket module used to pay by zaloPaymentSDK
 */
public class RedPacketPayServiceImpl implements IRedPacketPayService {
    final BalanceStore.Repository mBalanceRepository;
    final TransactionStore.Repository mTransactionRepository;
    private PaymentWrapper paymentWrapper;
    protected final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    public RedPacketPayServiceImpl(BalanceStore.Repository balanceRepository, TransactionStore.Repository transactionRepository) {
        this.mBalanceRepository = balanceRepository;
        this.mTransactionRepository = transactionRepository;
    }

    @Override
    public void pay(Activity activity, BundleOrder bundleOrder, final RedPacketPayListener listener) {

        final WeakReference<Activity> mWeakReference = new WeakReference<>(activity);

        this.paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(mBalanceRepository)
                .setTransactionRepository(mTransactionRepository)
                .setResponseListener(new PaymentResponseListener(listener, mWeakReference))
                .setRedirectListener(new DefaultPaymentRedirectListener(navigator) {
                    @Override
                    public Object getContext() {
                        if (mWeakReference == null) {
                            return null;
                        }
                        return mWeakReference.get();
                    }
                })
                .setShowNotificationLinkCard(false)
                .build();

        this.paymentWrapper.payWithOrder(activity, bundleOrder);
    }

//    @Override
//    public void payPendingOrder() {
//        if (paymentWrapper == null) {
//            return;
//        }
//        if (paymentWrapper.hasPendingOrder()) {
//            paymentWrapper.continuePayPendingOrder();
//        }
//    }

    public void destroyVariable() {
        paymentWrapper = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (paymentWrapper == null) {
            return;
        }

        paymentWrapper.onActivityResult(requestCode, resultCode, data);
    }

    private class PaymentResponseListener implements PaymentWrapper.IResponseListener {
        private final RedPacketPayListener mListener;
        private final WeakReference<Activity> mMWeakReference;

        public PaymentResponseListener(RedPacketPayListener listener, WeakReference<Activity> mWeakReference) {
            mListener = listener;
            mMWeakReference = mWeakReference;
        }

        @Override
        public void onParameterError(String param) {
            if (mListener != null) {
                mListener.onParameterError(param);
            }
        }

        @Override
        public void onResponseError(PaymentError paymentError) {
            if (mListener != null) {
                mListener.onResponseError(paymentError);
            }
        }

        @Override
        public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
            if (mListener != null) {
                mListener.onResponseSuccess(null);
            }
        }

        @Override
        public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {

        }

        @Override
        public void onResponseTokenInvalid() {

        }

        @Override
        public void onAppError(String msg) {
            if (mListener != null) {
                mListener.onAppError(msg);
            }
            destroyVariable();
        }

        @Override
        public void onNotEnoughMoney() {
            if (mMWeakReference == null || mMWeakReference.get() == null) {
                navigator.startDepositActivity(AndroidApplication.instance().getApplicationContext());
            } else {
                navigator.startDepositForResultActivity(mMWeakReference.get(), false);
            }
        }
    }

}
