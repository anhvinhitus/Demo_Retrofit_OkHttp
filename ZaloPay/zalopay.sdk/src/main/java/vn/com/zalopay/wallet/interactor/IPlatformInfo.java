package vn.com.zalopay.wallet.interactor;

import rx.Observable;

/**
 * Created by chucvv on 6/7/17.
 */

public interface IPlatformInfo {
    boolean isNewVersion(String appVersion);

    void clearCardMapCheckSum();

    void clearBankAccountMapCheckSum();

    long getExpireTime();

    String getAppVersion();

    String getPlatformInfoCheckSum();

    String getUserId();

    String getUnzipPath();

    String getResourceDownloadUrl();

    String getResourceVersion();

    boolean isValidConfig();

    Observable<Boolean> getSDKResource(String pUrl, String pResourceVersion);

    /***
     * Platform info expire time,unix time to exprired time (in milisecond)
     * After this expire time, client need hit to server again
     * @return
     */
    long getPlatformInfoDurationExpire();

    Observable<PlatformInfoCallback> loadPlatformInfo(String userId, String accessToken, boolean forceReload, boolean downloadResource, long currentTime, String appVersion);
}
