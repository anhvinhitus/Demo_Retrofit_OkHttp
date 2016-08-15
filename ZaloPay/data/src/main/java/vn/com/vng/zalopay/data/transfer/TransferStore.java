package vn.com.vng.zalopay.data.transfer;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.domain.model.RecentTransaction;

/**
 * Created by huuhoa on 7/5/16.
 * Public declaration for transfer money
 */
public interface TransferStore {
    interface LocalStorage {
        /**
         * @return Rx Observable for list of recent transfer transactions
         */
        List<TransferRecent> get();

        /**
         * Append new transaction
         *
         * @param recentTransfer new transaction
         */
        void append(TransferRecent recentTransfer);
    }

    interface Repository {
        Observable<List<RecentTransaction>> getRecent();

        Observable<Boolean> append(TransferRecent recentTransfer);
    }
}
