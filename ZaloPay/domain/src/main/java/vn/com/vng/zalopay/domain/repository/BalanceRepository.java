package vn.com.vng.zalopay.domain.repository;

import rx.Observable;

/**
 * Created by huuhoa on 6/15/16.
 * Interface for providing up-to-date balance information to outer layers
 */
public interface BalanceRepository {
    Observable<Long> balance();
    Observable<Long> updateBalance();
}
