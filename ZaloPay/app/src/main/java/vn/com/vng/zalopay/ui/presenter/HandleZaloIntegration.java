package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by khattn on 2/7/17.
 */

public class HandleZaloIntegration {

    @Inject
    BalanceStore.Repository mBalanceRepository;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    void initialize() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    void start() {
        Subscription subscription = mBalanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Long>());

        mCompositeSubscription.add(subscription);
    }
}
