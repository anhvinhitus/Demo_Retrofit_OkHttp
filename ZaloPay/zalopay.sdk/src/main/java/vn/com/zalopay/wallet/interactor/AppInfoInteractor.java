package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.entity.response.AppInfoResponse;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.BankHelper;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

/**
 * Interactor decide which get data from
 * do some bussiness logic on return result and delegate the result to caller
 * Created by chucvv on 6/8/17.
 */

public class AppInfoInteractor implements AppInfoStore.Interactor {
    private AppInfoStore.RequestService mRequestService;
    private AppInfoStore.LocalStorage mLocalStorage;

    @Inject
    public AppInfoInteractor(AppInfoStore.RequestService requestService, AppInfoStore.LocalStorage localStorage) {
        Timber.d("call constructor AppInfoInteractor");
        this.mRequestService = requestService;
        this.mLocalStorage = localStorage;
    }

    @Override
    public List<String> getPmcTranstypeKeyList(long pAppID, @TransactionType int pTransType) {
        return this.mLocalStorage.getPmcTranstypeKeyList(pAppID, pTransType);
    }

    @Override
    public MiniPmcTransType getPmcTranstype(long pAppId, @TransactionType int transtype, boolean isBankAcount, boolean isInternationalBank, String bankCode) {
        return this.mLocalStorage.getPmcTranstype(pAppId, transtype, isBankAcount, isInternationalBank, bankCode);
    }

    @Override
    public MiniPmcTransType getPmcTranstype(long pAppId, @TransactionType int pTranstype, int pPmcID, String pBankCode) {
        return this.mLocalStorage.getPmcTranstype(pAppId, pTranstype, pPmcID, pBankCode);
    }

    @Override
    public MiniPmcTransType getPmcConfigByPmcKey(String key) {
        return this.mLocalStorage.getPmcConfigByPmcKey(key);
    }

    @Override
    public MiniPmcTransType getPmcConfig(long pAppId, @TransactionType int pTranstype, String pBankCode) {
        String pmcConfig;
        if (pTranstype == TransactionType.WITHDRAW) {
            pmcConfig = this.mLocalStorage.sharePref().getZaloPayChannelConfig(pAppId, pTranstype, pBankCode);
        } else if (BankHelper.isBankAccount(pBankCode)) {
            pmcConfig = this.mLocalStorage.sharePref().getBankAccountChannelConfig(pAppId, pTranstype, pBankCode);
        } else if (BuildConfig.CC_CODE.equals(pBankCode)) {
            pmcConfig = this.mLocalStorage.sharePref().getCreditCardChannelConfig(pAppId, pTranstype, pBankCode);
        } else {
            pmcConfig = this.mLocalStorage.sharePref().getATMChannelConfig(pAppId, pTranstype, pBankCode);
        }
        if (TextUtils.isEmpty(pmcConfig)) {
            return null;
        }
        try {
            return GsonUtils.fromJsonString(pmcConfig, MiniPmcTransType.class);
        } catch (Exception e) {
            Timber.w(e, "Exception get pmc config");
        }
        return null;
    }

    @Override
    public AppInfo get(long appid) {
        return this.mLocalStorage.getSync(appid);
    }

    /***
     *if app info is not expire then use data on local
     * other hand, call api to reload app info
     */
    @Override
    public Observable<AppInfo> loadAppInfo(long appid, @TransactionType int[] transtypes, String userid, String accesstoken, String appversion, long currentTime) {
        String appInfoCheckSum = mLocalStorage.getAppInfoCheckSum(appid);
        String transtypeString = transtypeToString(transtypes);
        String transtypeCheckSum = transtypeCheckSum(appid, transtypes);
        long startTime = System.currentTimeMillis();
        int apiId = ZPEvents.CONNECTOR_V001_TPE_GETAPPINFO;
        Observable<AppInfo> appInfoOnCache = mLocalStorage
                .get(appid)
                .subscribeOn(Schedulers.io())
                .onErrorReturn(null);
        Observable<AppInfo> appInfoOnCloud = mRequestService.fetch(String.valueOf(appid), userid, accesstoken, appInfoCheckSum, transtypeString, transtypeCheckSum, appversion)
                .doOnError(throwable -> ZPAnalyticsTrackerWrapper.trackApiError(apiId, startTime, throwable))
                .map(this::changeAppName)
                .doOnNext(appInfoResponse -> mLocalStorage.put(appid, appInfoResponse))
                .doOnNext(appInfoResponse -> ZPAnalyticsTrackerWrapper.trackApiCall(apiId, startTime, appInfoResponse))
                .flatMap(mapResult(appid));
        return Observable.concat(appInfoOnCache, appInfoOnCloud)
                .first(appInfo -> appInfo != null && (appInfo.expriretime > currentTime));
    }

