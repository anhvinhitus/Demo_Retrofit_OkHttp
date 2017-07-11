package vn.com.zalopay.wallet.business.channel.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.design.widget.BottomSheetBehavior;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnProgressDialogTimeoutListener;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.ServiceManager;
import vn.com.zalopay.wallet.api.task.BaseTask;
import vn.com.zalopay.wallet.api.task.CheckOrderStatusFailSubmit;
import vn.com.zalopay.wallet.api.task.SDKReportTask;
import vn.com.zalopay.wallet.api.task.SendLogTask;
import vn.com.zalopay.wallet.api.task.getstatus.GetStatus;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.base.SecurityResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.WebViewHelper;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.constants.BankFlow;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.CardHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelFragment;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;
import vn.zalopay.promotion.CashBackRender;
import vn.zalopay.promotion.IBuilder;
import vn.zalopay.promotion.IInteractPromotion;
import vn.zalopay.promotion.IPromotionResult;
import vn.zalopay.promotion.IResourceLoader;
import vn.zalopay.promotion.PromotionEvent;

import static vn.com.zalopay.wallet.api.task.SDKReportTask.GENERAL_EXCEPTION;
import static vn.com.zalopay.wallet.api.task.SDKReportTask.TIMEOUT_WEBSITE;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_AUTHEN;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_BALANCE_ERROR;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_FAIL;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_FAIL_NETWORKING;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_FAIL_PROCESSING;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_SUCCESS;
import static vn.com.zalopay.wallet.constants.Constants.SCREEN_ATM;
import static vn.com.zalopay.wallet.constants.Constants.SCREEN_CC;
import static vn.com.zalopay.wallet.constants.Constants.TRANS_STATUS_MAX_RETRY;
import static vn.com.zalopay.wallet.helper.TransactionHelper.isTransNetworkError;

public abstract class AdapterBase {
    protected final DPaymentCard mCard;
    public boolean processingOrder = false;//this is flag prevent user back when user is submitting trans,authen payer,getstatus
    protected WeakReference<ChannelPresenter> mPresenter = null;
    protected CardGuiProcessor mGuiProcessor = null;
    protected StatusResponse mResponseStatus;
    protected boolean isLoadWebTimeout = false;
    protected int numberRetryOtp = 0;
    protected MapCard mMapCard;
    protected String mTransactionID;
    protected String mPageName;
    protected boolean existTransWithoutConfirm = true;
    //prevent duplicate many time
    protected boolean isAlreadyCheckStatusFailSubmit = false;
    //count of retry check status if submit order fail
    protected int mCountCheckStatus = 0;
    //check data in response get status api
    protected boolean isCheckDataInStatus = false;
    //submit log load website to server
    protected long mCaptchaBeginTime = 0, mCaptchaEndTime = 0;
    protected long mOtpBeginTime = 0, mOtpEndTime = 0;
    //whether show dialog or not?
    protected boolean showDialogOnChannelList = true;
    //need to switch to cc or atm
    protected boolean mNeedToSwitchChannel = false;
    protected boolean mIsOrderSubmit = false;
    protected boolean mCanEditCardInfo = false;
    @BankFlow
    protected int mECardFlowType;
    protected MiniPmcTransType mMiniPmcTransType;
    protected IBuilder mPromotionBuilder;
    protected IPromotionResult mPromotionResult;
    protected PaymentInfoHelper mPaymentInfoHelper;

