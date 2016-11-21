package vn.com.vng.zalopay.data.transaction;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.domain.model.TransHistory;

/**
 * Created by huuhoa on 6/15/16.
 * Hold declaration for transaction store
 */
public interface TransactionStore {
    interface LocalStorage {
        void put(List<TransHistoryEntity> val);

        List<TransHistoryEntity> get(int pageIndex, int limit, int statusType);

        boolean isHaveTransactionInDb();

        TransHistoryEntity getTransaction(long id);

        void updateStatusType(long transId, int statusType);

        void setLoadedTransactionSuccess(boolean loaded);

        void setLoadedTransactionFail(boolean loaded);

        boolean isLoadedTransactionSuccess();

        boolean isLoadedTransactionFail();

        long getLatestTimeTransaction(int statusType);

        long getOldestTimeTransaction(int statusType);

    }

    interface RequestService {
        @GET("tpe/transhistory")
        Observable<TransactionHistoryResponse> getTransactionHistories(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("statustype") int statustype);
    }

    interface Repository {

        Observable<List<TransHistory>> getTransactions(int pageIndex, int count);

        Observable<List<TransHistory>> getTransactionsFail(int pageIndex, int count);

        Observable<TransHistory> getTransaction(long id);

        Observable<Boolean> updateTransactionStatusSuccess(long transId);

        Boolean isLoadedTransactionSuccess();

        Boolean isLoadedTransactionFail();

        Observable<Boolean> fetchTransactionHistorySuccessLatest();

        Observable<Boolean> fetchTransactionHistoryFailLatest();

        Observable<Boolean> reloadTransactionHistoryTime(long time);

        Observable<Boolean> fetchTransactionHistoryLatest();

    }
}
