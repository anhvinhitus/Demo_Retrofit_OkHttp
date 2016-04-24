package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.ui.view.ILoginView;

/**
 * Created by AnhHieu on 3/26/16.
 */

@Singleton
public final class LoginPresenter extends BaseAppPresenter implements Presenter<ILoginView> {

    private ILoginView mView;

    @Inject
    public LoginPresenter() {
    }

    @Override
    public void setView(ILoginView view) {
        this.mView = view;
    }

    @Override
    public void destroyView() {
        this.mView = null;
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

    public void login(String user, String password) {
        
    }
}
