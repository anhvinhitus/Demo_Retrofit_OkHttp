package vn.com.vng.zalopay.data.api;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by AnhHieu on 9/20/16.
 * *
 */
public interface DynamicUrlService {

    @GET
    Observable<String> get(@Url String url, @HeaderMap Map<String, String> header);

    @POST
    Observable<String> post(@Url String url, @HeaderMap Map<String, String> header, @Body RequestBody body);

}
