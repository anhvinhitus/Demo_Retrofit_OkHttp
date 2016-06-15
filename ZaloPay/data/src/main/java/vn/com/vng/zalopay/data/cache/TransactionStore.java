package vn.com.vng.zalopay.data.cache;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;

/**
 * Created by huuhoa on 6/15/16.
 * Hold declaration for transaction store
 */
public interface TransactionStore {
    interface LocalStorage {
        void write(List<TransHistoryEntity> val);

        void write(TransHistoryEntity val);

        Observable<List<TransHistoryEntity>> transactionHistories();

        Observable<List<TransHistoryEntity>> transactionHistories(int limit);

        List<TransHistoryEntity> listTransHistories(int limit);

        boolean isHaveTransactionInDb();

        Observable<TransHistoryEntity> transactionHistory();
    }

    interface RequestService {
        @GET("tpe/transhistory")
        Observable<TransactionHistoryResponse> transactionHistorys(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order);
    }

    interface Repository {

    }
}
