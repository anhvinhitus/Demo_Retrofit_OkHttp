package vn.com.vng.zalopay.data.transaction;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.TransactionFragmentEntity;

/**
 * Created by khattn on 4/10/17.
 * Hold declaration for fragment transaction store
 */

public interface TransactionFragmentStore {
    interface LocalStorage {

        void put(TransactionFragmentEntity val);

        void updateOutOfData(long timestamp, int statustype, boolean outofdata);

        boolean isOutOfData(long timestamp, int statustype);

        void remove(long minreqdate);

        List<TransactionFragmentEntity> get(long timestamp, int statustype);

        TransactionFragmentEntity getLatestFragment(int statustype);

        long getLatestTimeTransaction(int statusType);

        long getOldestTimeTransaction(int statusType);

        boolean isHasData(long timestamp, int statustype);
    }
}
