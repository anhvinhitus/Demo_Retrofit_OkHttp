package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.PaymentWrapperException;
import vn.com.vng.zalopay.service.AbsPWResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.zpsdk.DefaultZPGatewayInfoCallBack;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hieuvm on 12/4/16.
 */

public class HandleInAppPayment {

    private final WeakReference<Activity> mActivity;
    private PaymentWrapper paymentWrapper;

    @Inject
    BalanceStore.Repository mBalanceRepository;

    @Inject
    ZaloPayRepository mZaloPayRepository;

    @Inject
    TransactionStore.Repository mTransactionRepository;

    @Inject
    User mUser;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    HandleInAppPayment(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    void initialize() {
        AndroidApplication.instance().getUserComponent().inject(this);
        if (paymentWrapper == null) {
            paymentWrapper = getPaymentWrapper();
        }
    }

    void start(final long appId, final String zptranstoken) {
        Subscription subscription = mBalanceRepository.fetchBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        if (paymentWrapper != null) {
                            paymentWrapper.payWithToken(appId, zptranstoken);
                        }
                    }
                });

        mCompositeSubscription.add(subscription);
    }

    private PaymentWrapper getPaymentWrapper() {
        return new PaymentWrapper(mBalanceRepository, mZaloPayRepository, mTransactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return mActivity.get();
            }
        }, new AbsPWResponseListener(mActivity) {

            private String mTransactionId;

            @Override
            public void onError(PaymentWrapperException exception) {
                Activity act = mActivity.get();
                if (act == null) {
                    return;
                }

                Intent data = new Intent();
                data.putExtra("code", exception.getErrorCode());

                act.setResult(RESULT_OK, data);
                act.finish();
            }

            @Override
            public void onCompleted() {
                Activity act = mActivity.get();
                if (act == null) {
                    return;
                }

                Intent data = new Intent();
                data.putExtra("code", 1);
                data.putExtra("transactionId", mTransactionId);

                act.setResult(RESULT_OK, data);
                act.finish();
            }

            @Override
            public void onPreComplete(boolean isSuccessful, String pTransId, String pAppTransId) {
                mTransactionId = pTransId;
            }
        });
    }


    private void loadGatewayInfoPaymentSDK(User user) {
        final ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloUserId = String.valueOf(user.zaloId);
        userInfo.zaloPayUserId = user.zaloPayId;
        userInfo.accessToken = user.accesstoken;
        paymentInfo.userInfo = userInfo;
        WalletSDKApplication.loadGatewayInfo(paymentInfo, new DefaultZPGatewayInfoCallBack());
    }

    public void loadPaymentSdk() {
        loadGatewayInfoPaymentSDK(mUser);
    }

    public void cleanUp() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
    }
}