    private AppInfoResponse changeAppName(AppInfoResponse appInfoResponse) {
        if (appInfoResponse == null) {
            return null;
        }
        AppInfo appInfo = appInfoResponse.info;
        if (appInfo == null) {
            return appInfoResponse;
        }
        if (appInfo.appid == Long.parseLong(GlobalData.getStringResource(RS.string.app_service_id))) {
            appInfo.appname = GlobalData.getStringResource(RS.string.app_service_name);
            appInfoResponse.info = appInfo;
        }
        return appInfoResponse;
    }

    private Func1<AppInfoResponse, Observable<AppInfo>> mapResult(long appId) {
        return appInfoResponse -> {
            if (appInfoResponse == null) {
                return Observable.error(new RequestException(RequestException.NULL,
                        GlobalData.getAppContext().getResources().getString(R.string.sdk_payment_generic_error_networking_mess)));
            } else if (appInfoResponse.returncode == 1) {
                //success, load app info from cache
                return mLocalStorage.get(appId);
            } else {
                return Observable.error(new RequestException(appInfoResponse.returncode, appInfoResponse.returnmessage));
            }
        };
    }

    private String transtypeToString(@TransactionType int[] pTranstype) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int length = pTranstype != null ? pTranstype.length : 0;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(pTranstype[i]);
            if (i + 1 < length) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private String transtypeCheckSum(long appId, @TransactionType int[] transtypes) {
        String[] transtypeCheckSum = new String[0];
        try {
            String appInfoCheckSum = mLocalStorage.getAppInfoCheckSum(appId);
            if (!TextUtils.isEmpty(appInfoCheckSum)) {
                transtypeCheckSum = new String[transtypes.length];
                for (int i = 0; i < transtypes.length; i++) {
                    transtypeCheckSum[i] = mLocalStorage
                            .getTranstypeCheckSum(mLocalStorage.getTranstypeCheckSumKey(appId, transtypes[i]));
                }
            }
        } catch (Exception ignored) {
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        boolean isReload = false;
        for (int i = 0; i < transtypeCheckSum.length; i++) {
            if (TextUtils.isEmpty(transtypeCheckSum[i])) {
                isReload = true;
                break;
            }
            stringBuilder.append(transtypeCheckSum[i]);
            if (i + 1 < transtypeCheckSum.length) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        if (isReload) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("[]");
        }
        return stringBuilder.toString();
    }

    @Override
    public void setExpireTime(long appId, long expireTime) {
        mLocalStorage.setExpireTime(appId, expireTime);
    }

    @Override
    public long minAmountTransType(@TransactionType int transtype) {
        try {
            return mLocalStorage.sharePref().getMinValueChannel(String.valueOf(transtype));
        } catch (Exception ex) {
            Timber.w(ex, "Exception get min value transtype");
        }
        return 0;
    }

    @Override
    public long maxAmountTransType(@TransactionType int transtype) {
        try {
            return mLocalStorage.sharePref().getMaxValueChannel(String.valueOf(transtype));
        } catch (Exception ex) {
            Timber.w(ex, "Exception get max value transtype");
        }
        return 0;
    }

    @Override
    public long getBankMinAmountSupport(String key) {
        return mLocalStorage.sharePref().getBankMinAmountSupport(key);
    }
}
