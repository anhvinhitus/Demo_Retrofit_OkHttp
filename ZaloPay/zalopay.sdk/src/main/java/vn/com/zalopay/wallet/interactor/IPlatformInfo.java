package vn.com.zalopay.wallet.interactor;

import rx.Observable;

/**
 * Created by chucvv on 6/7/17.
 */

public interface IPlatformInfo {
    boolean isNewVersion(String appVersion);

    boolean isNewUser(String userId);

    long getExpireTime();

    String getAppVersion();

    String getPlatformInfoCheckSum();

    String getUserId();

    String getUnzipPath();

    String getResourceVersion();

    boolean validFileConfig();

    Observable<PlatformInfoCallback> loadSDKPlatform(String userId, String accessToken, long currentTime);

    Observable<PlatformInfoCallback> loadSDKPlatformFromCloud(String userId, String accessToken, boolean forceReloadApi, boolean forceDownloadResource);

    /***
     * Platform info expire time,unix time to exprired time (in milisecond)
     * After this expire time, client need hit to server again
     * @return
     */
    long getPlatformInfoDurationExpire();
}
