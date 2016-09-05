package vn.com.vng.zalopay.transfer.ui;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.transfer.ui.ITransferHomeView;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by AnhHieu on 8/15/16.
 */
public class TransferHomePresenter extends BaseUserPresenter implements IPresenter<ITransferHomeView> {

    ITransferHomeView mView;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    public TransferHomePresenter() {
    }

    @Override
    public void setView(ITransferHomeView iTransferHomeView) {
        mView = iTransferHomeView;
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

    public void getRecent() {
        Subscription subscription = transferRepository.getRecent()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RecentSubscriber());
        compositeSubscription.add(subscription);
    }

    public void getUserInfo(String zpName) {
        Subscription subscription = accountRepository.getUserInfoByZaloPayName(zpName)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UserInfoSubscriber(zpName));
        compositeSubscription.add(subscription);
    }

    private class RecentSubscriber extends DefaultSubscriber<List<RecentTransaction>> {

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "onError");
        }

        @Override
        public void onNext(List<RecentTransaction> recentTransactions) {
            Timber.d("onNext %s", recentTransactions.size());
            mView.setData(recentTransactions);
        }
    }

    private class UserInfoSubscriber extends DefaultSubscriber<Person> {

        String zaloPayName;

        public UserInfoSubscriber(String zaloPayName) {
            this.zaloPayName = zaloPayName;
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "onError");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            String message = ErrorMessageFactory.create(applicationContext, e);
            mView.showError(message);
        }

        @Override
        public void onNext(Person person) {
            mView.onGetProfileSuccess(person, zaloPayName);
        }
    }

}
