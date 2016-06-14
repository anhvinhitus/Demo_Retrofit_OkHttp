package vn.com.vng.zalopay.data.cache;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;

/**
 * Created by huuhoa on 6/14/16.
 * BalanceStore interface
 */
public interface BalanceStore {
    interface Repository {
        void putBalance(long value);
        Observable<Long> getBalance();
    }

    interface RequestService {
        @GET("tpe/getbalance")
        Observable<BalanceResponse> balance(@Query("userid") String uid, @Query("accesstoken") String accesstoken);
    }
}
