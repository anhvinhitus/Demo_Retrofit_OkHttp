package vn.com.zalopay.wallet.repository.voucher;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.voucher.UseVoucherResponse;
import vn.com.zalopay.wallet.business.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;
import vn.com.zalopay.wallet.voucher.VoucherStatusResponse;

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
            //@API_NAME(https = ZPEvents.API_V001_TPE_GETTRANSSTATUS, connector = ZPEvents.CONNECTOR_V001_TPE_GETTRANSSTATUS)
        Observable<VoucherStatusResponse> getVoucherStatus(@Query("userid") String userID,
                                                           @Query("accesstoken") String accessToken,
                                                           @Query("vouchersig") String voucherSig);

        @POST(Constants.URL_USE_VOUCHER)
        Observable<UseVoucherResponse> useVoucher(@Query("userid") String userID,
                                                  @Query("accesstoken") String accessToken,
                                                  @Query("apptransid") String appTransID,
                                                  @Query("appid") long appID,
                                                  @Query("amount") long amount,
                                                  @Query("timestamp") long timestamp,
                                                  @Query("vouchercode") String voucherCode);

        @POST(Constants.URL_REVERT_VOUCHER)
        Observable<BaseResponse> revertVoucher(@Query("userid") String userID,
                                               @Query("accesstoken") String accessToken,
                                               @Query("timestamp") long timestamp,
                                               @Query("vouchersig") String voucherSig);

    }
}
