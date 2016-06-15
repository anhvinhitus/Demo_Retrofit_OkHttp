package vn.com.vng.zalopay.ui.presenter;

import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by AnhHieu on 3/26/16.
 */
public abstract class BaseZaloPayPresenter extends BaseUserPresenter {

    protected void transactionUpdate() {
        transactionRepository.transactionUpdate()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    protected void updateBalance() {
        balanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }
}
