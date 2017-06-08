package vn.com.vng.zalopay.data.ga;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;
import vn.com.vng.zalopay.data.cache.global.GoogleAnalytics;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by hieuvm on 6/6/17.
 * AnalyticsStore interface
 */
public interface AnalyticsStore {
    interface LocalStorage {
        void append(String type, String payload);

        List<String> getAll();

        List<String> getPayload(int limit);

        List<GoogleAnalytics> get(int limit);

        void remove(long timestamp);

        long count();
    }

    interface RequestService {
        @POST("collect")
        @FormUrlEncoded
        Observable<String> send(@FieldMap Map<String, String> query, @Header("User-Agent") String ua);

        @POST("batch")
        Observable<String> sendBatch(@Body RequestBody body, @Header("User-Agent") String ua);
    }

    interface Repository {
        Observable<Boolean> append(String type, String payload);

        Observable<Boolean> sendBatch();
    }
}
