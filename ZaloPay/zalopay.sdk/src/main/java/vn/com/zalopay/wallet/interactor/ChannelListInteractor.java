package vn.com.zalopay.wallet.interactor;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.event.SdkPaymentInfoReadyMessage;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

/**
 * Created by huuhoa on 6/29/17.
 * Interactor class to prepare payment information for ChannelListPresenter
 */

public class ChannelListInteractor {
    private final IAppInfo mAppInfoInteractor;
    private final IBank mBankInteractor;
    private final IPlatformInfo mPlatformInteractor;
    private final ZPMonitorEventTiming mEventTiming;
    private final Handler mApplicationHandler;

    private CompositeSubscription mSubscription = new CompositeSubscription();
    private PaymentInfoHelper mPaymentInfoHelper = null;
    private OnPaymentReadyListener mPaymentReadyListener = null;
    private SdkPaymentInfoReadyMessage mPaymentInfoReadyMessage = null;

    @Inject
    public ChannelListInteractor(Application application,
                                 IPlatformInfo platformInteractor,
                                 IAppInfo appInfoInteractor,
                                 IBank bankInteractor,
                                 ZPMonitorEventTiming eventTiming) {
        mApplicationHandler = new Handler(application.getMainLooper());

        mPlatformInteractor = platformInteractor;
        mAppInfoInteractor = appInfoInteractor;
        mBankInteractor = bankInteractor;
        mEventTiming = eventTiming;
    }

    /**
     * Start collect payment information for ChannelListPresenter to use
     * When done, post a event to ChannelListPresenter
     *
     * @param paymentInfoHelper
     */
    public void collectPaymentInfo(PaymentInfoHelper paymentInfoHelper) {
        cancelCurrentTask(paymentInfoHelper);

        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_GetAppInfo, ZPPaymentSteps.OrderStepResult_None);
        }

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                loadPaymentInfo();
                return null;
            }
        };

        // TODO: Call in MainThread
        task.execute();
    }

    private void loadPaymentInfo() {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        Observable<AppInfo> appInfoObservable = mAppInfoInteractor.loadAppInfo(
                mPaymentInfoHelper.getAppId(),
                new int[]{mPaymentInfoHelper.getTranstype()},
                userInfo.zalopay_userid, userInfo.accesstoken, appVersion, currentTime)
                .doOnSubscribe(() -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_APPINFO_START))
                .doOnNext(bankConfigResponse -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_APPINFO_END));

        currentTime = System.currentTimeMillis();
        Observable<BankConfigResponse> bankObservable = mBankInteractor.getBankList(appVersion, currentTime)
                .doOnSubscribe(() -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_BANKLIST_START))
                .doOnNext(bankConfigResponse -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_BANKLIST_END));

        currentTime = System.currentTimeMillis();
        Observable<Boolean> platformObservable = mPlatformInteractor.loadSDKPlatform(userInfo.zalopay_userid, userInfo.accesstoken, currentTime)
                .doOnSubscribe(() -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_PLATFORMINFO_START))
                .doOnNext(platformInfoCallback -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_PLATFORMINFO_END));

        Observable<Boolean> initResource = ResourceManager.initResource()
                .doOnSubscribe(() -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_INIT_RESOURCE_START))
                .doOnNext(aBoolean -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_INIT_RESOURCE_END));

        Subscription subscription = Observable.zip(appInfoObservable, bankObservable, platformObservable, initResource, this::zipData)
                .observeOn(Schedulers.io())
                .subscribe(this::loadInfoCompleted, this::loadInfoError);
        mSubscription.add(subscription);
    }

    public void subscribeOnPaymentReady(OnPaymentReadyListener listener) {
        if (mPaymentInfoReadyMessage == null) {
            mPaymentReadyListener = listener;
        } else {
            mPaymentReadyListener = listener;
            postReadyMessage();
        }
    }

    public void cleanup() {
        cancelCurrentTask(null);
        mPaymentInfoReadyMessage = null;
    }

    private SdkPaymentInfoReadyMessage zipData(AppInfo appInfo,
                                               BankConfigResponse bankConfigResponse,
                                               boolean finish,
                                               boolean resourceInitialized) {
        SdkPaymentInfoReadyMessage message = new SdkPaymentInfoReadyMessage();
        message.mAppInfo = appInfo;
        message.mErrorType = SdkPaymentInfoReadyMessage.ErrorType.SUCCESS;
        return message;
    }

    private void loadInfoError(Throwable throwable) {
        mPaymentInfoReadyMessage = new SdkPaymentInfoReadyMessage();
        mPaymentInfoReadyMessage.mError = throwable;
        mPaymentInfoReadyMessage.mErrorType = SdkPaymentInfoReadyMessage.ErrorType.LOAD_PAYMENT_INFO_ERROR;

        if (mPaymentReadyListener != null) {
            postReadyMessage();
        }
    }

    private void loadInfoCompleted(SdkPaymentInfoReadyMessage message) {
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_ON_INFO_READY);
        mPaymentInfoReadyMessage = message;
        if (mPaymentReadyListener != null) {
            postReadyMessage();
        }
    }

    private void cancelCurrentTask(PaymentInfoHelper paymentInfoHelper) {
        mSubscription.clear();
        mPaymentInfoHelper = paymentInfoHelper;
        mPaymentReadyListener = null;
    }

    private void postReadyMessage() {
        mApplicationHandler.post(() -> {
            if (mPaymentReadyListener == null) {
                return;
            }

            mPaymentReadyListener.onPaymentInfoReady(mPaymentInfoReadyMessage);
            mPaymentReadyListener = null;
            mPaymentInfoReadyMessage = null;
        });
    }

    public interface OnPaymentReadyListener {
        void onPaymentInfoReady(SdkPaymentInfoReadyMessage message);
    }
}
