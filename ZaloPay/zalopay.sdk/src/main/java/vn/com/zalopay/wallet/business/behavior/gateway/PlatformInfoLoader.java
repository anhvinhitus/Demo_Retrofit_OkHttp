package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkResourceInitMessage;
import vn.com.zalopay.wallet.event.SdkStartInitResourceMessage;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.interactor.IPlatformInfo;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;
import vn.com.zalopay.wallet.interactor.UpversionCallback;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;

public class PlatformInfoLoader extends SingletonBase {
    private static PlatformInfoLoader _object;
    protected UserInfo mUserInfo;
    protected IPlatformInfo mPlatformInteractor;
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
                ChannelActivity activity = BaseActivity.getChannelActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.getAdapter().getPaymentInfoHelper().updateTransactionResult(requestException.code);
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
            SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_PLATFORMINFO_END);
            if (platformInfoCallback instanceof UpversionCallback) {
                UpversionCallback upversionCallback = (UpversionCallback) platformInfoCallback;
                Log.d(PlatformInfoLoader.this, "need to up version from get platform info");
                if (!upversionCallback.forceupdate) {
                    SdkStartInitResourceMessage message = new SdkStartInitResourceMessage();
                    SDKApplication.getApplicationComponent().eventBus().post(message);
                }
                SdkUpVersionMessage message = new SdkUpVersionMessage(upversionCallback.forceupdate, upversionCallback.forceupdatemessage,
                        upversionCallback.newestappversion);
                SDKApplication.getApplicationComponent().eventBus().post(message);
            } else {
                Log.d(PlatformInfoLoader.this, "get platforminfo success, continue initialize resource to memory");
                SdkStartInitResourceMessage message = new SdkStartInitResourceMessage();
                SDKApplication.getApplicationComponent().eventBus().post(message);
            }
        }
    };

    public PlatformInfoLoader(UserInfo pUserInfo) {
        super();
        mUserInfo = pUserInfo;
        mPlatformInteractor = SDKApplication.getApplicationComponent().platformInfoInteractor();
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
        long expiredTime = mPlatformInteractor.getExpireTime();
        String checksumSDKV = mPlatformInteractor.getCheckSum();
        String userId = mPlatformInteractor.getUserId();
        boolean isNewUser = TextUtils.isEmpty(pUserId) || !pUserId.equals(userId);
        return currentTime > expiredTime || !SdkUtils.getAppVersion(GlobalData.getAppContext()).equals(checksumSDKV) ||
                !mPlatformInteractor.isValidConfig() || isNewUser;
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
        } else if (!mPlatformInteractor.isValidConfig()) {
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
            SdkStartInitResourceMessage message = new SdkStartInitResourceMessage();
            SDKApplication.getApplicationComponent().eventBus().post(message);
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
        String resourceVersion = mPlatformInteractor.getResourceVersion();
        String resourceDownloadUrl = mPlatformInteractor.getResourceDownloadUrl();
        if (!TextUtils.isEmpty(resourceDownloadUrl) && !TextUtils.isEmpty(resourceVersion)) {
            downloadResource(resourceDownloadUrl, resourceVersion);
        } else {
            loadPlatformInfo(true, true);
        }
    }

    private Subscription downloadResource(String pUrl, String pResourceVersion) {
        Log.d(this, "starting retry download resource " + pUrl);
        return mPlatformInteractor.getSDKResource(pUrl, pResourceVersion)
                .subscribe(aBoolean -> Log.d(this, "download resource on complete"),
                        throwable -> Log.d(this, "download resource on error", throwable));
    }

    private Subscription loadPlatformInfo(boolean pForceReload, boolean downloadResource) {
        Log.d(this, "need to retry load platforminfo again force " + pForceReload);
        long currentTime = System.currentTimeMillis();
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_PLATFORMINFO_START);
        return mPlatformInteractor
                .loadPlatformInfo(mUserInfo.zalopay_userid, mUserInfo.accesstoken, pForceReload, downloadResource, currentTime, appVersion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(platformInfoSubscriber);
    }
}
