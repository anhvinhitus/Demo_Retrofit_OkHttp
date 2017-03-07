package vn.com.vng.zalopay;


import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ServiceAPI {
    @POST("createaccesstoken")
    Call<TokenModel> getAccessToken(@Query("appid") String userID,
                                    @Query("loginuid") String accessToken,
                                    @Query("oauthcode") String zpTransid);
}
