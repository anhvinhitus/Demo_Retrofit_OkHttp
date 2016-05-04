package vn.com.vng.zalopay.data.api;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by AnhHieu on 5/4/16.
 * Các api liên quan đến thanh toán, số dư, lịch sử giao dịch, order ,...
 */
public interface ZaloPayService {

    @GET("/tpe/transhistory")
    void transactionHistory(@Query("userid") long userid,@Query("accesstoken") String accesstoken);
}
