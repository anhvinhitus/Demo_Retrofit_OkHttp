package vn.com.zalopay.wallet.business.channel.base;

import android.app.DialogFragment;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.lang.ref.WeakReference;

import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.view.ChannelProxy;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.base.SecurityResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.WebViewError;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.business.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.constants.BankFlow;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransAuthenType;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.CheckOrderStatusFailSubmit;
import vn.com.zalopay.wallet.datasource.task.MapCardListTask;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.datasource.task.SendLogTask;
import vn.com.zalopay.wallet.datasource.task.TrustSDKReportTask;
import vn.com.zalopay.wallet.datasource.task.getstatus.GetStatus;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;
import vn.zalopay.promotion.CashBackRender;
import vn.zalopay.promotion.IBuilder;
import vn.zalopay.promotion.IInteractPromotion;
import vn.zalopay.promotion.IPromotionResult;
import vn.zalopay.promotion.IResourceLoader;
import vn.zalopay.promotion.PromotionEvent;

public abstract class AdapterBase {

    public static final String PAGE_SUCCESS = RS.layout.screen__success;

    public static final String PAGE_SUCCESS_SPECIAL = RS.layout.screen__success__special;

    public static final String PAGE_REQUIRE_PIN = RS.layout.screen__require_pin;

    public static final String PAGE_FAIL = RS.layout.screen__fail;

    public static final String PAGE_FAIL_NETWORKING = RS.layout.screen__fail_networking;

    public static final String PAGE_FAIL_PROCESSING = RS.layout.screen__fail_processing;

    public static final String PAGE_BALANCE_ERROR = RS.layout.screen__zalopay__balance_error;

    public static final String SCREEN_CC = RS.layout.screen__card;

    public static final String SCREEN_ATM = RS.layout.screen__card;

    public static final String PAGE_AUTHEN = RS.layout.screen__local__card__authen;

    public static final String PAGE_COVER_BANK_AUTHEN = RS.layout.screen__cover__bank__authen;

    public static final String PAGE_SELECTION_ACCOUNT_BANK = RS.layout.screen_selection_account_list;

