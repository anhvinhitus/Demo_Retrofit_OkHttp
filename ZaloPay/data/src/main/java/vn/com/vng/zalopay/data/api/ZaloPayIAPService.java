package vn.com.vng.zalopay.data.api;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.GetMerchantUserInfoResponse;

/**
 * Created by AnhHieu on 5/24/16.
 */
public interface ZaloPayIAPService {

    @GET("um/getmerchantuserinfo")
    Observable<GetMerchantUserInfoResponse> getmerchantuserinfo(@Query("appid") long appid, @Query("userid") String userid, @Query("accesstoken") String accesstoken);
}
