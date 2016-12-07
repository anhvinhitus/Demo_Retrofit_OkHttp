package vn.com.vng.zalopay.service;

import android.app.Activity;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.react.redpacket.RedPacketPayListener;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.navigation.Navigator;
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

        this.paymentWrapper = new PaymentWrapper(mBalanceRepository, null, mTransactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mWeakReference.get();
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                if (listener != null) {
                    listener.onParameterError(param);
                }
            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                if (listener != null) {
                    listener.onResponseError(paymentError);
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                if (listener != null) {
                    listener.onResponseSuccess(null);
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
                if (listener != null) {
                    listener.onAppError(msg);
                }
                destroyVariable();
            }

            @Override
            public void onNotEnoughMoney() {
                if (mWeakReference == null || mWeakReference.get() == null) {
                    navigator.startDepositActivity(AndroidApplication.instance().getApplicationContext());
                } else {
                    navigator.startDepositForResultActivity(mWeakReference.get());
                }
            }
        }, false);

        this.paymentWrapper.payWithOrder(bundleOrder);
    }

    @Override
    public void onDepositSuccess() {
        Timber.d("onDepositSuccess");
        if (paymentWrapper == null) {
            return;
        }
        if (paymentWrapper.hasPendingOrder()) {
            paymentWrapper.continuePayAfterDeposit();
        }
    }

    public void destroyVariable() {
        paymentWrapper = null;
    }

}
