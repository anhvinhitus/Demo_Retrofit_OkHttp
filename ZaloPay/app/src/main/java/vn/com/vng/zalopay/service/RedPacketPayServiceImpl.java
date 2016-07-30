package vn.com.vng.zalopay.service;

import android.app.Activity;

import java.lang.ref.WeakReference;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.mdl.redpacket.IRedPacketPayListener;
import vn.com.vng.zalopay.mdl.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

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
    public void pay(Activity activity, BundleOrder bundleOrder, final IRedPacketPayListener listener) {

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
            public void onResponseError(int status) {
                if (listener != null) {
                    listener.onResponseError(status);
                }
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                if (listener != null) {
                    listener.onResponseSuccess(null);
                }
            }

            @Override
            public void onResponseTokenInvalid() {

            }

            @Override
            public void onResponseCancel() {
                if (listener != null) {
                    listener.onResponseCancel();
                }
                destroyVariable();
            }

            @Override
            public void onNotEnoughMoney() {
                navigator.startDepositActivity(AndroidApplication.instance().getApplicationContext());
            }
        });

        this.paymentWrapper.payWithOrder(bundleOrder);
    }

    public void destroyVariable() {
        paymentWrapper = null;
    }

}
