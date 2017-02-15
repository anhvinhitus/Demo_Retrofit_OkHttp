package vn.com.vng.zalopay.share;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.zpsdk.DefaultZPGatewayInfoCallBack;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;

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

}
