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
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.DeviceUtil;
import vn.com.zalopay.utility.DimensionUtil;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.IDownloadService;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.constants.ConstantParams;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStore;

/**
 * Interactor decide which get data from
 * do some bussiness logic on return result and delegate the result to caller
 * Created by chucvv on 6/7/17.
 */

public class PlatformInfoInteractor implements IPlatformInfo {
    private PlatformInfoStore.Repository repository;

    @Inject
    public PlatformInfoInteractor(PlatformInfoStore.Repository repository) {
        this.repository = repository;
    }

    Observable<PlatformInfoCallback> mapResult(PlatformInfoResponse platformInfoResponse, String appVersion) {
        if (platformInfoResponse == null) {
            return Observable.error(new RequestException(RequestException.NULL, GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error)));
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

    public PlatformInfoStore.LocalStorage getLocalStorage() {
        return repository.getLocalStorage();
    }

    @Override
    public boolean isNewVersion(String appVersion) {
        String checksumSDKV = repository.getLocalStorage().getAppVersion();
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
        return this.repository.getLocalStorage().getUserId();
    }

    @Override
    public String getUnzipPath() {
        return this.repository.getLocalStorage().getUnzipPath();
    }

    /***
     * is file config.json existed?
     */
    @Override
    public boolean validFileConfig() {
        String path = getUnzipPath();
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
        return repository
                .fetchCloud(params)
                .concatMap(this::tryDownloadResource)
                .flatMap(new Func1<PlatformInfoResponse, Observable<PlatformInfoCallback>>() {
                    @Override
                    public Observable<PlatformInfoCallback> call(PlatformInfoResponse platformInfoResponse) {
                        return mapResult(platformInfoResponse, appVersion);
                    }
                });
    }

    private Map<String, String> getParams(String userId, String accessToken, boolean forceReloadPlatform, boolean forceDownloadResource, String appVersion) {
        String checksum = repository.getLocalStorage().getPlatformInfoCheckSum();
        String resourceVersion = repository.getLocalStorage().getResourceVersion();
        if (forceReloadPlatform) {
            checksum = null;
            repository.getLocalStorage().setCardInfoCheckSum(null);
            repository.getLocalStorage().setBankAccountCheckSum(null);
        }
        if (forceDownloadResource) {
            resourceVersion = null;
        }
        String cardInfoCheckSum = repository.getLocalStorage().getCardInfoCheckSum();
        String bankAccountChecksum = repository.getLocalStorage().getBankAccountCheckSum();
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
        String resourceVersion = repository.getLocalStorage().getResourceVersion();
        if (platformInfoResponse.resource == null) {
            return Observable.just(platformInfoResponse);
        }

        if (!TextUtils.isEmpty(resourceVersion) &&
                !platformInfoResponse.isupdateresource &&
                resourceVersion.equals(platformInfoResponse.resource.rsversion)) {
            return Observable.just(platformInfoResponse);
        }

        repository.getLocalStorage().setResourceDownloadUrl(platformInfoResponse.resource.rsurl);

        Timber.d("start download sdk resource %s", platformInfoResponse.resource.rsurl);
        Context context = GlobalData.getAppContext();
        IDownloadService downloadService = SDKApplication.getApplicationComponent().downloadService();
        ResourceInteractor downloadResourceTask = new ResourceInteractor(context, downloadService,
                repository.getLocalStorage(),
                platformInfoResponse.resource.rsurl,
                platformInfoResponse.resource.rsversion);
        return downloadResourceTask.fetchResource()
                .map(b -> platformInfoResponse);
    }

    @Override
    public long getPlatformInfoDurationExpire() {
        return repository.getLocalStorage().getExpireTimeDuration();
    }

    @Override
    public long getExpireTime() {
        return repository.getLocalStorage().getExpireTime();
    }

    @Override
    public String getAppVersion() {
        return repository.getLocalStorage().getAppVersion();
    }

    @Override
    public String getResourceVersion() {
        return repository.getLocalStorage().getResourceVersion();
    }

    @Override
    public String getPlatformInfoCheckSum() {
        return repository.getLocalStorage().getPlatformInfoCheckSum();
    }
}
