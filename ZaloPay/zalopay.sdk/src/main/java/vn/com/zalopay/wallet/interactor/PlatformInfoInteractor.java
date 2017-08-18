package vn.com.zalopay.wallet.interactor;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.DeviceUtil;
import vn.com.zalopay.utility.DimensionUtil;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.IDownloadService;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.GlobalData;
import vn.com.zalopay.wallet.entity.response.PlatformInfoResponse;
import vn.com.zalopay.wallet.constants.ConstantParams;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStore;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

/**
 * Interactor decide which get data from
 * do some bussiness logic on return result and delegate the result to caller
 * Created by chucvv on 6/7/17.
 */

public class PlatformInfoInteractor implements PlatformInfoStore.Interactor {
    private PlatformInfoStore.PlatformInfoService mService;
    private PlatformInfoStore.LocalStorage mLocalStorage;

    @Inject
    public PlatformInfoInteractor(PlatformInfoStore.PlatformInfoService service, PlatformInfoStore.LocalStorage localStorage) {
        this.mService = service;
        this.mLocalStorage = localStorage;
    }

    Observable<PlatformInfoCallback> mapResult(PlatformInfoResponse platformInfoResponse, String appVersion) {
        if (platformInfoResponse == null) {
            return Observable.error(new RequestException(RequestException.NULL,
                    GlobalData.getAppContext().getResources().getString(R.string.sdk_payment_generic_error_networking_mess)));
        }
        if (platformInfoResponse.forceappupdate) {
            //notify force user update new app on store
            VersionCallback upversionCallback = new VersionCallback(true,
                    platformInfoResponse.newestappversion,
                    platformInfoResponse.forceupdatemessage);
            return Observable.just(upversionCallback);
        }
        if (!TextUtils.isEmpty(appVersion) && !appVersion.equals(platformInfoResponse.newestappversion)) {
            //notify user  have a new version on store but not force user update
            VersionCallback upversionCallback = new VersionCallback(false,
                    platformInfoResponse.newestappversion,
                    platformInfoResponse.forceupdatemessage);
            return Observable.just(upversionCallback);
        }
        if (platformInfoResponse.returncode == 1) {
            PlatformInfoCallback platformInfoCallback = new PlatformInfoCallback();
            return Observable.just(platformInfoCallback);
        } else {
            return Observable.error(new RequestException(platformInfoResponse.returncode, platformInfoResponse.returnmessage));
        }
    }

    @Override
    public boolean isNewVersion(String appVersion) {
        String checksumSDKV = getAppVersion();
        return !TextUtils.isEmpty(appVersion) && !appVersion.equals(checksumSDKV);
    }

    @Override
    public boolean isNewUser(String userId) {
        String userIdOnCache = getUserId();
        return TextUtils.isEmpty(userIdOnCache) || !userIdOnCache.equals(userId);
    }

    /***
     * rule for forcing reload get platform info in SDK
     * 1.api platform info never run (checksum is empty)
     * 2.setup newer version
     * 3.login new user
     * 4.miss resource info version
     */
    private boolean forceReloadPlatformInfo(String pUserId) throws Exception {
        String checkSum = getPlatformInfoCheckSum();
        String appVersionCache = getAppVersion();
        String resourceVersion = getResourceVersion();
        boolean isNewUser = isNewUser(pUserId);
        return TextUtils.isEmpty(checkSum) || !SdkUtils.getAppVersion(GlobalData.getAppContext()).equals(appVersionCache) ||
                isNewUser || TextUtils.isEmpty(resourceVersion);
    }

