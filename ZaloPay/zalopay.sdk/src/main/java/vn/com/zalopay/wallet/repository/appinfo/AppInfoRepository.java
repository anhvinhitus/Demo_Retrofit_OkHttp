package vn.com.zalopay.wallet.repository.appinfo;

import rx.Observable;
import rx.functions.Func1;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.datasource.RetryWithDelay;
import vn.com.zalopay.wallet.exception.RequestException;

/**
 * Created by chucvv on 6/7/17.
 */

public class AppInfoRepository implements AppInfoStore.Repository {
    private AppInfoStore.AppInfoService mAppInfoService;
    private AppInfoStore.LocalStorage mLocalStorage;

    public AppInfoRepository(AppInfoStore.AppInfoService appInfoService, AppInfoStore.LocalStorage localStorage) {
        this.mAppInfoService = appInfoService;
        this.mLocalStorage = localStorage;
        Log.d(this, "create AppInfoRepository");
    }

    @Override
    public AppInfoStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }

    @Override
    public Observable<AppInfo> fetchAppInfoCloud(String appid, String userid, String accesstoken, String checksum, String appversion) {
        return mAppInfoService.fetchAppInfo(appid, userid, accesstoken, checksum, appversion)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .filter(appInfoResponse -> appInfoResponse != null)
                .doOnNext(appInfoResponse -> mLocalStorage.putAppInfo(appid, appInfoResponse))
                .flatMap(new Func1<AppInfoResponse, Observable<AppInfo>>() {
                    @Override
                    public Observable<AppInfo> call(AppInfoResponse appInfoResponse) {
                        if (appInfoResponse.returncode != 1) {
                            return Observable.error(new RequestException(appInfoResponse.returncode, appInfoResponse.returnmessage));
                        } else if (appInfoResponse.info != null) {
                            appInfoResponse.info.expriretime = appInfoResponse.expiredtime + System.currentTimeMillis();
                            return Observable.just(appInfoResponse.info);
                        } else {
                            return mLocalStorage.getAppInfo(appid);
                        }
                    }
                });
    }
}
