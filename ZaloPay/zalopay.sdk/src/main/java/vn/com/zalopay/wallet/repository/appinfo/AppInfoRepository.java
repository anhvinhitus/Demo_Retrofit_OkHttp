package vn.com.zalopay.wallet.repository.appinfo;

import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.api.RetryWithDelay;

/**
 * Created by chucvv on 6/7/17.
 */

public class AppInfoRepository implements AppInfoStore.Repository {
    private AppInfoStore.AppInfoService mAppInfoService;
    private AppInfoStore.LocalStorage mLocalStorage;

    public AppInfoRepository(AppInfoStore.AppInfoService appInfoService, AppInfoStore.LocalStorage localStorage) {
        this.mAppInfoService = appInfoService;
        this.mLocalStorage = localStorage;
        Timber.d("create AppInfoRepository");
    }

    @Override
    public AppInfoStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }

    @Override
    public Observable<AppInfoResponse> fetchCloud(long appid, String userid, String accesstoken, String appinfochecksum, String transtypes, String transtypechecksums, String appversion) {
        return mAppInfoService.fetch(String.valueOf(appid), userid, accesstoken, appinfochecksum, transtypes, transtypechecksums, appversion)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(appInfoResponse -> mLocalStorage.put(appid, appInfoResponse));
    }
}
