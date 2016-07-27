package vn.com.vng.zalopay.data.balance;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by huuhoa on 6/15/16.
 * Implementation of @link{BalanceStore.Repository}
 */
public class BalanceRepositoryImpl implements BalanceStore.Repository {
    private final BalanceStore.LocalStorage mLocalStorage;
    private final BalanceStore.RequestService mRequestService;
    private User mUser;
    private EventBus mEventBus;
    private Long mCurrentBalance;

    public BalanceRepositoryImpl(BalanceStore.LocalStorage localStorage,
                                 BalanceStore.RequestService requestService,
                                 User user,
                                 EventBus eventBus) {
        mUser = user;
        mLocalStorage = localStorage;
        mRequestService = requestService;
        mEventBus = eventBus;
    }

    @Override
    public Observable<Long> balance() {
        return Observable.merge(balanceLocal(), updateBalance()
                .onErrorResumeNext(throwable -> Observable.empty()));
    }

    @Override
    public Observable<Long> updateBalance() {
        return mRequestService.balance(mUser.uid, mUser.accesstoken)
                .doOnNext(response -> {
                    mCurrentBalance = response.zpwbalance;
                    mLocalStorage.putBalance(response.zpwbalance);
                    mEventBus.post(new ChangeBalanceEvent(response.zpwbalance));
                })
                .map(response -> response.zpwbalance);
    }

    public Long currentBalance() {
        if (mCurrentBalance == null) {
            mCurrentBalance = 0l;
        }
        return mCurrentBalance;
    }

    private Observable<Long> balanceLocal() {
        return mLocalStorage.getBalance()
                .doOnNext(balance -> {
                    mCurrentBalance = balance;
                })
                ;
    }
}