    private Observable<PlatformInfoCallback> reloadPlatform(String userId, String accessToken, long currentTime) {
        try {
            boolean forceReloadApi = forceReloadPlatformInfo(userId);
            boolean isExpired = currentTime >= getExpireTime();
            boolean validResource = validFileConfig();
            if (!forceReloadApi && validResource && !isExpired) {
                return Observable.just(new PlatformInfoCallback());
            }
            return loadSDKPlatformFromCloud(userId, accessToken, forceReloadApi, !validResource);
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    @Override
    public String getUserId() {
        return mLocalStorage.getUserId();
    }

    @Override
    public String getResourcePath() {
        return mLocalStorage.getResourcePath();
    }

    /***
     * is file config.json existed?
     */
    @Override
    public boolean validFileConfig() {
        String path = getResourcePath();
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(path).append(File.separator).append(ResourceManager.CONFIG_FILE);
        File file = new File(pathBuilder.toString());
        return !TextUtils.isEmpty(path) && file.exists();
    }

    @Override
    public Observable<PlatformInfoCallback> loadSDKPlatform(String userId, String accessToken, long currentTime) {
        return reloadPlatform(userId, accessToken, currentTime);
    }

    @Override
    public Observable<PlatformInfoCallback> loadSDKPlatformFromCloud(String userId, String accessToken, boolean forceReloadApi, boolean forceDownloadResource) {
        Timber.d("start get platform info from server - should force reload api %s - force download res %s", forceReloadApi, forceDownloadResource);
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        Map<String, String> params = getParams(userId, accessToken, forceReloadApi, forceDownloadResource, appVersion);
        long startTime = System.currentTimeMillis();
        int apiId = ZPEvents.CONNECTOR_V001_TPE_V001GETPLATFORMINFO;
        return mService
                .fetch(params)
                .doOnError(throwable -> ZPAnalyticsTrackerWrapper.trackApiError(apiId, startTime, throwable))
                .doOnNext(platformInfoResponse -> mLocalStorage.put(params.get(ConstantParams.USER_ID), platformInfoResponse))
                .doOnNext(platformInfoResponse -> ZPAnalyticsTrackerWrapper.trackApiCall(apiId, startTime, platformInfoResponse))
                .concatMap(this::tryDownloadResource)
                .flatMap(new Func1<PlatformInfoResponse, Observable<PlatformInfoCallback>>() {
                    @Override
                    public Observable<PlatformInfoCallback> call(PlatformInfoResponse platformInfoResponse) {
                        return mapResult(platformInfoResponse, appVersion);
                    }
                });
    }

    private Map<String, String> getParams(String userId, String accessToken, boolean forceReloadPlatform, boolean forceDownloadResource, String appVersion) {
        String checksum = getPlatformInfoCheckSum();
        String resourceVersion = getResourceVersion();
        if (forceReloadPlatform) {
            checksum = null;
            mLocalStorage.setCardInfoCheckSum(null);
            mLocalStorage.setBankAccountCheckSum(null);
        }
        if (forceDownloadResource) {
            resourceVersion = null;
        }
        String cardInfoCheckSum = mLocalStorage.getCardInfoCheckSum();
        String bankAccountChecksum = mLocalStorage.getBankAccountCheckSum();
        //format data
        cardInfoCheckSum = cardInfoCheckSum != null ? cardInfoCheckSum : "";
        checksum = checksum != null ? checksum : "";
        resourceVersion = resourceVersion != null ? resourceVersion : "";
        bankAccountChecksum = bankAccountChecksum != null ? bankAccountChecksum : "";

        Map<String, String> params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            params = new ArrayMap<>();
        } else {
            params = new HashMap<>();
        }
        params.put(ConstantParams.USER_ID, userId);
        params.put(ConstantParams.ACCESS_TOKEN, accessToken);
        params.put(ConstantParams.PLATFORM_CODE, BuildConfig.PAYMENT_PLATFORM);
        float density = GlobalData.getAppContext().getResources().getDisplayMetrics().density;
        params.put(ConstantParams.DS_SCREEN_TYPE, DimensionUtil.getScreenType(density));
        params.put(ConstantParams.PLATFORM_IN_FOCHECKSUM, checksum);
        params.put(ConstantParams.RESOURCE_VERSION, resourceVersion);
        params.put(ConstantParams.APP_VERSION, appVersion);
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
        params.put(ConstantParams.CARDINFO_CHECKSUM, cardInfoCheckSum);
        params.put(ConstantParams.BANKACCOUNT_CHECKSUM, bankAccountChecksum);
        return params;
    }

    private Observable<PlatformInfoResponse> tryDownloadResource(PlatformInfoResponse platformInfoResponse) {
        /*
         need to download new resource if
         1.server return isupdateresource = true;
         2.resource version on cached client and resource version server return is different.This case user no need to update app.
         */
        Timber.d("start check download resource");
        String resourceVersion = getResourceVersion();
        if (platformInfoResponse.resource == null) {
            return Observable.just(platformInfoResponse);
        }

        if (!TextUtils.isEmpty(resourceVersion) &&
                !platformInfoResponse.isupdateresource &&
                resourceVersion.equals(platformInfoResponse.resource.rsversion)) {
            return Observable.just(platformInfoResponse);
        }

        mLocalStorage.setResourceDownloadUrl(platformInfoResponse.resource.rsurl);

        Timber.d("start download sdk resource %s", platformInfoResponse.resource.rsurl);
        Context context = GlobalData.getAppContext();
        IDownloadService downloadService = SDKApplication.getApplicationComponent().downloadService();
        ResourceInteractor downloadResourceTask = new ResourceInteractor(context, downloadService,
                mLocalStorage,
                platformInfoResponse.resource.rsurl,
                platformInfoResponse.resource.rsversion);
        return downloadResourceTask.fetchResource()
                .map(b -> platformInfoResponse);
    }

    @Override
    public long getPlatformInfoDurationExpire() {
        return mLocalStorage.getExpireTimeDuration();
    }

    @Override
    public long getExpireTime() {
        return mLocalStorage.getExpireTime();
    }

    @Override
    public String getAppVersion() {
        return mLocalStorage.getAppVersion();
    }

    @Override
    public String getResourceVersion() {
        return mLocalStorage.getResourceVersion();
    }

    @Override
    public String getPlatformInfoCheckSum() {
        return mLocalStorage.getPlatformInfoCheckSum();
    }

    @Override
    public boolean enableTopup() {
        return mLocalStorage.enableTopup();
    }

    @Override
    public Maintenance withdrawMaintain() {
        return mLocalStorage.withdrawMaintain();
    }
}