    public ZPWPaymentOpenNetworkingDialogListener closeSettingNetworkingListener = new ZPWPaymentOpenNetworkingDialogListener() {
        @Override
        public void onCloseNetworkingDialog() {
            whetherQuitPaymentOffline();
        }

        @Override
        public void onOpenSettingDialogClicked() {
        }
    };
    /*
     * loading website so long,over timeout 40s
     */
    int numberOfRetryTimeout = 1;
    private SDKTransactionAdapter mTransactionAdapter;
    private Action1<Throwable> loadCardException = throwable -> {
        Log.d(this, "load card list on error", throwable);
        String message = null;
        if (throwable instanceof RequestException) {
            message = throwable.getMessage();
        }
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.sdk_load_card_error);
        }
        try {
            getView().hideLoading();
            getView().showInfoDialog(message);
        } catch (Exception e) {
            Log.e(this, e);
        }
    };
    private Action1<Boolean> loadCardSubscriber = aBoolean -> {
        Timber.d("load card list finish");
        try {
            getView().hideLoading();
        } catch (Exception e) {
            Log.e(this, e);
        }
        String cardKey = getCard().getCardKey();
        if (!TextUtils.isEmpty(cardKey)) {
            MapCard mapCard = SDKApplication
                    .getApplicationComponent()
                    .linkInteractor()
                    .getCard(mPaymentInfoHelper.getUserId(), cardKey);
            if (mapCard != null) {
                DMapCardResult mapCardResult = CardHelper.cast(mapCard);
                mPaymentInfoHelper.setMapCardResult(mapCardResult);
                Log.d(this, "set map card to app", mapCardResult);
            }
        }
        //quit sdk right away
        if (GlobalData.isRedPacketChannel(mPaymentInfoHelper.getAppId())) {
            onClickSubmission();
        }
    };
    public ZPWOnProgressDialogTimeoutListener mProgressDialogTimeoutListener = new ZPWOnProgressDialogTimeoutListener() {
        @Override
        public void onProgressTimeout() {
            try {
                WeakReference<Activity> activity = new WeakReference<>(getView().getActivity());
                if (activity.get() == null || activity.get().isFinishing()) {
                    Timber.d("onProgressTimeout - activity is finish");
                    return;
                }
                if (isFinalScreen()) {
                    return;
                }
                //retry load website cc
                if (ConnectionUtil.isOnline(GlobalData.getAppContext()) && isCCFlow() && isLoadWeb() && hasTransId()) {
                    //max retry 3
                    if (numberOfRetryTimeout > Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_load_web_retry))) {
                        getOneShotTransactionStatus();
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
                                    getOneShotTransactionStatus();
                                }

                                @Override
                                public void onOKEvent() {
                                    DialogManager.showProcessDialog(activity.get(), mProgressDialogTimeoutListener);
                                    try {
                                        getGuiProcessor().reloadUrl();
                                    } catch (Exception e) {
                                        Log.e(this, e);
                                    }
                                }
                            });
                }
                //load web timeout, need to get oneshot to server to check status again
                else if (ConnectionUtil.isOnline(GlobalData.getAppContext()) && isParseWebFlow() && hasTransId()) {
                    getOneShotTransactionStatus();
                } else if (mPaymentInfoHelper.isBankAccountTrans() && AdapterBase.this instanceof AdapterLinkAcc && isFinalStep()) {
                    ((AdapterLinkAcc) AdapterBase.this).verifyServerAfterParseWebTimeout();
                    Timber.d("load website timeout, continue to verify server again to ask for new data list");
                } else if (!isFinalScreen()) {
                    getView().showInfoDialog(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error), () -> showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error)));
                }
                sdkReportError(TIMEOUT_WEBSITE, GsonUtils.toJsonString(mResponseStatus));
            } catch (Exception ex) {
                Timber.w(ex.getMessage());
                showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
                try {
                    sdkReportError(GENERAL_EXCEPTION, ex.getMessage());
                } catch (Exception e) {
                    Timber.w(e.getMessage());
                }
            }
        }
    };

    public AdapterBase(String pPageName, ChannelPresenter pPresenter,
                       MiniPmcTransType pMiniPmcTransType, PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) {
        mPresenter = new WeakReference<>(pPresenter);
        mMiniPmcTransType = pMiniPmcTransType;
        mCard = new DPaymentCard();
        mPaymentInfoHelper = paymentInfoHelper;
        mTransactionAdapter = SDKTransactionAdapter.shared().setAdapter(this);
        mResponseStatus = statusResponse;
        if (mResponseStatus != null) {
            mTransactionID = mResponseStatus.zptransid;
            mPageName = TransactionHelper.getPageName(paymentInfoHelper.getStatus());
            if (TransactionHelper.isSecurityFlow(mResponseStatus)) {
                mPageName = null;
            }
        }
        if (TextUtils.isEmpty(mPageName)) {
            mPageName = pPageName;
        }
    }

    public void showLoadindTimeout(String pTitle) {
        try {
            getView().showLoading(pTitle, mProgressDialogTimeoutListener);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    public String getTransactionID() {
        return mTransactionID;
    }

    public PaymentInfoHelper getPaymentInfoHelper() {
        return mPaymentInfoHelper;
    }

    public void setmPaymentInfoHelper(PaymentInfoHelper paymentInfoHelper) {
        this.mPaymentInfoHelper = paymentInfoHelper;
    }

    public void setMiniPmcTransType(MiniPmcTransType mMiniPmcTransType) {
        this.mMiniPmcTransType = mMiniPmcTransType;
    }

    public void requestReadOtpPermission() {
        try {
            getActivity().requestPermission(GlobalData.getAppContext());//request permission read/view sms on android 6.0+
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public boolean needReloadPmcConfig(String pBankCode) {
        return false;
    }

    public MiniPmcTransType getConfig() {
        return mMiniPmcTransType;
    }

    public MiniPmcTransType getConfig(String pBankCode) {
        return mMiniPmcTransType;
    }

    /**
     * getter and setter
     */
    public StatusResponse getResponseStatus() {
        return mResponseStatus;
    }

    public void init() throws Exception {
        //add overswipe for rootview scrollview
        ScrollView scrollViewRoot = (ScrollView) getView().findViewById(R.id.zpw_scrollview_container);
        if (scrollViewRoot != null) {
            OverScrollDecoratorHelper.setUpOverScroll(scrollViewRoot);
        }
        //flow password payment
        if (hasTransId()) {
            existTransWithoutConfirm = false;
            if (isTransactionSuccess()) {
                showTransactionSuccessView();
            } else if (!TransactionHelper.isSecurityFlow(mResponseStatus)) {
                showTransactionFailView(mResponseStatus.returnmessage);
            }
        }
        Log.d(this, "start adapter with page name", mPageName);
    }

    public abstract void onProcessPhrase() throws Exception;

    public int getChannelID() {
        if (mPaymentInfoHelper.isWithDrawTrans()) {
            return BuildConfig.channel_zalopay;
        } else {
            MiniPmcTransType miniPmcTransType = getConfig();
            if (miniPmcTransType != null) {
                return miniPmcTransType.pmcid;
            }
        }
        return -1;
    }

    public boolean isInputStep() {
        return false;
    }

    public boolean isCaptchaStep() {
        return false;
    }

    public boolean shouldFocusAfterCloseQuitDialog() {
        return isCaptchaStep() || isOtpStep();
    }

    public boolean isOtpStep() {
        return false;
    }

    public boolean isCanEditCardInfo() {
        return mCanEditCardInfo;
    }

    public void setCanEditCardInfo(boolean pCanEditCardInfo) {
        mCanEditCardInfo = pCanEditCardInfo;
    }

    public String getPageName() {
        return (mPageName != null) ? mPageName : "";
    }

    public boolean isBalanceErrorPharse() {
        return getPageName().equals(PAGE_BALANCE_ERROR);
    }

    public boolean isAuthenPayerPharse() {
        return getPageName().equals(PAGE_AUTHEN);
    }

    public boolean isFailProcessingPharse() {
        return getPageName().equals(PAGE_FAIL_PROCESSING);
    }

    public boolean isFailNetworkingPharse() {
        return getPageName().equals(PAGE_FAIL_NETWORKING);
    }

    public void onFinish() {
        Timber.d("onFinish - release gui processor - release pmc config");
        if (getGuiProcessor() != null) {
            getGuiProcessor().dispose();
            mGuiProcessor = null;
        }
        mMiniPmcTransType = null;
        mPresenter = null;
    }

    public void detectCard(String pCardNumber) {

    }

    protected void initWebView(String pRedirectUrl) {

    }

    protected void endingCountTimeLoadCaptchaOtp() {
        if (mCaptchaEndTime == 0)
            mCaptchaBeginTime = System.currentTimeMillis();
        if (mOtpEndTime == 0)
            mOtpBeginTime = System.currentTimeMillis();
    }

    public boolean isLoadWebTimeout() {
        return isLoadWebTimeout;
    }

    public void setLoadWebTimeout(boolean loadWebTimeout) {
        isLoadWebTimeout = loadWebTimeout;
    }

    @BankFlow
    public int getECardFlowType() {
        return mECardFlowType;
    }

    public void setECardFlowType(@BankFlow int mECardFlowType) {
        this.mECardFlowType = mECardFlowType;
    }

    public boolean isCardFlowWeb() {
        return isParseWebFlow() || isLoadWeb();
    }

    public boolean isParseWebFlow() {
        return getECardFlowType() == BankFlow.PARSEWEB;
    }

    public boolean isLoadWeb() {
        return getECardFlowType() == BankFlow.LOADWEB;
    }

    public boolean isCardFlow() {
        return isATMFlow() || isCCFlow();
    }

    public boolean isATMFlow() {
        return this instanceof AdapterBankCard;
    }

    public boolean isLinkAccFlow() {
        return this instanceof AdapterLinkAcc;
    }

    public boolean isCCFlow() {
        return this instanceof AdapterCreditCard;
    }

    public boolean isZaloPayFlow() {
        return this instanceof AdapterZaloPay;
    }

    public void tranferPaymentCardToMapCard() {
        mMapCard = new MapCard(mCard);
    }

    public boolean isOrderSubmit() {
        return mIsOrderSubmit;
    }

    public boolean isNeedToSwitchChannel() {
        return mNeedToSwitchChannel;
    }

    public void setNeedToSwitchChannel(boolean pNeedToSwitchChannel) {
        this.mNeedToSwitchChannel = pNeedToSwitchChannel;
    }

    public ChannelFragment getView() throws Exception {
        if (mPresenter == null || mPresenter.get() == null) {
            throw new IllegalStateException("invalid presenter");
        }
        return mPresenter.get().getViewOrThrow();
    }

    public ChannelActivity getActivity() throws Exception {
        try {
            return (ChannelActivity) getPresenter().getViewOrThrow().getActivity();
        } catch (Exception e) {
            throw new IllegalStateException("activity host invalid");
        }
    }

    public ChannelPresenter getPresenter() throws Exception {
        if (mPresenter == null || mPresenter.get() == null) {
            throw new IllegalStateException("invalid presenter");
        }
        return mPresenter.get();
    }

    public CardGuiProcessor getGuiProcessor() {
        return mGuiProcessor;
    }

    /***
     * submit order to server
     */
    protected void startSubmitTransaction() {
        processingOrder = true;
        mIsOrderSubmit = true;
        mCanEditCardInfo = false;
        try {
            getView().showLoading(GlobalData.getStringResource(RS.string.zpw_string_alert_submit_order));
            mTransactionAdapter.startTransaction();
        } catch (Exception e) {
            Log.e(this, e);
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
        }
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper
                    .step(ZPPaymentSteps.OrderStep_SubmitTrans)
                    .track();
        }
    }

    public boolean needToSwitchChannel() {
        return mNeedToSwitchChannel;
    }

    public void resetNeedToSwitchChannel() {
        mNeedToSwitchChannel = false;
    }

    public boolean isFinalStep() {
        return !getPageName().equals(SCREEN_ATM)
                && !getPageName().equals(SCREEN_CC)
                && !getPageName().equals(PAGE_SUCCESS)
                && !getPageName().equals(PAGE_FAIL)
                && !getPageName().equals(PAGE_FAIL_NETWORKING)
                && !getPageName().equals(PAGE_FAIL_PROCESSING);
    }

    protected void processWrongOtp() {
        numberRetryOtp++;
        //over number of retry
        if (numberRetryOtp > Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry))) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_alert_over_retry_otp));
            return;
        }
        showDialogWithCallBack(mResponseStatus.returnmessage, GlobalData.getStringResource(RS.string.dialog_close_button), () -> {
            //reset otp and show keyboard again
            if (isCardFlow()) {
                ((BankCardGuiProcessor) getGuiProcessor()).resetOtpWeb();
                getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getOtpAuthenPayerEditText());
            }
        });
    }

    public void autoFillOtp(String pSender, String pOtp) {
    }

    protected boolean shouldCheckStatusAgain() {
        return mResponseStatus == null && ConnectionUtil.isOnline(GlobalData.getAppContext()) && hasTransId();
    }

    protected boolean isOrderProcessing() {
        return mResponseStatus != null && mResponseStatus.isprocessing;
    }

    @CallSuper
    public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
        processingOrder = false;
        try {
            /***
             * networking error
             * 1.offline
             * 2.not stable
             */
            AbstractOrder order = mPaymentInfoHelper.getOrder();
            if (pAdditionParams == null || pAdditionParams.length == 0 || (pAdditionParams.length >= 1 && pAdditionParams[0] == null)) {
                getView().hideLoading();
                //offline
                if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
                    processNetworkingOffAfterSubmitTransaction();
                    return pAdditionParams;
                }
                if (isAlreadyCheckStatusFailSubmit) {
                    try {
                        showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
                    } catch (Exception e) {
                        Log.e(this, e);
                        terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
                    }
                    return pAdditionParams;
                }
                if (shouldCheckTransactionStatusByClientId() && order != null) {
                    checkTransactionStatusAfterSubmitFail(true, order.apptransid, GlobalData.getStringResource(RS.string.zingpaysdk_alert_checking));
                    return pAdditionParams;
                }
                mResponseStatus = null;
            }

            try {
                if (pAdditionParams[0] instanceof StatusResponse) {
                    mResponseStatus = (StatusResponse) pAdditionParams[0];
                }
            } catch (Exception e) {
                Timber.d(e != null ? e.getMessage() : "Exception");
            }

            //server is maintenance
            if (PaymentStatusHelper.isServerInMaintenance(mResponseStatus)) {
                getView().showMaintenanceServiceDialog(mResponseStatus.returnmessage);
                return null;
            }
            //callback finish transation from webview
            else if (pEventType == EEventType.ON_PAYMENT_RESULT_BROWSER) {
                //ending timer loading site
                mOtpEndTime = System.currentTimeMillis();
                mCaptchaEndTime = System.currentTimeMillis();
                getTransactionStatus(mTransactionID, false, GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
            }
            //callback load site error from webview
            //need to get status again if use submit otp or cc flow
            else if (pEventType == EEventType.ON_LOADSITE_ERROR || pEventType == EEventType.ON_BACK_WHEN_LOADSITE) {
                if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
                    showTransactionFailView(GlobalData.getAppContext().getString(R.string.zingpaysdk_alert_network_error));
                    return pAdditionParams;
                }
                //ending timer loading site
                mOtpEndTime = System.currentTimeMillis();
                mCaptchaEndTime = System.currentTimeMillis();

                WebViewHelper webViewError = null;
                if (pAdditionParams[0] instanceof WebViewHelper) {
                    webViewError = (WebViewHelper) pAdditionParams[0];
                }

                if (webViewError != null && webViewError.code == WebViewHelper.SSL_ERROR) {
                    showTransactionFailView(webViewError.getFriendlyMessage());
                    return null;
                }

                if (isCCFlow() || (isATMFlow() && ((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing())) {
                    isLoadWebTimeout = true;
                    getTransactionStatus(mTransactionID, false, GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
                } else {
                    String mess = (webViewError != null) ? webViewError.getFriendlyMessage() : GlobalData.getStringResource(RS.string.zpw_string_error_friendlymessage_end_transaction);
                    showTransactionFailView(mess);
                }
            }
            //submit order response
            else if (pEventType == EEventType.ON_SUBMIT_ORDER_COMPLETED) {
                handleEventSubmitOrderCompleted();
            }
            //check status again if have issue while submitting order
            else if (pEventType == EEventType.ON_CHECK_STATUS_SUBMIT_COMPLETE) {
                if (mResponseStatus != null) {
                    mTransactionID = mResponseStatus.zptransid;
                }
                //order haven't submitted to server yet.
                //need to retry check 5 times to server
                if (PaymentStatusHelper.isTransactionNotSubmit(mResponseStatus)) {
                    try {
                        mCountCheckStatus++;
                        if (mCountCheckStatus == TRANS_STATUS_MAX_RETRY) {
                            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_order_not_submit));
                        } else if (order != null) {
                            //retry again
                            checkTransactionStatusAfterSubmitFail(false, order.apptransid, GlobalData.getStringResource(RS.string.zingpaysdk_alert_checking));
                        }
                    } catch (Exception e) {
                        Log.e(this, e);
                        terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
                    }

                    return null;
                }

                if (mResponseStatus != null && mResponseStatus.isprocessing) {
                    getTransactionStatus(mTransactionID, true, null);
                } else {
                    onCheckTransactionStatus(mResponseStatus);
                }
            }

            //mapcard submit response
            else if (pEventType == EEventType.ON_VERIFY_MAPCARD_COMPLETE) {
                //get transid after submit success
                handleEventSubmitOrderCompleted();
            }
            //get status after submit order or authen payer
            else if (pEventType == EEventType.ON_GET_STATUS_COMPLETE) {
                getView().visibleCardViewNavigateButton(false);
                getView().visibleSubmitButton(true);
                //error
                if (mResponseStatus == null) {
                    showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
                    return pAdditionParams;
                }
                //retry otp
                if (PaymentStatusHelper.isWrongOtpResponse(mResponseStatus)) {
                    processWrongOtp();
                    return pAdditionParams;
                }
                if (TransactionHelper.isSecurityFlow(mResponseStatus)) {
                    SecurityResponse dataResponse = GsonUtils.fromJsonString(mResponseStatus.data, SecurityResponse.class);
                    //flow 3ds (atm + cc)
                    if (PaymentStatusHelper.is3DSResponse(dataResponse)) {
                        //no link for parsing
                        if (TextUtils.isEmpty(dataResponse.redirecturl)) {
                            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_empty_creditcard_url));
                            sdkReportErrorOnPharse(Constants.STATUS_PHARSE, GsonUtils.toJsonString(mResponseStatus));
                            return null;
                        }
                        //flow cover parse web (vietinbank)
                        BankConfig bankConfig = null;
                        String bankCode = mPaymentInfoHelper.getMapBank() != null ? mPaymentInfoHelper.getMapBank().bankcode : null;
                        if (!TextUtils.isEmpty(bankCode)) {
                            bankConfig = SDKApplication.getApplicationComponent().bankListInteractor().getBankConfig(bankCode);
                        }
                        if (bankConfig == null && getGuiProcessor().getCardFinder() != null) {
                            bankConfig = getGuiProcessor().getCardFinder().getDetectBankConfig();
                        }
                        if (isCardFlow() && bankConfig != null && bankConfig.isParseWebsite()) {
                            setECardFlowType(BankFlow.PARSEWEB);
                            showLoadindTimeout(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_bank));
                            initWebView(dataResponse.redirecturl);
                            endingCountTimeLoadCaptchaOtp();
                        }
                        //flow load web 3ds of cc
                        else {
                            try {
                                setECardFlowType(BankFlow.LOADWEB);
                                getGuiProcessor().loadUrl(dataResponse.redirecturl);
                                getView().hideLoading();
                                //begin count timer loading site until finish transaction
                                mOtpBeginTime = System.currentTimeMillis();
                                mCaptchaBeginTime = System.currentTimeMillis();
                            } catch (Exception e) {
                                showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_error_data));
                                sdkReportErrorOnPharse(Constants.STATUS_PHARSE, e.getMessage());
                                Log.e(this, e);
                            }
                        }
                    }
                    //otp flow
                    else if (PaymentStatusHelper.isOtpResponse(dataResponse)) {
                        mPageName = PAGE_AUTHEN;
                        ((BankCardGuiProcessor) getGuiProcessor()).showOtpTokenView();
                        getView().hideLoading();
                        //request permission read/view sms on android 6.0+
                        if (((BankCardGuiProcessor) getGuiProcessor()).isOtpAuthenPayerProcessing()) {
                            requestReadOtpPermission();
                        }
                        getView().renderKeyBoard();
                        //testing broadcast otp viettinbak
                        /*
                        new Handler().postDelayed(new Runnable() {
							@Override
							public void run()
							{
								//String sender = "VietinBank";
								//String body = "123456 la OTP xac nhan cua dich vu THANH TOAN CHO MA XAC NHAN...Ma GD 161224724381";
								String sender = "Sacombank";
								String body = "123456 la ma xac thuc (OTP) giao dich truc tuyen, thoi gian 5 phut. Vui long KHONG cung cap OTP cho bat ki ai";
								//send otp to channel activity
								Intent messageIntent = new Intent();
								messageIntent.setAction(Constants.FILTER_ACTION_BANK_SMS_RECEIVER);
								messageIntent.putExtra(Constants.BANK_SMS_RECEIVER_SENDER, sender);
								messageIntent.putExtra(Constants.BANK_SMS_RECEIVER_BODY,body);
								LocalBroadcastManager.get(GlobalData.getAppContext()).sendBroadcast(messageIntent);
							}
						},5000);
						*/
                    } else {
                        showTransactionFailView(GlobalData.getStringResource(RS.string.sdk_undefine_error));
                    }
                } else {
                    if (isOrderProcessing()) {
                        askToRetryGetStatus(mTransactionID);
                    } else {
                        onCheckTransactionStatus(mResponseStatus);
                    }
                }
            } else if (pEventType == EEventType.ON_NOTIFY_TRANSACTION_FINISH) {
                handleEventNotifyTransactionFinish(pAdditionParams);
            } else if (pEventType == EEventType.ON_PROMOTION) {
                if (handleEventPromotion(pAdditionParams)) {
                    return pAdditionParams;
                }
            }

        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_process_error));
            sdkReportErrorOnPharse(Constants.UNDEFINE, e.getMessage());
            Log.e(this, e);
        }

        return pAdditionParams;
    }

    private void handleEventSubmitOrderCompleted() {
        if (mResponseStatus != null) {
            mTransactionID = mResponseStatus.zptransid;
        }
        if (isOrderProcessing()) {
            if (mPaymentInfoHelper.payByCardMap()) {
                detectCard(mPaymentInfoHelper.getMapBank().getFirstNumber());
            }
            try {
                getPresenter().startTransactionExpiredTimer();//start count timer for checking transaction is expired.
            } catch (Exception e) {
                Log.e(this, e);
            }
            getTransactionStatus(mTransactionID, true, null);//get status transaction
        } else {
            onCheckTransactionStatus(mResponseStatus);//check status
        }
    }

    private void handleEventNotifyTransactionFinish(Object[] pAdditionParams) {
        Timber.d("processing result payment from notification");
        if (isTransactionSuccess()) {
            Timber.d("transaction is finish, skipping process notification");
            return;
        }
        if (!isTransactionInProgress()) {
            Timber.d("transaction is ending, skipping process notification");
            return;
        }
        if (pAdditionParams == null || pAdditionParams.length <= 0) {
            Timber.d("stopping processing result payment from notification because of empty pAdditionParams");
            return;
        }

        long notificationType = -1;
        try {
            notificationType = Long.parseLong(String.valueOf(pAdditionParams[0]));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        if (!Constants.TRANSACTION_SUCCESS_NOTIFICATION_TYPES.contains(notificationType)) {
            Timber.d("notification type is not accepted for this kind of transaction");
            return;
        }
        try {
            String transId = String.valueOf(pAdditionParams[1]);
            if (!TextUtils.isEmpty(transId) && transId.equals(mTransactionID)) {
                ServiceManager.shareInstance().cancelRequest();//cancel current request
                GetStatus.cancelRetryTimer();//cancel timer retry get status
                DialogManager.closeAllDialog();//close dialog
                if (mResponseStatus != null) {
                    mResponseStatus.returncode = 1;
                    mResponseStatus.returnmessage = GlobalData.getStringResource(RS.string.payment_success_label);
                }
                /***
                 *  get time from notification
                 *  in tranferring money case
                 */
                if (mPaymentInfoHelper.isMoneyTranferTrans() && pAdditionParams.length >= 3) {
                    try {
                        Long paymentTime = Long.parseLong(pAdditionParams[2].toString());
                        mPaymentInfoHelper.getOrder().apptime = paymentTime;
                        Timber.d("update transaction time from notification");
                    } catch (Exception ex) {
                        Log.e(this, ex);
                    }
                }
                showTransactionSuccessView();
            } else {
                Timber.d("transId is null");
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }

    private boolean handleEventPromotion(Object[] pAdditionParams) {
        Timber.d("got promotion from notification");
        if (pAdditionParams == null || pAdditionParams.length <= 0) {
            Timber.d("stopping processing promotion from notification because of empty pAdditionParams");
            return true;
        }

        PromotionEvent promotionEvent = null;
        if (pAdditionParams[0] instanceof PromotionEvent) {
            promotionEvent = (PromotionEvent) pAdditionParams[0];
        }
        if (mPromotionBuilder != null) {
            Log.d(this, "promotion event is updated", promotionEvent);
            mPromotionBuilder.setPromotion(promotionEvent);
            return true;
        }
        if (promotionEvent == null) {
            Timber.d("stopping processing promotion from notification because promotion event is null");
            return true;
        }
        if (pAdditionParams.length >= 2 && pAdditionParams[1] instanceof IPromotionResult) {
            mPromotionResult = (IPromotionResult) pAdditionParams[1];
        }

        long transId = -1;
        if (!TextUtils.isEmpty(mTransactionID)) {
            try {
                transId = Long.parseLong(mTransactionID);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (transId == -1) {
            Timber.d("stopping processing promotion from notification because transid is not same");
            if (mPromotionResult != null) {
                mPromotionResult.onReceiverNotAvailable();//callback again to notify that sdk don't accept this notification
            }
            return true;
        }
        if (!isTransactionSuccess()) {
            Timber.d("transaction is not success, skipping process promotion notification");
            return true;
        }

        IResourceLoader resourceLoader = null;
        if (pAdditionParams.length >= 3 && pAdditionParams[2] instanceof IResourceLoader) {
            resourceLoader = (IResourceLoader) pAdditionParams[2];
        }


        View contentView = View.inflate(GlobalData.getAppContext(), vn.zalopay.promotion.R.layout.layout_promotion_cash_back, null);
        mPromotionBuilder = CashBackRender.getBuilder()
                .setPromotion(promotionEvent)
                .setView(contentView)
                .setResourceProvider(resourceLoader)
                .setInteractPromotion(new IInteractPromotion() {
                    @Override
                    public void onUserInteract(PromotionEvent pPromotionEvent) {
                        if (mPromotionResult != null) {
                            try {
                                mPromotionResult.onNavigateToAction(getActivity(), pPromotionEvent);
                            } catch (Exception e) {
                                Log.e(this, e);
                            }
                        }
                    }

                    @Override
                    public void onClose() {
                        mPromotionResult = null;
                        mPromotionBuilder.release();
                        mPromotionBuilder = null;
                    }
                });
        UIBottomSheetDialog bottomSheetDialog = null;
        try {
            bottomSheetDialog = new UIBottomSheetDialog(getActivity(), vn.zalopay.promotion.R.style.CoffeeDialog, mPromotionBuilder.build());
            bottomSheetDialog.show();
            bottomSheetDialog.setState(BottomSheetBehavior.STATE_EXPANDED);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    /***
     * check networking is on/off
     * if off then open dialog networking for requesting open network again
     * @return
     */
    public boolean openSettingNetworking() {
        boolean isNetworkingOpen = ConnectionUtil.isOnline(GlobalData.getAppContext());
        if (!isNetworkingOpen) {
            try {
                getView().hideLoading();
                getView().showOpenSettingNetwokingDialog(closeSettingNetworkingListener);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        return isNetworkingOpen;
    }

    private boolean shouldSendLogToServer() {
        Timber.d("captcha " + (mCaptchaEndTime - mCaptchaBeginTime) + " ms" + ", otp " + (mOtpEndTime - mOtpBeginTime) + " ms");
        return ((mCaptchaEndTime - mCaptchaBeginTime) >= 0) || ((mOtpEndTime - mOtpBeginTime) > 0);
    }

    protected void sendLogTransaction() {
        try {
            if (!shouldSendLogToServer()) {
                return;
            }
            BaseTask sendLogTask = new SendLogTask(mPaymentInfoHelper.getUserInfo(), getChannelID(), mTransactionID, mCaptchaBeginTime, mCaptchaEndTime, mOtpBeginTime, mOtpEndTime);
            sendLogTask.makeRequest();
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void onClickSubmission() {
        try {
            Log.d(this, "page name", getPageName());
            SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
            //fail transaction
            if (isTransactionFail()) {
                terminate(null, true);
            }
            //pay successfully
            else if (isTransactionSuccess()) {
                finishTransaction();
            } else {
                onProcessPhrase();
            }
        } catch (Exception ex) {
            terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
            Log.e(this, ex);
        }
    }

    public DPaymentCard getCard() {
        return mCard;
    }

    public boolean isFinalScreen() {
        return getPageName().equals(PAGE_FAIL) || getPageName().equals(PAGE_SUCCESS)
                || getPageName().equals(PAGE_FAIL_NETWORKING)
                || getPageName().equals(PAGE_FAIL_PROCESSING);
    }

    public boolean isTransactionFail() {
        return TransactionHelper.isTransFail(getPageName());
    }

    public boolean isTransactionSuccess() {
        return isPaymentSuccess() || isLinkAccSuccess();
    }

    public boolean isPaymentSuccess() {
        return getPageName().equals(PAGE_SUCCESS);
    }

    public boolean isLinkAccSuccess() {
        return false;
    }

    /***
     * after show network error dialog.
     * close sdk if user is submitted order
     */
    public void whetherQuitPaymentOffline() {
        boolean isNeedCloseSDK = isOrderSubmit() || isLinkAccFlow();
        if (isNeedCloseSDK && !ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            try {
                SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
            } catch (Exception e) {
                Log.e(this, e);
            }
            showTransactionFailView(GlobalData.getOfflineMessage(mPaymentInfoHelper));
        }
    }

    public boolean exitWithoutConfirm() {
        if (getPageName().equals(PAGE_SUCCESS) || getPageName().equals(PAGE_FAIL) ||
                getPageName().equals(PAGE_FAIL_NETWORKING) || getPageName().equals(PAGE_FAIL_PROCESSING)) {
            existTransWithoutConfirm = true;
        }
        return existTransWithoutConfirm;
    }

    /***
     * internet if offline,move to result screen
     */
    protected void processNetworkingOffAfterSubmitTransaction() {
        try {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction));
        } catch (Exception e) {
            Log.e(this, e);
            terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
        }
    }

    public boolean shouldCheckTransactionStatusByClientId() {
        return !hasTransId();
    }

    protected boolean hasTransId() {
        return !TextUtils.isEmpty(mTransactionID);
    }

    /**
     * Get transaction status
     *
     * @param pTransID   ZmpTransID
     * @param pCheckData Checkdata true or false
     * @param pMessage   message show on progressbar
     */
    protected void getTransactionStatus(String pTransID, boolean pCheckData, String pMessage) {
        existTransWithoutConfirm = false;
        processingOrder = true;
        isCheckDataInStatus = pCheckData;
        getStatusStrategy(pTransID, pCheckData, pMessage);
    }

    private void getStatusStrategy(String pTransID, boolean pCheckData, String pMessage) {
        try {
            getView().showLoading(TextUtils.isEmpty(pMessage) ? GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing) : pMessage);
            mTransactionAdapter.getTransactionStatus(pTransID, pCheckData, pMessage);
        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
            Log.e(this, e);
        }
    }

    protected void onCheckTransactionStatus(StatusResponse pStatusResponse) {
        try {
            if (pStatusResponse != null && pStatusResponse.returncode < 0) {
                mPaymentInfoHelper.updateTransactionResult(pStatusResponse.returncode);
            }
            //order still need to continue processing
            if (TransactionHelper.isOrderProcessing(pStatusResponse)) {
                askToRetryGetStatus(pStatusResponse.zptransid);
            }
            //transaction is success
            else if (TransactionHelper.isTransactionSuccess(pStatusResponse)) {
                showTransactionSuccessView();
            }
            //transaction is fail with message
            else if (pStatusResponse != null && !pStatusResponse.isprocessing && !TextUtils.isEmpty(pStatusResponse.returnmessage)) {
                showTransactionFailView(pStatusResponse.returnmessage);
            }
            //response is null
            else {
                showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
            }
            getView().hideLoading();
        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
            Log.e(this, e);
        }
    }

    /***
     * show fail screen
     *
     * @param pMessage
     */
    protected void showFailScreen(String pMessage) {
        String message = pMessage;
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }
        String appName = TransactionHelper.getAppNameByTranstype(GlobalData.getAppContext(), mPaymentInfoHelper.getTranstype());
        if (TextUtils.isEmpty(appName)) {
            AppInfo appInfo = getAppInfoCache(mPaymentInfoHelper.getAppId());
            appName = appInfo != null ? appInfo.appname : null;
        }
        try {
            String title = mPaymentInfoHelper.getFailTitleByTrans(GlobalData.getAppContext());
            boolean isLink = mPaymentInfoHelper.isLinkTrans();
            getView().renderFail(isLink, message, mTransactionID, mPaymentInfoHelper.getOrder(), appName, mResponseStatus, true, title);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void makeRequestCheckStatusAfterSubmitFail(String pAppTransID) {
        BaseTask getStatusTask = new CheckOrderStatusFailSubmit(this, pAppTransID);
        getStatusTask.makeRequest();
    }

    /***
     * networking occur an error on the way,
     * client haven't get response from server,need to check to server
     */
    protected void checkTransactionStatusAfterSubmitFail(boolean shouldDelay, final String pAppTransID, String pMessage) {
        try {
            isAlreadyCheckStatusFailSubmit = true;
            getView().showLoading(TextUtils.isEmpty(pMessage) ? GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing) : pMessage);
            if (shouldDelay) {
                //delay 1s before continue check
                new Handler().postDelayed(() -> {
                    Timber.d("continue check transtatus by client id after 1s - because response submit order is null");
                    makeRequestCheckStatusAfterSubmitFail(pAppTransID);
                }, 1000);
            } else {
                makeRequestCheckStatusAfterSubmitFail(pAppTransID);
            }

        } catch (Exception ex) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
            Log.e(this, ex);
        }
    }

    protected void finishTransaction() {
        try {
            getPresenter().setPaymentStatusAndCallback(PaymentStatus.SUCCESS);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private boolean processSaveCardOnResult() throws Exception {
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }
        //link card channel, server auto save card , client only save card to local cache without hit server
        if (mPaymentInfoHelper.isLinkTrans()) {
            try {
                if (mMapCard == null) {
                    tranferPaymentCardToMapCard();
                }
                saveMappedCardToLocal(mMapCard);
            } catch (Exception e) {
                Log.e(this, e);
            }
            getView().hideLoading();
            return false;
        }
        if (needReloadCardMapAfterPayment()) {
            reloadMapCard(false);
        } else {
            getView().hideLoading();
        }
        return true;
    }

    /***
     * if this is redpacket,then close sdk and callback to app
     *
     * @return
     */
    protected boolean processResultForRedPackage() {
        if (GlobalData.isRedPacketChannel(mPaymentInfoHelper.getAppId())) {
            if (needReloadCardMapAfterPayment()) {
                reloadMapCard(false);
            } else {
                onClickSubmission();
            }
            return true;
        }
        return false;
    }

    private AppInfo getAppInfoCache(long appId) {
        return SDKApplication.getApplicationComponent()
                .appInfoInteractor()
                .get(appId);
    }

    /***
     * show success view base
     */
    protected synchronized void showTransactionSuccessView() {
        //stop timer
        try {
            getPresenter().cancelTransactionExpiredTimer();
        } catch (Exception e) {
            Log.e(this, e);
        }
        //hide webview
        if (isCardFlow() && getGuiProcessor() != null) {
            getGuiProcessor().useWebView(false);
        }

        //notify to app to do some background task
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onPreComplete(true, mTransactionID, mPaymentInfoHelper.getAppTransId());
        }
        //if this is redpacket,then close sdk and callback to app
        if (processResultForRedPackage()) {
            finishTransaction();
            return;
        }
        showDialogOnChannelList = false;
        existTransWithoutConfirm = true;
        mPageName = PAGE_SUCCESS;
        try {
            getView().marginSubmitButtonTopSuccess(true);
            getView().renderByResource(mPageName);
        } catch (Exception e) {
            Log.e(this, e);
        }

        AppInfo appInfo = getAppInfoCache(mPaymentInfoHelper.getAppId());
        String appName = TransactionHelper.getAppNameByTranstype(GlobalData.getAppContext(), mPaymentInfoHelper.getTranstype());
        if (TextUtils.isEmpty(appName)) {
            appName = appInfo != null ? appInfo.appname : null;
        }
        try {
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            boolean isTransfer = mPaymentInfoHelper.isMoneyTranferTrans();
            UserInfo receiverInfo = mPaymentInfoHelper.getMoneyTransferReceiverInfo();
            String title = mPaymentInfoHelper.getSuccessTitleByTrans(GlobalData.getAppContext());
            boolean isLink = mPaymentInfoHelper.isLinkTrans();
            boolean hideAmount = isLink;
            getView().renderSuccess(isLink, mTransactionID, userInfo, mPaymentInfoHelper.getOrder(), appName, null, hideAmount, isTransfer, receiverInfo, title);
        } catch (Exception e) {
            Log.e(this, e);
        }
        try {
            processSaveCardOnResult();
        } catch (Exception e) {
            Log.e(this, e);
        }
        //update password fingerprint
        try {
            if (PayProxy.get().getAuthenActor() != null && PayProxy.get().getAuthenActor().updatePassword()) {
                getView().showToast(R.layout.layout_update_password_toast);
            }
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        PaymentSnackBar.getInstance().dismiss();
        try {
            SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        //save payment card for show on channe list later
        String paymentCard = getCard() != null ? getCard().getCardKey() : null;
        if (TextUtils.isEmpty(paymentCard)) {
            paymentCard = mPaymentInfoHelper.getMapBank() != null ? mPaymentInfoHelper.getMapBank().getKey() : null;
        }
        if (!TextUtils.isEmpty(paymentCard)) {
            SDKApplication.getApplicationComponent()
                    .bankListInteractor().setPaymentBank(mPaymentInfoHelper.getUserId(), paymentCard);
        } else {
            SDKApplication.getApplicationComponent()
                    .bankListInteractor().setPaymentBank(mPaymentInfoHelper.getUserId(), null);
        }

        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Success);

        if (mPaymentInfoHelper.getOrder() != null && mPaymentInfoHelper.getOrder().appid == 12) {
            new Handler().postDelayed(() -> {
                try {
                    getView().setTextSubmitBtn(getActivity().getString(R.string.sdk_button_show_info_txt));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 100);
        }
    }

    protected void trackingTransactionEvent(int pResult) {
        int returnCode = mResponseStatus != null ? mResponseStatus.returncode : -1;
        String bankCode = null;
        if (getGuiProcessor() != null) {
            bankCode = getGuiProcessor().getDetectedBankCode();
        }
        if(TextUtils.isEmpty(bankCode)){
            bankCode = "";
        }
        Long transId;
        try {
            transId = Long.parseLong(mTransactionID);
        }catch (Exception e){
            Timber.w(e.getMessage());
            transId = 0L;
        }
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper
                    .step(ZPPaymentSteps.OrderStep_OrderResult)
                    .transId(transId)
                    .bankCode(bankCode)
                    .server_result(returnCode)
                    .step_result(pResult)
                    .track();
        }
    }

    public boolean isTransactionProcessing(String pMessage) {
        return pMessage.equalsIgnoreCase(GlobalData.getAppContext().getString(GlobalData.getTransProcessingMessage(mPaymentInfoHelper.getTranstype())))
                || pMessage.equalsIgnoreCase(GlobalData.getAppContext().getString(R.string.zpw_string_transaction_expired))
                || pMessage.equals(GlobalData.getAppContext().getString(R.string.sdk_error_generic_submitorder));
    }

    public boolean isTransactionInProgress() {
        return mResponseStatus != null && mResponseStatus.isprocessing;
    }

    public synchronized void showTransactionFailView(String pMessage) {
        //stop timer
        try {
            getPresenter().cancelTransactionExpiredTimer();
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onPreComplete(false, mTransactionID, mPaymentInfoHelper.getAppTransId());
        }
        //hide webview
        if (getGuiProcessor() != null && (isCardFlow() || (mPaymentInfoHelper.isBankAccountTrans() && GlobalData.shouldNativeWebFlow()))) {
            getGuiProcessor().useWebView(false);
        }
        if (isTransactionProcessing(pMessage)) {
            mPageName = PAGE_FAIL_PROCESSING;
        } else if (isTransNetworkError(GlobalData.getAppContext(), pMessage)) {
            mPageName = PAGE_FAIL_NETWORKING;
            mPaymentInfoHelper.updateResultNetworkingError(pMessage); //update payment status to no internet to app know
        } else {
            mPageName = PAGE_FAIL;
        }
        int status = mPaymentInfoHelper.getStatus();
        if (status != PaymentStatus.TOKEN_EXPIRE && status != PaymentStatus.USER_LOCK) {
            mPaymentInfoHelper.setResult(mPageName.equals(PAGE_FAIL_PROCESSING) ? PaymentStatus.NON_STATE : PaymentStatus.FAILURE);
        }

        showDialogOnChannelList = false;
        existTransWithoutConfirm = true;
        try {
            getView().marginSubmitButtonTop(true);
            getView().renderByResource(mPageName);
        } catch (Exception e) {
            Log.e(this, e);
        }
        showFailScreen(pMessage);
        //send log captcha, otp
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }
        //send log
        try {
            sdkReportErrorOnTransactionFail();
        } catch (Exception e) {
            Log.e(this, e);
        }
        try {
            SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
            getView().hideLoading();
        } catch (Exception e) {
            Log.e(this, e);
        }
        PaymentSnackBar.getInstance().dismiss();
        reloadMapListOnResponseMessage(pMessage);
        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Fail);
    }

    private void reloadMapListOnResponseMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (!message.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_error_mess_exist_mapcard))) {
            return;
        }
        //clear checksum cardinfo
        try {
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
            reloadMapCard(false);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public void terminate(String pMessage, boolean pExitSDK) {
        try {
            //full of 2 activity
            if (pExitSDK) {
                getPresenter().setCallBack(Activity.RESULT_OK);
            } else if (getActivity() != null && !getActivity().isFinishing()) {
                Intent intent = new Intent();
                intent.putExtra(Constants.SHOW_DIALOG, showDialogOnChannelList);
                intent.putExtra(Constants.MESSAGE, pMessage);
                getActivity().setResult(Activity.RESULT_CANCELED, intent);
            }
            // one of 2 activty is destroyed
            else if (GlobalData.getPaymentListener() != null) {
                GlobalData.getPaymentListener().onComplete();
            }
            getActivity().finish();
        } catch (Exception e) {
            Log.e(this, e);
        }
        Timber.d("callback transaction");
    }

    protected void showDialogWithCallBack(String pMessage, String pButtonText, ZPWOnEventDialogListener pCallBack) {
        try {
            getView().hideLoading();
            getView().showInfoDialog(pMessage, pCallBack);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected void showDialog(String pMessage) {
        try {
            getView().hideLoading();
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (ErrorManager.needToTerminateTransaction(mPaymentInfoHelper.getStatus())) {
            terminate(pMessage, true);
            return;
        }
        if (!TextUtils.isEmpty(pMessage)) {
            pMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }
        try {
            getView().showInfoDialog(pMessage);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected void askToRetryGetStatus(final String pZmpTransID) throws Exception {
        try {
            getView().hideLoading();
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (isFinalScreen()) {
            Timber.d("user in fail screen - skip retry get status");
            return;
        }
        String message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_ask_to_retry);
        getView().showRetryDialog(message, new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                showTransactionFailView(GlobalData.getAppContext().getString(GlobalData.getTransProcessingMessage(mPaymentInfoHelper.getTranstype())));
            }

            @Override
            public void onOKEvent() {
                try {
                    getView().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
                } catch (Exception e) {
                    Log.e(this, e);
                }
                /***
                 * if bank bypass opt, no need to check data when get status
                 * if bank not by pass opt, need to check data to determinate 3ds or api.
                 */
                try {
                    mTransactionAdapter.getTransactionStatus(pZmpTransID, isCheckDataInStatus, null);
                } catch (Exception e) {
                    Log.e(this, e);
                    terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
                }
            }
        });
    }

    /***
     * link card
     * auto save map card to local storage
     * make sure that reset card info checksum
     * @param mapCard
     */
    protected void saveMappedCardToLocal(MapCard mapCard) throws Exception {
        try {
            Log.d(this, "save map card to storage", mapCard);
            String userId = mPaymentInfoHelper.getUserId();
            String mappedCardList = SharedPreferencesManager.getInstance().getMapCardKeyList(userId);
            if (TextUtils.isEmpty(mappedCardList)) {
                mappedCardList = mapCard.getKey();
            } else if (!mappedCardList.contains(mapCard.getKey())) {
                mappedCardList += (Constants.COMMA + mapCard.getKey());
            }
            SharedPreferencesManager.getInstance().setMap(userId, mapCard.getKey(), GsonUtils.toJsonString(mapCard));
            SharedPreferencesManager.getInstance().setMapCardList(userId, mappedCardList);
            mPaymentInfoHelper.setMapBank(mapCard);
            //clear checksum cardinfo
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.RESULT_PHARSE, ex.getMessage());
            throw ex;
        }
    }

    /***
     * reload map card list
     */
    protected void reloadMapCard(boolean showLoading) {
        if (showLoading) {
            try {
                getView().showLoading(GlobalData.getStringResource(RS.string.zpw_string_get_card_info_processing));
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        Subscription subscription = SDKApplication.getApplicationComponent()
                .linkInteractor()
                .getCards(userInfo.zalopay_userid, userInfo.accesstoken, false, appVersion)
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(loadCardSubscriber, loadCardException);
        try {
            getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected boolean needReloadCardMapAfterPayment() {
        if (isZaloPayFlow()) {
            return false;
        }
        if (mPaymentInfoHelper.isWithDrawTrans()) {
            return false;
        }
        if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
            return false;
        }
        return !existPaymentCardOnCache();
    }

    protected boolean existPaymentCardOnCache() {
        if (getGuiProcessor() == null) {
            Timber.d("getGuiProcessor() = null");
            return false;
        }
        String cardNumber = getGuiProcessor().getCardNumber();
        if (TextUtils.isEmpty(cardNumber) || cardNumber.length() <= 6) {
            return false;
        }
        String first6cardno = cardNumber.substring(0, 6);
        String last4cardno = cardNumber.substring(cardNumber.length() - 4);
        //get card on cache
        String strMappedCard = null;
        try {
            strMappedCard = SharedPreferencesManager.getInstance().getMap(mPaymentInfoHelper.getUserId(), first6cardno + last4cardno);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return !TextUtils.isEmpty(strMappedCard) && GsonUtils.fromJsonString(strMappedCard, MapCard.class) != null;
    }

    public void needLinkCardBeforePayment(String pBankCode) {
        //save card number to show again when user go to link card again
        try {
            if (getGuiProcessor().isCardLengthMatchIdentifier(getGuiProcessor().getCardNumber())) {
                SharedPreferencesManager.getInstance().setCachedCardNumber(getGuiProcessor().getCardNumber());
            }
            //callback bank code to app to know what bank user input
            BaseMap card = new MapCard();
            card.bankcode = pBankCode;
            mPaymentInfoHelper.setMapBank(card);
            getPresenter().setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINKCARD_AND_PAYMENT);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void sdkReportErrorOnPharse(int pPharse, String pMessage) {
        String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
        if (TextUtils.isEmpty(paymentError) || !ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            return;
        }
        paymentError = String.format(paymentError, pPharse, 200, pMessage);
        try {
            sdkReportError(SDKReportTask.TRANSACTION_FAIL, paymentError);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    public void sdkReportErrorOnTransactionFail() throws Exception {
        if (!PaymentPermission.allowSendLogOnTransactionFail() && !ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            return;
        }
        String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
        if (!TextUtils.isEmpty(paymentError)) {
            paymentError = String.format(paymentError, Constants.RESULT_PHARSE, 200, GsonUtils.toJsonString(mResponseStatus));
            sdkReportError(SDKReportTask.TRANSACTION_FAIL, paymentError);
        }
    }

    public void sdkReportError(int pErrorCode, String pMessage) {
        if (getGuiProcessor() == null || !ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            return;
        }
        try {
            String bankCode = getGuiProcessor().getDetectedBankCode();
            SDKReportTask.makeReportError(mPaymentInfoHelper.getUserInfo(), pErrorCode, mTransactionID, pMessage, bankCode);
        } catch (Exception ex) {
            Timber.d(ex.getMessage());
        }
    }

    /***
     * * get status 1 oneshot to check status again in load website is timeout
     * */
    private void getOneShotTransactionStatus() {
        isLoadWebTimeout = true;
        getStatusStrategy(mTransactionID, false, null);
    }
}