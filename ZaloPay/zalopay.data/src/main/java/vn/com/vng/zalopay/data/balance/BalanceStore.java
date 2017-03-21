package vn.com.vng.zalopay.data.balance;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;
import vn.com.vng.zalopay.data.net.adapter.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

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
        @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETBALANCE)
        @GET(Constants.TPE_API.GETBALANCE)
        Observable<BalanceResponse> balance(@Query("userid") String uid, @Query("accesstoken") String accesstoken);
    }

    /**
     * Interface for providing up-to-date balance information to outer layers
     */
    interface Repository {
        Observable<Long> balance();

        Long currentBalance();

        Observable<Long> updateBalance();

        Observable<Long> balanceLocal();

        Observable<Long> fetchBalance();
    }
}
