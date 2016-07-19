package vn.com.vng.zalopay.account.ui.presenter;

import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.account.ui.view.IProfileView;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class ProfilePresenter extends BaseUserPresenter implements IPresenter<IProfileView> {

    IProfileView mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ProfilePresenter() {
    }

    @Override
    public void setView(IProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
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

    public void getProfile() {
        User user = userConfig.getCurrentUser();
        if (user != null) {
            mView.updateUserInfo(user);
        }
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
