package vn.com.vng.zalopay.data.api;

import retrofit2.http.GET;
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

    @GET("tpe/transhistory")
    Observable<TransactionHistoryResponse> transactionHistorys(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("timestamp") long timestamp, @Query("count") int count, @Query("order") int order);

    @GET("tpe/getbalance")
    Observable<BalanceResponse> balance(@Query("userid") String uid, @Query("accesstoken") String accesstoken);

    @GET("tpe/getorderinfo")
    Observable<GetOrderResponse> getorder(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query(Constants.APPID) long appId, @Query(Constants.ZPTRANSTOKEN) String apptransid);

    @GET("tpe/createwalletorder")
    Observable<GetOrderResponse> createwalletorder(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query(Constants.APPID) long appId, @Query(Constants.AMOUNT) long amount, @Query(Constants.TRANSTYPE) String transtype, @Query(Constants.APPUSER) String appUser, @Query(Constants.DESCRIPTION) String description);

}
