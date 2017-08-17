package vn.com.zalopay.wallet.repository.voucher;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.entity.voucher.UseVoucherResponse;
import vn.com.zalopay.wallet.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;
import vn.com.zalopay.wallet.entity.voucher.VoucherStatusResponse;

/*
 * Created by chucvv on 8/1/17.
 */

public class VoucherStore {
    public interface LocalStorage extends AbstractLocalStorage.LocalStorage {
        void put(String userId, VoucherInfo voucherInfo);

        Observable<String> get(String userId);

        Observable<Boolean> clearVoucher(String userId, String voucherCode);
    }

    public interface Interactor {
        Observable<VoucherInfo> validateVoucher(String userID, String accessToken, String appTransID,
                                                long appID, long amount, long timestamp, String voucherCode);

        Observable<VoucherStatusResponse> getVoucherStatus(String userID, String accessToken, String voucherSig);

        Observable<UseVoucherResponse> useVoucher(String userID, String accessToken, String appTransID,
                                                  long appID, long amount, long timestamp, String voucherCode);

        Observable<Boolean> revertVoucher(String userID, String accessToken);

        void put(String userId, VoucherInfo voucherInfo);

        Observable<Boolean> clearVoucher(String userId, String voucherCode);

        boolean hasRevertVouchers(String userId);
    }

    public interface VoucherService {
        @GET(Constants.URL_GET_VOUCHER_STATUS)
        @API_NAME(https = ZPEvents.API_GET_VOUCHER_STATUS, connector = ZPEvents.CONNECTOR_GET_VOUCHER_STATUS)
        Observable<VoucherStatusResponse> getVoucherStatus(@Query("userid") String userID,
                                                           @Query("accesstoken") String accessToken,
                                                           @Query("vouchersig") String voucherSig);

        @POST(Constants.URL_USE_VOUCHER)
        @API_NAME(https = ZPEvents.API_USE_VOUCHER, connector = ZPEvents.CONNECTOR_USE_VOUCHER)
        Observable<UseVoucherResponse> useVoucher(@Query("userid") String userID,
                                                  @Query("accesstoken") String accessToken,
                                                  @Query("apptransid") String appTransID,
                                                  @Query("appid") long appID,
                                                  @Query("amount") long amount,
                                                  @Query("timestamp") long timestamp,
                                                  @Query("vouchercode") String voucherCode);

        @POST(Constants.URL_REVERT_VOUCHER)
        @API_NAME(https = ZPEvents.API_REVERT_VOUCHER, connector = ZPEvents.CONNECTOR_REVERT_VOUCHER)
        Observable<BaseResponse> revertVoucher(@Query("userid") String userID,
                                               @Query("accesstoken") String accessToken,
                                               @Query("timestamp") long timestamp,
                                               @Query("vouchersig") String voucherSig);

    }
}
