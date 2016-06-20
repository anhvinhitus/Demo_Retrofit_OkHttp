package vn.com.vng.zalopay.ui.presenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.zalopay.wallet.application.ZingMobilePayApplication;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    IHomeView homeView;

    private boolean isLoadedGateWayInfo;

    @Override
    public void setView(IHomeView iHomeView) {
        this.homeView = iHomeView;
    }

    @Override
    public void destroyView() {
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

    public void initializeAppConfig() {
        mAppResourceRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

    public void loadGatewayInfoPaymentSDK() {
        User user = userConfig.getCurrentUser();
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;
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
                Timber.w("loadGatewayInfoPaymentSDK error %s", pMessage);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline && !isLoadedGateWayInfo) {
            loadGatewayInfoPaymentSDK();
        }
    }
}
