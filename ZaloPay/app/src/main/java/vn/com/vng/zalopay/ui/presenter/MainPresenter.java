package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.zing.pay.zmpsdk.ZingMobilePayApplication;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.listener.ZPWGatewayInfoCallback;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    IHomeView homeView;

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
            }
        });
    }
}
