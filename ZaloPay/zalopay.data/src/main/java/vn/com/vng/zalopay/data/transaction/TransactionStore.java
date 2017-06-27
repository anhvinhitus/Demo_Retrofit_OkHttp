package vn.com.vng.zalopay.data.transaction;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;

import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by huuhoa on 6/15/16.
 * Hold declaration for transaction store
 */
public interface TransactionStore {
    interface LocalStorage {
        void put(List<TransHistoryEntity> val);

        List<TransHistoryEntity> get(int offset, int count, int statusType, long maxreqdate, long minreqdate, List<Integer> transTypes, int sign);

        void remove(long id);

        boolean isHaveTransactionInDb();

        TransHistoryEntity getTransaction(long id);

        void updateThankMessage(long transId, String message);

        void updateStatusType(long transId, int statusType);

        void setLoadedTransactionSuccess(boolean loaded);

        void setLoadedTransactionFail(boolean loaded);

        boolean isLoadedTransactionSuccess();

        boolean isLoadedTransactionFail();

    }

    interface RequestService {
        @API_NAME(ZPEvents.CONNECTOR_V001_TPE_TRANSHISTORY)
        @GET(Constants.TPE_API.TRANSHISTORY)
        Observable<TransactionHistoryResponse> getTransactionHistories(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order, @Query("statustype") int statustype);
    }

    interface Repository {

        Observable<Pair<Integer, List<TransHistory>>> getTransactions(long timestamp, List<Integer> transTypes, int offset, int count, int sign);

        Observable<Pair<Integer, List<TransHistory>>> getTransactionsFail(long timestamp, List<Integer> transTypes, int offset, int count, int sign);

        Observable<List<TransHistory>> getTransactionsLocal(long timestamp, List<Integer> transTypes, int offset, int count, int sign);

        Observable<List<TransHistory>> getTransactionsFailLocal(long timestamp, List<Integer> transTypes, int offset, int count, int sign);

        Observable<Boolean> updateThankMessage(long transId, String message);

        Observable<Boolean> removeTransaction(long id);

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
