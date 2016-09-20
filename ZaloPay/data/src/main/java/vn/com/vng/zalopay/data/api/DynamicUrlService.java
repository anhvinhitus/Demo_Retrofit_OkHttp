package vn.com.vng.zalopay.data.api;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
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
    Observable<Object> get(@Url String url, @HeaderMap Map header);

    @FormUrlEncoded
    @POST
    Observable<Object> post(@Url String url, @HeaderMap Map header, @Body String body);
}
