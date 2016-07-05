package vn.com.vng.zalopay.data.transfer;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;

/**
 * Created by huuhoa on 7/5/16.
 * Public declaration for transfer money
 */
public interface TransferStore {
    interface LocalStorage {
        /**
         * @return Rx Observable for list of recent transfer transactions
         */
        Observable<List<TransferRecent>> get();

        /**
         * Append new transaction
         * @param recentTransfer new transaction
         */
        void append(TransferRecent recentTransfer);
    }
}
