package vn.com.zalopay.wallet.ui;

import android.support.annotation.CallSuper;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.interactor.IPlatformInfo;
import vn.com.zalopay.wallet.interactor.ResourceLoader;

/**
 * Created by chucvv on 6/24/17.
 */

public abstract class PaymentPresenter<T extends IContract> extends AbstractPresenter<T> implements ResourceLoader.ResourceLoaderListener {
    public Action1<Throwable> bankListException = this::loadBankListOnError;
    protected ZPMonitorEventTiming mEventTiming = SDKApplication.getApplicationComponent().monitorEventTiming();
    protected Action1<Boolean> onResourceComplete = initialized -> {
        if (initialized) {
            onResourceReady();
        } else {
            onResourceError(new Throwable(GlobalData.getAppContext().getString(R.string.sdk_alert_generic_init_resource)));
        }
    };
    protected Action1<Throwable> onResourceException = throwable -> onResourceError(throwable);
    protected IPlatformInfo platformInteractor = SDKApplication.getApplicationComponent().platformInfoInteractor();
    private Action0 appInfoInProcess = this::loadAppInfoOnProcess;
    private Action1<AppInfo> appInfoSubscriber = this::loadAppInfoOnComplete;
    private Action1<Throwable> appInfoException = this::loadAppInfoOnError;
    private Action1<BankConfigResponse> bankListSubscriber = this::loadBankListOnComplete;
    private Action0 loadBankInProcess = this::loadBankListOnProgress;

    @CallSuper
    public void onUpdateVersion(SdkUpVersionMessage pMessage) {
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onUpVersion(pMessage.forceupdate, pMessage.version, pMessage.message);
        }
    }

    protected void callBackThenTerminate() {
    }

    protected void loadAppInfoOnProcess() {
    }

    protected void loadAppInfoOnError(Throwable throwable) {
    }

    protected void loadAppInfoOnComplete(AppInfo appInfo) {
    }

    protected void loadBankListOnComplete(BankConfigResponse bankConfigResponse) {
    }

    protected void loadBankListOnError(Throwable throwable) {
    }

    protected void loadBankListOnProgress() {
    }

    public synchronized void onResourceInit() {
        if (!platformInteractor.isValidConfig()) {
            Timber.d("resource still not exist - skip init resource - wait for finish loading");
            return;
        }
        if (ResourceManager.isInit()) {
            return;
        }

        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_INIT_RESOURCE_START);
        Subscription subscription = ResourceManager.initResource()
                .doOnNext(aBoolean -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_INIT_RESOURCE_END))
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(onResourceComplete, onResourceException);
        addSubscription(subscription);
    }

    public void onResourceError(Throwable e) {
    }

    public void onPlatformError(Throwable e){

    }

    @CallSuper
    public void onResourceReady() {
        SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_SDK_INIT_RESOURCE_END);
    }

    protected boolean loadStaticResource(UserInfo userInfo) throws Exception {
        try {
            Timber.d("start validate resource");
            ResourceLoader.get()
                    .presenter(this)
                    .platformInteractor(platformInteractor)
                    .userInfo(userInfo)
                    .checkResource();
        } catch (Exception e) {
            Log.e(this, e);
            return false;
        }
        return true;
    }

    protected boolean manualRelease() {
        return false;
    }

    public void callback() {
        Timber.d("callback presenter");
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
        if (manualRelease()) {
            SingletonLifeCircleManager.disposeAll();
        }
    }

    /***
     * load app info from cache or api
     */
    protected void loadAppInfo(IAppInfo appInfoInteractor, long appId, @TransactionType int transtype, UserInfo userInfo) {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = appInfoInteractor.loadAppInfo(appId, new int[]{transtype},
                userInfo.zalopay_userid, userInfo.accesstoken, appVersion, currentTime)
                .compose(SchedulerHelper.applySchedulers())
                .doOnSubscribe(appInfoInProcess)
                .subscribe(appInfoSubscriber, appInfoException);
        addSubscription(subscription);
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_GetAppInfo, ZPPaymentSteps.OrderStepResult_None);
        }
    }

    protected void loadBankList(IBank bankInteractor) {
        loadBankList(bankInteractor, bankListSubscriber);
    }

    public void loadBankList(IBank bankInteractor, Action1<BankConfigResponse> bankListSubscriber) {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = bankInteractor.getBankList(appVersion, currentTime)
                .compose(SchedulerHelper.applySchedulers())
                .doOnSubscribe(loadBankInProcess)
                .subscribe(bankListSubscriber, bankListException);
        addSubscription(subscription);
    }
}
