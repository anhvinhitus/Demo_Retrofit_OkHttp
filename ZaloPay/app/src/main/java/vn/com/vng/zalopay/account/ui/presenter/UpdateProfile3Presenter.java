package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IUpdateProfile3View;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by AnhHieu on 7/1/16.
 */
public class UpdateProfile3Presenter extends BaseUserPresenter implements IPresenter<IUpdateProfile3View> {

    IUpdateProfile3View mView;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setView(IUpdateProfile3View iUpdateProfile3View) {
        mView = iUpdateProfile3View;
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

    public void update(String identityNumber,
                       String email,
                       String fimgPath,
                       String bimgPath,
                       String avatarPath) {

        Timber.d("identityNumber %s email %s fimgPath %s bimgPath %s avatarPath %s", identityNumber, email, fimgPath, bimgPath, avatarPath);

        mView.showLoading();
        Subscription subscription = accountRepository.updateProfile3(identityNumber, email, fimgPath, bimgPath, avatarPath)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateSubscriber());
        compositeSubscription.add(subscription);
    }


    private final void onUpdateSuccess() {
        mView.hideLoading();
        mView.updateSuccess();
    }

    private final void onUpdateError(Throwable e) {
        mView.hideLoading();
        String message = ErrorMessageFactory.create(applicationContext, e);
        mView.showError(message);
    }

    private final class UpdateSubscriber extends DefaultSubscriber<Boolean> {
        public UpdateSubscriber() {
        }

        @Override
        public void onCompleted() {
            UpdateProfile3Presenter.this.onUpdateSuccess();
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }
            UpdateProfile3Presenter.this.onUpdateError(e);
        }
    }
}
