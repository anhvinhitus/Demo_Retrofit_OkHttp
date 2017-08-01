package vn.com.zalopay.wallet.repository.voucher;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.voucher.UseVoucherResponse;
import vn.com.zalopay.wallet.voucher.VoucherInfo;
import vn.com.zalopay.wallet.voucher.VoucherStatusResponse;

/**
 * Created by chucvv on 8/1/17.
 */

public class VoucherStore {
    public interface Interactor {
        Observable<VoucherInfo> validateVoucher(String userID, String accessToken, String appTransID,
                                                long appID, long amount, long timestamp, String voucherCode);

        Observable<VoucherStatusResponse> getVoucherStatus(String userID, String accessToken, String voucherSig);

        Observable<UseVoucherResponse> useVoucher(String userID, String accessToken, String appTransID,
                                                  long appID, long amount, long timestamp, String voucherCode);
    }

    public interface VoucherService {
        @GET(Constants.URL_GET_VOUCHER_STATUS)
            //@API_NAME(https = ZPEvents.API_V001_TPE_GETTRANSSTATUS, connector = ZPEvents.CONNECTOR_V001_TPE_GETTRANSSTATUS)
        Observable<VoucherStatusResponse> getVoucherStatus(@Query("userID") String userID,
                                                           @Query("accesstoken") String accessToken,
                                                           @Query("voucherSig") String voucherSig);

        @POST(Constants.URL_USE_VOUCHER)
            //@API_NAME(https = ZPEvents.API_V001_TPE_ATMAUTHENPAYER, connector = ZPEvents.CONNECTOR_V001_TPE_ATMAUTHENPAYER)
        Observable<UseVoucherResponse> useVoucher(@Query("userID") String userID,
                                                  @Query("accesstoken") String accessToken,
                                                  @Query("appTransID") String appTransID,
                                                  @Query("appID") long appID,
                                                  @Query("amount") long amount,
                                                  @Query("timestamp") long timestamp,
                                                  @Query("voucherCode") String voucherCode);

    }
}
