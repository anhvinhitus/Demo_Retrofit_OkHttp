package vn.com.vng.zalopay.ui.presenter;

import android.os.Handler;
import android.text.TextUtils;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.transfer.ZaloFriendsFactory;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.zalopay.wallet.application.ZingMobilePayApplication;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    IHomeView homeView;

    ZaloFriendsFactory zaloFriendsFactory;

    public MainPresenter(ZaloFriendsFactory zaloFriendsFactory) {
        this.zaloFriendsFactory = zaloFriendsFactory;
    }

    public void getZaloFriend() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                zaloFriendsFactory.reloadZaloFriend(homeView.getActivity(), null);
            }
        }, 20000);
    }

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

    public void loadGatewayInfoPaymentSDK() {
        User user = userConfig.getCurrentUser();
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;
        ZingMobilePayApplication.loadGatewayInfo(homeView.getActivity(), paymentInfo, new ZPWGatewayInfoCallback() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onProcessing() {
            }

            @Override
            public void onError(String pMessage) {
                if (TextUtils.isEmpty(pMessage)) {
                    //Network error
                }
            }
        });
    }
}
