package vn.com.zalopay.wallet.interactor;

import rx.Observable;

/**
 * Created by chucvv on 6/7/17.
 */

public interface IPlatformInfo {
    boolean isNewVersion(String appVersion);

    boolean isNewUser(String userId);

    void clearCardMapCheckSum();

    void clearBankAccountMapCheckSum();

    long getExpireTime();

    void resetExpireTime();

    String getAppVersion();

    String getPlatformInfoCheckSum();

    String getUserId();

    String getUnzipPath();

    String getResourceDownloadUrl();

    String getResourceVersion();

    boolean validFileConfig();

    Observable<Boolean> getSDKResource(String pUrl, String pResourceVersion);

    Observable<Boolean> loadSDKPlatform(String userId, String accessToken, long currentTime);

    /***
     * Platform info expire time,unix time to exprired time (in milisecond)
     * After this expire time, client need hit to server again
     * @return
     */
    long getPlatformInfoDurationExpire();

    Observable<PlatformInfoCallback> loadPlatformInfo(String userId, String accessToken, boolean forceReload, boolean downloadResource, long currentTime, String appVersion);
}
