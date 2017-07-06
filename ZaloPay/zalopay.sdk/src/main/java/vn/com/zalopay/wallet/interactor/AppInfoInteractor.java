package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;

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
    public MiniPmcTransType getPmcTranstype(long pAppId, @TransactionType int transtype, boolean isBankAcount, String bankCode) {
        return this.mLocalStorage.getPmcTranstype(pAppId, transtype, isBankAcount, bankCode);
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
        Observable<AppInfo> appInfoOnCache = mLocalStorage
                .get(appid)
                .subscribeOn(Schedulers.io())
                .onErrorReturn(null);
        Observable<AppInfo> appInfoOnCloud = mRequestService.fetch(String.valueOf(appid), userid, accesstoken, appInfoCheckSum, transtypeString, transtypeCheckSum, appversion)
                .map(this::changeAppName)
                .doOnNext(appInfoResponse -> mLocalStorage.put(appid, appInfoResponse))
                .flatMap(mapResult(appid));
        return Observable.concat(appInfoOnCache, appInfoOnCloud)
                .first(appInfo -> appInfo != null && (appInfo.expriretime > currentTime));
    }

    private AppInfoResponse changeAppName(AppInfoResponse appInfoResponse){
        if(appInfoResponse == null){
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
                return Observable.error(new RequestException(RequestException.NULL, GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error)));
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
        for (int i = 0; i < pTranstype.length; i++) {
            stringBuilder.append(pTranstype[i]);
            if (i + 1 < pTranstype.length) {
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
        } catch (Exception e) {
            Log.e(this, e);
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
}
