package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.PaymentWrapperException;
import vn.com.vng.zalopay.pw.AbsPWResponseListener;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.SDKApplication;

import static android.app.Activity.RESULT_OK;

/**
 * Created by hieuvm on 12/4/16.
 * *
 */

public class HandleInAppPayment {

    private final WeakReference<Activity> mActivity;
    @Inject
    BalanceStore.Repository mBalanceRepository;
    @Inject
    ZaloPayRepository mZaloPayRepository;
    @Inject
    TransactionStore.Repository mTransactionRepository;
    @Inject
    User mUser;
    private PaymentWrapper paymentWrapper;
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
                            paymentWrapper.payWithToken(mActivity.get(), appId, zptranstoken, ZPPaymentSteps.OrderSource_AppToApp);
                        }
                    }
                });

        mCompositeSubscription.add(subscription);
    }

    private PaymentWrapper getPaymentWrapper() {
        PaymentWrapper wrapper = new PaymentWrapperBuilder()
                .setResponseListener(new AbsPWResponseListener(mActivity) {

                    private String mTransactionId;

                    @Override
                    protected ILoadDataView getView() {
                        return null;
                    }

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
                }).build();
        wrapper.initializeComponents();
        return wrapper;
    }


    private void loadGatewayInfoPaymentSDK(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.zalo_userid = String.valueOf(user.zaloId);
        userInfo.zalopay_userid = user.zaloPayId;
        userInfo.accesstoken = user.accesstoken;
        String appVersion = BuildConfig.VERSION_NAME;
        Subscription[] subscriptions = SDKApplication.loadSDKData(userInfo, appVersion, new DefaultSubscriber());
        if (subscriptions != null && subscriptions.length > 0) {
            for (int i = 0; i < subscriptions.length; i++) {
                mCompositeSubscription.add(subscriptions[i]);
            }
        }
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