    public static final String PAGE_CONFIRM = RS.layout.screen__confirm;
    //detect card info is mapped by logged user
    public static boolean existedMapCard = false;
    protected final DPaymentCard mCard;
    protected WeakReference<PaymentChannelActivity> mOwnerActivity = null;
    protected CardGuiProcessor mGuiProcessor = null;
    protected DialogFragment mFingerPrintDialog = null;
    protected StatusResponse mResponseStatus;
    protected boolean isLoadWebTimeout = false;
    protected int numberRetryOtp = 0;
    protected MapCard mMapCard;
    protected String mTransactionID;
    protected String mPageCode;
    protected boolean mIsSuccess = false;
    protected boolean mIsExitWithoutConfirm = true;
    //prevent duplicate many time
    protected boolean isAlreadyCheckStatusFailSubmit = false;
    //count of wrong pin time
    protected int mPinInputCount = 0;
    //count of retry check status if submit order fail
    protected int mCountCheckStatus = 0;
    //check data in response get status api
    protected boolean isCheckDataInStatus = false;
    //submit log load website to server
    protected long mCaptchaBeginTime = 0, mCaptchaEndTime = 0;
    protected long mOtpBeginTime = 0, mOtpEndTime = 0;
    //whether show dialog or not?
    protected boolean mIsShowDialog = true;
    private final View.OnClickListener onSupportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().showSupportView(mTransactionID);
        }
    };
    //need to switch to cc or atm
    protected boolean mNeedToSwitchChannel = false;
    protected boolean mIsOrderSubmit = false;
    protected boolean mCanEditCardInfo = false;
    protected String mLayoutId = null;
    @BankFlow
    protected int mECardFlowType;
    protected boolean preventRetryLoadMapCardList = false;
    protected MiniPmcTransType mMiniPmcTransType;
    protected IBuilder mPromotionBuilder;
    protected IPromotionResult mPromotionResult;
    protected PaymentInfoHelper mPaymentInfoHelper;
    //prevent click duplicate
    private boolean mMoreClick = true;
    private String olderPassword = null;
    private final IFPCallback mFingerPrintCallback = new IFPCallback() {
        @Override
        public void onError(FPError pError) {
            dismissDialogFingerPrint();
            showDialogWithCallBack(GlobalData.getStringResource(RS.string.zpw_error_authen_pin),
                    GlobalData.getStringResource(RS.string.dialog_continue_button), AdapterBase.this::moveToRequirePin);
        }

        @Override
        public void onCancel() {
            //user cancel authen payment by fingerprint
            if (!isFinalScreen()) {
                mPageCode = PAGE_CONFIRM;
            }
        }

        @Override
        public void onComplete(String pHashPin) {
            dismissDialogFingerPrint();
            if (isFinalScreen()) {
                return;
            }
            //user don't setting use fingerprint for payment
            if (TextUtils.isEmpty(pHashPin)) {
                moveToRequirePin();
                return;
            }
            olderPassword = pHashPin;
            GlobalData.setTransactionPin(pHashPin);
            startSubmitTransaction();
        }
    };
    private final View.OnClickListener onUpdateInfoClickListener = v -> {
        mPaymentInfoHelper.setResult(PaymentStatus.LEVEL_UPGRADE_CMND_EMAIL);
        onClickSubmission();
    };
    private final View.OnClickListener okClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(this, "===okClickListener===starting...click");
            if (mMoreClick) {
                mMoreClick = false;
                AdapterBase.this.onClickSubmission();
                Log.d(this, "===okClickListener===onClickSubmission");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMoreClick = true;
                        Log.d(this, "===okClickListener===release click event");
                    }
                }, 3000);
            }
        }
    };

    public AdapterBase(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType, PaymentInfoHelper paymentInfoHelper) {
        mOwnerActivity = new WeakReference<>(pOwnerActivity);
        mMiniPmcTransType = pMiniPmcTransType;
        mCard = new DPaymentCard();
        mPaymentInfoHelper = paymentInfoHelper;
    }

    public PaymentInfoHelper getPaymentInfoHelper() {
        return mPaymentInfoHelper;
    }

    public void setMiniPmcTransType(MiniPmcTransType mMiniPmcTransType) {
        this.mMiniPmcTransType = mMiniPmcTransType;
    }

    public void requestReadOtpPermission() {
        getActivity().requestPermission(getActivity().getApplicationContext());//request permission read/view sms on android 6.0+
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

    public abstract void init() throws Exception;

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
        return (mPageCode != null) ? mPageCode : "";
    }

    public String getLayoutID() {
        return mLayoutId;
    }

    public String getChannelName() {
        MiniPmcTransType miniPmcTransType = getConfig();
        return miniPmcTransType != null ? miniPmcTransType.pmcname : getClass().getSimpleName();
    }

    public boolean isRequirePinPharse() {
        return getPageName().equals(PAGE_REQUIRE_PIN);
    }

    public boolean isBalanceErrorPharse() {
        return getPageName().equals(PAGE_BALANCE_ERROR);
    }

    public boolean isAuthenPayerPharse() {
        return getPageName().equals(PAGE_AUTHEN);
    }

    public boolean isConfirmTransactionPharse() {
        return getPageName().equals(PAGE_CONFIRM);
    }

    public boolean isFailProcessingPharse() {
        return getPageName().equals(PAGE_FAIL_PROCESSING);
    }

    public boolean isFailNetworkingPharse() {
        return getPageName().equals(PAGE_FAIL_NETWORKING);
    }

    public void onFinish() {
        Log.d(this, "onFinish");
        if (getGuiProcessor() != null) {
            getGuiProcessor().dispose();
            mGuiProcessor = null;
        }
        Log.d(this, "start dismiss dialog fingerprint - release pmc config");
        dismissDialogFingerPrint();
        mMiniPmcTransType = null;
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

    public PaymentChannelActivity getActivity() {
        if (mOwnerActivity.get() == null && BasePaymentActivity.getCurrentActivity() instanceof PaymentChannelActivity) {
            mOwnerActivity = new WeakReference<>((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity());
        }
        if (mOwnerActivity != null && mOwnerActivity.get() != null) {
            return mOwnerActivity.get();
        } else {
            Log.d(this, "mOwnerActivity is null");
            terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
            return null;
        }
    }

    public CardGuiProcessor getGuiProcessor() {
        return mGuiProcessor;
    }

    /***
     * submit order to server
     */
    protected void startSubmitTransaction() {
        getActivity().processingOrder = true;
        mIsOrderSubmit = true;
        mCanEditCardInfo = false;
        BasePaymentActivity.resetAttributeCascade(false);
        showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_alert_submit_order));
        try {
            SDKTransactionAdapter.shared()
                    .setAdapter(this)
                    .startTransaction();
        } catch (Exception e) {
            Log.e(this, e);
            terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
        }
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_SubmitTrans, ZPPaymentSteps.OrderStepResult_None, getChannelID());
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
                && !getPageName().equals(PAGE_SUCCESS) && !getPageName().equals(PAGE_SUCCESS_SPECIAL)
                && !getPageName().equals(PAGE_FAIL) && !getPageName().equals(PAGE_FAIL_NETWORKING) && !getPageName().equals(PAGE_FAIL_PROCESSING);
    }

    protected void processWrongOtp() {
        numberRetryOtp++;
        //over number of retry
        if (numberRetryOtp > Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry))) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_alert_over_retry_otp));
            return;
        }

        showDialogWithCallBack(mResponseStatus.getMessage(), GlobalData.getStringResource(RS.string.dialog_close_button), () -> {
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

    protected boolean isOrderProcessing(StatusResponse pStatus) {
        return pStatus != null && pStatus.isprocessing;
    }

    @CallSuper
    public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().processingOrder = false;
        }

        try {
            /***
             * networking error
             * 1.offline
             * 2.not stable
             */
            AbstractOrder order = mPaymentInfoHelper.getOrder();
            if (pAdditionParams == null || pAdditionParams.length == 0 || (pAdditionParams.length >= 1 && pAdditionParams[0] == null)) {
                showProgressBar(false, null);
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
                Log.d(this, e);
            }

            //server is maintenance
            if (PaymentStatusHelper.isServerInMaintenance(mResponseStatus)) {
                getActivity().showServerMaintenanceDialog(mResponseStatus.getMessage());
                return null;
            }
            //reload map card list
            if (pEventType == EEventType.ON_GET_CARDINFO_LIST_COMPLETE) {
                handleEventGetCardInfoListComplete(pAdditionParams[0]);
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
                //ending timer loading site
                mOtpEndTime = System.currentTimeMillis();
                mCaptchaEndTime = System.currentTimeMillis();

                WebViewError webViewError = null;
                if (pAdditionParams[0] instanceof WebViewError) {
                    webViewError = (WebViewError) pAdditionParams[0];
                }

                if (webViewError != null && webViewError.code == WebViewError.SSL_ERROR) {
                    showTransactionFailView(webViewError.getFriendlyMessage());
                    return null;
                }

                if (isCCFlow() || (isATMFlow() && ((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing())) {
                    getTransactionStatus(mTransactionID, false, GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
                } else if (webViewError != null) {
                    showTransactionFailView(webViewError.getFriendlyMessage());
                } else {
                    showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_error_friendlymessage_end_transaction));
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
                        if (mCountCheckStatus == Constants.GETSTATUS_CLIENT_COUNT) {
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
                releaseClickSubmit();

                getActivity().visibleCardViewNavigateButton(false);
                getActivity().visibleSubmitButton(true);

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

                if (mResponseStatus.isprocessing && !TextUtils.isEmpty(mResponseStatus.data)) {
                    SecurityResponse dataResponse = GsonUtils.fromJsonString(mResponseStatus.data, SecurityResponse.class);

                    if (dataResponse == null) {
                        showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
                        return pAdditionParams;
                    }

                    //flow 3ds (atm + cc)
                    if (PaymentStatusHelper.is3DSResponse(dataResponse)) {
                        //no link for parsing
                        if (TextUtils.isEmpty(dataResponse.redirecturl)) {
                            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_empty_creditcard_url));
                            sdkReportErrorOnPharse(Constants.STATUS_PHARSE, GsonUtils.toJsonString(mResponseStatus));
                            return null;
                        }
                        //flow cover parse web (vietinbank)
                        if (isCardFlow() && getGuiProcessor().getCardFinder().isDetected() && getGuiProcessor().getCardFinder().getDetectBankConfig() != null && getGuiProcessor().getCardFinder().getDetectBankConfig().isCoverBank()) {
                            setECardFlowType(BankFlow.PARSEWEB);
                            showProgressBar(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_bank));
                            initWebView(dataResponse.redirecturl);
                            endingCountTimeLoadCaptchaOtp();
                        }
                        //flow load web 3ds of cc
                        else {
                            try {
                                setECardFlowType(BankFlow.LOADWEB);
                                getGuiProcessor().loadUrl(dataResponse.redirecturl);
                                showProgressBar(false, null);
                                //begin count timer loading site until finish transaction
                                mOtpBeginTime = System.currentTimeMillis();
                                mCaptchaBeginTime = System.currentTimeMillis();
                            } catch (Exception e) {
                                showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_error_data));
                                sdkReportErrorOnPharse(Constants.STATUS_PHARSE, e != null ? e.getMessage() : GsonUtils.toJsonString(mResponseStatus));
                                Log.e(this, e);
                            }
                        }
                    }
                    //otp flow for atm
                    else if (isATMFlow() && PaymentStatusHelper.isOtpResponse(dataResponse)) {
                        mPageCode = PAGE_AUTHEN;
                        ((BankCardGuiProcessor) getGuiProcessor()).showOtpTokenView();

                        showProgressBar(false, null);

                        //request permission read/view sms on android 6.0+
                        if (((BankCardGuiProcessor) getGuiProcessor()).isOtpAuthenPayerProcessing()) {
                            requestReadOtpPermission();
                        }

                        if (getActivity().getActivityRender() != null) {
                            getActivity().getActivityRender().renderKeyBoard();
                        }

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
                        showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_error_system));
                    }
                } else {
                    if (isOrderProcessing()) {
                        askToRetryGetStatus(mTransactionID);
                    } else {
                        onCheckTransactionStatus(mResponseStatus);
                    }
                }
            } else if (pEventType == EEventType.ON_NOTIFY_TRANSACTION_FINISH) {
                if (handleEventNotifyTransactionFinish(pAdditionParams)) {
                    return pAdditionParams;
                }
            } else if (pEventType == EEventType.ON_PROMOTION) {
                if (handleEventPromotion(pAdditionParams)) {
                    return pAdditionParams;
                }
            }

        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_process_error));
            sdkReportErrorOnPharse(Constants.UNDEFINE, e != null ? e.getMessage() : GsonUtils.toJsonString(mResponseStatus));
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
            getActivity().startTransactionExpiredTimer();//start count timer for checking transaction is expired.
            getTransactionStatus(mTransactionID, true, null);//get status transaction
        } else {
            onCheckTransactionStatus(mResponseStatus);//check status
            releaseClickSubmit();//allow click button again
        }
    }

    private void handleEventGetCardInfoListComplete(Object pAdditionParam) {
        showProgressBar(false, null);

        try {
            CardInfoListResponse cardInfoListResponse = (CardInfoListResponse) pAdditionParam;

            if (cardInfoListResponse.returncode < 0 && !TextUtils.isEmpty(cardInfoListResponse.getMessage())) {
                getActivity().showInfoDialog(null, cardInfoListResponse.getMessage());
            } else {
                processCardInfoListResponse(cardInfoListResponse);
            }
        } catch (Exception ex) {
            if (getActivity() != null && isTransactionSuccess()) {
                getActivity().showInfoDialog(null, GlobalData.getStringResource(RS.string.zpw_string_save_card_error));
            }
            Log.e(this, ex);
        }
    }

    private boolean handleEventNotifyTransactionFinish(Object[] pAdditionParams) {
        Log.d(this, "processing result payment from notification");
        if (isTransactionSuccess()) {
            Log.d(this, "transaction is finish, skipping process notification");
            return true;
        }
        if (!isTransactionInProgress()) {
            Log.d(this, "transaction is ending, skipping process notification");
            return true;
        }
        if (pAdditionParams == null || pAdditionParams.length <= 0) {
            Log.d(this, "stopping processing result payment from notification because of empty pAdditionParams");
            return true;
        }

        int notificationType = -1;
        try {
            notificationType = Integer.parseInt(String.valueOf(pAdditionParams[0]));
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        if (!Constants.TRANSACTION_SUCCESS_NOTIFICATION_TYPES.contains(notificationType)) {
            Log.d(this, "notification type is not accepted for this kind of transaction");
            return true;
        }
        try {
            String transId = String.valueOf(pAdditionParams[1]);
            if (!TextUtils.isEmpty(transId) && transId.equals(mTransactionID)) {
                DataRepository.shareInstance().cancelRequest();//cancel current request
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
                        Log.d(this, "update transaction time from notification");
                    } catch (Exception ex) {
                        Log.e(this, ex);
                    }
                }
                showTransactionSuccessView();
            } else {
                Log.d(this, "transId is null");
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }
        return false;
    }

    private boolean handleEventPromotion(Object[] pAdditionParams) {
        Log.d(this, "got promotion from notification");
        if (pAdditionParams == null || pAdditionParams.length <= 0) {
            Log.d(this, "stopping processing promotion from notification because of empty pAdditionParams");
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
            Log.d(this, "stopping processing promotion from notification because promotion event is null");
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
            Log.d(this, "stopping processing promotion from notification because transid is not same");
            if (mPromotionResult != null) {
                mPromotionResult.onReceiverNotAvailable();//callback again to notify that sdk don't accept this notification
            }
            return true;
        }
        if (!isTransactionSuccess()) {
            Log.d(this, "transaction is not success, skipping process promotion notification");
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
                            mPromotionResult.onNavigateToAction(getActivity(), pPromotionEvent);
                        }
                    }

                    @Override
                    public void onClose() {
                        mPromotionResult = null;
                        mPromotionBuilder.release();
                        mPromotionBuilder = null;
                    }
                });
        UIBottomSheetDialog bottomSheetDialog = new UIBottomSheetDialog(getActivity(), vn.zalopay.promotion.R.style.CoffeeDialog, mPromotionBuilder.build());
        bottomSheetDialog.show();
        return false;
    }

    /***
     * check networking is on/off
     * if off then open dialog networking for requesting open network again
     * @return
     */
    public boolean checkNetworkingAndShowRequest() {
        boolean isNetworkingOpen = ConnectionUtil.isOnline(GlobalData.getAppContext());
        if (!isNetworkingOpen) {
            showProgressBar(false, null);
            processNetworkingOff();
        }
        return isNetworkingOpen;
    }

    private boolean shouldSendLogToServer() {
        Log.d("sendLogTransaction===time loading site", "captcha " + (mCaptchaEndTime - mCaptchaBeginTime) + " ms" + ", otp " + (mOtpEndTime - mOtpBeginTime) + " ms");

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
            Log.d(this, "page name " + getPageName());
            SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
            //fail transaction
            if (isTransactionFail()) {
                terminate(null, true);
                return;
            }
            //pay successfully
            if (isTransactionSuccess()) {
                finishTransaction(null);
                return;
            }
            //confirm transaction pharse
            if (isConfirmTransactionPharse() && needUserPasswordPayment()) {
                try {
                    if (PaymentFingerPrint.isDeviceSupportFingerPrint() && PaymentFingerPrint.isAllowFingerPrintFeature()) {
                        //ask user use finger print
                        //  if (PaymentFingerPrint.isDeviceSupportFingerPrint() && PaymentFingerPrint.isAllowFingerPrintFeature()) {
                        long start = System.currentTimeMillis();
                        Log.d(this, "===starting get dialog fingerprint===" + start);
                        mFingerPrintDialog = PaymentFingerPrint.shared().getDialogFingerprintAuthentication(getActivity(), mFingerPrintCallback);

                        if (mFingerPrintDialog != null) {
                            mPageCode = PAGE_REQUIRE_PIN;
                            if (getDialogFingerPrint() != null) {
                                getDialogFingerPrint().show(getActivity().getFragmentManager(), null);
                            } else {
                                moveToRequirePin();
                                Log.d(this, "mFingerPrintDialog=NULL");
                            }
                        } else {
                            moveToRequirePin();
                            Log.d(this, "use password instend of use fingerprint");
                        }
                    } else {
                        moveToRequirePin();
                    }
                } catch (Exception ex) {
                    moveToRequirePin();
                    Log.d(this, ex);
                }
            } else {
                onProcessPhrase();
            }
        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.UNDEFINE, GsonUtils.toJsonString(mResponseStatus) + (ex != null ? ex.getMessage() : "onClickSubmission"));
            if (mPaymentInfoHelper.isCardLinkTrans()) {
                terminate(null, true);
            } else {
                showDialog(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            }
            Log.e(this, ex);
        }
    }

    public DPaymentCard getCard() {
        return mCard;
    }

    /***
     * check whether this is the result screen
     *
     * @return
     */
    public boolean isFinalScreen() {
        return getPageName().equals(PAGE_FAIL) || getPageName().equals(PAGE_SUCCESS)
                || getPageName().equals(PAGE_SUCCESS_SPECIAL)
                || getPageName().equals(PAGE_FAIL_NETWORKING)
                || getPageName().equals(PAGE_FAIL_PROCESSING);
    }

    public boolean isTransactionFail() {
        return getPageName().equals(AdapterBase.PAGE_FAIL) || getPageName().equals(AdapterBase.PAGE_FAIL_NETWORKING)
                || getPageName().equals(AdapterBase.PAGE_FAIL_PROCESSING);
    }

    public boolean isTransactionSuccess() {
        return isPaymentSuccess() || isPaymentSpecialSuccess() || isLinkAccSuccess();
    }

    public boolean isPaymentSuccess() {
        return getPageName().equals(AdapterBase.PAGE_SUCCESS);
    }

    public boolean isPaymentSpecialSuccess() {
        return getPageName().equals(AdapterBase.PAGE_SUCCESS_SPECIAL);
    }

    public boolean isLinkAccSuccess() {
        return false;
    }

    /***
     * after show network error dialog.
     * close sdk if user is submitted order
     */
    public void closeSDKAfterNetworkOffline() {
        boolean isNeedCloseSDK = mOwnerActivity != null;

        if (isNeedCloseSDK) {
            isNeedCloseSDK = isNeedCloseSDK && (isOrderSubmit() || isLinkAccFlow());
        }

        if (isNeedCloseSDK && !ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
            showTransactionFailView(GlobalData.getOfflineMessage(mPaymentInfoHelper));
        }
    }

    public void moveToConfirmScreen(MiniPmcTransType pMiniPmcTransType) throws Exception {
        try {
            //add overswipe for rootview scrollview
            ScrollView scrollViewRoot = (ScrollView) getActivity().findViewById(R.id.zpw_scrollview_container);
            if (scrollViewRoot != null) {
                OverScrollDecoratorHelper.setUpOverScroll(scrollViewRoot);
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    protected void showConfrimScreenForCardChannel(MiniPmcTransType pMiniPmcTransType) throws
            Exception {
        try {
            getActivity().enableSubmitBtn(true);
            getActivity().showConfirmView(true, true, pMiniPmcTransType);
            setBankInfoConfirmView();

            getActivity().setToolBarTitle();
            if (mPaymentInfoHelper.payByCardMap()) {
                getGuiProcessor().getCardFinder().detectOnAsync(mPaymentInfoHelper.getMapBank().getFirstNumber(), isDetected -> {
                    if (isDetected) {
                        getActivity().setText(R.id.zpw_channel_label_textview, getGuiProcessor().getCardFinder().getBankName());
                    }
                });
            } else if (mPaymentInfoHelper.payByBankAccountMap()) {
                BankConfig bankConfig = BankLoader.getInstance().getBankByBankCode(mPaymentInfoHelper.getMapBank().bankcode);
                if (bankConfig != null) {
                    getActivity().setText(R.id.zpw_channel_label_textview, bankConfig.name);
                }
            }

        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.PAYMENT_INIT, ex != null ? ex.getMessage() : GsonUtils.toJsonString(mResponseStatus));
            throw ex;
        }
    }

    public void setListener() {
        //payment button
        getActivity().setOnClickListener(R.id.zpsdk_btn_submit, okClickListener);

        //continue button on success screen
        getActivity().setOnClickListener(R.id.zpw_rippleview_continue, okClickListener);

        //click support view
        getActivity().setOnClickListener(R.id.zpw_payment_fail_rl_support, onSupportClickListener);

        //click update info view
        getActivity().setOnClickListener(R.id.zpw_payment_fail_rl_update_info, onUpdateInfoClickListener);
    }

    public void showFee() {
        getActivity().showOrderFeeView();
    }

    public boolean exitWithoutConfirm() {
        if (getPageName().equals(PAGE_SUCCESS) || getPageName().equals(PAGE_SUCCESS_SPECIAL)
                || getPageName().equals(PAGE_FAIL) || getPageName().equals(PAGE_FAIL_NETWORKING) || getPageName().equals(PAGE_FAIL_PROCESSING)) {
            mIsExitWithoutConfirm = true;
        }

        return mIsExitWithoutConfirm;
    }

    protected void moveToRequirePin() {
        mPinInputCount++;
        // Normal: authen with pin
        mPageCode = PAGE_REQUIRE_PIN;
        getActivity().renderByResource();
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_title_require_pin_page));
        getActivity().configureRequirePinPage();
    }

    /***
     * networking is off
     * show dialog notify
     */
    public void processNetworkingOff() {
        getActivity().askToOpenSettingNetwoking();
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

    public boolean shouldGetOneShotTransactionStatus() {
        return hasTransId();
    }

    public boolean shouldCheckTransactionStatusByClientId() {
        return !hasTransId();
    }

    protected boolean hasTransId() {
        return !TextUtils.isEmpty(mTransactionID);
    }

    /***
     * get status 1 oneshot to check status again in load website is timeout
     */
    public void getOneShotTransactionStatus() {
        setLoadWebTimeout(true);
        getStatusStrategy(mTransactionID, false, null);
    }

    /**
     * Get transaction status
     *
     * @param pTransID   ZmpTransID
     * @param pCheckData Checkdata true or false
     * @param pMessage   message show on progressbar
     */
    protected void getTransactionStatus(String pTransID, boolean pCheckData, String pMessage) {
        mIsExitWithoutConfirm = false;
        getActivity().processingOrder = true;

        isCheckDataInStatus = pCheckData;

        getStatusStrategy(pTransID, pCheckData, pMessage);
    }

    /***
     * get status by channel
     *
     * @param pTransID
     * @param pCheckData
     * @param pMessage
     */
    private void getStatusStrategy(String pTransID, boolean pCheckData, String pMessage) {
        showProgressBar(true, TextUtils.isEmpty(pMessage) ? GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing) : pMessage);

        try {
            SDKTransactionAdapter.shared().getTransactionStatus(pTransID, pCheckData, pMessage);
        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
            Log.e(this, e);
        }
    }

    /***
     * check status in case networking error
     *
     * @param pAppTransID
     * @param pMessage
     */
    protected void checkTransactionStatus(String pAppTransID, String pMessage) {
        isAlreadyCheckStatusFailSubmit = true;
        showProgressBar(true, TextUtils.isEmpty(pMessage) ? GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing) : pMessage);
        BaseTask checkOrderStatusFailSubmit = new CheckOrderStatusFailSubmit(this, pAppTransID);
        checkOrderStatusFailSubmit.makeRequest();
    }

    /**
     * Check transaction status
     *
     * @param pStatusResponse data response
     */
    protected void onCheckTransactionStatus(StatusResponse pStatusResponse) {
        try {
            if (pStatusResponse != null && pStatusResponse.returncode < 0) {
                ErrorManager.updateTransactionResult(mPaymentInfoHelper, pStatusResponse.returncode);
            }
            //error pin
            if (pStatusResponse != null && pStatusResponse.returncode == Constants.PIN_WRONG_RETURN_CODE) {
                //limit number of submit pin
                Log.d(this, "===number of pin retry=" + mPinInputCount);
                //clear error on pin view and reset pin view
                if (mPinInputCount < Constants.MAX_COUNT_RETRY_PIN) {
                    mIsExitWithoutConfirm = false;

                    moveToRequirePin();
                    if (getActivity().getPaymentPassword() != null) {
                        getActivity().getPaymentPassword().setErrorPin(pStatusResponse.getMessage());
                    }

                } else {
                    showTransactionFailView(!TextUtils.isEmpty(pStatusResponse.getMessage()) ? pStatusResponse.getMessage() : GlobalData.getStringResource(RS.string.zpw_string_pin_wrong));
                }
            }
            //order still need to continue processing
            else if (isOrderProcessing(pStatusResponse)) {
                askToRetryGetStatus(pStatusResponse.zptransid);
            }
            //transaction is success
            else if (pStatusResponse != null && !pStatusResponse.isprocessing && pStatusResponse.returncode == 1) {
                showTransactionSuccessView();
            }
            //transaction is fail with message
            else if (pStatusResponse != null && !pStatusResponse.isprocessing && !TextUtils.isEmpty(pStatusResponse.getMessage())) {
                showTransactionFailView(pStatusResponse.getMessage());
            }
            //response is null
            else {
                try {
                    showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
                } catch (Exception e) {
                    Log.e(this, e);
                    terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
                }
            }
        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
            Log.e(this, e);
        }

        showProgressBar(false, null);
    }

    /***
     * show fail screen
     *
     * @param pMessage
     */
    protected void setContentForFailScreen(String pMessage) {
        String message = pMessage;

        if (TextUtils.isEmpty(message))
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);

        getActivity().showFailView(message, mTransactionID);
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
            showProgressBar(true, TextUtils.isEmpty(pMessage) ? GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing) : pMessage);

            if (shouldDelay) {
                //delay 1s before continue check
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(this, "===continue check transtatus by client id after 1s===because reponse submit order is null===");
                        makeRequestCheckStatusAfterSubmitFail(pAppTransID);
                    }
                }, 1000);
            } else {
                makeRequestCheckStatusAfterSubmitFail(pAppTransID);
            }

        } catch (Exception ex) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
            Log.e(this, ex);
        }
    }

    protected void finishTransaction(String pMessage) {
        mIsSuccess = true;
        if (pMessage == null || pMessage.length() == 0) {
            pMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_transaction_success);
        }
        mPaymentInfoHelper.setResult(PaymentStatus.SUCCESS);
        terminate(pMessage, false);
    }

    protected boolean needUserPasswordPayment() {
        Log.d(this, "start check require for using password");
        MiniPmcTransType pmcTransType = getConfig();
        Log.d(this, "pmc transype for checking require pin", pmcTransType);
        if (pmcTransType == null) {
            mIsShowDialog = true;
            terminate(GlobalData.getStringResource(RS.string.sdk_config_invalid), false);
            return false;
        }
        int transAuthenType = TransAuthenType.PIN;
        AbstractOrder order = mPaymentInfoHelper.getOrder();
        if (pmcTransType.isNeedToCheckTransactionAmount() && order != null && order.amount_total > pmcTransType.amountrequireotp) {
            transAuthenType = pmcTransType.overamounttype;
        } else if (pmcTransType.isNeedToCheckTransactionAmount() && order != null && order.amount_total < pmcTransType.amountrequireotp) {
            transAuthenType = pmcTransType.inamounttype;
        }
        return transAuthenType == TransAuthenType.PIN || transAuthenType == TransAuthenType.BOTH;
    }

    /**
     * set message in layout success
     */
    protected void setSuccessLabel() {
        String strMessage = GlobalData.getStringResource(RS.string.zpw_string_payment_success_label);
        if (mPaymentInfoHelper.isCardLinkTrans()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_linkcard_success_label);
        } else if (mPaymentInfoHelper.isTopupTrans()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_topup_success_label);
        } else if (mPaymentInfoHelper.isMoneyTranferTrans()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_tranfer_success_label);
        } else if (mPaymentInfoHelper.isWithDrawTrans()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_withdraw_success_label);
        } else if (GlobalData.isRedPacketChannel(mPaymentInfoHelper.getAppId())) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_lixi_success_label);
        }
        getActivity().setText(R.id.zpw_payment_success_textview, strMessage);
    }

    protected boolean isNeedToNotifyMapCardToApp(CardInfoListResponse pCardInfoResponse) {
        return MapCardHelper.isGetMapCardInfoSuccessAndHaveNewMapCard(pCardInfoResponse) && !GlobalData.isRedPacketChannel(mPaymentInfoHelper.getAppId());
    }

    protected void processCardInfoListResponse(CardInfoListResponse pCardInfoResponse) throws
            Exception {
        try {
            //get list map card successfully, callback info mapped card to app to show tutorial page
            if (isNeedToNotifyMapCardToApp(pCardInfoResponse)) {
                //get new map card to notify
                String cardKey = getCard().getCardKey();
                if (TextUtils.isEmpty(cardKey)) {
                    Log.d(this, "===processCardInfoListResponse===cardKey=NULL");
                    return;
                }
                MapCard mappedCard = null;
                for (MapCard card : pCardInfoResponse.cardinfos) {
                    if (card.getCardKey(mPaymentInfoHelper.getUserId()).equals(cardKey)) {
                        mappedCard = card.clone();
                        break;
                    }
                }
                if (mappedCard != null) {
                    MapCardHelper.notifyNewMapCardToApp(mPaymentInfoHelper, mappedCard);
                }
            }

            //this is redpacket channel
            //quit sdk right away
            if (GlobalData.isRedPacketChannel(mPaymentInfoHelper.getAppId())) {
                onClickSubmission();
                return;
            }

            //this card is mapped by another account
            if (pCardInfoResponse != null && PaymentStatusHelper.isCardMapByOtherAccount(pCardInfoResponse)) {
                Log.d(this, "this card is maped by other account");
                return;
            }
            if (PaymentStatusHelper.isErrorResponse(pCardInfoResponse)) {
                showDialog(pCardInfoResponse.getMessage());
            } else if (PaymentStatusHelper.isNetworkingErrorResponse(pCardInfoResponse)) {
                showDialog(GlobalData.getStringResource(RS.string.zpw_string_save_card_error));
            }
        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.RESULT_PHARSE, ex != null ? ex.getMessage() : GsonUtils.toJsonString(mResponseStatus));
            throw ex;
        }
    }

    protected void setFailLabel() {
        String strMessage = mPaymentInfoHelper.isCardLinkTrans() ? GlobalData.getStringResource(RS.string.zpw_string_payment_fail_linkcard) :
                GlobalData.getStringResource(RS.string.zpw_string_payment_fail_transaction);

        if (isFailProcessingPharse()) {
            strMessage = mPaymentInfoHelper.isCardLinkTrans() ? GlobalData.getStringResource(RS.string.zpw_string_linkcard_processing) :
                    GlobalData.getStringResource(RS.string.zpw_string_transaction_processing);
        } else if (isFailNetworkingPharse()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_transaction_networking_error);
        }

        getActivity().setText(R.id.zpw_payment_fail_textview, strMessage);
    }

    /***
     * have 2 type of success pay
     * each type have each type of interface
     */
    protected void getSuccessPageType() {
        AppInfo appInfo = AppInfoLoader.getAppInfo(mPaymentInfoHelper.getAppId());
        if (appInfo != null && appInfo.viewresulttype == 2) {
            mPageCode = PAGE_SUCCESS_SPECIAL;
        } else
            mPageCode = PAGE_SUCCESS;
    }

    protected boolean processSaveCardOnResult() {
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }

        //link card channel, server auto save card , client only save card to local cache withou hit server
        if (mPaymentInfoHelper.isCardLinkTrans()) {
            try {
                if (mMapCard == null) {
                    tranferPaymentCardToMapCard();
                }
                saveMappedCardToLocal(mMapCard);

            } catch (Exception e) {
                Log.e(this, e);
            }

            showProgressBar(false, null);

            return false;
        }

        if (isNeedToGetCardInfoListAfterPayment()) {
            reloadMapCardList(true);
        } else {
            showProgressBar(false, null);
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
            if (isNeedToGetCardInfoListAfterPayment()) {
                reloadMapCardList(true);
            } else {
                onClickSubmission();
            }

            return true;
        }

        return false;
    }

    /***
     * show success view base
     */
    protected synchronized void showTransactionSuccessView() {
        //stop timer
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().cancelTransactionExpiredTimer();
        }
        //call update new pin to app
        if (!TextUtils.isEmpty(olderPassword) && !TextUtils.isEmpty(GlobalData.getTransactionPin())
                && !olderPassword.equals(GlobalData.getTransactionPin())) {
            try {
                PaymentFingerPrint.shared().updatePassword(olderPassword, GlobalData.getTransactionPin());
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        //dismiss fingerprint dialog if still there
        dismissDialogFingerPrint();
        //hide webview
        if (isCardFlow() && getGuiProcessor() != null) {
            getGuiProcessor().useWebView(false);
        }

        //notify to app to do some background task
        try {
            if (GlobalData.getPaymentListener() != null)
                GlobalData.getPaymentListener().onPreComplete(true, mTransactionID, mPaymentInfoHelper.getAppTransId());
        } catch (Exception e) {
            Log.e(this, e);
        }

        getSuccessPageType();

        //if this is redpacket,then close sdk and callback to app
        if (processResultForRedPackage()) {
            return;
        }

        //show intro fingerprint dialog
        try {
            PaymentFingerPrint.shared().showSuggestionDialog(getActivity(), GlobalData.getTransactionPin());
        } catch (Exception e) {
            Log.e(this, e);
        }

        mIsShowDialog = false;
        mIsExitWithoutConfirm = true;

        getActivity().setMarginSubmitButtonTop(true);
        getActivity().renderByResource();
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_title_header_pay_result));
        getActivity().enableSubmitBtn(true);

        //dismiss snackbar networking
        PaymentSnackBar.getInstance().dismiss();

        if (isPaymentSuccess()) {
            try {
                getActivity().showPaymentSuccessContent(mTransactionID);
            } catch (Exception e) {
                Log.e(this, e);
            }

            setSuccessLabel();
        } else if (isPaymentSpecialSuccess()) {
            getActivity().showPaymentSpecialSuccessContent(mTransactionID);
        }
        SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
        processSaveCardOnResult();
        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Success);
    }

    protected void trackingTransactionEvent(int pResult) {
        int returnCode = mResponseStatus != null ? mResponseStatus.returncode : -1;
        String bankCode = null;
        if (getGuiProcessor() != null) {
            bankCode = getGuiProcessor().getDetectedBankCode();
        }
        if (mPaymentInfoHelper.isBankAccountTrans()) {
            bankCode = mPaymentInfoHelper.getLinkAccBankCode();
        }
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_OrderResult, pResult,
                    getChannelID(), mTransactionID, returnCode, 1, bankCode);
        }
    }

    public boolean isTransactionErrorNetworking(String pMessage) {
        return pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_alert_order_not_submit))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_link_account))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_alert_networking_off_in_unlink_account));
    }

    public boolean isTransactionProcessing(String pMessage) {
        return pMessage.equalsIgnoreCase(GlobalData.getStringResource(GlobalData.getTransProcessingMessage(mPaymentInfoHelper.getTranstype())))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_transaction_expired));
    }

    public boolean isTransactionInProgress() {
        return mResponseStatus != null && mResponseStatus.isprocessing;
    }

    public synchronized void showTransactionFailView(String pMessage) {
        if (preventRetryLoadMapCardList) {
            return;
        }
        //stop timer
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().cancelTransactionExpiredTimer();
        }
        //dismiss fingerprint dialog if still there
        dismissDialogFingerPrint();
        //hide webview
        if (isCardFlow() && getGuiProcessor() != null) {
            getGuiProcessor().useWebView(false);
        }
        //hide webview
        if (mPaymentInfoHelper.isBankAccountTrans() && GlobalData.shouldNativeWebFlow() && getGuiProcessor() != null) {
            getGuiProcessor().useWebView(false);
        }
        //notify to app to do some background task
        try {
            if (GlobalData.getPaymentListener() != null)
                GlobalData.getPaymentListener().onPreComplete(false, mTransactionID, mPaymentInfoHelper.getAppTransId());
        } catch (Exception e) {
            Log.e(this, e);
        }

        //keep error code TOKEN_EXPIRE to app known
        int status = mPaymentInfoHelper.getStatus();
        if (status != PaymentStatus.TOKEN_EXPIRE && status != PaymentStatus.USER_LOCK) {
            mPaymentInfoHelper.setResult(PaymentStatus.FAILURE);
        }

        if (isTransactionProcessing(pMessage)) {
            mPageCode = PAGE_FAIL_PROCESSING;
        } else if (isTransactionErrorNetworking(pMessage)) {
            mPageCode = PAGE_FAIL_NETWORKING;

            //update payment status to no internet to app know
            GlobalData.updateResultNetworkingError(mPaymentInfoHelper, pMessage);
        } else {
            mPageCode = PAGE_FAIL;
        }

        mIsShowDialog = false;
        mIsExitWithoutConfirm = true;
        getActivity().setMarginSubmitButtonTop(true);

        getActivity().renderByResource();
        getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_title_header_pay_result));
        getActivity().enableSubmitBtn(true);

        setFailLabel();

        PaymentSnackBar.getInstance().dismiss();

        setContentForFailScreen(pMessage);

        //send log captcha,otp
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }
        SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());

        if (ConnectionUtil.isOnline(GlobalData.getAppContext())
                && isNeedToGetCardInfoListAfterPayment()
                && needToReloadMapCardListOnTransactionFail(pMessage)
                && !shouldCheckTransactionStatusByClientId()) {
            preventRetryLoadMapCardList = true;
            reloadMapCardList(false);
        }
        showProgressBar(false, null);
        //send log
        try {
            sdkReportErrorOnTransactionFail();
        } catch (Exception e) {
            Log.e(this, e);
        }
        //tracking translogid on fail event
        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Fail);
    }

    /**
     * set bank info
     */
    public void setBankInfoConfirmView() {
        try {
            PaymentChannel channel = ChannelProxy.get().getChannel();
            if (channel != null) {
                getActivity().setImage(R.id.zpw_zalopay_logo_imageview, channel.channel_icon);
                getActivity().setVisible(R.id.linearlayout_price, false);
                getActivity().setVisible(R.id.zalopay_info_error, false);
                getActivity().setVisible(R.id.zpw_channel_layout, true);
                getActivity().setText(R.id.zpw_channel_name_textview, channel.pmcname);
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }

    public void terminate(String pMessage, boolean pExitSDK) {
        //full of 2 activity: payment gateway and payment channel
        if (pExitSDK && GlobalData.getChannelActivityCallBack() != null) {
            getActivity().recycleActivity();
        } else if (GlobalData.getChannelActivityCallBack() != null) {
            GlobalData.getChannelActivityCallBack().onCallBackAction(mIsShowDialog, pMessage);
            getActivity().finish();
        }
        // one of 2 activty is destroyed
        else if (BasePaymentActivity.getCurrentActivity() != null) {
            ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).recycleActivity();
        }
        Log.d(this, "terminate transaction");
    }

    protected void showDialogWithCallBack(String pMessage, String
            pButtonText, ZPWOnEventDialogListener pCallBack) {
        showProgressBar(false, null);

        getActivity().showInfoDialog(pCallBack, pMessage, pButtonText);
    }

    /**
     * Show đialog
     *
     * @param pMessage message in dialog
     */
    protected void showDialog(final String pMessage) {
        showProgressBar(false, null);

        if (ErrorManager.needToTerminateTransaction(mPaymentInfoHelper.getStatus())) {
            terminate(pMessage, false);
            return;
        }

        String message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        if (!TextUtils.isEmpty(pMessage))
            message = pMessage;

        final String finalMessage = message;

        String buttonText = GlobalData.getStringResource(RS.string.dialog_close_button);

        if (isRequirePinPharse()) {
            buttonText = GlobalData.getStringResource(RS.string.dialog_retry_button);
        }

        getActivity().showInfoDialog(() -> {
            //error networking. if user in pin input pharse,then need to try inputting again
            if (finalMessage.equals(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error)) && isRequirePinPharse()) {
                moveToRequirePin();
            }
        }, message, buttonText);
    }

    /**
     * Show dialog retry get transaction status
     *
     * @param pZmpTransID ZmpTransID
     */
    protected void askToRetryGetStatus(final String pZmpTransID) {
        showProgressBar(false, null);

        if (isFinalScreen()) {
            Log.d(this, "===askToRetryGetStatus===in final screen");
            return;
        }

        String message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_ask_to_retry);
        getActivity().showRetryDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                try {
                    showTransactionFailView(GlobalData.getStringResource(GlobalData.getTransProcessingMessage(mPaymentInfoHelper.getTranstype())));
                } catch (Exception e) {
                    Log.e(this, e);

                    terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
                }
            }

            @Override
            public void onOKevent() {
                showProgressBar(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
                /***
                 * if bank bypass opt, no need to check data when get status
                 * if bank not by pass opt, need to check data to determinate 3ds or api.
                 */
                try {
                    SDKTransactionAdapter.shared().getTransactionStatus(pZmpTransID, isCheckDataInStatus, null);
                } catch (Exception e) {
                    Log.e(this, e);

                    terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
                }
            }
        }, message);
    }

    public void releaseClickSubmit() {
        mMoreClick = true;
        Log.d(this, "release submit button");
    }

    public void showProgressBar(boolean pIsShow, String pStatusMessage) {
        if (getActivity() != null) {
            getActivity().showProgress(pIsShow, pStatusMessage);
        }
    }

    /**
     * Show confirm when exit pin screen
     */
    public void confirmExitTransWithoutPin() {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        String strButtton = GlobalData.getStringResource(RS.string.dialog_choose_again_button);

        boolean isQuitSDK = false;

        if (PaymentGatewayActivity.isUniqueChannel()) {
            strButtton = GlobalData.getStringResource(RS.string.dialog_cancel_button);
            isQuitSDK = true;
        }

        final boolean finalIsQuitSDK = isQuitSDK;

        getActivity().showNoticeDialog(new ZPWOnEventConfirmDialogListener() {
                                           @Override
                                           public void onCancelEvent() {
                                               mIsShowDialog = false;
                                               terminate(null, finalIsQuitSDK);
                                           }

                                           @Override
                                           public void onOKevent() {
                                               //resume keyboard again
                                               if (getActivity() != null && !getActivity().isFinishing()) {
                                                   getActivity().showKeyBoardForPin();
                                               }
                                           }
                                       }, GlobalData.getStringResource(RS.string.zpw_string_exit_without_pin), GlobalData.getStringResource(RS.string.dialog_continue_button),
                strButtton);
    }

    /***
     * if link card channel, use save new map card
     * to local without call api again
     * sure that set checksum = null
     *
     * @param pMappedCard
     */
    protected void saveMappedCardToLocal(MapCard pMappedCard) throws Exception {
        try {
            Log.d("saveMappedCardToLocal", "pMappedCard=" + pMappedCard);
            String userId = mPaymentInfoHelper.getUserId();
            String mappedCardList = SharedPreferencesManager.getInstance().getMapCardKeyList(userId);

            if (TextUtils.isEmpty(mappedCardList)) {
                mappedCardList = pMappedCard.getCardKey(userId);
            } else if (!mappedCardList.contains(pMappedCard.getCardKey(userId))) {
                mappedCardList += (Constants.COMMA + pMappedCard.getCardKey(userId));
            }
            SharedPreferencesManager.getInstance().setMapCard(userId, pMappedCard.getCardKey(userId), GsonUtils.toJsonString(pMappedCard));
            SharedPreferencesManager.getInstance().setMapCardList(userId, mappedCardList);
            mPaymentInfoHelper.setMapBank(pMappedCard);
            //clear checksum cardinfo
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.RESULT_PHARSE, ex != null ? ex.getMessage() : GsonUtils.toJsonString(mResponseStatus));
            throw ex;
        }
    }

    /***
     * reload map card list
     */
    protected void reloadMapCardList(boolean pIsShowProgress) {
        if (pIsShowProgress) {
            showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_get_card_info_processing));
        }
        BaseTask getCardInfoList = new MapCardListTask(this, mPaymentInfoHelper.getUserInfo());
        getCardInfoList.makeRequest();
    }

    /***
     * need to reload map card list if transaction has an error by networking
     * or marked as transaction is processing
     * @param pMessage
     * @return
     */
    protected boolean needToReloadMapCardListOnTransactionFail(String pMessage) {
        return isTransactionProcessing(pMessage) || isTransactionErrorNetworking(pMessage);
    }

    protected boolean isNeedToGetCardInfoListAfterPayment() {
        //this is zalopay channel
        if (isZaloPayFlow()) {
            AdapterBase.existedMapCard = false;
            return AdapterBase.existedMapCard;
        }
        //this is card channel
        AdapterBase.existedMapCard = mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap();
        if (!AdapterBase.existedMapCard) {
            try {
                AdapterBase.existedMapCard = isExistedCardNumberOnCache();
            } catch (Exception e) {
                Log.d(this, e);
            }
        }
        return !mPaymentInfoHelper.isWithDrawTrans() && !AdapterBase.existedMapCard;
    }

    /**
     * Check where this card is existed on cache
     *
     * @return
     */

    protected boolean isExistedCardNumberOnCache() throws Exception {
        if (getGuiProcessor() == null) {
            Log.d("isExistedCardNumberOnCache", "getGuiProcessor() = null");
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
            strMappedCard = SharedPreferencesManager.getInstance().getMapCardByKey(mPaymentInfoHelper.getUserId(), first6cardno + last4cardno);
        } catch (Exception e) {
            Log.d(this, e);
        }

        return !TextUtils.isEmpty(strMappedCard) && GsonUtils.fromJsonString(strMappedCard, MapCard.class) != null;
    }

    /**
     * show dialog confirm upgrade level
     */
    public void confirmUpgradeLevel() {
        mIsShowDialog = false;

        getActivity().showNoticeDialog(new ZPWOnEventConfirmDialogListener() {
                                           @Override
                                           public void onCancelEvent() {
                                               terminate(null, true);
                                           }

                                           @Override
                                           public void onOKevent() {
                                               //notify to app to show upgrade level interface
                                               mPaymentInfoHelper.setResult(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
                                               terminate(null, true);
                                           }
                                       }, GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update), GlobalData.getStringResource(RS.string.dialog_upgrade_button),
                GlobalData.getStringResource(RS.string.dialog_close_button));
    }

    public void needLinkCardBeforePayment(String pBankCode) {
        //save card number to show again when user go to link card again
        try {
            if (getGuiProcessor().isCardLengthMatchIdentifier(getGuiProcessor().getCardNumber())) {
                SharedPreferencesManager.getInstance().setCachedCardNumber(getGuiProcessor().getCardNumber());
            }
            //callback bank code to app to know what bank user input
            DBaseMap card = new MapCard();
            card.bankcode = pBankCode;
            mPaymentInfoHelper.setMapBank(card);
        } catch (Exception e) {
            Log.e(this, e);
        }
        mPaymentInfoHelper.setResult(PaymentStatus.DIRECT_LINKCARD_AND_PAYMENT);
        if (getActivity() != null) {
            getActivity().recycleActivity();
        }
    }

    public void sdkReportErrorOnPharse(int pPharse, String pMessage) {
        String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
        if (!TextUtils.isEmpty(paymentError)) {
            paymentError = String.format(paymentError, pPharse, 200, pMessage);
            try {
                sdkReportError(SDKReportTask.TRANSACTION_FAIL, paymentError);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public void sdkReportErrorOnTransactionFail() throws Exception {
        if (PaymentPermission.allowSendLogOnTransactionFail()) {
            String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
            if (!TextUtils.isEmpty(paymentError)) {
                paymentError = String.format(paymentError, Constants.RESULT_PHARSE, 200, GsonUtils.toJsonString(mResponseStatus));
                sdkReportError(SDKReportTask.TRANSACTION_FAIL, paymentError);
            }
        }
    }

    public void sdkReportError(int pErrorCode, String pMessage) throws Exception {
        String bankCode = null;
        try {
            if (getGuiProcessor() != null) {
                bankCode = getGuiProcessor().getDetectedBankCode();
            }
        } catch (Exception ex) {
            Log.d(this, ex);
        }
        SDKReportTask.makeReportError(mPaymentInfoHelper.getUserInfo(), pErrorCode, mTransactionID, pMessage, bankCode);
    }


    public void sdkReportError(int pErrorCode) throws Exception {
        String bankCode = null;
        try {
            if (getGuiProcessor() != null) {
                bankCode = getGuiProcessor().getDetectedBankCode();
            }
        } catch (Exception ex) {
            Log.d(this, ex);
        }
        SDKReportTask.makeReportError(mPaymentInfoHelper.getUserInfo(), pErrorCode, mTransactionID, mResponseStatus.toJsonString(), bankCode);
    }

    /***
     * send log to sever no check duplicate request
     * @param pErrorCode
     * @throws Exception
     */
    public void sdkTrustReportError(int pErrorCode) throws Exception {
        String bankCode = null;
        try {
            if (getGuiProcessor() != null) {
                bankCode = getGuiProcessor().getDetectedBankCode();
            }
        } catch (Exception ex) {
            Log.d(this, ex);
        }
        TrustSDKReportTask.makeTrustReportError(pErrorCode, mTransactionID, mResponseStatus.toJsonString(), bankCode);
    }

    protected DialogFragment getDialogFingerPrint() {
        return mFingerPrintDialog;
    }

    protected void dismissDialogFingerPrint() {
        if (getDialogFingerPrint() != null && !getDialogFingerPrint().isDetached()) {
            getDialogFingerPrint().dismiss();
            mFingerPrintDialog = null;
            Log.d(this, "dissmis dialog fingerprint");
        } else {
            Log.d(this, "===dismissDialogFingerPrint===getDialogFingerPrint()=NULL");
        }
    }
}