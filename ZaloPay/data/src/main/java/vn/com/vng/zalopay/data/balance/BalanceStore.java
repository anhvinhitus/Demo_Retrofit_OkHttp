package vn.com.vng.zalopay.data.balance;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;

/**
 * Created by huuhoa on 6/14/16.
 * BalanceStore interface
 */
public interface BalanceStore {
    interface LocalStorage {
        void putBalance(long value);
        long getBalance();
    }

    interface RequestService {
        @GET("tpe/getbalance")
        Observable<BalanceResponse> balance(@Query("userid") String uid, @Query("accesstoken") String accesstoken);
    }

    /**
     * Interface for providing up-to-date balance information to outer layers
     */
    interface Repository {
        Observable<Long> balance();
        Long currentBalance();
        Observable<Long> updateBalance();
    }
}
