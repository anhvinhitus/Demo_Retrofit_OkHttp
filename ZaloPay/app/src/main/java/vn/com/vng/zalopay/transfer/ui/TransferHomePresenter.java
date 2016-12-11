package vn.com.vng.zalopay.transfer.ui;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by AnhHieu on 8/15/16.
 */
public class TransferHomePresenter extends BaseUserPresenter implements IPresenter<ITransferHomeView> {

    ITransferHomeView mView;
    CompositeSubscription compositeSubscription = new CompositeSubscription();
    private TransferStore.Repository mTransferRepository;

    @Inject
    public TransferHomePresenter(TransferStore.Repository transferRepository) {
        this.mTransferRepository = transferRepository;
    }

    @Override
    public void attachView(ITransferHomeView iTransferHomeView) {
        mView = iTransferHomeView;
    }

    @Override
    public void detachView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
        this.getRecent();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    public void getRecent() {
        Subscription subscription = mTransferRepository.getRecent()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RecentSubscriber());
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
}
