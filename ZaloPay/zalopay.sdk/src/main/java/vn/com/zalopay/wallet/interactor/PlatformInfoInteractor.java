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
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.DeviceUtil;
import vn.com.zalopay.utility.DimensionUtil;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.IDownloadService;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
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
    private ZPMonitorEventTiming mEventTiming;

    @Inject
    public PlatformInfoInteractor(PlatformInfoStore.Repository repository, ZPMonitorEventTiming eventTiming) {
        this.repository = repository;
        this.mEventTiming = eventTiming;
    }

    public PlatformInfoStore.LocalStorage getLocalStorage() {
        return repository.getLocalStorage();
    }

    @Override
    public void resetExpireTime() {
        this.repository.getLocalStorage().setExpireTime(0);
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
     * 4. miss resource info version
     * @return
     */
    private boolean forceReloadPlatformInfo(String pUserId) throws Exception {
        String checkSum = getPlatformInfoCheckSum();
        String appVersionCache = getAppVersion();
        String resourceVersion = getResourceVersion();
        boolean isNewUser = isNewUser(pUserId);
        return TextUtils.isEmpty(checkSum) || !SdkUtils.getAppVersion(GlobalData.getAppContext()).equals(appVersionCache) ||
                isNewUser || TextUtils.isEmpty(resourceVersion);
    }

    public Observable<Boolean> reloadPlatform(String userId, String accessToken, long currentTime) {
        try {
            boolean forceReload = forceReloadPlatformInfo(userId);
            boolean isExpired = currentTime >= getExpireTime();
            boolean shouldDownloadRes = !isValidConfig();
            if (!forceReload && !shouldDownloadRes && !isExpired) {
                return Observable.just(true);
            }
            Timber.d("start reload platform info - force download resource %s", shouldDownloadRes);
            String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
            return loadPlatformInfo(userId, accessToken, forceReload, shouldDownloadRes, currentTime, appVersion)
                    .flatMap(new Func1<PlatformInfoCallback, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(PlatformInfoCallback platformInfoCallback) {
                            return Observable.just(shouldDownloadRes);
                        }
                    });
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    private Observable<Boolean> initResourceConfig() {
        Timber.d("start init SDK resource");
        if (ResourceManager.isInit()) {
            return Observable.just(true);
        }
        return ResourceManager.initResource()
                .doOnSubscribe(() -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_INIT_RESOURCE_START))
                .doOnNext(aBoolean -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_INIT_RESOURCE_END));
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
     * @return
     */
    @Override
    public boolean isValidConfig() {
        String path = getUnzipPath();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(path).append(File.separator).append(ResourceManager.CONFIG_FILE);
        File file = new File(stringBuilder.toString());
        return !TextUtils.isEmpty(path) && file.exists();
    }

    @Override
    public Observable<Boolean> loadSDKPlatform(String userId, String accessToken, long currentTime) {
        Observable<Boolean> reloadPlatform = reloadPlatform(userId, accessToken, currentTime);
        return reloadPlatform;
//        Observable<Boolean> loadSDKResource = initResourceConfig();
//        return Observable.concat(reloadPlatform, loadSDKResource)
//                .first(stopStream -> stopStream);
    }

    @Override
    public Observable<PlatformInfoCallback> loadPlatformInfo(String userId, String accessToken, boolean forceReload, boolean shouldDownloadResource, long currentTime, String appVersion) {
        Log.d(this, "prepare param to get platform info from server - should download resource", shouldDownloadResource);
        String checksum = repository.getLocalStorage().getPlatformInfoCheckSum();
        String resrcVer = repository.getLocalStorage().getResourceVersion();
        if (forceReload) {
            checksum = null;
            resrcVer = null;
            repository.getLocalStorage().setCardInfoCheckSum(null);
            repository.getLocalStorage().setBankAccountCheckSum(null);
            Timber.d("reset platform checksum for forcing reload");
        }
        String cardInfoCheckSum = repository.getLocalStorage().getCardInfoCheckSum();
        String bankAccountChecksum = repository.getLocalStorage().getBankAccountCheckSum();
        //format data
        cardInfoCheckSum = cardInfoCheckSum != null ? cardInfoCheckSum : "";
        checksum = checksum != null ? checksum : "";
        resrcVer = resrcVer != null ? resrcVer : "";
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
        params.put(ConstantParams.RESOURCE_VERSION, resrcVer);
        params.put(ConstantParams.APP_VERSION, appVersion);
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
        params.put(ConstantParams.CARDINFO_CHECKSUM, cardInfoCheckSum);
        params.put(ConstantParams.BANKACCOUNT_CHECKSUM, bankAccountChecksum);

        Observable<PlatformInfoCallback> infoOnCache = repository
                .getLocalStorage()
                .get()
                .subscribeOn(Schedulers.io());
        Observable<PlatformInfoCallback> infoOnCloud = repository
                .fetchCloud(params)
                .doOnNext(downloadResource(shouldDownloadResource))
                .flatMap(mapResult(params.get(ConstantParams.APP_VERSION)));
        return Observable.concat(infoOnCache, infoOnCloud)
                .first(platformInfoCallback -> platformInfoCallback != null && (platformInfoCallback.expire_time > currentTime));
    }

    @Override
    public Observable<Boolean> getSDKResource(String pUrl, String pResourceVersion) {
        Timber.d("start download sdk resource %s", pUrl);
        Context context = GlobalData.getAppContext();
        IDownloadService downloadService = SDKApplication.getApplicationComponent().downloadService();
        ResourceInteractor downloadResourceTask = new ResourceInteractor(context, downloadService, repository.getLocalStorage(), pUrl, pResourceVersion);
        return downloadResourceTask.getResource();
    }

    private Action1<PlatformInfoResponse> downloadResource(boolean shouldDownloadResource) {
        return new Action1<PlatformInfoResponse>() {
            @Override
            public void call(PlatformInfoResponse pResponse) {
                /**
                 need to download new resource if
                 1.server return isupdateresource = true;
                 2.resource version on cached client and resource version server return is different.This case user no need to update app.
                 */
                Log.d(this, "start download resource - should download ", shouldDownloadResource);
                String resourceVersion = repository.getLocalStorage().getResourceVersion();
                if (shouldDownloadResource && pResponse.resource != null && (pResponse.isupdateresource ||
                        (!TextUtils.isEmpty(resourceVersion) && !resourceVersion.equals(pResponse.resource.rsversion)))) {
                    repository.getLocalStorage().setResourceVersion(pResponse.resource.rsversion);
                    repository.getLocalStorage().setResourceDownloadUrl(pResponse.resource.rsurl);
                    getSDKResource(pResponse.resource.rsurl, pResponse.resource.rsversion)
                            .subscribe(aBoolean -> Timber.d("download resource on complete"),
                                    throwable -> Log.d(this, "download resource on error", throwable));
                }
            }
        };
    }


    private Func1<PlatformInfoResponse, Observable<PlatformInfoCallback>> mapResult(String appVersion) {
        return platformInfoResponse -> {
            long expiretime = repository.getLocalStorage().getExpireTime();
            if (platformInfoResponse == null) {
                return Observable.error(new RequestException(RequestException.NULL, null));
            } else if (platformInfoResponse.forceappupdate) {
                //notify force user update new app on store
                VersionCallback upversionCallback = new VersionCallback(platformInfoResponse.forceappupdate, platformInfoResponse.newestappversion,
                        platformInfoResponse.forceupdatemessage, expiretime);
                return Observable.just(upversionCallback);
            } else if (!TextUtils.isEmpty(appVersion) && !appVersion.equals(platformInfoResponse.newestappversion)) {
                //notify user  have a new version on store but not force user update
                VersionCallback upversionCallback = new VersionCallback(false, platformInfoResponse.newestappversion,
                        platformInfoResponse.forceupdatemessage, expiretime);
                return Observable.just(upversionCallback);
            } else if (platformInfoResponse.returncode == 1) {
                PlatformInfoCallback platformInfoCallback = new PlatformInfoCallback(expiretime);
                return Observable.just(platformInfoCallback);
            } else {
                return Observable.error(new RequestException(platformInfoResponse.returncode, platformInfoResponse.returnmessage));
            }
        };
    }

    @Override
    public void clearCardMapCheckSum() {
        repository.getLocalStorage().clearCardMapCheckSum();
    }

    @Override
    public void clearBankAccountMapCheckSum() {
        repository.getLocalStorage().clearBankAccountMapCheckSum();
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
    public String getResourceDownloadUrl() {
        return repository.getLocalStorage().getResourceDownloadUrl();
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
