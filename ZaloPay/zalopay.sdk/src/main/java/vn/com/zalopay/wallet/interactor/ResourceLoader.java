package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;

public class ResourceLoader extends SingletonBase {
    private static ResourceLoader _object;
    protected UserInfo mUserInfo;
    private IPlatformInfo mPlatformInteractor;
    private WeakReference<ResourceLoaderListener> mPresenterWeakReference;
    private boolean forceDownloadResource;
    private ZPMonitorEventTiming mEventTiming;

    private Action1<PlatformInfoCallback> platformInfoSubscriber = platformInfoCallback -> {
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_PLATFORMINFO_END);
        if (platformInfoCallback instanceof VersionCallback) {
            try {
                VersionCallback upversionCallback = (VersionCallback) platformInfoCallback;
                Timber.d("need to up version from get platform info");
                SdkUpVersionMessage message = new SdkUpVersionMessage(upversionCallback.forceupdate,
                        upversionCallback.forceupdatemessage,
                        upversionCallback.newestappversion);
                getPresenter().onUpdateVersion(message);
                if (!upversionCallback.forceupdate) {
                    callbackOnPlatform();
                }
            } catch (Exception e) {
                Timber.w(e);
            }
        } else {
            Timber.d("platform info success");
            callbackOnPlatform();
        }
    };
    private Action1<Throwable> platformInfoException = throwable -> {
        Log.d(this, "load platform info on error", throwable);
        String message = null;
        if (throwable instanceof RequestException) {
            RequestException requestException = (RequestException) throwable;
            message = throwable.getMessage();
            ChannelActivity activity = BaseActivity.getChannelActivity();
            if (activity != null && !activity.isFinishing()) {
                activity.getAdapter().getPaymentInfoHelper().updateTransactionResult(requestException.code);
            }
        }
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.sdk_load_generic_error_message);
        }
        try {
            getPresenter().onPlatformError(new Exception(message));
        } catch (Exception e) {
            Timber.w(e);
        }
    };

    public ResourceLoader() {
        super();
        mEventTiming = SDKApplication.getApplicationComponent().monitorEventTiming();
    }

    public static ResourceLoader get() {
        if (ResourceLoader._object == null) {
            ResourceLoader._object = new ResourceLoader();
        }
        return ResourceLoader._object;
    }

    private void callbackOnPlatform() {
        if (!forceDownloadResource) {
            try {
                getPresenter().onResourceInit();
            } catch (Exception e) {
                Timber.w(e);
            }
        }
    }

    public ResourceLoader userInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
        return this;
    }

    public ResourceLoader platformInteractor(IPlatformInfo pPlatformInteractor) {
        mPlatformInteractor = pPlatformInteractor;
        forceDownloadResource = !mPlatformInteractor.isValidConfig();
        return this;
    }

    public ResourceLoader presenter(ResourceLoaderListener paymentPresenter) {
        mPresenterWeakReference = new WeakReference<>(paymentPresenter);
        return this;
    }

    private ResourceLoaderListener getPresenter() throws Exception {
        if (mPresenterWeakReference == null || mPresenterWeakReference.get() == null) {
            throw new IllegalStateException("invalid payment presenter");
        }
        return mPresenterWeakReference.get();
    }

    /***
     * rule for retry call get platform info in SDK
     * 1.api platform info never run (checksum is empty)
     * 2.setup newer version
     * 3.login new user
     * @return
     */
    private boolean needReloadPlatformInfo(String pUserId) throws Exception {
        String checkSum = mPlatformInteractor.getPlatformInfoCheckSum();
        String appVersionCache = mPlatformInteractor.getAppVersion();
        String userId = mPlatformInteractor.getUserId();
        boolean isNewUser = TextUtils.isEmpty(pUserId) || !pUserId.equals(userId);
        return TextUtils.isEmpty(checkSum) || !SdkUtils.getAppVersion(GlobalData.getAppContext()).equals(appVersionCache) || isNewUser;
    }

    public boolean validatePlatform() throws Exception {
        boolean reloadPlatform;
        try {
            reloadPlatform = needReloadPlatformInfo(mUserInfo.zalopay_userid);
        } catch (Exception e) {
            Log.e(this, e);
            reloadPlatform = true;
        }
        if (reloadPlatform) {
            Timber.d("start reload platform info");
            Subscription subscription = loadPlatformInfo(true, forceDownloadResource);
            getPresenter().addSubscription(subscription);
        }
        return reloadPlatform;
    }

    /***
     * get platform info already run but download resource fail
     * when into payment, sdk retry download resource
     * @return
     */
    private void retryDownloadResouce() throws Exception {
        String resourceVersion = mPlatformInteractor.getResourceVersion();
        String resourceDownloadUrl = mPlatformInteractor.getResourceDownloadUrl();
        Subscription subscription;
        if (!TextUtils.isEmpty(resourceDownloadUrl) && !TextUtils.isEmpty(resourceVersion)) {
            Timber.d("start retry download resource");
            subscription = downloadResource(resourceDownloadUrl, resourceVersion);
        } else {
            Timber.d("start reload platform info");
            subscription = loadPlatformInfo(true, true);
        }
        getPresenter().addSubscription(subscription);
    }

    public void checkResource() throws Exception {
        if (validatePlatform()) {
            return;
        }
        if (forceDownloadResource) {
            retryDownloadResouce();
            return;
        }
        //resource existed  and need to load into memory
        if (!ResourceManager.isInit()) {
            Timber.d("start init resource into memory");
            getPresenter().onResourceInit();
        }
        //resource ready for use
        else {
            getPresenter().onResourceReady();
        }
    }

    private Subscription downloadResource(String pUrl, String pResourceVersion) {
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_DOWNLOAD_RESOURCE_START);
        return mPlatformInteractor.getSDKResource(pUrl, pResourceVersion)
                .subscribe(aBoolean -> Timber.d("download resource on complete"),
                        throwable -> Log.d(this, "download resource on error", throwable));
    }

    private Subscription loadPlatformInfo(boolean pForceReload, boolean downloadResource) {
        Timber.d("load platform info again force reload:%s download resource:%s", pForceReload, downloadResource);
        long currentTime = System.currentTimeMillis();
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_PLATFORMINFO_START);
        return mPlatformInteractor
                .loadPlatformInfo(mUserInfo.zalopay_userid, mUserInfo.accesstoken, pForceReload, downloadResource, currentTime, appVersion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(platformInfoSubscriber, platformInfoException);
    }

    public interface ResourceLoaderListener {
        void onResourceInit();
        void onResourceReady();
        void addSubscription(Subscription subscription);

        void onPlatformError(Throwable e);

        void onUpdateVersion(SdkUpVersionMessage message);
    }
}
