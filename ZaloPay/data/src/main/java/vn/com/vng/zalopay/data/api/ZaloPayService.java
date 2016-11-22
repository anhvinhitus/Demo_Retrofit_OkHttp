package vn.com.vng.zalopay.data.api;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BalanceResponse;
import vn.com.vng.zalopay.data.api.response.GetOrderResponse;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;

/**
 * Created by AnhHieu on 5/4/16.
 * Các api liên quan đến thanh toán, số dư, lịch sử giao dịch, order ,...
 */
public interface ZaloPayService {

    @GET("v001/tpe/getorderinfo")
    Observable<GetOrderResponse> getorder(@Query("userid") String userid,
                                          @Query("accesstoken") String accesstoken,
                                          @Query(Constants.APPID) long appId,
                                          @Query(Constants.ZPTRANSTOKEN) String apptransid);

    @FormUrlEncoded
    @POST("v001/tpe/createwalletorder")
    Observable<GetOrderResponse> createwalletorder(@Field("userid") String userid,
                                                   @Field("accesstoken") String accesstoken,
                                                   @Field(Constants.APPID) long appId,
                                                   @Field(Constants.AMOUNT) long amount,
                                                   @Field(Constants.TRANSTYPE) String transtype,
                                                   @Field(Constants.APPUSER) String appUser,
                                                   @Field(Constants.DESCRIPTION) String description,
                                                   @Field(Constants.EMBEDDATA) String embeddata);

}
