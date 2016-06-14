package vn.com.vng.zalopay.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseZaloPayPresenter extends BaseUserPresenter {

    protected ZaloPayRepository zaloPayRepository = AndroidApplication.instance().getUserComponent().zaloPayRepository();

    protected Subscription subscriptionGetOrder;

    protected void transactionUpdate() {
        zaloPayRepository.transactionUpdate()
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
