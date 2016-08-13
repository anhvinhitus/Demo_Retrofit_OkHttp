package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IEditAccountNameView;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
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
        unsubscribeIfNotNull(mCompositeSubscription);
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
        Timber.d("exist account name %s", accountName);
        Subscription subscription = accountRepository.checkZaloPayNameExist(accountName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ExistAccountNameSubscriber());
        mCompositeSubscription.add(subscription);
    }

    public void updateAccountName(String accountName) {
        Timber.d("update account name %s", accountName);
        Subscription subscription = accountRepository.updateZaloPayName(accountName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateAccountNameSubscriber());
        mCompositeSubscription.add(subscription);
    }

    private class ExistAccountNameSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            if (e instanceof BodyException && ((BodyException) e).errorCode == NetworkError.USE_EXISTED) {
                mView.accountNameValid(false);
            } else {
                mView.showError(ErrorMessageFactory.create(applicationContext, e));
            }
        }

        @Override
        public void onNext(Boolean resp) {
            mView.accountNameValid(resp);
        }
    }


    private class UpdateAccountNameSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(Boolean resp) {
        }
    }
}
