package vn.com.zalopay.wallet.interactor;

import rx.Observable;

/**
 * Created by chucvv on 6/7/17.
 */

public interface IPlatformInfo {
    boolean isNewVersion(String appVersion);

    void clearCardMapCheckSum();

    void clearBankAccountMapCheckSum();

    Observable<PlatformInfoCallback> loadPlatformInfoCloud(String userId, String accessToken, boolean forceReload, boolean downloadResource, long currentTime, String appVersion);
}
