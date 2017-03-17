package vn.com.vng.zalopay.data.transaction;

import android.support.annotation.Nullable;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.net.adapter.API_NAME;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.zalopay.analytics.ZPEvents;

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

        void putBackup(@Nullable TransHistoryEntity val);

        @Nullable
        TransHistoryEntity getBackup(long transId);

    }

    interface RequestService {
        @API_NAME(ZPEvents.CONNECTOR_V001_TPE_TRANSHISTORY)
        @GET(Constants.TPE_API.TRANSHISTORY)
        @Headers({Constants.HEADER_EVENT + ZPEvents.CONNECTOR_V001_TPE_TRANSHISTORY})
        Observable<TransactionHistoryResponse> getTransactionHistories(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("statustype") int statustype);
    }

    interface Repository {

        Observable<List<TransHistory>> getTransactions(int pageIndex, int count);

        Observable<List<TransHistory>> getTransactionsFail(int pageIndex, int count);

        Observable<List<TransHistory>> getTransactionsLocal(int pageIndex, int count);

        Observable<List<TransHistory>> getTransactionsFailLocal(int pageIndex, int count);

        Observable<TransHistory> getTransaction(long id);

        Observable<Boolean> updateTransactionStatusSuccess(long transId);

        Boolean isLoadedTransactionSuccess();

        Boolean isLoadedTransactionFail();

        Observable<Boolean> fetchTransactionHistorySuccessLatest();

        Observable<Boolean> fetchTransactionHistoryFailLatest();

        Observable<Boolean> fetchTransactionHistoryLatest();

        Observable<Boolean> fetchTransactionHistoryOldest(long thresholdTime);

        Observable<Boolean> fetchTransactionHistoryLatest(long thresholdTime);

        Observable<TransHistory> reloadTransactionHistory(long id, long time);
    }
}
