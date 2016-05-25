package vn.com.vng.zalopay.account.ui.presenter;

import vn.com.vng.zalopay.account.ui.view.IProfileView;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class ProfilePresenter extends BaseUserPresenter implements IPresenter<IProfileView> {

    IProfileView mView;
    private UserConfig mUserConfig;

    public ProfilePresenter(UserConfig userConfig) {
        mUserConfig = userConfig;
    }

    @Override
    public void setView(IProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {
        mView.updateUserInfo(userConfig.getCurrentUser());
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

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
