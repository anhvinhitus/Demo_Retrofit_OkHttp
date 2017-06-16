package vn.com.zalopay.wallet.view.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnCloseDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnProgressDialogTimeoutListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.feedback.FeedbackCollector;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.PermissionUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.PlatformInfoLoader;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.dao.CFontManager;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.ESuggestActionType;
import vn.com.zalopay.wallet.business.entity.feedback.Feedback;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.KeyboardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.api.task.SDKReportTask;
import vn.com.zalopay.wallet.event.SdkDownloadResourceMessage;
import vn.com.zalopay.wallet.event.SdkLoadingTaskMessage;
import vn.com.zalopay.wallet.event.SdkNetworkEventMessage;
import vn.com.zalopay.wallet.event.SdkResourceInitMessage;
import vn.com.zalopay.wallet.event.SdkStartInitResourceMessage;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.listener.onShowDetailOrderListener;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.view.custom.EllipsizingTextView;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

public abstract class BasePaymentActivity extends FragmentActivity {
    private static Stack<BasePaymentActivity> mActivityStack = new Stack<>();//stack to keep activity
    public final String TAG = getClass().getSimpleName();
    public boolean mIsBackClick = true;
    public boolean processingOrder = false;//this is flag prevent user back when user is submitting trans,authen payer,getstatus
    public AppInfo appInfo;
    protected CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    protected String mTitleHeaderText;
    //dialog asking open networking listener
    public ZPWPaymentOpenNetworkingDialogListener paymentOpenNetworkingDialogListener = new ZPWPaymentOpenNetworkingDialogListener() {
        @Override
        public void onCloseNetworkingDialog() {
            if (getCurrentActivity() instanceof PaymentChannelActivity) {
                ((PaymentChannelActivity) getCurrentActivity()).getAdapter().closeSDKAfterNetworkOffline();
            }
        }

        @Override
        public void onOpenSettingDialogClicked() {
            if (getCurrentActivity() instanceof PaymentChannelActivity) {
                ((PaymentChannelActivity) getCurrentActivity()).resetPin();
            }
        }
    };
    protected EventBus mBus;
    protected PaymentInfoHelper mPaymentInfoHelper;
    protected int numberOfRetryOpenNetwoking = 0;//number of openning networking dialog retry
    protected boolean isAllowLinkCardATM = true;
    protected boolean isAllowLinkCardCC = true;
    /**
     * show more info icon click listener
     */
    protected onShowDetailOrderListener mShowDetailOrderClick = new onShowDetailOrderListener() {

        @Override
        public void onShowDetailOrder() {
            try {
                showInfoDialog(null, mPaymentInfoHelper.getOrder().description);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };
    protected ZPWOnCloseDialogListener mCloseDialog = this::onCloseDialogSelection;
    /***
     * loading website so long,over timeout 40s
     */
    int numberOfRetryTimeout = 1;
    ZPWOnProgressDialogTimeoutListener mProgressDialogTimeoutListener = new ZPWOnProgressDialogTimeoutListener() {
        @Override
        public void onProgressTimeout() {
            final WeakReference<Activity> activity = new WeakReference<>(BasePaymentActivity.getCurrentActivity());
            if (activity.get() == null || activity.get().isFinishing()) {
                Log.d(this, "===mProgressDialogTimeoutListener===activity == null || activity.isFinishing()");
                return;
            }
            try {
                //user in channel screen and not go to the result screen.
                if (activity.get() instanceof PaymentChannelActivity && !((PaymentChannelActivity) activity.get()).getAdapter().isFinalScreen()) {
                    //retry load website cc
                    if (ConnectionUtil.isOnline(GlobalData.getAppContext()) && getAdapter().isCCFlow() && getAdapter().isLoadWeb() && getAdapter().shouldGetOneShotTransactionStatus()) {
                        //max retry 3
                        if (numberOfRetryTimeout > Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_load_web_retry))) {
                            getAdapter().getOneShotTransactionStatus();
                            return;
                        }
                        numberOfRetryTimeout++;
                        DialogManager.showSweetDialogOptionNotice(activity.get(),
                                GlobalData.getStringResource(RS.string.zpw_string_load_website_timeout_message),
                                GlobalData.getStringResource(RS.string.dialog_continue_load_button),
                                GlobalData.getStringResource(RS.string.dialog_cancel_button),
                                new ZPWOnEventConfirmDialogListener() {
                                    @Override
                                    public void onCancelEvent() {
                                        getAdapter().getOneShotTransactionStatus();
                                    }

                                    @Override
                                    public void onOKevent() {
                                        //show loading dialog again
                                        DialogManager.showProcessDialog(activity.get(), mProgressDialogTimeoutListener);
                                        try {
                                            getAdapter().getGuiProcessor().reloadUrl();
                                        } catch (Exception e) {
                                            Log.e(this, e);
                                        }
                                    }
                                });

                        if (getAdapter() != null) {
                            try {
                                getAdapter().sdkReportError(SDKReportTask.TIMEOUT_WEBSITE);
                            } catch (Exception e) {
                                Log.d(this, e);
                            }
                        }
                        return;
                    }
                    //load web timeout, need to shared oneshot to server to check status again
                    if (ConnectionUtil.isOnline(GlobalData.getAppContext()) && getAdapter().isParseWebFlow() && getAdapter().shouldGetOneShotTransactionStatus()) {
                        getAdapter().getOneShotTransactionStatus();
                        //send logs timeout
                        if (getAdapter() != null) {
                            try {
                                getAdapter().sdkTrustReportError(SDKReportTask.TIMEOUT_WEBSITE);
                            } catch (Exception e) {
                                Log.d(this, e);
                            }
                        }
                        Log.d(this, "getOneShotTransactionStatus");
                    } else if (mPaymentInfoHelper.isBankAccountTrans() && getAdapter() instanceof AdapterLinkAcc && getAdapter().isFinalStep()) {
                        ((AdapterLinkAcc) getAdapter()).verifyServerAfterParseWebTimeout();
                        Log.d(this, "load website timeout, continue to verify server again to ask for new data list");
                    } else if (!getAdapter().isFinalScreen()) {
                        ((PaymentChannelActivity) activity.get()).showWarningDialog(() -> ((PaymentChannelActivity) activity.get()).getAdapter()
                                        .showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error)),
                                GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));//show dialog and move to fail screen
                    }
                    return;
                }
                //user in gateway screen
                /*if (activity.get() instanceof PaymentGatewayActivity) {
                    ((PaymentGatewayActivity) activity.get()).showWarningDialog(BasePaymentActivity.this::finish, GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
                }*/
            } catch (Exception ex) {
                ((PaymentChannelActivity) activity.get()).getAdapter().showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
                if (getAdapter() != null) {
                    try {
                        getAdapter().sdkReportError(SDKReportTask.GENERAL_EXCEPTION, ex != null ? ex.getMessage() : "onProgressTimeout");
                    } catch (Exception e) {
                        Log.d(this, e);
                    }
                }
            }
        }
    };
    public Action1<Throwable> bankListException = throwable -> {
        Log.d(this, "load appinfo on error", throwable);
        String message = getMessage(throwable);
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zpw_alert_error_networking_when_load_banklist);
        }
        onExit(message, true);
    };
    private boolean isVisibilitySupport = false;
    private Feedback mFeedback = null;
    //close snackbar networking alert listener
    private onCloseSnackBar mOnCloseSnackBarListener = this::askToOpenSettingNetwoking;
    private View.OnClickListener mSupportButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                int i = view.getId();

                if (i == R.id.question_button) {
                    Intent intent = new Intent();
                    intent.setAction(Constants.SUPPORT_INTRO_ACTION_SUPPORT_CENTER);
                    startActivity(intent);

                } else if (i == R.id.support_button) {
                    Log.d("OnClickListener", "support_button");

                    startSupportScreen();

                } else if (i == R.id.zpw_pay_support_buttom_view) {
                    Log.d("OnClickListener", "zpw_pay_support_buttom_view");

                } else if (i == R.id.cancel_button) {
                    Log.d("OnClickListener", "cancel_button");
                }
                closeSupportView();
            } catch (Exception ex) {
                Log.e(this, ex);
            }
        }
    };
    private Action1<Throwable> appInfoException = throwable -> {
        Log.d(this, "load appinfo on error", throwable);
        showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));
        String message = getMessage(throwable);
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.sdk_load_appinfo_error_message);
        }
        showDialogAndExit(message, ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus()));
    };

    public static Activity getCurrentActivity() {
        synchronized (mActivityStack) {
            if (mActivityStack == null || mActivityStack.size() == 0) {
                return GlobalData.getMerchantActivity();
            }
            return mActivityStack.peek();
        }
    }

    public static int getCurrentActivityCount() {
        if (mActivityStack != null) {
            return mActivityStack.size();
        }
        return 0;
    }

    public static BasePaymentActivity getPaymentChannelActivity() {
        if (mActivityStack == null || mActivityStack.size() <= 0) {
            return null;
        }
        for (BasePaymentActivity activity : mActivityStack) {
            if (activity instanceof PaymentChannelActivity) {
                return activity;
            }
        }
        return null;
    }

    public static void resetAttributeCascade(boolean... pAttr) {
        try {
            if (mActivityStack != null && mActivityStack.size() == 2) {
                mActivityStack.get(0).mIsBackClick = pAttr[0];
                mActivityStack.get(1).mIsBackClick = pAttr[0];
            }
        } catch (Exception e) {
            Log.d("resetAttributeCascade", e);
        }
    }

    public void updatePaymentStatus(int code) {
        mPaymentInfoHelper.updateTransactionResult(code);
    }

    public String getMessage(Throwable throwable) {
        String message = null;
        if (throwable instanceof RequestException) {
            RequestException requestException = (RequestException) throwable;
            message = requestException.getMessage();
            switch (requestException.code) {
                case RequestException.NULL:
                    message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
                    break;
                default:
                    updatePaymentStatus(requestException.code);
            }
        } else if (throwable instanceof NetworkConnectionException) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }
        return message;
    }

    public void addSuscription(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentInfoEvent(PaymentInfoHelper paymentInfoHelper) {
        mBus.removeStickyEvent(PaymentInfoHelper.class);
        mPaymentInfoHelper = paymentInfoHelper;
        paymentInfoReady();
        Log.d(this, "got event payment info", mPaymentInfoHelper);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnTaskInProcessEvent(SdkLoadingTaskMessage pMessage) {
        Log.d(this, "OnTaskInProcessEvent" + GsonUtils.toJsonString(pMessage));
        showProgress(true, pMessage.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnInitialResourceCompleteEvent(SdkResourceInitMessage pMessage) {
        Log.d(this, "OnFinishInitialResourceEvent" + GsonUtils.toJsonString(pMessage));
        if (pMessage.success) {
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
            Subscription subscription = SDKApplication.getApplicationComponent()
                    .linkInteractor()
                    .getMap(userInfo.zalopay_userid, userInfo.accesstoken, false, appVersion)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aBoolean -> readyForPayment(), throwable -> {
                        showDialogAndExit(GlobalData.getStringResource(RS.string.zpw_generic_error), true);
                        Log.e("load card and bank account error", throwable.getMessage());
                    });
            addSuscription(subscription);
        } else {
            Log.d(this, "init resource error " + pMessage);
            /***
             * delete folder resource to download again.
             * this prevent case file resource downloaded but was damaged on the wire so
             * can not parse json file.
             */
            try {
                String resPath = SharedPreferencesManager.getInstance().getUnzipPath();
                if (!TextUtils.isEmpty(resPath))
                    StorageUtil.deleteRecursive(new File(resPath));
            } catch (Exception e) {
                Log.d(this, e);
            }
            String message = pMessage.message;
            if (TextUtils.isEmpty(message)) {
                message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
            }
            showDialogAndExit(message, ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus()));   //notify error and close sdk
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUpVersionEvent(SdkUpVersionMessage pMessage) {
        Log.d(this, "OnUpVersionEvent" + GsonUtils.toJsonString(pMessage));
        notifyUpVersionToApp(pMessage.forceupdate, pMessage.version, pMessage.message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnDownloadResourceMessageEvent(SdkDownloadResourceMessage result) {
        Log.d(this, "OnDownloadResourceMessageEvent " + GsonUtils.toJsonString(result));
        if (result.success) {
            SdkStartInitResourceMessage message = new SdkStartInitResourceMessage();
            mBus.post(message);
        } else {
            SdkResourceInitMessage message = new SdkResourceInitMessage(result.success, result.message);
            mBus.post(message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnInitialResourceCompleteEvent(SdkStartInitResourceMessage pMessage) {
        if (!SDKApplication.getApplicationComponent().platformInfoInteractor().isValidConfig()) {
            Log.d(this, "call init resource but not ready for now, waiting for downloading resource");
            return;
        }
        Subscription subscription = ResourceManager.initResource()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    SdkResourceInitMessage message = new SdkResourceInitMessage(true);
                    mBus.post(message);
                }, throwable -> {
                    SdkResourceInitMessage message = new SdkResourceInitMessage(false, GlobalData.getStringResource(RS.string.zpw_alert_error_resource_not_download));
                    mBus.post(message);
                    Log.d("init resource fail", throwable);
                });
        mCompositeSubscription.add(subscription);
    }

    private void startSupportScreen() throws Exception {
        FeedBackCollector feedBackCollector = FeedBackCollector.shared();
        if (mFeedback != null) {
            FeedbackCollector collector = feedBackCollector.getFeedbackCollector();
            collector.setScreenShot(mFeedback.imgByteArray);
            collector.setTransaction(mFeedback.category, mFeedback.transID, mFeedback.errorCode, mFeedback.description);
        } else {
            Log.d("support_button", "IFeedBack == null");
        }
        feedBackCollector.showDialog(this);
    }

    protected void loadStaticReload() {
        try {
            Log.d(this, "check static resource start");
            PlatformInfoLoader.getInstance(mPaymentInfoHelper.getUserInfo()).checkPlatformInfo();
        } catch (Exception e) {
            showDialogAndExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error), true);   //notify error and close sdk
            Log.e(this, e);
        }
    }

    public abstract void paymentInfoReady();

    public abstract void callBackThenTerminate();

    protected abstract void actionIfPreventApp();

    protected abstract void showDialogAndExit(String pMessage, boolean pIsShow);

    protected abstract void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage);

    protected abstract void readyForPayment();

    protected AdapterBase getAdapter() {
        return null;
    }

    protected void onCloseDialogSelection() {
    }

    protected String getCloseButtonText() {
        return null;
    }

    public boolean isAllowLinkCardATM() {
        return isAllowLinkCardATM;
    }

    public boolean isAllowLinkCardCC() {
        return isAllowLinkCardCC;
    }

    protected void onReturnCancel(final String pMessage) {
        showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));
        showWarningDialog(() -> {
            GlobalData.updateResultNetworkingError(mPaymentInfoHelper, pMessage);
            if (GlobalData.getPaymentListener() != null) {
                GlobalData.getPaymentListener().onComplete();
            }
            finish();
        }, pMessage);
    }

    public void onExit(String pMessage, boolean pIsShowDialog) {
        showProgress(false, null);
        //just exit without show dialog.
        if (!pIsShowDialog) {
            callBackThenTerminate();
            return;
        }
        //continue to show dialog and quit.
        String message = pMessage;
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }

        showWarningDialog(this::callBackThenTerminate, message);
    }

    public void showDialogWarningLinkCardAndResetCardNumber() {
        showInfoDialog(() -> {
            if (getAdapter() != null) {
                getAdapter().getGuiProcessor().resetCardNumberAndShowKeyBoard();
            }
        }, GlobalData.getStringResource(RS.string.zpw_alert_linkcard_not_support));
    }

    public synchronized void recycleGateway() {
        setEnableView(R.id.zpsdk_exit_ctl, false);
        finish();
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
    }

    //animation
    protected void fadeOutTransition() {
        this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    protected void fadeInTransition() {
        this.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
    }

    protected void slideOutTransition() {
        this.overridePendingTransition(R.anim.closeslidein, R.anim.closeslideout);
    }

    public void requestPermission(Context pContext) {
        if (PermissionUtils.isNeedToRequestPermissionAtRuntime() && !PermissionUtils.checkIfAlreadyhavePermission(pContext)) {
            PermissionUtils.requestForSpecificPermission(this, Constants.REQUEST_CODE_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    Log.d(getClass().getName(), "permission granted");
                } else {
                    //not granted
                    Log.d(getClass().getName(), "permission not granted");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(this);
        ZPAnalytics.trackScreen(TAG);
        Log.d(this, "onStart");
    }

    @Override
    public void finish() {
        super.finish();
        if (mIsBackClick) {
            //notify to app know that user click back on sdk.
            int status = mPaymentInfoHelper.getStatus();
            if (status == PaymentStatus.FAILURE) {
                mPaymentInfoHelper.setResult(PaymentStatus.USER_CLOSE);
            }
            slideOutTransition();
        } else {
            fadeOutTransition();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        synchronized (mActivityStack) {
            if (mActivityStack == null) {
                mActivityStack = new Stack<>();
            }
            mActivityStack.push(this);
        }
        mBus = SDKApplication.getApplicationComponent().eventBus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(this, "on resume");
        //clear snackbar if has networkign again when user resume
        if (ConnectionUtil.isOnline(this)) {
            PaymentSnackBar.getInstance().dismiss();
            numberOfRetryOpenNetwoking = 0;
        } else {
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_offline),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_INDEFINITE, mOnCloseSnackBarListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (mActivityStack) {
            mActivityStack.remove(this);
            if (getCurrentActivityCount() == 0 && BaseActivity.getActivityCount() == 0) {
                if (GlobalData.analyticsTrackerWrapper != null) {
                    GlobalData.analyticsTrackerWrapper.trackUserCancel();
                }
                //dispose all instance and static resource.
                SingletonLifeCircleManager.disposeAll();
                if (mCompositeSubscription.hasSubscriptions()) {
                    mCompositeSubscription.unsubscribe();
                    Log.d(this, "unsubscribe all subscriptions");
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(this);
        Log.d(this, "onStop");
    }

    public void loadBankList(Action1<BankConfigResponse> success, Action1<Throwable> error) {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = SDKApplication.getApplicationComponent().bankListInteractor()
                .getBankList(appVersion, currentTime)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success, error);
        addSuscription(subscription);
    }

    /***
     * load app info from cache or api
     */
    protected void loadAppInfo(long appId, @TransactionType int transtype, String userId, String accessToken) {
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        long currentTime = System.currentTimeMillis();
        Subscription subscription = SDKApplication.getApplicationComponent().appInfoInteractor().loadAppInfo(appId, new int[]{transtype},
                userId, accessToken, appVersion, currentTime)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_check_app_info)))
                .subscribe(new Action1<AppInfo>() {
                    @Override
                    public void call(AppInfo pAppInfo) {
                        Log.d(this, "load appinfo success");
                        if (pAppInfo == null || !pAppInfo.isAllow()) {
                            actionIfPreventApp();
                            return;
                        }
                        try {
                            appInfo = pAppInfo;
                            showApplicationInfo(appInfo);
                        } catch (Exception e) {
                            Log.d(this, e);
                        }
                        loadStaticReload();
                    }
                }, appInfoException);
        mCompositeSubscription.add(subscription);
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_GetAppInfo, ZPPaymentSteps.OrderStepResult_None);
        }
    }

    /***
     * set keyboard type for edittext from config.json
     * @param pStrID
     * @param pKeyBoardType
     * @return
     */
    public View setKeyBoard(String pStrID, @KeyboardType int pKeyBoardType) {
        final int ID = getViewID(pStrID);
        View view = this.findViewById(ID);
        if (view == null && isVisible(view)) {
            return view;
        }
        if (pKeyBoardType == KeyboardType.NUMBER && view instanceof EditText) {
            //user using the laban key for exmple
            if (!SdkUtils.useDefaultKeyBoard(this)) {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
        } else if (pKeyBoardType == KeyboardType.TEXT && view instanceof EditText) {
            ((EditText) view).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        return view;
    }

    public void setViewColor(int pId, int pColor) {
        try {
            View view = findViewById(pId);
            if (view != null && view instanceof TextView) {
                ((TextView) view).setTextColor(pColor);
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    public void setTextHtml(int pId, String pHtmlText) {
        try {
            View view = findViewById(pId);
            if (view != null && view instanceof TextView) {
                ((TextView) view).setText(Html.fromHtml(pHtmlText));
            }
        } catch (Exception ignored) {
        }
    }

    public void setEnableView(int pId, boolean pIsEnable) {
        try {
            View view = findViewById(pId);
            if (view != null) {
                view.setEnabled(pIsEnable);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public int getViewID(String pStrID) {
        return this.getResources().getIdentifier(pStrID, "id", this.getPackageName());
    }

    public View setVisible(String pStrID, boolean pIsVisible) {
        final int ID = getViewID(pStrID);
        View view = this.findViewById(ID);
        if (view == null) {
            return view;
        }
        if (pIsVisible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        return view;
    }

    public void setVisible(int pId, boolean pIsVisible) {
        View view = findViewById(pId);
        if (view != null) {
            view.setVisibility(pIsVisible ? View.VISIBLE : View.GONE);
        }
    }

    private View addOrRemoveProperty(int pID, int property) {
        View view = this.findViewById(pID);
        if (view == null) {
            return view;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.addRule(property);
        view.setLayoutParams(layoutParams);
        return view;
    }

    private View setMarginLeft(int pID, int margin) {
        View view = this.findViewById(pID);
        if (view == null) {
            return view;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = margin;
        view.setLayoutParams(layoutParams);
        return view;
    }

    private View setMarginBottom(int pID, int margin) {
        View view = this.findViewById(pID);
        if (view == null) {
            return view;
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.bottomMargin = margin;
        view.setLayoutParams(layoutParams);
        return view;
    }

    public void setOnClickListener(int pId, View.OnClickListener pListener) {
        View view = this.findViewById(pId);
        if (view == null) {
            return;
        }
        view.setOnClickListener(pListener);
    }

    public String getTransactionTitle() {
        String barTitle = GlobalData.getStringResource(RS.string.walletsdk_string_bar_title);
        if (mPaymentInfoHelper.isTopupTrans()) {
            barTitle = GlobalData.getStringResource(RS.string.zpw_string_pay_title);
        } else if (mPaymentInfoHelper.isMoneyTranferTrans()) {
            barTitle = GlobalData.getStringResource(RS.string.zpw_string_tranfer_title);
        } else if (mPaymentInfoHelper.isWithDrawTrans()) {
            barTitle = GlobalData.getStringResource(RS.string.zpw_string_withdraw_title);
        }
        return barTitle;
    }

    public void setToolBarTitle() {
        setBarTitle(getTransactionTitle());
    }

    public void setText(String pStrID, String pText) {
        final int ID = getViewID(pStrID);
        View textView = this.findViewById(ID);

        if (textView == null) {
            return;
        }
        if (textView instanceof ToggleButton) {
            ((ToggleButton) this.findViewById(ID)).setText(pText);
        } else if (textView instanceof EditText) {
            EditText editText = ((EditText) this.findViewById(ID));
            if (editText instanceof VPaymentEditText && ((VPaymentEditText) editText).getTextInputLayout() instanceof TextInputLayout) {
                TextInputLayout textInputLayout = ((VPaymentEditText) editText).getTextInputLayout();
                textInputLayout.setHint(pText);
            } else {
                editText.setHint(pText);
            }
        } else if (textView instanceof TextView) {
            ((TextView) this.findViewById(ID)).setText(pText);
        }
    }

    public void setText(int pID, String pText) {
        View view = this.findViewById(pID);
        if (view == null) {
            return;
        }
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            if (editText instanceof VPaymentEditText && ((VPaymentEditText) editText).getTextInputLayout() instanceof TextInputLayout) {
                TextInputLayout textInputLayout = ((VPaymentEditText) editText).getTextInputLayout();
                textInputLayout.setHint(pText);
            } else {
                editText.setHint(pText);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setText(!TextUtils.isEmpty(pText) ? Html.fromHtml(pText) : pText);
        }
    }

    public void setImage(int pId, String pImageName) {
        ResourceManager.loadImageIntoView(findViewById(pId), pImageName);
    }

    public void setImage(String pId, String pImageName) {
        ResourceManager.loadImageIntoView(findViewById(getViewID(pId)), pImageName);
    }

    public View findViewById(String pName) {
        return findViewById(RS.getID(pName));
    }

    protected void showApplicationInfo(AppInfo appInfo) {
        //withdraw no need to show app name
        if (mPaymentInfoHelper.isWithDrawTrans()) {
            return;
        }
        if (appInfo != null && !TextUtils.isEmpty(appInfo.appname)) {
            setText(R.id.appname_txt, appInfo.appname);
            setVisible(R.id.appname_txt, true);
        } else {
            setVisible(R.id.appname_txt, false);
        }
    }

    public void askToOpenSettingNetwoking() {
        askToOpenSettingNetwoking(paymentOpenNetworkingDialogListener);
    }

    public void askToOpenSettingNetwoking(final ZPWPaymentOpenNetworkingDialogListener pListener) {
        BasePaymentActivity activity = (BasePaymentActivity) BasePaymentActivity.getCurrentActivity();

        if (activity != null && !activity.isFinishing()) {
            if (numberOfRetryOpenNetwoking >= Constants.MAX_RETRY_OPEN_NETWORKING && activity instanceof PaymentChannelActivity) {
                ((PaymentChannelActivity) getCurrentActivity()).getAdapter().closeSDKAfterNetworkOffline();
                numberOfRetryOpenNetwoking = 0;
                return;
            }
            numberOfRetryOpenNetwoking++;
            DialogManager.showSweetDialog(activity, SweetAlertDialog.NO_INTERNET, getString(R.string.zingpaysdk_alert_title_nointernet), getString(R.string.zingpaysdk_alert_content_nointernet), pIndex -> {
                if (pIndex == 0) {
                    if (pListener != null)
                        pListener.onCloseNetworkingDialog();

                } else if (pIndex == 1) {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                    if (pListener != null)
                        pListener.onOpenSettingDialogClicked();
                }
            }, GlobalData.getStringResource(RS.string.dialog_turn_off), GlobalData.getStringResource(RS.string.dialog_turn_on));
        }
    }

    public boolean isInProgress() {
        return DialogManager.isShowingProgressDialog();
    }

    public void showProgress(boolean pIsShow, String pStatusMessage) {
        if (pIsShow) {
            setText(R.id.payment_method_name, pStatusMessage);
            if (!isInProgress())
                DialogManager.showProcessDialog(BasePaymentActivity.getCurrentActivity(), mProgressDialogTimeoutListener);
        } else {
            DialogManager.closeProcessDialog();
            setText(R.id.payment_method_name, mTitleHeaderText);
        }
    }

    public void visibleAppInfo(boolean pIsVisible) {
        setVisible(R.id.zpsdk_app_info, pIsVisible);
    }

    public void visibleCardInfo(boolean pIsVisible) {
        setVisible(R.id.zpw_card_info, pIsVisible);
    }

    public void visibleTranferWalletInfo(boolean pIsVisible) {
        setVisible(R.id.zpsdk_transfer_info, pIsVisible);
    }

    public void visibleSubmitButton(boolean pIsVisible) {
        setVisible(R.id.zpw_submit_view, pIsVisible);
    }

    public void visibleCardViewNavigateButton(boolean pIsVisible) {
        setVisible(R.id.zpw_switch_card_button, pIsVisible);
    }

    public void visibleInputCardView(boolean pIsVisible) {
        setVisible(R.id.localcard_view_root, pIsVisible);
    }

    public void visibleWebView(boolean pIsVisible) {
        setVisible(R.id.zpw_threesecurity_webview, pIsVisible);
    }

    public void visiblePinView(boolean pIsVisible) {
        setVisible(R.id.zpsdk_pin_layout, pIsVisible);
    }

    public void visibleConfirmView(boolean pIsVisible) {
        setVisible(R.id.zpw_confirm, pIsVisible);
    }

    public void setBarTitle(String pTitle) {
        setText(R.id.payment_method_name, pTitle);
        mTitleHeaderText = pTitle;
    }

    public void setTitle() {
        String title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway);
        if (mPaymentInfoHelper.isTopupTrans()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_topup);
        } else if (mPaymentInfoHelper.isMoneyTranferTrans()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_tranfer);
        } else if (mPaymentInfoHelper.isWithDrawTrans()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_withdraw);
        }
        setText(R.id.title_payment_method, title);
    }

    public void visibleHeaderInfo() {
        if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
            if (mPaymentInfoHelper.isMoneyTranferTrans()) {
                getAdapter().getActivity().visibleTranferWalletInfo(true);
            } else {
                getAdapter().getActivity().visibleAppInfo(true);
            }
        }
    }

    public void setConfirmTitle(String pTitle) {
        setText(R.id.zpw_payment_method_label, pTitle);
    }

    public void showOrderFeeView() {
        AbstractOrder order = mPaymentInfoHelper.getOrder();
        if (order == null) {
            return;
        }
        if (order.amount_total > 0) {
            setVisible(R.id.zpw_fee_view_wrapper, true);
            setVisible(R.id.zpw_total_view_wrapper, true);
            setText(R.id.zpw_payment_channel_fee, StringUtil.formatVnCurrence(String.valueOf(order.amount_total)));
            setText(R.id.zpw_payment_channel_total_pay, StringUtil.formatVnCurrence(String.valueOf(order.amount_total)));
            applyFont(findViewById(R.id.zpw_payment_channel_total_pay), GlobalData.getStringResource(RS.string.zpw_font_medium));
        }

    }

    protected void showAmount() {
        try {
            AbstractOrder order = mPaymentInfoHelper.getOrder();
            if (order != null && order.amount > 0) {
                String txtAmount = StringUtil.formatVnCurrence(String.valueOf(order.amount));
                setText(R.id.amount_txt, txtAmount);
                setText(R.id.currency_unit_txt, GlobalData.getStringResource(RS.string.zpw_string_payment_currency_label));
            } else {
                setVisible(R.id.amount_txt, false);
                setVisible(R.id.currency_unit_txt, false);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected void setBackground(int color) {
        View view = findViewById(R.id.supperRootView);
        if (view != null)
            view.setBackgroundColor(color);
    }

    public void animationImageViewSuccessSpecial() {
        startAnimate(R.id.zpw_imageview_success_icon);
    }

    public void animationImageViewSuccess() {
        startAnimate(R.id.zpw_payment_success_imageview);
    }

    public void animateImageViewFail() {
        startAnimate(R.id.zpw_payment_fail_imageview);
    }

    public void showFailView(String pMessage, String pTransID) {
        setText(R.id.zpw_textview_error_message, pMessage);

        if (this instanceof PaymentChannelActivity) {
            StatusResponse statusResponse = getAdapter().getResponseStatus();
//            statusResponse.returncode = Constants.PAYMENT_LIMIT_PER_DAY_CODE.get(0);
            // The inform text would be set from server
            if (statusResponse != null) {
                setText(R.id.zpw_textview_update_level_inform, statusResponse.getSuggestMessage());
                setVisible(R.id.zpw_textview_update_level_inform, !TextUtils.isEmpty(statusResponse.getSuggestMessage()));

//                int[] status = new int[]{2, 1};
                if (statusResponse.getSuggestactions() != null && statusResponse.getSuggestactions().length > 0) {
                    setLayoutBasedOnSuggestActions(statusResponse.getSuggestactions());
                } else {
                    setVisible(R.id.zpw_payment_fail_rl_update_info, false);
                    setVisible(R.id.zpw_payment_fail_rl_support, true);
                }
            } else {
                setVisible(R.id.zpw_textview_update_level_inform, false);
                setVisible(R.id.zpw_payment_fail_rl_update_info, false);
                setVisible(R.id.zpw_payment_fail_rl_support, false);
            }
        }

        setVisible(R.id.zpw_pay_info_buttom_view, true);

        if (!TextUtils.isEmpty(pTransID) && Long.parseLong(pTransID) > 0) {
            setVisible(R.id.zpw_transaction_wrapper, true);
            setText(R.id.zpw_textview_transaction, pTransID);
            setVisible(R.id.zpw_notransid_textview, false);
        } else {
            setVisible(R.id.zpw_transaction_wrapper, false);
            setVisible(R.id.zpw_notransid_textview, !((mPaymentInfoHelper.isBankAccountTrans() && GlobalData.shouldNativeWebFlow()) || mPaymentInfoHelper.bankAccountUnlink()));//hide all if unlink account
        }
        applyFont(findViewById(R.id.zpw_textview_transaction), GlobalData.getStringResource(RS.string.zpw_font_medium));
        addOrRemoveProperty(R.id.payment_method_name, RelativeLayout.CENTER_IN_PARENT);
        animateImageViewFail();
    }

    private void setLayoutBasedOnSuggestActions(int[] suggestActions) {
        // Define view to set view position based on suggest action from server response
        View rlUpdateInfo = findViewById(R.id.zpw_payment_fail_rl_update_info);
        View rlSupport = findViewById(R.id.zpw_payment_fail_rl_support);

        RelativeLayout.LayoutParams pUpdateInfo = (RelativeLayout.LayoutParams) rlUpdateInfo.getLayoutParams();
        RelativeLayout.LayoutParams pSupport = (RelativeLayout.LayoutParams) rlSupport.getLayoutParams();

        if (Arrays.equals(ESuggestActionType.UPDATE_INFO_DISPLAY.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_update_info, true);
            setVisible(R.id.zpw_payment_fail_rl_support, false);

        } else if (Arrays.equals(ESuggestActionType.SUPPORT_DISPLAY.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_update_info, false);
            setVisible(R.id.zpw_payment_fail_rl_support, true);

        } else if (Arrays.equals(ESuggestActionType.UPDATE_INFO_ABOVE.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_update_info, true);
            setVisible(R.id.zpw_payment_fail_rl_support, true);
            pSupport.addRule(RelativeLayout.BELOW, rlUpdateInfo.getId());
            rlSupport.setLayoutParams(pSupport);

        } else if (Arrays.equals(ESuggestActionType.SUPPORT_ABOVE.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_update_info, true);
            setVisible(R.id.zpw_payment_fail_rl_support, true);
            pUpdateInfo.addRule(RelativeLayout.BELOW, rlSupport.getId());
            rlUpdateInfo.setLayoutParams(pUpdateInfo);
        }
    }

    public void showPaymentSuccessContent(String pTransID) throws Exception {
        if (!TextUtils.isEmpty(pTransID) && Long.parseLong(pTransID) > 0) {
            setVisible(R.id.zpw_transaction_wrapper, true);
            setText(R.id.zpw_textview_transaction, pTransID);
            setVisible(R.id.zpw_notransid_textview, false);
        } else {
            setVisible(R.id.zpw_transaction_wrapper, false);
            setVisible(R.id.zpw_notransid_textview, !(GlobalData.shouldNativeWebFlow() || mPaymentInfoHelper.bankAccountUnlink()));//hide all if unlink account
        }
        setVisible(R.id.zpw_pay_info_buttom_view, true);
        long appId = mPaymentInfoHelper.getAppId();
        if (GlobalData.isRedPacketChannel(appId)) {
            setVisible(R.id.zpw_transaction_wrapper, false);
            setVisible(R.id.zpw_textview_transaction_lixi_label, true);
            String formattedString = "<b>" + GlobalData.getStringResource(RS.string.zpw_string_lixi_notice_title_02) + "</b>";
            setTextHtml(R.id.zpw_textview_transaction_lixi_label, String.format(GlobalData.getStringResource(RS.string.zpw_string_lixi_notice_title), formattedString));
        }
        applyFont(findViewById(R.id.zpw_textview_transaction), GlobalData.getStringResource(RS.string.zpw_font_medium));
        //show transaction amount when ! withdraw
        AbstractOrder order = mPaymentInfoHelper.getOrder();
        if (order != null && order.amount_total > 0 && !mPaymentInfoHelper.isWithDrawTrans() && !mPaymentInfoHelper.isMoneyTranferTrans()) {
            setTextHtml(R.id.payment_price_label, StringUtil.formatVnCurrence(String.valueOf(order.amount_total)));
            if (!TextUtils.isEmpty(order.description)) {
                setVisible(R.id.payment_description_label, true);
                setText(R.id.payment_description_label, order.description);
            } else {
                setVisible(R.id.payment_description_label, false);
            }
        } else if (mPaymentInfoHelper.isBankAccountTrans()) { // show label for linkAcc
            if (getAdapter().getPageName().equals(AdapterLinkAcc.PAGE_LINKACC_SUCCESS)) {
                setViewColor(R.id.zpw_payment_success_textview, getResources().getColor(R.color.text_color_primary));
                setVisible(R.id.payment_description_label, true);

                String desc = GlobalData.getStringResource(RS.string.zpw_string_linkacc_notice_description);
                if (getPaymentChannelActivity().getAdapter() instanceof AdapterLinkAcc &&
                        ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification() != null) {
                    desc = ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification().getMsg();
                }
                setText(R.id.payment_description_label, desc);
                setVisible(R.id.price_linearlayout, false);
                setMarginBottom(R.id.zpw_payment_success_textview, (int) getResources().getDimension(R.dimen.zpw_margin_top_medium_label));
            } else {
                setVisible(R.id.zpw_payment_success_textview, false);
                setVisible(R.id.payment_description_label, true);
                String desc = GlobalData.getStringResource(RS.string.zpw_string_unlinkacc_notice_description);
                if (getPaymentChannelActivity().getAdapter() instanceof AdapterLinkAcc &&
                        ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification() != null) {
                    desc = ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification().getMsg();
                }
                setText(R.id.payment_description_label, desc);
                setVisible(R.id.price_linearlayout, false);
                setMarginBottom(R.id.zpw_payment_success_textview, (int) getResources().getDimension(R.dimen.zpw_margin_top_supper_supper_label));
            }
        } else if (mPaymentInfoHelper.isMoneyTranferTrans()) {// Show detail tranfer
            //prevent capture screen
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
            if (!SdkUtils.isTablet(this)) {
                int[] locate = new int[2];
                View view = findViewById(R.id.zpw_pay_info_buttom_view);
                view.getLocationInWindow(locate);
                int mLocate = locate[1];

                View submitButton = findViewById(R.id.zpw_submit_view);
                LinearLayout.LayoutParams submitButtonParams = (LinearLayout.LayoutParams) submitButton.getLayoutParams();
                submitButtonParams.setMargins(0, 0, 0, 0);
                submitButton.setLayoutParams(submitButtonParams);

                View LayoutRoot = findViewById(R.id.supperRootView);
                View LayoutScrollView = findViewById(R.id.zpw_scrollview_layout);
                ViewTreeObserver vto = LayoutRoot.getViewTreeObserver();

                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        if (LayoutRoot.getViewTreeObserver().isAlive()) {
                            LayoutRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        int mHeightScrollView = LayoutScrollView.getHeight();
                        LayoutScrollView.setMinimumHeight(mHeightScrollView + mLocate);
                        Log.d(this, "addOnGlobalLayoutListener==" + LayoutScrollView.getHeight());

                    }
                });

            }

            setTextHtml(R.id.payment_price_label, StringUtil.formatVnCurrence(String.valueOf(order.amount_total)));
            setVisible(R.id.layout_detail, true);
            setVisible(R.id.payment_description_label, false);
            if (order != null && !TextUtils.isEmpty(order.description)) {
                setText(R.id.text_description, order.description);
            } else {
                setVisible(R.id.layout_success_description, false);
            }
            UserInfo destinationUser = mPaymentInfoHelper.getDestinationUser();
            if (destinationUser != null) {
                if (TextUtils.isEmpty(destinationUser.zalopay_name)) {// check zalo pay ID
                    setText(R.id.text_zalopay_id, GlobalData.getStringResource(RS.string.zpw_string_transfer_zalopay_id_null));
                } else {
                    setText(R.id.text_zalopay_id, destinationUser.zalopay_name);
                }

                setText(R.id.text_userTo, destinationUser.zalo_name);
                findViewAndLoadUri(R.id.img_avatarTo, destinationUser.avatar);
            }
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            if (userInfo != null && !TextUtils.isEmpty(userInfo.avatar)) {
                findViewAndLoadUri(R.id.img_avatarFrom, userInfo.avatar);
            }
            if (order != null) {
                Long paymentTime = order.apptime;
                if (paymentTime == null || paymentTime == 0) {
                    paymentTime = new Date().getTime();
                }
                setTransferDate(SdkUtils.convertDateTime(paymentTime));
            }
        } else {
            setVisible(R.id.payment_description_label, false);
            setVisible(R.id.price_linearlayout, false);
            setMarginBottom(R.id.zpw_payment_success_textview, (int) getResources().getDimension(R.dimen.zpw_margin_top_supper_supper_label));
        }
        addOrRemoveProperty(R.id.payment_method_name, RelativeLayout.CENTER_IN_PARENT);
        animationImageViewSuccess();
    }

    public void showPaymentSpecialSuccessContent(String pTransID) {
        setText(R.id.zpw_textview_transaction_id, pTransID);
        AbstractOrder order = mPaymentInfoHelper.getOrder();
        if (order != null && order.amount_total >= 0) {
            setTextHtml(R.id.zpw_textview_transaction_amount, StringUtil.formatVnCurrence(String.valueOf(order.amount_total)));
        } else {
            findViewById(R.id.zpw_textview_transaction_amount).setVisibility(View.GONE);
        }
        if (order != null) {
            if (appInfo != null && appInfo.viewresulttype == 2) {
                setVisible(R.id.zpw_textview_transaction_description, true);
                setText(R.id.zpw_textview_transaction_description, appInfo.appname);
            } else
                setVisible(R.id.zpw_textview_transaction_description, false);
            //set time transaction
            long apptime = order.apptime;
            if (apptime > 0) {
                setText(R.id.zpw_textview_transaction_time, SdkUtils.convertDateTime(apptime));
            }
        }
        //re-margin title header.
        setMarginLeft(R.id.payment_method_name, (int) getResources().getDimension(R.dimen.zpw_header_label_margin));
        //animate icon
        animationImageViewSuccessSpecial();
    }

    protected void showBalanceContent(MiniPmcTransType pConfig) throws Exception {
        long user_balance = mPaymentInfoHelper.getBalance();
        double amount_total = mPaymentInfoHelper.getAmountTotal();
        setText(R.id.zalopay_bill_info, StringUtil.formatVnCurrence(String.valueOf(user_balance)));
        setVisible(R.id.zpw_channel_layout, true);
        setVisible(R.id.zpw_channel_label_textview, false);
        setText(R.id.zpw_channel_name_textview, pConfig != null ? pConfig.pmcname : GlobalData.getStringResource(RS.string.zpw_string_zalopay_wallet_method_name));
        if (user_balance < amount_total) {
            setText(R.id.zalopay_info_error, GlobalData.getStringResource(RS.string.zpw_string_zalopay_balance_error_label));
            setViewColor(R.id.zalopay_info_error, getResources().getColor(R.color.holo_red_light));
        } else {
            setText(R.id.zalopay_info_error, GlobalData.getStringResource(RS.string.zpw_string_zalopay_balance_label));
            setViewColor(R.id.zalopay_info_error, getResources().getColor(R.color.text_color));
        }
    }

    public void showMessageSnackBar(View pRootView, String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(pRootView)
                    .setBgColor(GlobalData.getAppContext().getResources().getColor(R.color.yellow_bg_popup_error))
                    .setMessage(pMessage)
                    .setActionMessage(pActionMessage)
                    .setDuration(pDuration)
                    .setOnCloseListener(pOnCloseListener)
                    .show();

        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void showMessageSnackBar(View pRootView, String pTitle, String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(pRootView)
                    .setBgColor(GlobalData.getAppContext().getResources().getColor(R.color.yellow_bg_popup_error))
                    .setTitle(pTitle)
                    .setMessage(pMessage)
                    .setActionMessage(pActionMessage)
                    .setDuration(pDuration)
                    .setOnCloseListener(pOnCloseListener)
                    .show();

        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    /***
     * show special view for wallet tranfer and withdraw
     */
    protected void showUserInfoWalletTransfer() {
        if (!mPaymentInfoHelper.isMoneyTranferTrans()) {
            return;
        }

        //show fee
        showConfirmView(false, false, null);

        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        AbstractOrder order = mPaymentInfoHelper.getOrder();
        //zalo name
        if (!TextUtils.isEmpty(userInfo.zalo_name))
            setText(R.id.receiver_zaloname_txt, userInfo.zalo_name);
        else
            setVisible(R.id.receiver_zaloname_relativelayout, false);

        //zalopay name
        if (!TextUtils.isEmpty(userInfo.zalopay_name))
            setText(R.id.receiver_zalopayname_txt, userInfo.zalopay_name);
        else
            setVisible(R.id.receiver_zalopayname_relativelayout, false);

        //description
        if (!TextUtils.isEmpty(order.description)) {
            setVisible(R.id.transfer_description_txt, true);
            setText(R.id.transfer_description_txt, order.description);
        } else
            setVisible(R.id.transfer_description_txt, false);

        //amount
        if (order.amount > 0) {
            String txtAmount = StringUtil.formatVnCurrence(String.valueOf(order.amount));
            setText(R.id.transfer_amount_txt, txtAmount);
        }
    }

    /***
     * alert invalid data and quit payment
     */
    private void showDialogUserInfo() {
        showErrorDialog(() -> {
            mPaymentInfoHelper.setResult(PaymentStatus.INVALID_DATA);
            callBackThenTerminate();
        }, GlobalData.getStringResource(RS.string.zpw_string_alert_userinfo_invalid));
    }

    /**
     * show fee
     */
    public void showConfirmView(boolean pHidden, boolean pShow, MiniPmcTransType pChannel) {
        if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper) && pHidden) {
            visibleTranferWalletInfo(false);
            visibleAppInfo(false);

            return;
        }

        if (mPaymentInfoHelper.isMoneyTranferTrans()) {
            visibleTranferWalletInfo(true);
            visibleAppInfo(false);

            AbstractOrder order = mPaymentInfoHelper.getOrder();
            if (pShow && order.fee > 0) {
                setVisible(R.id.transfer_amount_txt, true);
                setVisible(R.id.transfer_fee_relativelayout, true);

                if (pChannel != null) {
                    String txtprice = StringUtil.formatVnCurrence(String.valueOf(order.fee));
                    setText(R.id.transfer_fee_txt, txtprice);
                }

                try {
                    if (order.amount_total > 0) {
                        String txtAmount = StringUtil.formatVnCurrence(String.valueOf(order.amount_total));
                        setText(R.id.transfer_amounttotal_txt, txtAmount);

                    }
                } catch (Exception e) {
                    Log.e(this, e);
                }
            } else {
                setVisible(R.id.transfer_amount_txt, false);
                setVisible(R.id.transfer_fee_relativelayout, false);
            }
        } else if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            visibleTranferWalletInfo(false);
            visibleAppInfo(false);
        }

    }

    /***
     * show header text
     */
    protected void showDisplayInfo() {
       if (mPaymentInfoHelper.isMoneyTranferTrans()) {
            visibleTranferWalletInfo(true);
            visibleAppInfo(false);
            showUserInfoWalletTransfer();
        } else {
            AbstractOrder order = mPaymentInfoHelper.getOrder();
            if (mPaymentInfoHelper.isWithDrawTrans()) {
                setText(R.id.item_name, GlobalData.getStringResource(RS.string.zpw_string_withdraw_description));
            } else if (order != null) {
                setText(R.id.item_name, order.description);
            }
        }
    }

    protected void startAnimate(int pID) {
        View view = findViewById(pID);

        if (view != null) {

            Animation animationBounce = AnimationUtils.loadAnimation(this, R.anim.bounce_interpolator);

            AnimationSet growShrink = new AnimationSet(true);
            growShrink.addAnimation(animationBounce);

            view.startAnimation(growShrink);
        }
    }

    protected void resizeGridPasswordView() {
        Log.d(this, "start resize password view");
        final View passwordView = findViewById(R.id.zpw_gridview_pin);
        if (passwordView != null) {
            int width = SdkUtils.widthScreen(getCurrentActivity());
            int pinLength = getResources().getInteger(R.integer.wallet_pin_length);
            int margin = (int) SdkUtils.convertDpToPixel(getResources().getDimension(R.dimen.zpw_pin_margin), getApplicationContext());
            width = width - margin * 2;
            int height = width / pinLength;
            if (width == 0 || height == 0) {
                return;
            }
            passwordView.getLayoutParams().height = height;
            passwordView.getLayoutParams().width = width;
            passwordView.requestLayout();
        }
    }

    /***
     * set margin top  Submit Button tab or phone
     *
     * @param viewEnd successview or failview
     */
    public void setMarginSubmitButtonTop(boolean viewEnd) {
        View submitButton = findViewById(R.id.zpw_submit_view);
        View confirmRootView = findViewById(R.id.zpw_payment_confirm_root_view);
        View authenLocalView = findViewById(R.id.linearlayout_selection_authen);
        View authenInputCardView = findViewById(R.id.linearlayout_authenticate_local_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int paddingButtom = (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_phone);
        if (!SdkUtils.isTablet(this)) {
            params.setMargins(0, (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_phone), 0, 0);
            if (submitButton != null) {
                submitButton.setLayoutParams(params);
                submitButton.requestLayout();
            }
            if (confirmRootView != null) {
                confirmRootView.setPadding(0, 0, 0, paddingButtom);
                confirmRootView.requestLayout();
            }
            if (authenLocalView != null) {
                authenLocalView.setPadding(0, 0, 0, paddingButtom);
                authenLocalView.requestLayout();
            }
            if (authenInputCardView != null) {
                authenInputCardView.setPadding(0, 0, 0, paddingButtom);
                authenInputCardView.requestLayout();
            }

            Log.d(this, "setMarginSubmitButtonTop  Phone");

        } else {
            if (submitButton != null) {
                if (viewEnd)
                    params.setMargins(0, (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_phone), 0, 0);
                else
                    params.setMargins(0, (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_tab), 0, 0);
                submitButton.setLayoutParams(params);
                submitButton.requestLayout();
            }
            Log.d(this, "setMarginSubmitButtonTop  Tab");
        }
    }

    /***
     * user level 1 can not tranfer money.
     * user level 1 can not withdraw.
     */
    public boolean checkUserLevelValid() {
        boolean userLevelValid = true;
        try {
            int user_level = mPaymentInfoHelper.getLevel();
            if (mPaymentInfoHelper.isMoneyTranferTrans() && user_level < BuildConfig.level_allow_use_zalopay) {
                userLevelValid = false;
            } else if (mPaymentInfoHelper.isWithDrawTrans() && user_level < BuildConfig.level_allow_withdraw) {
                userLevelValid = false;
            } else if ((mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) && user_level < BuildConfig.level_allow_cardmap) {
                userLevelValid = false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return userLevelValid;
    }

    /***
     * apply font for all view on screen.
     */
    protected void applyFont() {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        overrideFonts(viewGroup, GlobalData.getStringResource(RS.string.zpw_font_regular));
        applyFont(findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium));
        applyFont(findViewById(R.id.payment_method_name), GlobalData.getStringResource(RS.string.zpw_font_medium));
    }

    protected void setListener() {
        View view = findViewById(R.id.item_name);
        if (view != null)
            ((EllipsizingTextView) view).setOnShowDetailOrderListener(mShowDetailOrderClick);
    }

    public void applyFont(View pView, String pFontName) {
        Typeface tf = CFontManager.getInstance().loadFont(pFontName);
        if (tf != null) {
            if (pView instanceof TextView)
                ((TextView) pView).setTypeface(tf);
            else if (pView instanceof VPaymentDrawableEditText)
                ((VPaymentDrawableEditText) pView).setTypeface(tf);
        }
    }

    public void overrideFonts(final View pView, String pFontName) {
        try {
            if (pView instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) pView;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(child, pFontName);
                }
            } else if (pView.getId() != R.id.front_card_number &&
                    ((pView instanceof TextView) || pView instanceof VPaymentDrawableEditText || pView instanceof VPaymentValidDateEditText)) {
                Typeface typeFace = CFontManager.getInstance().loadFont(pFontName);
                if (typeFace != null) {
                    if (pView instanceof TextView) {
                        ((TextView) pView).setTypeface(typeFace);
                    } else {
                        ((VPaymentEditText) pView).setTypeface(typeFace);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    /***
     * server maintenance dialog
     *
     * @param pStatusMessage
     */
    public void showServerMaintenanceDialog(String pStatusMessage) {
        String mMessage = GlobalData.getStringResource(RS.string.zpw_string_alert_maintenance);
        if (!TextUtils.isEmpty(pStatusMessage)) {
            mMessage = pStatusMessage;
        }

        showInfoDialog(() -> {
            mPaymentInfoHelper.setResult(PaymentStatus.SERVICE_MAINTENANCE);
            finish();
        }, mMessage);
    }

    /**
     * Show dialog confirm upgrade level when user input bank account number
     */
    public void confirmUpgradeLevelIfUserInputBankAccount(final String pMessage, ZPWOnEventConfirmDialogListener pListener) {
        showNoticeDialog(pListener, pMessage, GlobalData.getStringResource(RS.string.dialog_upgrade_button), GlobalData.getStringResource(RS.string.dialog_retry_input_card_button));
    }

    /***
     * show bank maintenance dialog
     *
     * @param pBankCode
     * @return
     */
    public boolean showBankMaintenance(String pBankCode) {
        return showBankMaintenance(null, pBankCode);
    }

    /***
     * show bank maintenance dialog
     *
     * @param pListener
     * @param pBankCode
     * @return
     */
    public boolean showBankMaintenance(ZPWOnEventDialogListener pListener, String pBankCode) {
        try {
            int bankFunction = GlobalData.getCurrentBankFunction();
            BankConfig bankConfig = SDKApplication
                    .getApplicationComponent()
                    .bankListInteractor()
                    .getBankConfig(pBankCode);
            if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
                showInfoDialog(pListener, bankConfig.getMaintenanceMessage(bankFunction));
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    /**
     * is bank support,
     * is not support show dialog
     *
     * @return
     */
    public boolean showBankSupport(String pBankCode) {
        try {
            BankConfig bankConfig = SDKApplication.getApplicationComponent().bankListInteractor().getBankConfig(pBankCode);
            if (bankConfig == null || !bankConfig.isActive()) {
                String message = GlobalData.getStringResource(RS.string.zpw_string_bank_not_support);
                showInfoDialog(null, message);
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return true;
    }

    public void showConfirmDialogWithManyOption(String pMessage, ZPWOnSweetDialogListener pListener, String... pButtonList) {
        DialogManager.showDialog(this, SweetAlertDialog.NORMAL_TYPE, null, pMessage, pListener, pButtonList);
    }

    public void showConfirmDialog(final ZPWOnEventConfirmDialogListener pListener, String pMessage, final String pButtonLeftText, final String pButtonRightText) {
        DialogManager.showSweetDialogConfirm(this, pMessage, pButtonLeftText, pButtonRightText, pListener);
    }

    /***
     * show error dialog
     *
     * @param pDialogListener
     * @param params
     */
    public void showErrorDialog(final ZPWOnEventDialogListener pDialogListener, String... params) {
        if (params == null || params.length <= 0) {
            Log.d(this, "===showWarningDialog===params=NULL");
            return;
        }
        String message = params[0];
        String closeButtonText = null;
        if (params.length >= 2) {
            closeButtonText = params[1];
        }
        if (TextUtils.isEmpty(closeButtonText)) {
            closeButtonText = GlobalData.getStringResource(RS.string.dialog_close_button);
        }
        // Show dialog ERROR_TYPE
        DialogManager.showSweetDialogCustom(this,
                message, closeButtonText,
                SweetAlertDialog.ERROR_TYPE, pDialogListener);
    }

    /***
     * warning dialog
     * @param pDialogListener
     * @param params
     */
    public void showWarningDialog(final ZPWOnEventDialogListener pDialogListener, String... params) {
        if (params == null || params.length <= 0) {
            Log.d(this, "===showWarningDialog===params=NULL");
            return;
        }

        String message = params[0];
        String closeButtonText = null;

        if (params.length >= 2) {
            closeButtonText = params[1];
        }

        if (TextUtils.isEmpty(closeButtonText)) {
            closeButtonText = GlobalData.getStringResource(RS.string.dialog_close_button);
        }
        DialogManager.showSweetDialogCustom(this,
                message, closeButtonText,
                SweetAlertDialog.WARNING_TYPE, pDialogListener);
    }

    /***
     * info dialog
     * @param pDialogListener
     * @param params
     */
    public void showInfoDialog(final ZPWOnEventDialogListener pDialogListener, String... params) {
        if (params == null || params.length <= 0) {
            Log.d(this, "===showWarningDialog===params=NULL");
            return;
        }
        String message = params[0];
        String closeButtonText = null;
        if (params.length >= 2) {
            closeButtonText = params[1];
        }
        if (TextUtils.isEmpty(closeButtonText)) {
            closeButtonText = GlobalData.getStringResource(RS.string.dialog_close_button);
        }
        //Show dialog INFO_TYPE
        DialogManager.showSweetDialogCustom(this,
                message, closeButtonText, SweetAlertDialog.INFO_TYPE, pDialogListener);
    }

    public void showNoticeDialog(final ZPWOnEventConfirmDialogListener pDialogListener, String... params) {
        if (params == null || params.length <= 0) {
            Log.d(this, "===showWarningDialog===params=NULL");
            return;
        }
        String message = params[0];
        String leftButtonText = null, rightButtonText = null;
        if (params.length >= 2) {
            leftButtonText = params[1];
        }
        if (params.length >= 3) {
            rightButtonText = params[2];
        }
        if (TextUtils.isEmpty(leftButtonText)) {
            leftButtonText = GlobalData.getStringResource(RS.string.dialog_upgrade_button);
        }
        if (TextUtils.isEmpty(rightButtonText)) {
            rightButtonText = GlobalData.getStringResource(RS.string.dialog_cancel_button);
        }
        DialogManager.showSweetDialogOptionNotice(this, message,
                leftButtonText,
                rightButtonText, pDialogListener);
    }

    public void showRetryDialog(final ZPWOnEventConfirmDialogListener pDialogListener, String... params) {
        if (params == null || params.length <= 0) {
            Log.d(this, "===showRetryDialog===params=NULL");
            return;
        }
        String message = params[0];
        //Show dialog Retry
        DialogManager.showSweetDialogRetry(this, message, pDialogListener);
    }

    /***
     * Show support view
     */
    public void showSupportView(String pTransactionID) {
        try {
            Bitmap mBitmap = SdkUtils.CaptureScreenshot(getCurrentActivity());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byteArray = stream.toByteArray();
            String transactionTitle = getTransactionTitle();

            if (mPaymentInfoHelper.isCardLinkTrans()) {
                transactionTitle = GlobalData.getStringResource(RS.string.zpw_string_credit_card_link);
            }
            int errorcode = getAdapter().getResponseStatus() != null ? getAdapter().getResponseStatus().returncode : Constants.NULL_ERRORCODE;
            //Create Parcelable Feedback
            mFeedback = new Feedback(byteArray, getMessageFailView(), transactionTitle, pTransactionID, errorcode);
            setVisible(R.id.zpw_pay_support_buttom_view, true);
            isVisibilitySupport = true;
            View view_support = findViewById(R.id.zpw_pay_support_buttom_view);
            View btn_question = findViewById(R.id.question_button);
            View btn_support = findViewById(R.id.support_button);
            View btn_cancel = findViewById(R.id.cancel_spview_button);

            btn_question.setOnClickListener(mSupportButtonClickListener);
            view_support.setOnClickListener(mSupportButtonClickListener);
            btn_support.setOnClickListener(mSupportButtonClickListener);
            btn_cancel.setOnClickListener(mSupportButtonClickListener);

            View v = findViewById(R.id.layout_spview_animation);
            if (v != null) {
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
                v.startAnimation(hyperspaceJumpAnimation);
            }
        } catch (Exception e) {
            Log.d(this, e);
        }

    }

    public void closeSupportView() {
        View v = findViewById(R.id.layout_spview_animation);
        if (v != null) {
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(BasePaymentActivity.this, R.anim.slide_out_bottom);
            v.startAnimation(hyperspaceJumpAnimation);
        }
        isVisibilitySupport = false;
        final Handler handler = new Handler();
        handler.postDelayed(() -> setVisible(R.id.zpw_pay_support_buttom_view, false), 300);
    }

    public boolean getVisibilitySupportView() {
        return isVisibilitySupport;
    }

    private String getMessageFailView() {
        TextView textView = (TextView) findViewById(R.id.zpw_textview_error_message);
        return ((textView != null) ? String.valueOf(textView.getText()) : "");
    }

    public void showSelectionBankAccountDialog() {
        Intent intent = new Intent(getApplicationContext(), MapListSelectionActivity.class);
        intent.putExtra(MapListSelectionActivity.BANKCODE_EXTRA, CardType.PVCB);
        intent.putExtra(MapListSelectionActivity.BUTTON_LEFT_TEXT_EXTRA, getCloseButtonText());
        startActivity(intent);
    }

    public void setTextInputLayoutHint(EditText pEditext, String pMessage, Context pContext) {
        if (pEditext == null) {
            return;
        }

        if (pEditext instanceof VPaymentEditText && ((VPaymentEditText) pEditext).getTextInputLayout() instanceof TextInputLayout) {

            try {
                TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();

                int color = pContext.getResources().getColor(R.color.color_primary);

                int textColor = pContext.getResources().getColor(R.color.text_color);

                Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
                fDefaultTextColor.setAccessible(true);
                fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{textColor}));

                Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
                fFocusedTextColor.setAccessible(true);
                fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

                int paddingLeft = pEditext.getPaddingLeft();
                int paddingTop = pEditext.getPaddingTop();
                int paddingRight = pEditext.getPaddingRight();
                int paddingBottom = pEditext.getPaddingBottom();

                pEditext.setBackground(pContext.getResources().getDrawable(R.drawable.txt_bottom_default_style));

                //restore padding
                pEditext.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

                textInputLayout.refreshDrawableState();

                textInputLayout.setHint(!TextUtils.isEmpty(pMessage) ? pMessage : (textInputLayout.getTag() != null ? textInputLayout.getTag().toString() : null));

            } catch (Exception ignored) {
            }
        }
    }

    // fresco load Uri
    private SimpleDraweeView findViewAndLoadUri(@IdRes int viewId, String uri) {
        SimpleDraweeView view = this.findAndPrepare(viewId);
        view.setImageURI(Uri.parse(uri));
        return view;
    }

    private SimpleDraweeView findAndPrepare(@IdRes int viewId) {
        SimpleDraweeView view = (SimpleDraweeView) findViewById(viewId);
        return view;
    }

    public void setTransferDate(String pDate) {
        setText(R.id.text_transfer_date, pDate);
    }

    //endregion
    public void setTextInputLayoutHintError(EditText pEditext, String pError, Context pContext) {
        if (pEditext == null) {
            return;
        }

        if (pEditext instanceof VPaymentEditText && ((VPaymentEditText) pEditext).getTextInputLayout() instanceof TextInputLayout) {
            try {
                TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();

                int color = pContext.getResources().getColor(R.color.holo_red_light);

                Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
                fDefaultTextColor.setAccessible(true);
                fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

                Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
                fFocusedTextColor.setAccessible(true);
                fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

                int paddingLeft = pEditext.getPaddingLeft();
                int paddingTop = pEditext.getPaddingTop();
                int paddingRight = pEditext.getPaddingRight();
                int paddingBottom = pEditext.getPaddingBottom();

                pEditext.setBackground(pContext.getResources().getDrawable(R.drawable.txt_bottom_error_style));

                //restore padding
                pEditext.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

                textInputLayout.refreshDrawableState();

                textInputLayout.setHint(pError);

            } catch (Exception ignored) {
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkEvent(SdkNetworkEventMessage pNetworkEventMessage) {
        //user sitting in the result screen
        if (getCurrentActivity() instanceof PaymentChannelActivity && ((PaymentChannelActivity) getCurrentActivity()).getAdapter().isFinalScreen()) {
            Log.d(this, "onNetworkMessageEvent user is on fail screen...");
            return;
        }
        //come from api request fail with handshake
        if (pNetworkEventMessage.origin == Constants.API_ORIGIN) {
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_not_stable),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_LONG, mOnCloseSnackBarListener);

            Log.d(this, "networking is not stable");
        }
        //networking indeed offline
        else if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            Log.d(this, "networking is off");
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_offline),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_INDEFINITE, mOnCloseSnackBarListener);
        }
        //networking online again
        else {
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_online), null, TSnackbar.LENGTH_SHORT, null);
            Log.d(this, "networking is online again");
        }
    }
}
