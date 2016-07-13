package vn.com.vng.zalopay.ui.presenter;

import android.text.TextUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.zalopay.wallet.application.ZingMobilePayApplication;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    IHomeView homeView;

    FriendStore.Repository mFriendRepository;

    private boolean isLoadedGateWayInfo;


    public MainPresenter(FriendStore.Repository friendRepository) {
        this.mFriendRepository = friendRepository;
    }

    public void getZaloFriend() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                if (homeView == null || homeView.getActivity() == null || mFriendRepository == null) {
                    return;
                }
                mFriendRepository.retrieveZaloFriendsAsNeeded().subscribe(new DefaultSubscriber<List<ZaloFriend>>());
            }
        }).start();
    }

    @Override
    public void setView(IHomeView iHomeView) {
        this.homeView = iHomeView;
    }

    @Override
    public void destroyView() {
        this.mFriendRepository = null;
        this.homeView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    public void initialize() {
        this.initializeAppConfig();
        this.loadGatewayInfoPaymentSDK();
    }

    private void initializeAppConfig() {
        mAppResourceRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

    private void loadGatewayInfoPaymentSDK() {
        User user = userConfig.getCurrentUser();
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloUserId = String.valueOf(user.zaloId);
        userInfo.zaloPayUserId = user.uid;
        userInfo.accessToken = user.accesstoken;
        paymentInfo.userInfo = userInfo;
        ZingMobilePayApplication.loadGatewayInfo(homeView.getActivity(), paymentInfo, new ZPWGatewayInfoCallback() {
            @Override
            public void onFinish() {
                Timber.d("loadGatewayInfoPaymentSDK finish");
                isLoadedGateWayInfo = true;
            }

            @Override
            public void onProcessing() {
            }

            @Override
            public void onError(String pMessage) {

                if (!TextUtils.isEmpty(pMessage)) {
                    Timber.d("loadGatewayInfoPaymentSDK onError %s", pMessage);
                } else {
                    Timber.d("loadGatewayInfoPaymentSDK onError null");
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline && !isLoadedGateWayInfo) {
            loadGatewayInfoPaymentSDK();
        }
    }


    public void logout() {
        passportRepository.logout()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
        applicationComponent.applicationSession().clearUserSession();
    }

}
