package vn.com.vng.zalopay.share;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.SDKApplication;

/**
 * Created by khattn on 2/7/17.
 */

public class HandleZaloIntegration {

    @Inject
    BalanceStore.Repository mBalanceRepository;

    @Inject
    User mUser;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    void initialize() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    void getBalance() {
        Subscription subscription = mBalanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Long>());

        mCompositeSubscription.add(subscription);
    }

    private void loadGatewayInfoPaymentSDK(User user) {

        if (user == null) {
            return;
        }

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

}
