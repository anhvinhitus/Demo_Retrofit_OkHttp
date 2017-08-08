package vn.com.zalopay.wallet.interactor;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.wallet.business.entity.voucher.UseVoucherResponse;
import vn.com.zalopay.wallet.business.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.repository.voucher.VoucherStore;
import vn.com.zalopay.wallet.voucher.VoucherStatusResponse;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by chucvv on 8/1/17.
 * implement note at {@link "https://gitlab.zalopay.vn/zalopay-apps/task/wikis/voucher" }
 */

public class VoucherInteractor implements VoucherStore.Interactor {
    static final int VOUCHER_VALID_CODE = 1;
    static final int VOUCHER_PROCESSING_CODE = 5;
    static final int VOUCHER_STATUS_VALID_CODE = 4;
    VoucherStore.VoucherService mVoucherService;
    int mRetryCount = 0;
    long mIntervalRetry = Constants.VOUCHER_STATUS_DELAY_RETRY;

    public VoucherInteractor(VoucherStore.VoucherService voucherService) {
        mVoucherService = voucherService;
    }

    boolean isVoucherError(int returnCode) {
        return !isVoucherValid(returnCode) && !isProcessingVoucher(returnCode);
    }

    boolean isVoucherValid(int returnCode) {
        return returnCode == VOUCHER_VALID_CODE;
    }

    private boolean isProcessingVoucher(int returnCode) {
        return returnCode == VOUCHER_PROCESSING_CODE;
    }

    private boolean shouldStopCheckVoucherStatus(VoucherStatusResponse pResponse) {
        Timber.d("start check should stop check voucher status");
        if (pResponse == null) {
            return false;
        }
        if (mRetryCount >= Constants.VOUCHER_STATUS_MAX_RETRY) {
            return true;
        }
        return pResponse.voucherstatus == VOUCHER_STATUS_VALID_CODE;
    }

    Observable<VoucherInfo> mapResultGetVoucherStatus(VoucherStatusResponse statusResponse, VoucherInfo voucherInfo) {
        if (statusResponse == null) {
            return Observable.error(new NetworkConnectionException());
        }
        if (statusResponse.voucherstatus == VOUCHER_STATUS_VALID_CODE) {
            return Observable.just(voucherInfo);
        } else {
            return Observable.error(new RequestException(-1, statusResponse.returnmessage));
        }
    }

    Observable<VoucherInfo> mapResultUseVoucher(String userId, String accessToken, UseVoucherResponse useVoucherResponse) {
        if (useVoucherResponse == null) {
            return Observable.error(new NetworkConnectionException());
        }
        int returnCode = useVoucherResponse.returncode;
        if (isVoucherValid(returnCode)) {
            return Observable.just(useVoucherResponse.data);
        }
        if (isVoucherError(useVoucherResponse.returncode)) {
            return Observable.error(new RequestException(-1, useVoucherResponse.returnmessage));
        }
        return getVoucherStatus(userId, accessToken, useVoucherResponse.data.vouchersig)
                .flatMap(new Func1<VoucherStatusResponse, Observable<VoucherInfo>>() {
                    @Override
                    public Observable<VoucherInfo> call(VoucherStatusResponse voucherStatusResponse) {
                        return mapResultGetVoucherStatus(voucherStatusResponse, useVoucherResponse.data);
                    }
                });
    }

    @Override
    public Observable<VoucherInfo> validateVoucher(String userID, String accessToken, String appTransID, long appID, long amount, long timestamp, String voucherCode) {
        mRetryCount = 0;
        Observable<UseVoucherResponse> useVoucherObservable = useVoucher(userID, accessToken, appTransID, appID, amount, timestamp, voucherCode);
        return useVoucherObservable
                .onErrorReturn(throwable -> null)
                .flatMap(new Func1<UseVoucherResponse, Observable<VoucherInfo>>() {
                    @Override
                    public Observable<VoucherInfo> call(UseVoucherResponse useVoucherResponse) {
                        return mapResultUseVoucher(userID, accessToken, useVoucherResponse);
                    }
                });
    }

    @Override
    public Observable<UseVoucherResponse> useVoucher(String userID, String accessToken, String appTransID, long appID, long amount,
                                                     long timestamp, String voucherCode) {
        return mVoucherService.useVoucher(userID, accessToken, appTransID, appID, amount, timestamp, voucherCode);
    }

    @Override
    public Observable<VoucherStatusResponse> getVoucherStatus(String userID, String accessToken, String voucherSig) {
        return mVoucherService.getVoucherStatus(userID, accessToken, voucherSig)
                .onErrorReturn(throwable -> null)
                .doOnSubscribe(() -> mRetryCount++)
                .repeatWhen(observable -> observable.delay(mIntervalRetry, MILLISECONDS))
                .takeUntil(this::shouldStopCheckVoucherStatus)
                .filter(this::shouldStopCheckVoucherStatus);
    }
}
