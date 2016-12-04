package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hieuvm on 12/4/16.
 */

public class HandleInAppPayment {

    private final Activity mActivity;
    private PaymentWrapper paymentWrapper;

    @Inject
    BalanceStore.Repository mBalanceRepository;

    @Inject
    ZaloPayRepository mZaloPayRepository;

    @Inject
    TransactionStore.Repository mTransactionRepository;

    HandleInAppPayment(Activity activity) {
        mActivity = activity;
    }

    void initialize() {
        AndroidApplication.instance().getUserComponent().inject(this);

        if (paymentWrapper == null) {
            paymentWrapper = new PaymentWrapper(mBalanceRepository, mZaloPayRepository, mTransactionRepository, new PaymentWrapper.IViewListener() {
                @Override
                public Activity getActivity() {
                    return mActivity;
                }
            }, new PaymentWrapper.IResponseListener() {
                private String mTransactionId;

                @Override
                public void onParameterError(String param) {
                    Timber.d("onParameterError: %s", param);
                    Intent data = new Intent();

                    data.putExtra("code", -1);
                    mActivity.setResult(RESULT_OK, data);
                    mActivity.finish();
                }

                @Override
                public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {
                    mTransactionId = transId;
                }

                @Override
                public void onResponseError(PaymentError paymentError) {
                    Timber.d("onResponseError: %s", paymentError);
                    Intent data = new Intent();

                    data.putExtra("code", paymentError.value());
                    mActivity.setResult(RESULT_OK, data);
                    mActivity.finish();
                }

                @Override
                public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                    Timber.d("onResponseSuccess");
                    Intent data = new Intent();

                    data.putExtra("code", 1);
                    data.putExtra("transactionId", mTransactionId);
                    mActivity.setResult(RESULT_OK, data);
                    mActivity.finish();
                }

                @Override
                public void onResponseTokenInvalid() {
                }

                @Override
                public void onAppError(String msg) {
                }

                @Override
                public void onNotEnoughMoney() {
                }
            });
        }
    }

    void start(long appId, String zptranstoken) {
        paymentWrapper.payWithToken(appId, zptranstoken);
    }
}
