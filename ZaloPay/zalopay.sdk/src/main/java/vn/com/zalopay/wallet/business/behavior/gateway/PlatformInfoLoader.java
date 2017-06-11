package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.task.DownloadResourceTask;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.interactor.IPlatformInfo;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;
import vn.com.zalopay.wallet.interactor.UpversionCallback;
import vn.com.zalopay.wallet.event.SdkResourceInitMessage;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;

public class PlatformInfoLoader extends SingletonBase {
    private static PlatformInfoLoader _object;
    protected UserInfo mUserInfo;
    protected IPlatformInfo platformInteractor;
    private Observer<PlatformInfoCallback> platformInfoSubscriber = new Observer<PlatformInfoCallback>() {
        @Override
        public void onCompleted() {
            Log.d(this, "load platform info oncomplete");
        }

        @Override
        public void onError(Throwable e) {
            Log.d(this, "load platform info on error", e);
            String message = null;
            if (e instanceof RequestException) {
                RequestException requestException = (RequestException) e;
                message = e.getMessage();
                if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                    ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).updatePaymentStatus(requestException.code);
                }
            }
            if (TextUtils.isEmpty(message)) {
                message = GlobalData.getStringResource(RS.string.sdk_load_generic_error_message);
            }
            SdkResourceInitMessage errorEvent = new SdkResourceInitMessage(false, message);
            SDKApplication.getApplicationComponent().eventBus().post(errorEvent);
        }

        @Override
        public void onNext(PlatformInfoCallback platformInfoCallback) {
            if (platformInfoCallback instanceof UpversionCallback) {
                UpversionCallback upversionCallback = (UpversionCallback) platformInfoCallback;
                Log.d(this, "need to up version from get platform info");
                if (!upversionCallback.forceupdate) {
                    if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                        ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).initializeResource();
                    }
                }
                SdkUpVersionMessage message = new SdkUpVersionMessage(upversionCallback.forceupdate, upversionCallback.forceupdatemessage,
                        upversionCallback.newestappversion);
                SDKApplication.getApplicationComponent().eventBus().post(message);
            } else {
                Log.d(this, "get platforminfo success, continue initialize resource to memory");
                if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                    ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).initializeResource();
                }
            }
        }
    };

    public PlatformInfoLoader(UserInfo pUserInfo) {
        super();
        mUserInfo = pUserInfo;
        platformInteractor = SDKApplication.getApplicationComponent().platformInfoInteractor();
    }

    public synchronized static PlatformInfoLoader getInstance(UserInfo pUserInfo) {
        if (PlatformInfoLoader._object == null) {
            PlatformInfoLoader._object = new PlatformInfoLoader(pUserInfo);
        }
        return PlatformInfoLoader._object;
    }

    /***
     * rule for retry call get platform info
     * 1.expired time over
     * 2.app version is different
     * 3.resource file not exist
     * 4.new user
     * @return
     */
    public boolean needGetPlatformInfo(String pUserId) throws Exception {
        long currentTime = System.currentTimeMillis();
        long expiredTime = platformInteractor.getExpireTime();
        String checksumSDKV = platformInteractor.getCheckSum();
        String userId = platformInteractor.getUserId();
        boolean isNewUser = TextUtils.isEmpty(pUserId) || !pUserId.equals(userId);
        return currentTime > expiredTime || !SdkUtils.getAppVersion(GlobalData.getAppContext()).equals(checksumSDKV) ||
                !platformInteractor.isValidConfig() || isNewUser;
    }

    public void checkPlatformInfo() throws Exception {
        boolean needReload;
        try {
            needReload = needGetPlatformInfo(mUserInfo.zalopay_userid);
        } catch (Exception e) {
            Log.e(this, e);
            needReload = true;
        }
        if (needReload) {
            try {
                Log.d(this, "start retry platform info");
                loadPlatformInfo(false, false);
            } catch (Exception e) {
                Log.e(this, e);
                throw e;
            }
        } else if (!platformInteractor.isValidConfig()) {
            try {
                Log.d(this, "resource not found - start retry load plaform info");
                retryLoadInfo();
            } catch (Exception e) {
                Log.d(this, e);
                throw e;
            }
        }
        //resource existed  and need to load into memory
        else if (!ResourceManager.isInit()) {
            Log.d(this, "resource was downloaded but not init - init resource now");
            if (BasePaymentActivity.getCurrentActivity() instanceof BasePaymentActivity) {
                ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).initializeResource();
            }
        }
        //everything is ok now.
        else {
            SdkResourceInitMessage message = new SdkResourceInitMessage(true);
            SDKApplication.getApplicationComponent().eventBus().post(message);
        }
    }

    /***
     * in case load gateway info successful but can not donwload resource
     * then we had resource version,resource url in cache
     * now need to retry to download again.
     */
    private void retryLoadInfo() throws Exception {
        String resourceVersion = platformInteractor.getResourceVersion();
        String resourceDownloadUrl = platformInteractor.getResourceDownloadUrl();
        if (!TextUtils.isEmpty(resourceDownloadUrl) && !TextUtils.isEmpty(resourceVersion)) {
            downloadResource(resourceDownloadUrl, resourceVersion);
        } else {
            loadPlatformInfo(true, true);
        }
    }

    private void downloadResource(String pUrl, String pResourceVersion) {
        DownloadResourceTask downloadResourceTask = new DownloadResourceTask(pUrl, pResourceVersion);
        downloadResourceTask.makeRequest();
        Log.d(this, "starting retry download resource " + pUrl);
    }

    private Subscription loadPlatformInfo(boolean pForceReload, boolean downloadResource) {
        Log.d(this, "need to retry load platforminfo again force " + pForceReload);
        long currentTime = System.currentTimeMillis();
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        return platformInteractor
                .loadPlatformInfo(mUserInfo.zalopay_userid, mUserInfo.accesstoken, pForceReload, downloadResource, currentTime, appVersion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(platformInfoSubscriber);
    }
}
