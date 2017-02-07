package vn.com.vng.zalopay.data.qrcode;

import com.google.gson.JsonObject;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by longlv on 2/6/17.
 * *
 */

public interface QRCodeStore {

    interface RequestService {
        @GET
        Observable<JsonObject> getPaymentInfo(@Url String url, @Query("ua") String ua);
    }

    interface Repository {
        Observable<JsonObject> getPaymentInfo(String url);
    }
}
