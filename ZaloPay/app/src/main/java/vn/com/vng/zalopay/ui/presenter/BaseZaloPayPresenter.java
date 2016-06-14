package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseZaloPayPresenter extends BaseUserPresenter {

    protected Subscription subscriptionGetOrder;

    protected void transactionUpdate() {
        subscriptionGetOrder = zaloPayRepository.transactionUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(subscriptionGetOrder);
    }

    protected void destroy() {
        unsubscribe();
    }

}
