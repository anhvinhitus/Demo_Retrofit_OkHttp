package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.ui.view.IZaloPayView;

/**
 * Created by AnhHieu on 5/9/16.
 */
public class ZaloPayPresenterImpl extends BaseUserPresenter implements ZaloPayPresenter<IZaloPayView> {

    private IZaloPayView mZaloPayView;

    @Override
    public void setView(IZaloPayView o) {
        this.mZaloPayView = o;
    }

    @Override
    public void destroyView() {
        this.mZaloPayView = null;
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

    @Override
    public void initialize() {
    }



}
