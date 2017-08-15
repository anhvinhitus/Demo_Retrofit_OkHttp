package vn.com.vng.zalopay.share;

import java.util.List;

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
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.zalopay.wallet.entity.UserInfo;
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
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            userComponent.inject(this);
            SDKApplication.getBuilder().setRetrofit(userComponent.retrofitConnector());
        }
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
        List<Subscription> subscriptions = SDKApplication.loadSDKData(userInfo, appVersion, new DefaultSubscriber<>());
        if (subscriptions != null) {
            mCompositeSubscription.addAll(subscriptions.toArray(new Subscription[0]));
        }
    }

    void loadPaymentSdk() {
        loadGatewayInfoPaymentSDK(mUser);
    }

}
