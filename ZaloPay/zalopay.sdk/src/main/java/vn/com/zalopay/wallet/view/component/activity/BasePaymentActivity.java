package vn.com.zalopay.wallet.view.component.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Stack;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.GatewayLoader;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EKeyBoardType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.feedback.Feedback;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.request.DownloadBundle;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.listener.ZPWOnCloseDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnProgressDialogTimeoutListener;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.listener.onShowDetailOrderListener;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.PermissionUtils;
import vn.com.zalopay.wallet.utils.StringUtil;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.custom.EllipsizingTextView;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

public abstract class BasePaymentActivity extends FragmentActivity {
    //stack to keep activity
    private static Stack<BasePaymentActivity> mZaloaPayActivitiesStack = new Stack<BasePaymentActivity>();
    //app info
    public DAppInfo appEntity;
    public boolean mIsBackClick = true;
    //this is flag prevent user back when user is submitting trans,authen payer,getstatus.
    public boolean processingOrder = false;
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
    protected boolean mLoadingMapCard = false, mLoadingBankAccount = false;
    protected NetworkingReceiver mNetworkingEventReceiver;
    protected int numberOfRetryOpenNetwoking = 0;
    protected boolean isAllowLinkCardATM = true;
    protected boolean isAllowLinkCardCC = true;
    /**
     * show more info icon click listener
     */
    protected onShowDetailOrderListener mShowDetailOrderClick = new onShowDetailOrderListener() {

        @Override
        public void onShowDetailOrder() {
            try {
                showInfoDialog(null, GlobalData.getPaymentInfo().description);
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
            final WeakReference<Activity> activity = new WeakReference<Activity>(BasePaymentActivity.getCurrentActivity());
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
                                getAdapter().sdkReportError(SDKReport.TIMEOUT_WEBSITE);
                            } catch (Exception e) {
                                Log.d(this, e);
                            }
                        }

                        return;
                    }
                    //load web timeout, need to get oneshot to server to check status again
                    if (ConnectionUtil.isOnline(GlobalData.getAppContext()) && getAdapter().isParseWebFlow() && getAdapter().shouldGetOneShotTransactionStatus()) {
                        getAdapter().getOneShotTransactionStatus();

                        //send logs timeout
                        if (getAdapter() != null) {
                            try {
                                getAdapter().sdkTrustReportError(SDKReport.TIMEOUT_WEBSITE);
                            } catch (Exception e) {
                                Log.d(this, e);
                            }
                        }
                        Log.d(this, "getOneShotTransactionStatus");
                    } else {
                        //show dialog and move to fail screen
                        ((PaymentChannelActivity) activity.get()).showWarningDialog(() -> ((PaymentChannelActivity) activity.get()).getAdapter().showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error)), GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
                    }
                    return;
                }
                //user in gateway screen
                if (activity.get() instanceof PaymentGatewayActivity) {
                    ((PaymentGatewayActivity) activity.get()).showWarningDialog(BasePaymentActivity.this::finish, GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
                }
            } catch (Exception ex) {
                ((PaymentChannelActivity) activity.get()).getAdapter().showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));

                if (getAdapter() != null) {
                    try {
                        getAdapter().sdkReportError(SDKReport.GENERAL_EXCEPTION, ex != null ? ex.getMessage() : "onProgressTimeout");
                    } catch (Exception e) {
                        Log.d(this, e);
                    }
                }
            }
        }
    };
    private boolean isVisibilitySupport = false;
    private Feedback mFeedback = null;
    /***
     * check static resource listener.
     */
    private GatewayLoader.onCheckResourceStaticListener checkResourceStaticListener = new GatewayLoader.onCheckResourceStaticListener() {
        @Override
        public void onCheckResourceStaticComplete(boolean isSuccess, String pError) {
            if (isSuccess) {
                reloadMapCardList();
                reloadBankAccountList();
            } else {
                //notify error and close sdk
                String message = pError;
                if (TextUtils.isEmpty(message)) {
                    message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
                    ;
                }

                if (!TextUtils.isEmpty(DownloadBundle.errorMessage)) {
                    message = DownloadBundle.errorMessage;
                }

                showDialogAndExit(message, ErrorManager.shouldShowDialog());
            }
        }

        @Override
        public void onCheckResourceStaticInProgress() {
            showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_loading_resource));
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            showProgress(false, null);

            notifyUpVersionToApp(pForceUpdate, pVersion, pMessage);

            if (!pForceUpdate) {
                readyForPayment();
            }
        }
    };
    protected void loadStaticReload()
    {
        //check static resource whether ready or not
        try {
            GatewayLoader.getInstance().setOnCheckResourceStaticListener(checkResourceStaticListener).checkStaticResource();
        } catch (Exception e) {
            if (checkResourceStaticListener != null) {
                checkResourceStaticListener.onCheckResourceStaticComplete(false, e != null ? e.getMessage() : null);
            }
        }
    }
    /***
     * load app info listener
     */
    private ILoadAppInfoListener loadAppInfoListener = new ILoadAppInfoListener() {
        @Override
        public void onProcessing() {
            showProgress(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_check_app_info));
        }

        @Override
        public void onSuccess() {
            Log.d(this, "===onSuccess===");

            if (!GlobalData.isAllowApplication()) {
                actionIfPreventApp();
                return;
            }

            try {
                showApplicationInfo();
            } catch (Exception e) {
                Log.d(this, e);
            }
            loadStaticReload();
        }

        @Override
        public void onError(DAppInfoResponse pMessage) {
            Log.d(this, "===onError===");

            showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));

            String message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);

            if (pMessage != null && !TextUtils.isEmpty(pMessage.getMessage())) {
                message = pMessage.getMessage();
            }

            if (pMessage != null && pMessage.returncode < 0) {
                //sometimes get app info return empty message and return code -2.that mean app not allow from backend.
                if (pMessage.returncode == -2 && TextUtils.isEmpty(pMessage.getMessage()))
                    message = GlobalData.getStringResource(RS.string.zpw_not_allow_payment_app);

                ErrorManager.updateTransactionResult(pMessage.returncode);
            }

            showDialogAndExit(message, ErrorManager.shouldShowDialog());
        }
    };
    //close snackbar networking alert listener
    private onCloseSnackBar mOnCloseSnackBarListener = new onCloseSnackBar() {
        @Override
        public void onClose() {
            askToOpenSettingNetwoking();
        }
    };
    private View.OnClickListener mSupportButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                int i = view.getId();

                if (i == R.id.question_button) {
                    Log.d("OnClickListener", "question_button");

                    Intent intent = new Intent();
                    intent.setAction(Constants.SUPPORT_INTRO_ACTION_SUPPORT_CENTER);
                    startActivity(intent);

                } else if (i == R.id.support_button) {
                    Log.d("OnClickListener", "support_button");

                    Intent intent = new Intent();
                    intent.setAction(Constants.SUPPORT_INTRO_ACTION_FEEDBACK);
                    if (mFeedback != null) {
                        intent.putExtra(Constants.CATEGORY, mFeedback.category);
                        intent.putExtra(Constants.TRANSACTIONID, mFeedback.transID);
                        intent.putExtra(Constants.SCREENSHOT, mFeedback.imgByteArray);
                        intent.putExtra(Constants.DESCRIPTION, mFeedback.description);

                    } else {
                        Log.d("support_button", "FeedBack == null");
                    }
                    startActivity(intent);

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

    public static Activity getCurrentActivity() {
        synchronized (mZaloaPayActivitiesStack) {
            if (mZaloaPayActivitiesStack == null || mZaloaPayActivitiesStack.size() == 0) {
                return GlobalData.getMerchantActivity();
            }

            return mZaloaPayActivitiesStack.peek();
        }
    }

    public static int getCurrentActivityCount() {
        if (mZaloaPayActivitiesStack != null) {
            return mZaloaPayActivitiesStack.size();
        }

        return 0;
    }

    public static BasePaymentActivity getPaymentGatewayActivity() {
        if (mZaloaPayActivitiesStack == null || mZaloaPayActivitiesStack.size() <= 0) {
            return null;
        }

        for (BasePaymentActivity activity : mZaloaPayActivitiesStack) {
            if (activity instanceof PaymentGatewayActivity) {
                return activity;
            }
        }

        return null;
    }

    public static BasePaymentActivity getPaymentChannelActivity() {
        if (mZaloaPayActivitiesStack == null || mZaloaPayActivitiesStack.size() <= 0) {
            return null;
        }
        for (BasePaymentActivity activity : mZaloaPayActivitiesStack) {
            if (activity instanceof PaymentChannelActivity) {
                return activity;
            }
        }

        return null;
    }

    public static void resetAttributeCascade(boolean... pAttr) {
        try {
            if (mZaloaPayActivitiesStack != null && mZaloaPayActivitiesStack.size() == 2) {
                mZaloaPayActivitiesStack.get(0).mIsBackClick = pAttr[0];
                mZaloaPayActivitiesStack.get(1).mIsBackClick = pAttr[0];
            }

        } catch (Exception e) {

        }
    }

    public abstract void recycleActivity();

    protected abstract void actionIfPreventApp();

    protected abstract void showDialogAndExit(String pMessage, boolean pIsShow);

    protected abstract void notifyUpVersionToApp(boolean pForceUpdate, String pVersion, String pMessage);

    protected abstract void readyForPayment();

    protected AdapterBase getAdapter() {
        return null;
    }

    protected ZPWOnCloseDialogListener getCloseDialog() {
        return mCloseDialog;
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
    //

    protected void onReturnCancel(final String pMessage) {
        showProgress(false, GlobalData.getStringResource(RS.string.walletsdk_string_bar_title));

        showWarningDialog(() -> {
            GlobalData.updateResultNetworkingError(pMessage);
            GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());

            finish();
        }, pMessage);
    }

    public void onExit(String pMessage, boolean pIsShowDialog) {
        showProgress(false, null);
        //just exit without show dialog.
        if (!pIsShowDialog) {
            recycleActivity();
            return;
        }
        //continue to show dialog and quit.
        String message = pMessage;

        if (TextUtils.isEmpty(message))
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);

        showWarningDialog(() -> recycleActivity(), message);
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
            GlobalData.getPaymentListener().onComplete(GlobalData.getPaymentResult());
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
        if (PermissionUtils.isNeedToRequestPermissionAtRuntime() && !PermissionUtils.checkIfAlreadyhavePermission(pContext))
            PermissionUtils.requestForSpecificPermission(this);
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

					/*
                    DialogManager.showSweetDialogCustom(BasePaymentActivity.getCurrentActivity(),
							GlobalData.getStringResource(RS.string.zpw_string_alert_permission_sms_not_allow),
							GlobalData.getStringResource(RS.string.dialog_close_button),
							SweetAlertDialog.NORMAL_TYPE, new ZPWOnEventDialogListener() {

								@Override
								public void onOKevent() {
								}
							});
							*/
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void finish() {
        super.finish();

        if (mIsBackClick) {
            //notify to app know that user click back on sdk.
            if (GlobalData.getPaymentResult() != null && GlobalData.getPaymentResult().paymentStatus == EPaymentStatus.ZPC_TRANXSTATUS_FAIL) {
                GlobalData.setResultUserClose();
            }

            slideOutTransition();
        } else {
            fadeOutTransition();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        synchronized (mZaloaPayActivitiesStack) {
            if (mZaloaPayActivitiesStack == null)
                mZaloaPayActivitiesStack = new Stack<>();

            mZaloaPayActivitiesStack.push(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(this, "===onResume===");

        // Register the local broadcast receiver networking change event
        IntentFilter messageFilter = new IntentFilter();
        messageFilter.addAction(Constants.FILTER_ACTION_NETWORKING_CHANGED);
        mNetworkingEventReceiver = new NetworkingReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mNetworkingEventReceiver, messageFilter);

        //clear snackbar if has networkign again when user resume
        if (ConnectionUtil.isOnline(this)) {
            Log.d(this, "===networking is on===");
            PaymentSnackBar.getInstance().dismiss();

            numberOfRetryOpenNetwoking = 0;
        } else {
            Log.d(this, "===networking is off===");
            showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_offline),
                    GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_INDEFINITE, mOnCloseSnackBarListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        synchronized (mZaloaPayActivitiesStack) {
            mZaloaPayActivitiesStack.remove(this);

            if (getCurrentActivityCount() == 0) {
                //dispose all instance and static resource.
                SingletonLifeCircleManager.disposeAll();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //release local receiver networking change event
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNetworkingEventReceiver);

    }

    /***
     * load app info from cache or api
     */
    protected void checkAppInfo() {
        AppInfoLoader.getInstance().setOnLoadAppInfoListener(loadAppInfoListener).execute();
    }

    protected void reloadMapCardList() {
        Log.d(this, "===starting reload map card list====");
        mLoadingMapCard = true;
        MapCardHelper.loadMapCardList(false, new IReloadMapInfoListener<DMappedCard>() {
            @Override
            public void onComplete(List<DMappedCard> pMapCardList) {
                Log.d("loadMapCardList", "===onComplete===" + GsonUtils.toJsonString(pMapCardList));
                mLoadingMapCard = false;
                checkLoadMapCardAndBankAccountToFinish();
            }

            @Override
            public void onError(String pErrorMess) {
                Log.d("loadMapCardList", "===onError=" + pErrorMess);
                mLoadingMapCard = false;
                checkLoadMapCardAndBankAccountToFinish();
            }
        });
    }

    protected void reloadBankAccountList() {
        Log.d(this, "===starting reload bank account list====");
        mLoadingBankAccount = true;
        BankAccountHelper.loadBankAccountList(false, new IReloadMapInfoListener<DBankAccount>() {
            @Override
            public void onComplete(List<DBankAccount> pMapList) {
                Log.d("reloadBankAccountList", "===onComplete===" + GsonUtils.toJsonString(pMapList));
                mLoadingBankAccount = false;
                checkLoadMapCardAndBankAccountToFinish();
            }

            @Override
            public void onError(String pErrorMess) {
                Log.d("reloadBankAccountList", "===onError=" + pErrorMess);
                mLoadingBankAccount = false;
                checkLoadMapCardAndBankAccountToFinish();
            }
        });
    }

    protected synchronized void checkLoadMapCardAndBankAccountToFinish() {
        if (!mLoadingBankAccount && !mLoadingMapCard) {
            readyForPayment();
        }
    }

    public View setKeyBoard(String pStrID, EKeyBoardType pKeyBoardType) {
        final int ID = getViewID(pStrID);
        View view = this.findViewById(ID);

        if (view == null && isViewVisible(view)) {
            Log.d("setKeyBoard", "===pStrID==NULL or NOT VISIBLE");
            return view;
        }

        if (pKeyBoardType == EKeyBoardType.NUMBER && view instanceof EditText) {
            //user using the laban key for exmple
            if (!ZPWUtils.useDefaultKeyBoard(this)) {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
        } else if (pKeyBoardType == EKeyBoardType.TEXT && view instanceof EditText) {
            ((EditText) view).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

        return view;
    }

    public void setViewColor(int pId, int pColor) {
        try {
            View view = findViewById(pId);

            if (view != null && view instanceof TextView)
                ((TextView) view).setTextColor(pColor);
        } catch (Exception e) {
        }
    }

    public boolean isViewVisible(View view) {
        if (view == null) {
            return false;
        }
        return view.getVisibility() == View.VISIBLE;
    }

    public void setTextHtml(int pId, String pHtmlText) {
        try {
            View view = findViewById(pId);

            if (view != null && view instanceof TextView)
                ((TextView) view).setText(Html.fromHtml(pHtmlText));
        } catch (Exception e) {
        }
    }

    public void setEnableView(int pId, boolean pIsEnable) {
        try {
            View view = findViewById(pId);

            if (view != null)
                view.setEnabled(pIsEnable);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public int getViewID(String pStrID) {
        return this.getResources().getIdentifier(pStrID, "id", this.getPackageName());
    }

    public View setView(String pStrID, boolean pIsVisible) {
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

    public void setView(int pId, boolean pIsVisible) {
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

        if (view == null)
            return view;

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();

        layoutParams.bottomMargin = margin;

        view.setLayoutParams(layoutParams);

        return view;
    }

    public View setViewAlign(String pStrID, int... gravity) {
        final int ID = getViewID(pStrID);
        View view = this.findViewById(ID);

        if (view == null)
            return view;

        if (view instanceof TextView)
            if (gravity.length == 2)
                ((TextView) view).setGravity(gravity[0] | gravity[1]);
            else
                ((TextView) view).setGravity(gravity[0]);

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

        if (GlobalData.isTopupChannel()) {
            barTitle = GlobalData.getStringResource(RS.string.zpw_string_pay_title);
        } else if (GlobalData.isTranferMoneyChannel()) {
            barTitle = GlobalData.getStringResource(RS.string.zpw_string_tranfer_title);
        } else if (GlobalData.isWithDrawChannel()) {
            barTitle = GlobalData.getStringResource(RS.string.zpw_string_withdraw_title);
        }

        return barTitle;
    }

    public void setToolBarTitle() {
        setBarTitle(getTransactionTitle());
    }

    /***
     * set text for view ,use by bundle
     *
     * @param pStrID
     * @param pText
     */
    public void setText(String pStrID, String pText) {
        final int ID = getViewID(pStrID);
        View textView = this.findViewById(ID);

        if (textView == null)
            return;

        if (textView instanceof ToggleButton)
            ((ToggleButton) this.findViewById(ID)).setText(pText);
        else if (textView instanceof EditText) {
            //SET HINT TO PARENT IF EDITTEXT'S PARENT IS TEXTINPUTLAYOUT
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

    /***
     * set text for by used in sdk
     *
     * @param pID
     * @param pText
     */
    public void setText(int pID, String pText) {
        View view = this.findViewById(pID);

        if (view == null) {
            return;
        }

        if (view instanceof EditText) {
            /***
             * SET HINT TO PARENT IF EDITTEXT'S PARENT IS TEXTINPUTLAYOUT
             */
            EditText editText = (EditText) view;

            if (editText instanceof VPaymentEditText && ((VPaymentEditText) editText).getTextInputLayout() instanceof TextInputLayout) {
                TextInputLayout textInputLayout = ((VPaymentEditText) editText).getTextInputLayout();
                textInputLayout.setHint(pText);
            } else {
                editText.setHint(pText);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setText(pText);
        }
    }

    /***
     * set bitmap for imageview ,used by bundle
     *
     * @param pStrID
     * @param pBitmap
     */
    public void setImage(String pStrID, Bitmap pBitmap) {
        int ID = getViewID(pStrID);

        ImageView imageView = ((ImageView) this.findViewById(ID));

        if (imageView != null) {
            if (pBitmap == null) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setImageBitmap(pBitmap);
            }
        }
    }

    public void setImage(int pId, Bitmap pBitmap) {
        View view = findViewById(pId);

        if (view != null && view instanceof ImageView) {
            if (pBitmap == null) {
                view.setVisibility(View.GONE);
            } else {
                ((ImageView) view).setImageBitmap(pBitmap);
            }
        }
    }

    public View findViewById(String pName) {
        return findViewById(RS.getID(pName));
    }

    /***
     * show app name
     */
    protected void showApplicationInfo() throws Exception {
        if (appEntity == null) {
            appEntity = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getAppById(String.valueOf(GlobalData.appID)), DAppInfo.class);
        }

        //withdraw no need to show app name
        if (GlobalData.isWithDrawChannel()) {
            return;
        }

        if (appEntity != null && !TextUtils.isEmpty(appEntity.appname)) {
            setText(R.id.zalosdk_bill_info_ctl, appEntity.appname);
            setView(R.id.zalosdk_bill_info_ctl, true);
        } else {
            setView(R.id.zalosdk_bill_info_ctl, false);
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
            DialogManager.showSweetDialog(activity, SweetAlertDialog.NO_INTERNET, getString(R.string.zingpaysdk_alert_title_nointernet), getString(R.string.zingpaysdk_alert_content_nointernet), new ZPWOnSweetDialogListener() {
                @Override
                public void onClickDiaLog(int pIndex) {
                    if (pIndex == 0) {
                        if (pListener != null)
                            pListener.onCloseNetworkingDialog();

                    } else if (pIndex == 1) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        if (pListener != null)
                            pListener.onOpenSettingDialogClicked();
                    }
                }
            }, new String[]{GlobalData.getStringResource(RS.string.dialog_turn_off), GlobalData.getStringResource(RS.string.dialog_turn_on)});
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
        setView(R.id.zpsdk_app_info, pIsVisible);
    }

    public void visibleCardInfo(boolean pIsVisible) {
        setView(R.id.zpw_card_info, pIsVisible);
    }

    public void visibleTranferWalletInfo(boolean pIsVisible) {
        setView(R.id.zpsdk_transfer_info, pIsVisible);
    }

    public void visibleSubmitButton(boolean pIsVisible) {
        setView(R.id.zpw_submit_view, pIsVisible);
    }

    public void visibleCardViewNavigateButton(boolean pIsVisible) {
        setView(R.id.zpw_switch_card_button, pIsVisible);
    }

    public void visibleInputCardView(boolean pIsVisible) {
        setView(R.id.localcard_view_root, pIsVisible);
    }

    public void visibleWebView(boolean pIsVisible) {
        setView(R.id.zpw_threesecurity_webview, pIsVisible);
    }

    public void visiblePinView(boolean pIsVisible) {
        setView(R.id.zpsdk_pin_layout, pIsVisible);
    }

    public void visibleConfirmView(boolean pIsVisible) {
        setView(R.id.zpw_confirm, pIsVisible);
    }

    public int getVisible(int pID) {
        View view = findViewById(pID);

        if (view != null) {
            return view.getVisibility();
        }
        return -1;
    }

    public void setBarTitle(String pTitle) {
        setText(R.id.payment_method_name, pTitle);

        mTitleHeaderText = pTitle;
    }

    public void setTitle() {
        String title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway);

        if (GlobalData.isTopupChannel()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_topup);
        } else if (GlobalData.isTranferMoneyChannel()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_tranfer);
        } else if (GlobalData.isWithDrawChannel()) {
            title = GlobalData.getStringResource(RS.string.zpw_string_title_payment_gateway_withdraw);
        }

        setText(R.id.title_payment_method, title);
    }

    public void visibleHeaderInfo() {
        if (GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel()) {
            if (GlobalData.isTranferMoneyChannel()) {
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

        if (GlobalData.orderAmountFee > 0) {
            setView(R.id.zpw_fee_view_wrapper, true);
            setView(R.id.zpw_total_view_wrapper, true);

            setText(R.id.zpw_payment_channel_fee, StringUtil.formatVnCurrence(String.valueOf(GlobalData.orderAmountFee)));

            setText(R.id.zpw_payment_channel_total_pay,
                    StringUtil.formatVnCurrence(String.valueOf(GlobalData.orderAmountTotal)));

            ZPWUtils.applyFont(findViewById(R.id.zpw_payment_channel_total_pay),
                    GlobalData.getStringResource(RS.string.zpw_font_medium));
        }

    }

    protected void showAmount() {
        try {
            if (GlobalData.getPaymentInfo() != null && GlobalData.getPaymentInfo().amount > 0) {
                String txtAmount = StringUtil.formatVnCurrence(String.valueOf(GlobalData.getOrderAmount()));
                setText(R.id.payment_method_amount, txtAmount);

                setText(R.id.payment_currency_label, GlobalData.getStringResource(RS.string.zpw_string_payment_currency_label));

            } else {
                setView(R.id.payment_method_amount, false);
                setView(R.id.payment_currency_label, false);
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

        if(this instanceof PaymentChannelActivity)
        {
            StatusResponse res = getAdapter().getResponseStatus();
            /*res.returncode = Constants.PAYMENT_LIMIT_PER_DAY_CODE.get(0);
            getAdapter().setmResponseStatus(res);*/
            // The inform text would be set from server
            if(res != null) {
                setText(R.id.zpw_textview_update_level_inform, res.suggest_actions);//show suggest action which return from server
                setView(R.id.zpw_textview_update_level_inform, !TextUtils.isEmpty(res.suggest_actions));
            }

            //exception case for payment overlimit per day
            if(PaymentStatusHelper.isPaymentOverLimitPerDay(getAdapter().getResponseStatus())) {
                setView(R.id.zpw_payment_fail_rl_update_info, true);
            } else {
                setView(R.id.zpw_payment_fail_rl_update_info, false);
            }
        }
        setView(R.id.zpw_pay_info_buttom_view, true);

        if (!TextUtils.isEmpty(pTransID) && Long.parseLong(pTransID) > 0) {
            setView(R.id.zpw_transaction_wrapper, true);
            setText(R.id.zpw_textview_transaction, pTransID);
            setView(R.id.zpw_notransid_textview, false);
        } else {
            setView(R.id.zpw_transaction_wrapper, false);
            setView(R.id.zpw_notransid_textview, true);
        }

        // set color for text in linkacc
        if (GlobalData.isLinkAccChannel()) {
            if (getAdapter().getPageName().equals(AdapterBase.PAGE_LINKACC_FAIL)) { // linkacc fail
                setView(R.id.zpw_payment_fail_textview, true);
            } else { // unlinkacc fail
                setView(R.id.zpw_payment_fail_textview, false);
            }
        }

        ZPWUtils.applyFont(findViewById(R.id.zpw_textview_transaction),
                GlobalData.getStringResource(RS.string.zpw_font_medium));

        //re-align top
        addOrRemoveProperty(R.id.payment_method_name, RelativeLayout.CENTER_IN_PARENT);

        animateImageViewFail();

    }

    public void showPaymentSuccessContent(String pTransID) throws Exception {
        setText(R.id.zpw_textview_transaction, pTransID);
        setView(R.id.zpw_pay_info_buttom_view, true);
        //show a different view for lixi.
        if (GlobalData.isRedPacketChannel()) {
            setView(R.id.zpw_transaction_wrapper, false);
            setView(R.id.zpw_textview_transaction_lixi_label, true);
            String formattedString = "<b>" + GlobalData.getStringResource(RS.string.zpw_string_lixi_notice_title_02) + "</b>";
            setTextHtml(R.id.zpw_textview_transaction_lixi_label, String.format(GlobalData.getStringResource(RS.string.zpw_string_lixi_notice_title), formattedString));
        }

        ZPWUtils.applyFont(findViewById(R.id.zpw_textview_transaction), GlobalData.getStringResource(RS.string.zpw_font_medium));
        //show transaction amount when ! withdraw
        if (GlobalData.orderAmountTotal > 0 && GlobalData.getTransactionType() != ETransactionType.WITHDRAW) {

            setTextHtml(R.id.payment_price_label, StringUtil.formatVnCurrence(String.valueOf(GlobalData.orderAmountTotal)));
            if (!TextUtils.isEmpty(GlobalData.getPaymentInfo().description)) {
                setView(R.id.payment_description_label, true);
                setText(R.id.payment_description_label, GlobalData.getPaymentInfo().description);
            } else
                setView(R.id.payment_description_label, false);
        } else if (GlobalData.isLinkAccChannel()) { // show label for linkAcc
            if (getAdapter().getPageName().equals(AdapterBase.PAGE_LINKACC_SUCCESS)) {
                setViewColor(R.id.zpw_payment_success_textview, getResources().getColor(R.color.text_color_primary));
                setView(R.id.payment_description_label, true);

                String desc = GlobalData.getStringResource(RS.string.zpw_string_linkacc_notice_description);
                if(getPaymentChannelActivity().getAdapter() instanceof AdapterLinkAcc &&
                        ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification() != null)
                {
                    desc = ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification().getMsg();
                }
                setText(R.id.payment_description_label,desc);
                setView(R.id.price_linearlayout, false);
                setMarginBottom(R.id.zpw_payment_success_textview, (int) getResources().getDimension(R.dimen.zpw_margin_top_medium_label));
            } else {
                setView(R.id.zpw_payment_success_textview, false);
                setView(R.id.payment_description_label, true);
                String desc = GlobalData.getStringResource(RS.string.zpw_string_unlinkacc_notice_description);
                if(getPaymentChannelActivity().getAdapter() instanceof AdapterLinkAcc &&
                        ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification() != null)
                {
                    desc = ((AdapterLinkAcc) getPaymentChannelActivity().getAdapter()).getNotification().getMsg();
                }
                setText(R.id.payment_description_label,desc);
                setView(R.id.price_linearlayout, false);
                setMarginBottom(R.id.zpw_payment_success_textview, (int) getResources().getDimension(R.dimen.zpw_margin_top_supper_supper_label));
            }
        } else {
            setView(R.id.payment_description_label, false);
            setView(R.id.price_linearlayout, false);
            setMarginBottom(R.id.zpw_payment_success_textview, (int) getResources().getDimension(R.dimen.zpw_margin_top_supper_supper_label));

        }
        //re-align title header.
        addOrRemoveProperty(R.id.payment_method_name, RelativeLayout.CENTER_IN_PARENT);
        animationImageViewSuccess();
    }

    public void showPaymentSpecialSuccessContent(String pTransID) {
        setText(R.id.zpw_textview_transaction_id, pTransID);

        if (GlobalData.orderAmountTotal >= 0) {
            setTextHtml(R.id.zpw_textview_transaction_amount, StringUtil.formatVnCurrence(String.valueOf(GlobalData.orderAmountTotal)));
        } else {
            findViewById(R.id.zpw_textview_transaction_amount).setVisibility(View.GONE);
        }

        //set app name
        try {
            if (appEntity == null)
                appEntity = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getAppById(String.valueOf(GlobalData.appID)), DAppInfo.class);
        } catch (Exception e) {
            Log.d(this, e);
        }

        if (appEntity != null && appEntity.viewresulttype == 2) {
            setView(R.id.zpw_textview_transaction_description, true);
            setText(R.id.zpw_textview_transaction_description, appEntity.appname);
        } else
            setView(R.id.zpw_textview_transaction_description, false);

        //set time transaction
        try {
            if (GlobalData.getPaymentInfo().appTime > 0) {
                setText(R.id.zpw_textview_transaction_time, ZPWUtils.convertDateTime(GlobalData.getPaymentInfo().appTime));
            }
        } catch (Exception e) {
            Log.e(this, e);
        }

        //re-margin title header.
        setMarginLeft(R.id.payment_method_name, (int) getResources().getDimension(R.dimen.zpw_header_label_margin));

        //animate icon
        animationImageViewSuccessSpecial();
    }

    protected void showBalanceContent(DPaymentChannel pConfig) throws Exception {

        setText(R.id.zalopay_bill_info, StringUtil.formatVnCurrence(String.valueOf(GlobalData.getBalance())));

        setView(R.id.zpw_channel_layout, true);
        setView(R.id.zpw_channel_label_textview, false);
        setText(R.id.zpw_channel_name_textview, pConfig != null ? pConfig.pmcname : GlobalData.getStringResource(RS.string.zpw_string_zalopay_wallet_method_name));


        if (GlobalData.getBalance() < GlobalData.orderAmountTotal) {
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
    protected void showUserInfoWalletTransfer() throws Exception {
        if (!GlobalData.isTranferMoneyChannel())
            return;

        if (GlobalData.getPaymentInfo() != null && GlobalData.getPaymentInfo().userInfo == null) {
            showDialogUserInfo();
            return;
        }

        //show fee
        showConfirmView(false, false, null);

        //zalo name
        if (!TextUtils.isEmpty(GlobalData.getPaymentInfo().userInfo.userName))
            setText(R.id.zpw_wallet_transfer_user, GlobalData.getPaymentInfo().userInfo.userName);
        else
            setView(R.id.tranfer_user_name_relative_wrapper, false);

        //zalopay name
        if (!TextUtils.isEmpty(GlobalData.getPaymentInfo().userInfo.zaloPayName))
            setText(R.id.zpw_zalopay_name_textview, GlobalData.getPaymentInfo().userInfo.zaloPayName);
        else
            setView(R.id.zalopay_name_relative_wrapper, false);

        //description
        if (!TextUtils.isEmpty(GlobalData.getPaymentInfo().description)) {
            setView(R.id.zpw_wallet_transfer_description, true);
            setText(R.id.zpw_wallet_transfer_description, GlobalData.getPaymentInfo().description);
        } else
            setView(R.id.zpw_wallet_transfer_description, false);

        //amount
        if (GlobalData.getPaymentInfo().amount > 0) {
            String txtAmount = StringUtil.formatVnCurrence(String.valueOf(GlobalData.getPaymentInfo().amount));
            setText(R.id.zpw_wallet_transfer_amount, txtAmount);
        }
    }

    /***
     * alert invalid data and quit payment
     */
    private void showDialogUserInfo() {
        showErrorDialog(() -> {
            GlobalData.setResultInvalidInput();
            recycleActivity();
        }, GlobalData.getStringResource(RS.string.zpw_string_alert_userinfo_invalid));
    }

    /**
     * show fee
     */
    public void showConfirmView(boolean pHidden, boolean pShow, DPaymentChannel pChannel) {
        if (GlobalData.isChannelHasInputCard() && pHidden) {
            visibleTranferWalletInfo(false);
            visibleAppInfo(false);

            return;
        }

        if (GlobalData.isTranferMoneyChannel()) {
            visibleTranferWalletInfo(true);
            visibleAppInfo(false);

            if (pShow && GlobalData.orderAmountFee > 0) {
                setView(R.id.zpw_wallet_transfer_layout_price, true);
                setView(R.id.zpw_wallet_transfer_layout_payamont, true);

                if (pChannel != null) {
                    String txtprice = StringUtil.formatVnCurrence(String.valueOf(GlobalData.orderAmountFee));
                    setText(R.id.zpw_wallet_transfer_price, txtprice);
                }

                try {
                    if (GlobalData.orderAmountTotal > 0) {
                        String txtAmount = StringUtil.formatVnCurrence(String.valueOf(GlobalData.orderAmountTotal));
                        setText(R.id.zpw_wallet_transfer_payamount, txtAmount);

                    }
                } catch (Exception e) {
                    Log.e(this, e);
                }
            } else {
                setView(R.id.zpw_wallet_transfer_layout_price, false);
                setView(R.id.zpw_wallet_transfer_layout_payamont, false);
            }
        } else if (GlobalData.isChannelHasInputCard()) {
            visibleTranferWalletInfo(false);
            visibleAppInfo(false);
        }

    }

    /***
     * show header text
     */
    protected void showDisplayInfo() throws Exception {
        //linkcard channel
        if (GlobalData.isLinkCardChannel()) {

            setView(R.id.app_info_linerlayout, false);
            setView(R.id.linkcard_channel_desc_textview, true);

            String linkcardChannelDesc = GlobalData.getStringResource(RS.string.zpw_conf_wallet_linkcard_desc);

            linkcardChannelDesc = String.format(linkcardChannelDesc, StringUtil.formatVnCurrence(GlobalData.getStringResource(RS.string.zpw_conf_wallet_amount)));

            setText(R.id.linkcard_channel_desc_textview, linkcardChannelDesc);
        }
        //tranfer money channel
        else if (GlobalData.isTranferMoneyChannel()) {
            visibleTranferWalletInfo(true);
            visibleAppInfo(false);

            showUserInfoWalletTransfer();
        } else {

            if (GlobalData.isWithDrawChannel()) {
                setText(R.id.item_name, GlobalData.getStringResource(RS.string.zpw_string_withdraw_description));
            } else if (GlobalData.getPaymentInfo() != null) {
                setText(R.id.item_name, !TextUtils.isEmpty(GlobalData.getPaymentInfo().description) ? GlobalData.getPaymentInfo().description : null);
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
        final View passwordView = findViewById(R.id.zpw_gridview_pin);

        if (passwordView != null) {
            ViewTreeObserver vto = passwordView.getViewTreeObserver();

            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = passwordView.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);

                    int width = ZPWUtils.widthScreen(getCurrentActivity());

                    int pinLength = getResources().getInteger(R.integer.wallet_pin_length);

                    int margin = (int) ZPWUtils.convertDpToPixel(getResources().getDimension(R.dimen.zpw_pin_margin), getApplicationContext());

                    width = width - margin * 2;

                    int height = width / pinLength;

                    if (width == 0 || height == 0)
                        return;

                    passwordView.getLayoutParams().height = height;
                    passwordView.getLayoutParams().width = width;
                }
            });
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
        if (!ZPWUtils.isTablet(this)) {
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

    protected void resizePaymentButton() {
        final View submitButton = findViewById(R.id.zpsdk_btn_submit);

        if (submitButton != null) {
            ViewTreeObserver vto = submitButton.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = submitButton.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);

                    ScrollView scrollView = (ScrollView) findViewById(R.id.zpw_scrollview_container);
                    if (scrollView != null) {
                        try {
                            if (scrollView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 0, 0, submitButton.getHeight());
                                scrollView.setLayoutParams(params);
                            } else if (scrollView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 0, 0, submitButton.getHeight());
                                scrollView.setLayoutParams(params);
                            }
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }
            });
        }
    }

    /***
     * user level 1 can not tranfer money.
     * user level 1 can not withdraw.
     */
    public boolean checkUserLevelValid() {
        boolean userLevelValid = true;
        try {
            if (GlobalData.isTranferMoneyChannel() && GlobalData.getLevel() < Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_level_allow_use_zalopay))) {
                userLevelValid = false;
            } else if (GlobalData.isWithDrawChannel() && GlobalData.getLevel() < Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_level_allow_withdraw))) {
                userLevelValid = false;
            } else if (GlobalData.isMapCardChannel() && GlobalData.getLevel() < Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_level_allow_mapcard))) {
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
        ZPWUtils.overrideFonts(viewGroup, GlobalData.getStringResource(RS.string.zpw_font_regular));

        ZPWUtils.applyFont(findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium));
        ZPWUtils.applyFont(findViewById(R.id.payment_method_name), GlobalData.getStringResource(RS.string.zpw_font_medium));
    }

    protected void performClick(int id) {
        setVisible(false);
        findViewById(id).performClick();
    }

    protected void setListener() {
        View view = findViewById(R.id.item_name);
        if (view != null)
            ((EllipsizingTextView) view).setOnShowDetailOrderListener(mShowDetailOrderClick);
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
            if (GlobalData.getPaymentResult() != null) {
                GlobalData.setResultServiceMaintenance();
            }
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
            if (BankLoader.getInstance().isBankMaintenance(pBankCode)) {
                showInfoDialog(pListener, StringUtil.getFormattedBankMaintenaceMessage());
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
            if (!BankLoader.getInstance().isBankSupport(pBankCode)) {
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
     *
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
        //Show dialog WARNING_TYPE
        DialogManager.showSweetDialogCustom(this,
                message, closeButtonText,
                SweetAlertDialog.WARNING_TYPE, pDialogListener);
    }

    /***
     * info dialog
     *
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

        //Show dialog DialogOptionNotice
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
            Bitmap mBitmap = ZPWUtils.CaptureScreenshot(getCurrentActivity());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byteArray = stream.toByteArray();

            String transactionTitle = getTransactionTitle();

            if (GlobalData.isLinkCardChannel()) {
                transactionTitle = GlobalData.getStringResource(RS.string.zpw_string_credit_card_link);
            }

            //Create Parcelable Feedback
            mFeedback = new Feedback(byteArray, getMessageFailView(), transactionTitle, pTransactionID);

            setView(R.id.zpw_pay_support_buttom_view, true);
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setView(R.id.zpw_pay_support_buttom_view, false);
            }
        }, 300);
    }

    public boolean getVisibilitySupportView() {
        return isVisibilitySupport;
    }

    private String getMessageFailView() {
        TextView textView = (TextView) findViewById(R.id.zpw_textview_error_message);
        return ((textView != null) ? String.valueOf(textView.getText()) : "");
    }

    public void showSelectionBankAccountDialog() {
        MapListSelectionActivity.setCloseDialogListener(getCloseDialog());
        Intent intent = new Intent(getApplicationContext(), MapListSelectionActivity.class);
        intent.putExtra(MapListSelectionActivity.BANKCODE_EXTRA, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank));
        intent.putExtra(MapListSelectionActivity.BUTTON_LEFT_TEXT_EXTRA, getCloseButtonText());
        startActivity(intent);
    }

    /***
     * internal class for receiving networking event
     */
    public class NetworkingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Constants.FILTER_ACTION_NETWORKING_CHANGED)) {
                //user sitting in the result screen
                if (getCurrentActivity() instanceof PaymentChannelActivity && ((PaymentChannelActivity) getCurrentActivity()).getAdapter().isFinalScreen()) {
                    return;
                }
                //come from api request fail with handshake
                if (intent.getExtras() != null && intent.getExtras().getBoolean(Constants.NETWORKING_NOT_STABLE)) {
                    showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_not_stable),
                            GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_LONG, mOnCloseSnackBarListener);

                    Log.d(this, "===networking is not stable===");
                }
                //networking indeed offline
                else if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
                    Log.d(this, "===networking is off===");
                    showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_offline),
                            GlobalData.getStringResource(RS.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_INDEFINITE, mOnCloseSnackBarListener);
                }
                //networking online again
                else {
                    //DialogManager.closeNetworkingDialog();
                    showMessageSnackBar(findViewById(R.id.supperRootView), GlobalData.getStringResource(RS.string.zpw_string_alert_networking_online), null, TSnackbar.LENGTH_SHORT, null);
                    Log.d(this, "===networking is online again===");
                }
            }
        }
    }
    //endregion

}
