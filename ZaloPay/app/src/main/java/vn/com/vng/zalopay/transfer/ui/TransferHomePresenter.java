package vn.com.vng.zalopay.transfer.ui;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.transfer.TransferStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

/**
 * Created by AnhHieu on 8/15/16.
 * *
 */
public class TransferHomePresenter extends AbstractPresenter<ITransferHomeView> {

    private TransferStore.Repository mTransferRepository;
    private EventBus mEventBus;

    @Inject
    public TransferHomePresenter(TransferStore.Repository transferRepository,
                                 EventBus eventBus) {
        this.mTransferRepository = transferRepository;
        this.mEventBus = eventBus;
    }

    @Override
    public void attachView(ITransferHomeView view) {
        super.attachView(view);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        mEventBus.unregister(this);
        super.detachView();
    }

    @Override
    public void resume() {
        this.getRecent();
    }

    private void getRecent() {
        Subscription subscription = mTransferRepository.getRecent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RecentSubscriber());
        mSubscription.add(subscription);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        if (event != null && mView != null) {
            mView.reloadIntroAnimation();
        }
    }
}
