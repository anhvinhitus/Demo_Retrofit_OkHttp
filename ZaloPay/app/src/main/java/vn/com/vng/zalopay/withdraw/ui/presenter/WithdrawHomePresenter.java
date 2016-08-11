package vn.com.vng.zalopay.withdraw.ui.presenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.ui.presenter.BaseZaloPayPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawHomeView;

/**
 * Created by longlv on 11/08/2016.
 */
public class WithdrawHomePresenter extends BaseZaloPayPresenter implements IPresenter<IWithdrawHomeView> {

    private IWithdrawHomeView mView;
    private User mUser;

    public WithdrawHomePresenter(User user) {
        mUser = user;
    }

    public void updateUserInfo() {
        mView.updateUserInfo(mUser);
    }

    @Override
    public void setView(IWithdrawHomeView iWithdrawView) {
        mView = iWithdrawView;
    }

    @Override
    public void destroyView() {

    }

    @Override
    public void resume() {
        eventBus.register(this);
        mView.updateBalance(balanceRepository.currentBalance());
        updateBalance();
        updateUserInfo();
    }

    @Override
    public void pause() {
        eventBus.unregister(this);
    }

    @Override
    public void destroy() {
        mView = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ChangeBalanceEvent event) {
        Timber.d("onEventMainThread ChangeBalanceEvent");
        if (mView != null) {
            mView.updateBalance(event.balance);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline) {
            updateBalance();
        }
    }
}
