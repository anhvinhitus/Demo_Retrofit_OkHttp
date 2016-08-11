package vn.com.vng.zalopay.withdraw.ui.presenter;

import vn.com.vng.zalopay.ui.presenter.BaseZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawView;

/**
 * Created by longlv on 11/08/2016.
 */
public class WithdrawPresenter extends BaseZaloPayPresenter implements IPresenter<IWithdrawView> {

    IWithdrawView mView;

    public void continueWithdraw(long amount) {
        mView.showError("Chức năng sẽ sớm được ra mắt.");
    }

    @Override
    public void setView(IWithdrawView iWithdrawView) {
        mView = iWithdrawView;
    }

    @Override
    public void destroyView() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        mView = null;
    }
}
