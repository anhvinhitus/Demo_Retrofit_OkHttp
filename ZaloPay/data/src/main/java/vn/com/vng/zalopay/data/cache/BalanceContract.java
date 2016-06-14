package vn.com.vng.zalopay.data.cache;

import rx.Observable;

/**
 * Created by huuhoa on 6/14/16.
 * BalanceContract interface
 */
public interface BalanceContract {
    interface Repository {
        void putBalance(long value);
        Observable<Long> getBalance();
    }
}
