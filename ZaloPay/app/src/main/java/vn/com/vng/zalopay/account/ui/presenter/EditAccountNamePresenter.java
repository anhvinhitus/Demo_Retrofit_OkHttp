package vn.com.vng.zalopay.account.ui.presenter;

import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.account.ui.view.IEditAccountNameView;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by AnhHieu on 8/12/16.
 */
public class EditAccountNamePresenter extends BaseUserPresenter implements IPresenter<IEditAccountNameView> {

    IEditAccountNameView mView;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public EditAccountNamePresenter() {
    }

    @Override
    public void setView(IEditAccountNameView view) {
        mView = view;
    }

    @Override
    public void destroyView() {
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

    public void existAccountName(String accountName) {
        if (TextUtils.isEmpty(accountName)) {
            return;
        }
        Subscription subscription = accountRepository.checkZaloPayNameExist(accountName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ExistAccountNameSubscriber());
        mCompositeSubscription.add(subscription);
    }

    private class ExistAccountNameSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(Boolean isValid) {
        }
    }
}
