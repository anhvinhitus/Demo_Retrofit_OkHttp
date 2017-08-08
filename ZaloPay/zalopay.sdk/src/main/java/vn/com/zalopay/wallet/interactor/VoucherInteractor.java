package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
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

    static final int VOUCHER_SUCCESS_CODE = 1;
    static final int VOUCHER_PROCESSING_CODE = 5;
    static final int VOUCHER_STATUS_VALID_CODE = 4;
    static final int VOUCHER_STATUS_VALID_REVERT_CODE = 2;
    static final int MAX_RETRY_REVERT = 1;

    VoucherStore.VoucherService mVoucherService;
    VoucherStore.LocalStorage mLocalStorage;
    int mRetryCount = 0;
    int stopCode = VOUCHER_STATUS_VALID_CODE;
    long mIntervalRetry = Constants.VOUCHER_STATUS_DELAY_RETRY;

    public VoucherInteractor(VoucherStore.VoucherService voucherService, VoucherStore.LocalStorage localStorage) {
        mVoucherService = voucherService;
        mLocalStorage = localStorage;
    }

    @Override
    public boolean hasRevertVouchers(String userId) {
        try {
            String[] vouchers = mLocalStorage.sharePref().getVouchers(userId);
            return vouchers != null && vouchers.length >= 1;
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    @Override
    public Observable<Boolean> clearVoucher(String userId, String voucherCode) {
        return mLocalStorage.clearVoucher(userId, voucherCode);
    }

    @Override
    public void put(String userId, VoucherInfo voucherInfo) {
        if (mLocalStorage != null) {
            mLocalStorage.put(userId, voucherInfo);
            Timber.d("put voucher sign to cache %s", GsonUtils.toJsonString(voucherInfo));
        }
    }

    boolean isVoucherError(int returnCode) {
        return !isSuccess(returnCode) && !isProcessingVoucher(returnCode);
    }

    boolean isSuccess(int returnCode) {
        return returnCode == VOUCHER_SUCCESS_CODE;
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
        return pResponse.voucherstatus == stopCode;
    }

    Observable<VoucherInfo> mapResultGetVoucherStatus(VoucherStatusResponse statusResponse, VoucherInfo voucherInfo) {
        if (statusResponse == null) {
            return Observable.error(new NetworkConnectionException());
        }
        if (statusResponse.voucherstatus == stopCode) {
            return Observable.just(voucherInfo);
        } else {
            return Observable.error(new RequestException(statusResponse.returncode, statusResponse.returnmessage));
        }
    }

    Observable<VoucherInfo> mapResultUseVoucher(String userId, String accessToken, UseVoucherResponse useVoucherResponse) {
        if (useVoucherResponse == null) {
            return Observable.error(new NetworkConnectionException());
        }
        int returnCode = useVoucherResponse.returncode;
        if (isSuccess(returnCode)) {
            return Observable.just(useVoucherResponse.data);
        }
        if (isVoucherError(returnCode)) {
            return Observable.error(new RequestException(returnCode, useVoucherResponse.returnmessage));
        }
        return getVoucherStatus(userId, accessToken, useVoucherResponse.data.vouchersig)
                .flatMap(new Func1<VoucherStatusResponse, Observable<VoucherInfo>>() {
                    @Override
                    public Observable<VoucherInfo> call(VoucherStatusResponse voucherStatusResponse) {
                        return mapResultGetVoucherStatus(voucherStatusResponse, useVoucherResponse.data);
                    }
                });
    }

    Observable<Boolean> mapResultRevertVoucher(String userId, String accessToken, String voucherSign, BaseResponse baseResponse) {
        if (baseResponse == null) {
            return Observable.error(new NetworkConnectionException());
        }
        int returnCode = baseResponse.returncode;
        if (isSuccess(returnCode)) {
            return Observable.just(true);
        }
        if (isVoucherError(returnCode)) {
            return Observable.error(new RequestException(returnCode, baseResponse.returnmessage));
        }
        return getVoucherStatus(userId, accessToken, voucherSign)
                .flatMap(new Func1<VoucherStatusResponse, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(VoucherStatusResponse voucherStatusResponse) {
                        if (voucherStatusResponse == null) {
                            return Observable.error(new NetworkConnectionException());
                        }
                        if (voucherStatusResponse.voucherstatus == stopCode) {
                            return Observable.just(true);
                        } else {
                            return Observable.error(new RequestException(voucherStatusResponse.returncode, voucherStatusResponse.returnmessage));
                        }
                    }
                });
    }

    @Override
    public Observable<VoucherInfo> validateVoucher(String userID, String accessToken, String appTransID, long appID, long amount, long timestamp, String voucherCode) {
        stopCode = VOUCHER_STATUS_VALID_CODE;
        Observable<UseVoucherResponse> useVoucherObservable = useVoucher(userID, accessToken, appTransID, appID, amount, timestamp, voucherCode);
        return useVoucherObservable
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
        mRetryCount = 0;
        return mVoucherService.getVoucherStatus(userID, accessToken, voucherSig)
                .doOnSubscribe(() -> mRetryCount++)
                .repeatWhen(observable -> observable.delay(mIntervalRetry, MILLISECONDS))
                .takeUntil(this::shouldStopCheckVoucherStatus)
                .filter(this::shouldStopCheckVoucherStatus);
    }

    @Override
    public Observable<Boolean> revertVoucher(String userID, String accessToken) {
        stopCode = VOUCHER_STATUS_VALID_REVERT_CODE;
        long timestamp = System.currentTimeMillis();
        return mLocalStorage.get(userID)
                .filter(voucherInfo -> !TextUtils.isEmpty(voucherInfo))
                .map(s -> {
                    try {
                        return GsonUtils.fromJsonString(s, VoucherInfo.class);
                    } catch (Exception e) {
                        Timber.d(e);
                    }
                    return null;
                })
                .filter(voucherInfo -> {
                    if (voucherInfo == null) {
                        return false;
                    }
                    if (voucherInfo.retry > MAX_RETRY_REVERT) {
                        clearVoucher(userID, voucherInfo.vouchercode);
                    }
                    return voucherInfo.retry <= MAX_RETRY_REVERT;
                })
                .flatMap(new Func1<VoucherInfo, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(VoucherInfo voucherInfo) {
                        return revertVoucher(userID, accessToken, timestamp, voucherInfo);
                    }
                });
    }

    Observable<Boolean> revertVoucher(String userID, String accessToken, long timeStamp, VoucherInfo voucherInfo) {
        String voucherSign = voucherInfo.vouchersig;
        return mVoucherService.revertVoucher(userID, accessToken, timeStamp, voucherSign)
                .flatMap(new Func1<BaseResponse, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BaseResponse baseResponse) {
                        return mapResultRevertVoucher(userID, accessToken, voucherSign, baseResponse);
                    }
                })
                .doOnError(throwable -> {
                    Timber.d("Count the retry revert time");
                    voucherInfo.retry++;
                    put(userID, voucherInfo);
                })
                .concatMap(aBoolean -> clearVoucher(userID, voucherInfo.vouchercode));
    }
}
