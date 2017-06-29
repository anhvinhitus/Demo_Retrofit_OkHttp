package vn.com.zalopay.wallet.interactor;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkPaymentInfoReadyMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;

/**
 * Created by huuhoa on 6/29/17.
 * Interactor class to prepare payment information for ChannelListPresenter
 */

public class ChannelListInteractor {
    private final IAppInfo mAppInfoInteractor;
    private final ILink mLinkInteractor;
    private final IBank mBankInteractor;
    private final ZPMonitorEventTiming mEventTiming;
    private PaymentInfoHelper mPaymentInfoHelper;
    protected CompositeSubscription mSubscription = new CompositeSubscription();
    private OnPaymentReadyListener mPaymentReadyListener = null;
    private SdkPaymentInfoReadyMessage mPaymentInfoReadyMessage = null;
    private Handler mApplicationHandler;

    public interface OnPaymentReadyListener {
        void onPaymentInfoReady(SdkPaymentInfoReadyMessage message);
    }

    @Inject
    public ChannelListInteractor(Application application,
                                 IAppInfo appInfoInteractor,
                                 ILink linkInteractor,
                                 IBank bankInteractor,
                                 ZPMonitorEventTiming eventTiming) {
        mApplicationHandler = new Handler(application.getMainLooper());

        mAppInfoInteractor = appInfoInteractor;
        mLinkInteractor = linkInteractor;
        mBankInteractor = bankInteractor;
        mEventTiming = eventTiming;
    }

    /**
     * Start collect payment information for ChannelListPresenter to use
     * When done, post a event to ChannelListPresenter
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
                .doOnSubscribe(this::loadAppInfoOnProcess)
                .doOnNext(this::loadAppInfoOnComplete);

        Observable<Boolean> linkObservable = mLinkInteractor
                .getMap(userInfo.zalopay_userid, userInfo.accesstoken, false, appVersion)
                .doOnSubscribe(() -> mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_CARDLIST_START))
                .doOnNext(this::loadCardListOnComplete);

        currentTime = System.currentTimeMillis();
        Observable<BankConfigResponse> bankObservable = mBankInteractor.getBankList(appVersion, currentTime)
                .doOnSubscribe(this::loadBankListOnProgress)
                .doOnNext(this::loadBankListOnComplete);

        Subscription subscription = Observable.zip(appInfoObservable, linkObservable, bankObservable, this::zipData)
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

    private SdkPaymentInfoReadyMessage zipData(AppInfo appInfo, Boolean aBoolean, BankConfigResponse bankConfigResponse) {
        SdkPaymentInfoReadyMessage message = new SdkPaymentInfoReadyMessage();
        message.mAppInfo = appInfo;
        return message;
    }


    private void loadInfoError(Throwable throwable) {

    }

    private void loadInfoCompleted(SdkPaymentInfoReadyMessage message) {
        mPaymentInfoReadyMessage = message;
        if (mPaymentReadyListener != null) {
            postReadyMessage();
        }
    }

    private void loadCardListOnComplete(Boolean result) {
        try {
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_CARDLIST_END);
//            readyForPayment();
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    private void cancelCurrentTask(PaymentInfoHelper paymentInfoHelper) {
        mSubscription.clear();
        mPaymentInfoHelper = paymentInfoHelper;
        mPaymentReadyListener = null;
    }

    private void loadAppInfoOnProcess() {
        try {
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_APPINFO_START);
            // getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_check_app_info));
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    private void loadAppInfoOnError(Throwable throwable) {
        Log.d(this, "load app info on error", throwable);
        try {
            //update payment status depend on api code from server
            if (throwable instanceof RequestException) {
                RequestException requestException = (RequestException) throwable;
                mPaymentInfoHelper.updateTransactionResult(requestException.code);
            }
            String message = TransactionHelper.getMessage(throwable);
            if (TextUtils.isEmpty(message)) {
                message = GlobalData.getStringResource(RS.string.sdk_load_appinfo_error_message);
            }
            boolean showDialog = ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus());
//            if (showDialog) {
//                getViewOrThrow().showError(message);
//            } else {
//                getViewOrThrow().hideLoading();
//                getViewOrThrow().callbackThenTerminate();
//            }
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    private void loadAppInfoOnComplete(AppInfo appInfo) {
        Log.d(this, "load app info success", appInfo);
        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_APPINFO_END);
//        mPaymentInfoReadyMessage = new SdkPaymentInfoReadyMessage();
//        mPaymentInfoReadyMessage.mAppInfo = appInfo;
//
//        if (mPaymentReadyListener != null) {
//            postReadyMessage();
//        }
    }

    private void loadBankListOnProgress() {
        try {
            mEventTiming.recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_BANKLIST_START);
//            getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void loadBankListOnError(Throwable throwable) {
        Log.d(this, "load bank list error", throwable);
        String message = TransactionHelper.getMessage(throwable);
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zpw_alert_error_networking_when_load_banklist);
        }
        try {
//            getViewOrThrow().showError(message);
        } catch (Exception e) {
            Timber.d(e != null ? e.getMessage() : "Exception");
        }
    }

    private void loadBankListOnComplete(BankConfigResponse bankConfigResponse) {
        try {
            SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_SDK_LOAD_BANKLIST_END);
//            loadChannels();
        } catch (Exception e) {
            Timber.d(e != null ? e.getMessage() : "Exception");
        }
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
}
