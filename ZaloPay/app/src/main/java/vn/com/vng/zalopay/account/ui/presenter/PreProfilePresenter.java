package vn.com.vng.zalopay.account.ui.presenter;

import javax.inject.Inject;

import vn.com.vng.zalopay.account.ui.view.IPreProfileView;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.ui.presenter.BaseAppPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 19/05/2016.
 */
public class PreProfilePresenter extends BaseAppPresenter implements IPresenter<IPreProfileView> {

    IPreProfileView mView;

    @Inject
    UserConfig mUserConfig;

    @Inject
    public PreProfilePresenter() {
    }

    @Override
    public void setView(IPreProfileView iPreProfileView) {
        mView = iPreProfileView;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {
        mView.updateUserInfo(mUserConfig.getCurrentUser());
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.destroyView();
    }

    public void saveUserPhone(String phone) {
        mUserConfig.updateUserPhone(phone);
    }

    public void saveZaloPayName(String zaloPayName) {
        mUserConfig.updateZaloPayName(zaloPayName);
    }

    public void showLoading() {
        mView.showLoading();
    }

    public void hideLoading() {
        mView.hideLoading();
    }

    public void showRetry() {
        mView.showRetry();
    }

    public void hideRetry() {
        mView.hideRetry();
    }
}
