package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Singleton;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.view.ILeftMenuView;

/**
 * Created by AnhHieu on 5/11/16.
 */

@Singleton
public class LeftMenuPresenter extends BaseUserPresenter implements Presenter<ILeftMenuView> {
    private ILeftMenuView menuView;

    public LeftMenuPresenter() {
    }

    @Override
    public void setView(ILeftMenuView iLeftMenuView) {
        menuView = iLeftMenuView;
    }

    @Override
    public void destroyView() {
        menuView = null;
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

    public void getBalance() {
        zaloPayRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());
    }

    private final void onGetBalanceSuccess(Long balance) {

        Timber.tag(TAG).d("onGetBalanceSuccess %s", balance);
        menuView.setBalance(balance);
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        public BalanceSubscriber() {
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            Timber.tag(TAG).e(e, " exception ");
        }

        @Override
        public void onNext(Long aLong) {
            LeftMenuPresenter.this.onGetBalanceSuccess(aLong);
        }
    }
}
