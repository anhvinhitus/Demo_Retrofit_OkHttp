package vn.com.zalopay.wallet.interactor;

import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.DeviceUtil;
import vn.com.zalopay.utility.DimensionUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.datasource.task.DownloadResourceTask;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
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

    @Override
    public boolean isNewVersion(String appVersion) {
        String checksumSDKV = repository.getLocalStorage().getChecksumSDKVersion();
        return !TextUtils.isEmpty(appVersion) && !appVersion.equals(checksumSDKV);
    }

    private boolean isNewUser(String userId) {
        String userIdOnCache = this.repository.getLocalStorage().getUserId();
        return TextUtils.isEmpty(userIdOnCache) || !userIdOnCache.equals(userId);
    }

    @Override
    public Observable<PlatformInfoCallback> loadPlatformInfoCloud(String userId, String accessToken, boolean forceReload, boolean shouldDownloadResource, long currentTime, String appVersion) {
        //build params
        Log.d(this, "===prepare param to get platform info from server===");
        String checksum = repository.getLocalStorage().getChecksumSDK();
        String checksumSDKV = repository.getLocalStorage().getChecksumSDKVersion();
        String resrcVer = repository.getLocalStorage().getResourceVersion();
        //is this new user ?
        boolean isNewUser = isNewUser(userId);
        //mForceReload :: refresh gateway info from app
        if ((!TextUtils.isEmpty(appVersion) && !appVersion.equals(checksumSDKV)) || !BGatewayInfo.isValidConfig() || isNewUser || forceReload) {
            checksum = null;   //server will see this is new install, so return new resource to download
            resrcVer = null;
            repository.getLocalStorage().setCardInfoCheckSum(null);
            repository.getLocalStorage().setBankAccountCheckSum(null);
            Log.d(this, "checksum =null resrVer=null..reset card check sum, bank checksum");
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
        params.put(ConstantParams.DS_SCREEN_TYPE, DimensionUtil.getScreenType(GlobalData.getAppContext()));
        params.put(ConstantParams.PLATFORM_IN_FOCHECKSUM, checksum);
        params.put(ConstantParams.RESOURCE_VERSION, resrcVer);
        params.put(ConstantParams.APP_VERSION, appVersion);
        params.put(ConstantParams.DEVICE_MODEL, DeviceUtil.getDeviceName());
        params.put(ConstantParams.MNO, ConnectionUtil.getSimOperator(GlobalData.getAppContext()));
        params.put(ConstantParams.CARDINFO_CHECKSUM, cardInfoCheckSum);
        params.put(ConstantParams.BANKACCOUNT_CHECKSUM, bankAccountChecksum);

        Observable<PlatformInfoCallback> infoOnCache = repository
                .getLocalStorage()
                .get();
        Observable<PlatformInfoCallback> infoOnCloud = repository
                .fetchCloud(params)
                .doOnNext(downloadResource(shouldDownloadResource))
                .flatMap(mapResult(params.get(ConstantParams.APP_VERSION)));
        return Observable.concat(infoOnCache, infoOnCloud).first(platformInfoCallback -> platformInfoCallback != null && (platformInfoCallback.expire_time > currentTime))
                .compose(SchedulerHelper.applySchedulers());
    }

    private Action1<PlatformInfoResponse> downloadResource(boolean shouldDownloadResource) {
        return new Action1<PlatformInfoResponse>() {
            @Override
            public void call(PlatformInfoResponse pResponse) {
                /*
                  need to download new resource if
                  1.server return isupdateresource = true;
                  2.resource version on cached client and resource version server return is different.This case user no need to update app.
                 */
                String resrcVer = repository.getLocalStorage().getResourceVersion();
                if (shouldDownloadResource && pResponse.resource != null && (pResponse.isupdateresource ||
                        (!TextUtils.isEmpty(resrcVer) && !resrcVer.equals(pResponse.resource.rsversion)))) {
                    repository.getLocalStorage().setResourceVersion(pResponse.resource.rsversion);
                    repository.getLocalStorage().setResourceDownloadUrl(pResponse.resource.rsurl);
                    Log.d(this, "start download sdk resource " + pResponse.resource.rsurl);
                    DownloadResourceTask downloadResourceTask = new DownloadResourceTask(pResponse.resource.rsurl, pResponse.resource.rsversion);
                    downloadResourceTask.makeRequest();
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
                UpversionCallback upversionCallback = new UpversionCallback(platformInfoResponse.forceappupdate, platformInfoResponse.newestappversion,
                        platformInfoResponse.forceupdatemessage, expiretime);
                return Observable.just(upversionCallback);
            } else if (!TextUtils.isEmpty(appVersion) && !appVersion.equals(platformInfoResponse.newestappversion)) {
                //notify user  have a new version on store but not force user update
                UpversionCallback upversionCallback = new UpversionCallback(false, platformInfoResponse.newestappversion,
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
}
