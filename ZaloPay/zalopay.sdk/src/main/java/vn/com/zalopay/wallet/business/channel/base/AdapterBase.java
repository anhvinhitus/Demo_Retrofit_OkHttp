package vn.com.zalopay.wallet.business.channel.base;

import android.app.DialogFragment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.behavior.view.ChannelStartProcessor;
import vn.com.zalopay.wallet.business.channel.creditcard.AdapterCreditCard;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.channel.zalopay.AdapterZaloPay;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.base.SecurityResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.WebViewError;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardFlowType;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;
import vn.com.zalopay.wallet.business.error.CErrorValidate;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.business.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.datasource.request.CheckOrderStatusFailSubmit;
import vn.com.zalopay.wallet.datasource.request.GetBankAccountList;
import vn.com.zalopay.wallet.datasource.request.GetMapCardInfoList;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.datasource.request.SendLog;
import vn.com.zalopay.wallet.datasource.request.TrustSDKReport;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.component.activity.PaymentGatewayActivity;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;

public abstract class AdapterBase {

    public static String SCREEN_LINK_ACC = RS.layout.screen__link__acc;

    public static String PAGE_SUCCESS = RS.layout.screen__success;

    public static String PAGE_SUCCESS_SPECIAL = RS.layout.screen__success__special;

    public static String PAGE_REQUIRE_PIN = RS.layout.screen__require_pin;

    public static String PAGE_FAIL = RS.layout.screen__fail;

    public static String PAGE_FAIL_NETWORKING = RS.layout.screen__fail_networking;

    public static String PAGE_FAIL_PROCESSING = RS.layout.screen__fail_processing;

    public static String PAGE_BALANCE_ERROR = RS.layout.screen__zalopay__balance_error;

    public static String SCREEN_CC = RS.layout.screen__card;

    public static String SCREEN_ATM = RS.layout.screen__card;

    public static String PAGE_AUTHEN = RS.layout.screen__local__card__authen;

    public static String PAGE_COVER_BANK_AUTHEN = RS.layout.screen__cover__bank__authen;

    public static String PAGE_SELECTION_ACCOUNT_BANK = RS.layout.screen_selection_account_list;

    public static String PAGE_CONFIRM = RS.layout.screen__confirm;

    // for vcb
    public static String PAGE_VCB_LOGIN = RS.layout.screen__vcb__login;
    public static String PAGE_VCB_CONFIRM_LINK = RS.layout.screen__vcb__confirm_link;
    public static String PAGE_VCB_OTP = RS.layout.screen_vcb_otp;
    public static String PAGE_VCB_CONFIRM_UNLINK = RS.layout.screen__vcb__confirm_unlink;
    public static String PAGE_LINKACC_SUCCESS = RS.layout.screen__linkacc__success;
    public static String PAGE_LINKACC_FAIL = RS.layout.screen__linkacc__fail;
    public static String PAGE_UNLINKACC_SUCCESS = RS.layout.screen__unlinkacc__success;
    public static String PAGE_UNLINKACC_FAIL = RS.layout.screen__unlinkacc__fail;
    //detect card info is mapped by logged user
    public static boolean existedMapCard = false;
    //payment config
    public DPaymentChannel mConfig;
    protected WeakReference<PaymentChannelActivity> mOwnerActivity = null;
    protected CardGuiProcessor mGuiProcessor = null;
    protected DialogFragment mFingerPrintDialog = null;
    protected StatusResponse mResponseStatus;
    protected ECardFlowType mECardFlowType;
    protected boolean isLoadWebTimeout = false;
    protected int numberRetryOtp = 0;
    protected DPaymentCard mCard;
    protected DMappedCard mMapCard;
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
    //need to switch to cc or atm
    protected boolean mNeedToSwitchChannel = false;
    protected boolean mIsOrderSubmit = false;
    protected boolean mCanEditCardInfo = false;
    protected String mLayoutId = null;
    /**
     * show Fail view
     *
     * @param pMessage
     */
    protected boolean preventRetryLoadMapCardList = false;
    //prevent click duplicate
    private boolean mMoreClick = true;
    private View.OnClickListener onSupportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().showSupportView(mTransactionID);
        }
    };
    private String olderPassword = null;
    private IFPCallback mFingerPrintCallback = new IFPCallback() {
        @Override
        public void onError(FPError pError) {
            dismissDialogFingerPrint();
            //has an error in authen fingerprint
            Log.d(this, "===onError===" + GsonUtils.toJsonString(pError));
            showDialogWithCallBack(GlobalData.getStringResource(RS.string.zpw_error_authen_pin),
                    GlobalData.getStringResource(RS.string.dialog_continue_button), new ZPWOnEventDialogListener() {
                        @Override
                        public void onOKevent() {
                            moveToRequirePin();
                        }
                    });
        }

        @Override
        public void onCancel() {
            //user cancel authen payment by fingerprint
            Log.d(this, "==onCancel===");
            if (!isFinalScreen()) {
                mPageCode = PAGE_CONFIRM;
            }
        }

        @Override
        public void onComplete(String pHashPin) {
            dismissDialogFingerPrint();
            Log.d(this, "===onComplete===pHashPin=" + pHashPin);

            if (isFinalScreen()) {
                Log.d(this, "===transaction is finish===not use fingerprint any more");
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
    private View.OnClickListener okClickListener = new View.OnClickListener() {
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

    public AdapterBase(PaymentChannelActivity pOwnerActivity) {
        if (pOwnerActivity != null) {
            mOwnerActivity = new WeakReference<PaymentChannelActivity>(pOwnerActivity);
        }

        try {
            mConfig = getChannelConfig();
        } catch (Exception e) {

            terminate(GlobalData.getStringResource(RS.string.zpw_alert_error_data), true);
            Log.e(this, e);
        }

        mCard = new DPaymentCard();
    }

    public abstract void init();

    public abstract DPaymentChannel getChannelConfig() throws Exception;

    public abstract void onProcessPhrase() throws Exception;

    public abstract String getChannelID();

    public boolean isInputStep() {
        return false;
    }

    public boolean isCaptchaStep() {
        return false;
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
        return mPageCode;
    }

    public String getLayoutID() {
        return mLayoutId;
    }

    public String getChannelName() {
        if (mConfig != null) {
            return mConfig.pmcname;
        }
        return getClass().getName();
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
        Log.d(this, "===onFinish===");
        if (getGuiProcessor() != null) {
            Log.d(this, "===onFinish===getGuiProcessor().dispose()");

            getGuiProcessor().dispose();
            mGuiProcessor = null;
        }
        dismissDialogFingerPrint();
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

    public String getTransactionID() {
        return mTransactionID;
    }

    public boolean isLoadWebTimeout() {
        return isLoadWebTimeout;
    }

    public void setLoadWebTimeout(boolean loadWebTimeout) {
        isLoadWebTimeout = loadWebTimeout;
    }

    public ECardFlowType getECardFlowType() {
        return mECardFlowType;
    }

    public void setECardFlowType(ECardFlowType mECardFlowType) {
        this.mECardFlowType = mECardFlowType;
    }

    public boolean isCardFlowWeb() {
        return isParseWebFlow() || isLoadWeb();
    }

    public boolean isParseWebFlow() {
        return getECardFlowType() == ECardFlowType.PARSEWEB;
    }

    public boolean isLoadWeb() {
        return getECardFlowType() == ECardFlowType.LOADWEB;
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
        mMapCard = new DMappedCard(mCard);
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
            mOwnerActivity = new WeakReference<PaymentChannelActivity>((PaymentChannelActivity) BasePaymentActivity.getCurrentActivity());
        }
        if (mOwnerActivity != null && mOwnerActivity.get() != null) {
            return mOwnerActivity.get();
        } else {
            Log.e(this, "mOwnerActivity is null");
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
    protected boolean startSubmitTransaction() {
        if (GlobalData.getPaymentInfo() == null || !GlobalData.getPaymentInfo().isPaymentInfoValid()) {
            Log.e(this, "===startSubmitTransaction===" + GsonUtils.toJsonString(GlobalData.getPaymentInfo()));

            if (GlobalData.getPaymentResult() != null) {
                GlobalData.setResultInvalidInput();
            }
            getActivity().onExit(GlobalData.getStringResource(RS.string.zpw_error_paymentinfo), true);
            return false;
        }

        getActivity().processingOrder = true;

        mIsOrderSubmit = true;

        mCanEditCardInfo = false;

        BasePaymentActivity.resetAttributeCascade(false);

        showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_alert_submit_order));

        try {
            SDKTransactionAdapter.shared().startTransaction(this);
        } catch (Exception e) {
            Log.e(this, e);
            terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
        }

        return mIsOrderSubmit;
    }

    public boolean needToSwitchChannel() {
        return mNeedToSwitchChannel;
    }

    public void resetNeedToSwitchChannel() {
        mNeedToSwitchChannel = false;
    }

    public boolean isFinalStep() {
        if (!getPageName().equals(SCREEN_ATM) && !getPageName().equals(SCREEN_CC) && !getPageName().equals(PAGE_SUCCESS) && !getPageName().equals(PAGE_SUCCESS_SPECIAL)
                && !getPageName().equals(PAGE_FAIL) && !getPageName().equals(PAGE_FAIL_NETWORKING) && !getPageName().equals(PAGE_FAIL_PROCESSING)
                && !getPageName().equals(SCREEN_LINK_ACC) && !getPageName().equals(PAGE_VCB_LOGIN) && !getPageName().equals(PAGE_VCB_CONFIRM_LINK)
                && !getPageName().equals(PAGE_LINKACC_SUCCESS) && !getPageName().equals(PAGE_LINKACC_FAIL)
                && !getPageName().equals(PAGE_UNLINKACC_SUCCESS) && !getPageName().equals(PAGE_UNLINKACC_FAIL)
                && !getPageName().equals(PAGE_VCB_OTP) && !getPageName().equals(PAGE_VCB_CONFIRM_UNLINK))
            return true;
        return false;
    }

    protected boolean processWrongOtp() {
        numberRetryOtp++;
        //over number of retry
        if (numberRetryOtp > Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry))) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_alert_over_retry_otp));
            return false;
        }

        showDialogWithCallBack(mResponseStatus.returnmessage, GlobalData.getStringResource(RS.string.dialog_close_button), new ZPWOnEventDialogListener() {

            @Override
            public void onOKevent() {
                //reset otp and show keyboard again
                if (isCardFlow()) {
                    ((BankCardGuiProcessor) getGuiProcessor()).resetOtpWeb();
                    getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getOtpAuthenPayerEditText());
                }
            }
        });

        return true;
    }

    /***
     * auto fill otp
     *
     * @param pSender
     * @param pOtp
     */
    public abstract void autoFillOtp(String pSender, String pOtp);

    protected boolean shouldCheckStatusAgain() {
        return mResponseStatus == null && ConnectionUtil.isOnline(GlobalData.getAppContext()) && hasTransId();
    }

    protected boolean isOrderProcessing() {
        return mResponseStatus != null && mResponseStatus.isprocessing;
    }

    protected boolean isOrderProcessing(StatusResponse pStatus) {
        return pStatus != null && pStatus.isprocessing;
    }

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

                if (shouldCheckTransactionStatusByClientId()) {
                    checkTransactionStatusAfterSubmitFail(true, GlobalData.getPaymentInfo().appTransID, GlobalData.getStringResource(RS.string.zingpaysdk_alert_checking));
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
                getActivity().showServerMaintenanceDialog(mResponseStatus.returnmessage);
                return null;
            }
            //reload map card list
            if (pEventType == EEventType.ON_GET_CARDINFO_LIST_COMPLETE) {
                showProgressBar(false, null);

                try {
                    CardInfoListResponse cardInfoListResponse = (CardInfoListResponse) pAdditionParams[0];

                    if (cardInfoListResponse.returncode < 0 && !TextUtils.isEmpty(cardInfoListResponse.returnmessage)) {
                        getActivity().showInfoDialog(null, cardInfoListResponse.returnmessage);
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
            //reload bank account list
            if (pEventType == EEventType.ON_GET_BANKACCOUNT_LIST_COMPLETE) {
                showProgressBar(false, null);
                try {
                    BankAccountListResponse cardInfoListResponse = (BankAccountListResponse) pAdditionParams[0];

                    if (cardInfoListResponse.returncode < 0 && !TextUtils.isEmpty(cardInfoListResponse.returnmessage)) {
                        getActivity().showInfoDialog(null, cardInfoListResponse.returnmessage);
                    } else if (BankAccountHelper.isNeedUpdateBankAccountInfoOnCache(cardInfoListResponse.bankaccountchecksum)) {
                        BankAccountHelper.updateBankAccountListOnCache(cardInfoListResponse.bankaccountchecksum, cardInfoListResponse.bankaccounts);
                    }
                } catch (Exception ex) {
                    if (getActivity() != null && isTransactionSuccess()) {
                        getActivity().showInfoDialog(null, GlobalData.getStringResource(RS.string.zpw_string_get_bank_account_error));
                    }
                    Log.e(this, ex);
                }
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
                if (mResponseStatus != null) {
                    mTransactionID = mResponseStatus.zptransid;
                }

                if (isOrderProcessing()) {
                    if (GlobalData.isMapCardChannel()) {
                        detectCard(GlobalData.getPaymentInfo().mapBank.getFirstNumber());
                    }
                    //start count timer for checking transaction is expired.
                    getActivity().startTransactionExpiredTimer();
                    getTransactionStatus(mTransactionID, true, null);
                } else {
                    onCheckTransactionStatus(mResponseStatus);
                    releaseClickSubmit();
                }
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
                        } else {
                            //retry again
                            checkTransactionStatusAfterSubmitFail(false, GlobalData.getPaymentInfo().appTransID, GlobalData.getStringResource(RS.string.zingpaysdk_alert_checking));
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
                if (mResponseStatus != null) {
                    mTransactionID = mResponseStatus.zptransid;
                }

                if (isOrderProcessing()) {
                    if (GlobalData.isMapCardChannel()) {
                        detectCard(GlobalData.getPaymentInfo().mapBank.getFirstNumber());
                    }

                    //start count timer for checking transaction is expired.
                    getActivity().startTransactionExpiredTimer();
                    //get status with checking existed of data response
                    getTransactionStatus(mTransactionID, true, null);
                } else {
                    onCheckTransactionStatus(mResponseStatus);
                    releaseClickSubmit();
                }
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
                            setECardFlowType(ECardFlowType.PARSEWEB);
                            showProgressBar(true, GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_bank));
                            initWebView(dataResponse.redirecturl);
                            endingCountTimeLoadCaptchaOtp();
                        }
                        //flow load web 3ds of cc
                        else {
                            try {
                                setECardFlowType(ECardFlowType.LOADWEB);
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
                            getActivity().requestPermission(getActivity().getApplicationContext());
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
								LocalBroadcastManager.getInstance(GlobalData.getAppContext()).sendBroadcast(messageIntent);
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
            }

        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_process_error));
            sdkReportErrorOnPharse(Constants.UNDEFINE, e != null ? e.getMessage() : GsonUtils.toJsonString(mResponseStatus));
            Log.e(this, e);
        }

        return pAdditionParams;
    }

    /***
     * check networking is on/off
     * if off then open dialog networking for requesting open network again
     *
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

            BaseRequest sendLogTask = new SendLog(mConfig.pmcid, mTransactionID, mCaptchaBeginTime, mCaptchaEndTime, mOtpBeginTime, mOtpEndTime);
            sendLogTask.makeRequest();
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void onClickSubmission() {
        try {
            mIsShowDialog = false;
            mIsExitWithoutConfirm = true;

            ZPWUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());

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
            if (isConfirmTransactionPharse()) {
                int iCheck = onRequirePin();

                if (iCheck == Constants.INPUT_INVALID) {
                    mIsShowDialog = true;
                    terminate(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), false);

                    return;
                } else if (iCheck == Constants.REQUIRE_PIN) {
                    try {
                        //ask user use finger print
                        if (PaymentFingerPrint.isDeviceSupportFingerPrint() && PaymentFingerPrint.isAllowFingerPrintFeature()) {
                            mPageCode = PAGE_REQUIRE_PIN;
                            long start = System.currentTimeMillis();
                            Log.d(this, "===starting get dialog fingerprint===" + start);
                            mFingerPrintDialog = PaymentFingerPrint.shared().getDialogFingerprintAuthentication(getActivity(), mFingerPrintCallback);
                            if (getDialogFingerPrint() != null) {
                                getDialogFingerPrint().show(getActivity().getFragmentManager(), null);
                            } else {
                                moveToRequirePin();
                                Log.d(this, "mFingerPrintDialog=NULL");
                            }
                        } else {
                            moveToRequirePin();
                        }
                    } catch (Exception ex) {
                        moveToRequirePin();
                        Log.d(this, ex);
                    }
                    return;
                }
            }

            onProcessPhrase();

        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.UNDEFINE, GsonUtils.toJsonString(mResponseStatus) + (ex != null ? ex.getMessage() : "onClickSubmission"));
            if (GlobalData.isLinkCardChannel()) {
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
        if (getPageName().equals(PAGE_FAIL) || getPageName().equals(PAGE_SUCCESS) || getPageName().equals(PAGE_SUCCESS_SPECIAL)
                || getPageName().equals(PAGE_FAIL_NETWORKING) || getPageName().equals(PAGE_FAIL_PROCESSING)
                || getPageName().equals(PAGE_LINKACC_SUCCESS) || getPageName().equals(PAGE_LINKACC_FAIL)
                || getPageName().equals(PAGE_UNLINKACC_SUCCESS) || getPageName().equals(PAGE_UNLINKACC_FAIL))
            return true;
        return false;
    }

    public boolean isTransactionFail() {
        return getPageName().equals(AdapterBase.PAGE_FAIL) || getPageName().equals(AdapterBase.PAGE_FAIL_NETWORKING)
                || getPageName().equals(AdapterBase.PAGE_FAIL_PROCESSING) || getPageName().equals(AdapterBase.PAGE_LINKACC_FAIL)
                || getPageName().equals(AdapterBase.PAGE_UNLINKACC_FAIL);
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
        return getPageName().equals(PAGE_LINKACC_SUCCESS) || getPageName().equals(PAGE_UNLINKACC_SUCCESS);
    }

    /***
     * after show network error dialog.
     * close sdk if user is submitted order
     */
    public void closeSDKAfterNetworkOffline() {
        boolean isNeedCloseSDK = mOwnerActivity != null;

        if (isNeedCloseSDK) {
            isNeedCloseSDK = isNeedCloseSDK && isOrderSubmit();
        }

        if (isNeedCloseSDK && !ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            ZPWUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction));
        }
    }

    public void moveToConfirmScreen() throws Exception {
        try {

            getActivity().setConfirmTitle();

            //add overswipe for rootview scrollview
            ScrollView scrollViewRoot = (ScrollView) getActivity().findViewById(R.id.zpw_scrollview_container);
            if (scrollViewRoot != null) {
                OverScrollDecoratorHelper.setUpOverScroll(scrollViewRoot);
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    protected void showConfrimScreenForCardChannel() throws Exception {
        try {
            getActivity().enableSubmitBtn(true);
            getActivity().showConfirmView(true, true, mConfig);
            setBankInfoConfirmView();

            getActivity().setToolBarTitle();
            if (GlobalData.isMapCardChannel()) {
                boolean isDetected = getGuiProcessor().getCardFinder().detectCard(GlobalData.getPaymentInfo().mapBank.getFirstNumber());
                if (isDetected) {
                    getActivity().setText(R.id.zpw_channel_label_textview, getGuiProcessor().getCardFinder().getDetectedBankName());
                }
            }
            else if(GlobalData.isMapBankAccountChannel())
            {
                BankConfig bankConfig = BankLoader.getInstance().getBankByBankCode(GlobalData.getPaymentInfo().mapBank.bankcode);
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
        getActivity().setOnClickListener(R.id.zpw_submit_support, onSupportClickListener);

        //intro button on linkcard screen
        //getActivity().setOnClickListener(R.id.zpw_linkcard_intro_imageview,mLinkCardIntroClick);
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
            SDKTransactionAdapter.shared().getTransactionStatus(this, pTransID, pCheckData, pMessage);
        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
            Log.e(this, e);
        }
    }

    /***
     * networking occur an error on the way,
     * client haven't get response from server,need to check to server
     */
    protected void checkTransactionStatusIfSubmitOrderFail() {
        try {
            checkTransactionStatus(GlobalData.getPaymentInfo().appTransID, GlobalData.getStringResource(RS.string.zingpaysdk_alert_checking));
        } catch (Exception e) {
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));

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

        BaseRequest getStatusTask = new CheckOrderStatusFailSubmit(this, pAppTransID, false);
        getStatusTask.makeRequest();
    }

    /**
     * Check transaction status
     *
     * @param pStatusResponse data response
     */
    protected void onCheckTransactionStatus(StatusResponse pStatusResponse) {
        try {
            if (pStatusResponse != null && pStatusResponse.returncode < 0) {
                CErrorValidate.updateTransactionResult(pStatusResponse.returncode);
            }
            //error pin
            if (pStatusResponse != null && pStatusResponse.returncode == Constants.PIN_WRONG_RETURN_CODE) {
                //limit number of submit pin
                Log.d(this, "===number of pin retry=" + mPinInputCount);
                //clear error on pin view and reset pin view
                if (mPinInputCount < Constants.MAX_COUNT_RETRY_PIN) {
                    mIsExitWithoutConfirm = false;

                    moveToRequirePin();
                    if (getActivity().getPinPage() != null) {
                        getActivity().getPinPage().setErrorPin(pStatusResponse.returnmessage);
                    }

                } else {
                    showTransactionFailView(!TextUtils.isEmpty(pStatusResponse.returnmessage) ? pStatusResponse.returnmessage : GlobalData.getStringResource(RS.string.zpw_string_pin_wrong));
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
            else if (pStatusResponse != null && !pStatusResponse.isprocessing && !TextUtils.isEmpty(pStatusResponse.returnmessage)) {
                showTransactionFailView(pStatusResponse.returnmessage);
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
        BaseRequest getStatusTask = new CheckOrderStatusFailSubmit(this, pAppTransID, false);
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

    /**
     * set Pmc to payment Result
     */
    protected void setPmcToResult() {
        GlobalData.getPaymentResult().channelID = getChannelID();
        GlobalData.getPaymentResult().channelDetail = getChannelName();
    }

    protected boolean processSaveCardOnResultSuccess() {
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }

        //link card channel, server auto save card , client only save card to local cache withou hit server
        if (GlobalData.isLinkCardChannel()) {
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
            getMapCardInfoList(true);
        } else {
            showProgressBar(false, null);
        }
        return true;
    }

    protected void finishTransaction(String pMessage) {
        mIsSuccess = true;

        setPmcToResult();

        if (pMessage == null || pMessage.length() == 0) {
            pMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_transaction_success);
        }

        GlobalData.setResultSuccess();
        terminate(pMessage, false);
    }

    protected int onRequirePin() {
        int requirePin = Constants.REQUIRE_OTP;

        //reset pmc to zalopay if this is withdraw channel
        if (mConfig != null && GlobalData.getTransactionType() == ETransactionType.WITHDRAW) {
            mConfig.pmcid = Integer.parseInt(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_zalopay));
        }

        if (mConfig == null) {
            requirePin = Constants.INPUT_INVALID;
        } else if (!mConfig.isRequireOtp() || mConfig.isBankAccount())
            requirePin = Constants.REQUIRE_PIN;

        if (requirePin == Constants.REQUIRE_PIN) {
            if (mConfig != null && mConfig.isNeedToCheckTransactionAmount() && GlobalData.orderAmountTotal > mConfig.amountrequireotp)
                requirePin = Constants.REQUIRE_OTP;

        }


        return requirePin;
    }

    /**
     * set message in layout success
     */
    protected void setSuccessLabel() {
        String strMessage = GlobalData.getStringResource(RS.string.zpw_string_payment_success_label);

        if (GlobalData.isLinkCardChannel()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_linkcard_success_label);
        } else if (GlobalData.isTopupChannel()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_topup_success_label);
        } else if (GlobalData.isTranferMoneyChannel()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_tranfer_success_label);
        } else if (GlobalData.isWithDrawChannel()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_withdraw_success_label);
        } else if (GlobalData.isRedPacketChannel()) {
            strMessage = GlobalData.getStringResource(RS.string.zpw_string_lixi_success_label);
        }

        getActivity().setText(R.id.zpw_payment_success_textview, strMessage);
    }

    protected boolean isNeedToNotifyMapCardToApp(CardInfoListResponse pCardInfoResponse) {
        return MapCardHelper.isGetMapCardInfoSuccessAndHaveNewMapCard(pCardInfoResponse) && !GlobalData.isRedPacketChannel();
    }

    protected void processCardInfoListResponse(CardInfoListResponse pCardInfoResponse) throws Exception {
        try {
            //get list map card successfully, callback info mapped card to app to show tutorial page
            if (isNeedToNotifyMapCardToApp(pCardInfoResponse)) {
                //get new map card to notify
                String cardKey = getCard().getCardKey();

                if (TextUtils.isEmpty(cardKey)) {
                    Log.d(this, "===processCardInfoListResponse===cardKey=NULL");
                    return;
                }

                DMappedCard mappedCard = null;

                for (DMappedCard card : pCardInfoResponse.cardinfos) {
                    if (card.getCardKey().equals(cardKey)) {
                        mappedCard = card.clone();
                        break;
                    }
                }

                if (mappedCard != null) {
                    MapCardHelper.notifyNewMapCardToApp(mappedCard);
                }
            }

            //this is redpacket channel
            //update new card info to card
            //quit sdk right away
            if (GlobalData.isRedPacketChannel()) {
                if (MapCardHelper.isNeedUpdateMapCardInfoOnCache(pCardInfoResponse.cardinfochecksum)) {
                    try {
                        MapCardHelper.updateMapCardInfoListOnCache(pCardInfoResponse.cardinfochecksum, pCardInfoResponse.cardinfos);
                    } catch (Exception ex) {
                        Log.e(this, ex);
                    }
                }

                onClickSubmission();
                return;
            }

            //this card is mapped by another account
            if (pCardInfoResponse != null && PaymentStatusHelper.isCardMapByOtherAccount(pCardInfoResponse)) {
                Log.d(this, "this card is maped by other account");
                return;
            }
            if (MapCardHelper.isNeedUpdateMapCardInfoOnCache(pCardInfoResponse.cardinfochecksum)) {
                try {
                    MapCardHelper.updateMapCardInfoListOnCache(pCardInfoResponse.cardinfochecksum, pCardInfoResponse.cardinfos);
                } catch (Exception ex) {
                    Log.e(this, ex);
                }
            } else if (PaymentStatusHelper.isErrorResponse(pCardInfoResponse)) {
                showDialog(pCardInfoResponse.returnmessage);
            } else if (PaymentStatusHelper.isNetworkingErrorResponse(pCardInfoResponse)) {
                showDialog(GlobalData.getStringResource(RS.string.zpw_string_save_card_error));
            }
        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.RESULT_PHARSE, ex != null ? ex.getMessage() : GsonUtils.toJsonString(mResponseStatus));
            throw ex;
        }
    }

    protected void setFailLabel() {
        String strMessage = GlobalData.isLinkCardChannel() ? GlobalData.getStringResource(RS.string.zpw_string_payment_fail_linkcard) :
                GlobalData.getStringResource(RS.string.zpw_string_payment_fail_transaction);

        if (isFailProcessingPharse()) {
            strMessage = GlobalData.isLinkCardChannel() ? GlobalData.getStringResource(RS.string.zpw_string_linkcard_processing) :
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
        DAppInfo appEntity = getActivity().appEntity;

        try {
            if (appEntity == null)
                appEntity = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getAppById(String.valueOf(GlobalData.appID)), DAppInfo.class);
        } catch (Exception e) {
            Log.d(this, e);
        }

        if (appEntity != null && appEntity.viewresulttype == 2) {
            mPageCode = PAGE_SUCCESS_SPECIAL;
        } else
            mPageCode = PAGE_SUCCESS;
    }

    protected boolean processSaveCardOnResult() {
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }

        //link card channel, server auto save card , client only save card to local cache withou hit server
        if (GlobalData.isLinkCardChannel()) {
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
            getMapCardInfoList(true);
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
        if (GlobalData.isRedPacketChannel()) {
            if (isNeedToGetCardInfoListAfterPayment()) {
                getMapCardInfoList(true);
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
    protected synchronized boolean showTransactionSuccessView() {
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
            if (GlobalData.getPaymentListener() != null && GlobalData.getPaymentInfo() != null)
                GlobalData.getPaymentListener().onPreComplete(true, mTransactionID, GlobalData.getPaymentInfo().appTransID);
        } catch (Exception e) {
            Log.e(this, e);
        }

        getSuccessPageType();

        //if this is redpacket,then close sdk and callback to app
        if (processResultForRedPackage()) {
            return false;
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

        ZPWUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
        return processSaveCardOnResult();
    }

    public boolean isTransactionErrorNetworking(String pMessage) {
        return pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_alert_order_not_submit))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_alert_networking_off_in_transaction));
    }

    public boolean isTransactionProcessing(String pMessage) {
        return pMessage.equalsIgnoreCase(GlobalData.getStringResource(GlobalData.getTransProcessingMessage()))
                || pMessage.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_transaction_expired));
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
        //notify to app to do some background task
        try {
            if (GlobalData.getPaymentListener() != null && GlobalData.getPaymentInfo() != null)
                GlobalData.getPaymentListener().onPreComplete(false, mTransactionID, GlobalData.getPaymentInfo().appTransID);
        } catch (Exception e) {
            Log.e(this, e);
        }

        //keep error code ZPC_TRANXSTATUS_TOKEN_INVALID to app known
        if (GlobalData.getPaymentResult() != null &&
                (GlobalData.getPaymentResult().paymentStatus != EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID
                        && GlobalData.getPaymentResult().paymentStatus != EPaymentStatus.ZPC_TRANXSTATUS_LOCK_USER)) {
            GlobalData.setResultFail();
        }

        if (isTransactionProcessing(pMessage)) {
            mPageCode = PAGE_FAIL_PROCESSING;
        } else if (isTransactionErrorNetworking(pMessage)) {
            mPageCode = PAGE_FAIL_NETWORKING;

            //update payment status to no internet to app know
            GlobalData.updateResultNetworkingError(pMessage);
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
        ZPWUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());

        if (ConnectionUtil.isOnline(GlobalData.getAppContext()) && isNeedToGetCardInfoListAfterPayment() && isNeedGetMapCardInfoListOnFailTrans(pMessage) && !shouldCheckTransactionStatusByClientId()) {
            preventRetryLoadMapCardList = true;
            getMapCardInfoList(false);
        }
        showProgressBar(false, null);
        //send log
        try {
            sdkReportErrorOnTransactionFail();
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    /**
     * set bank info
     */
    public void setBankInfoConfirmView() {
        try {
            DPaymentChannelView channel = ChannelStartProcessor.getInstance(null).getChannel();
            if (channel != null) {
                getActivity().setImage(R.id.zpw_zalopay_logo_imageview, ResourceManager.getImage(channel.channel_icon));
                getActivity().setView(R.id.linearlayout_price, false);
                getActivity().setView(R.id.zalopay_info_error, false);
                getActivity().setView(R.id.zpw_channel_layout, true);

                getActivity().setText(R.id.zpw_channel_name_textview, channel.pmcname);
            }

        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }

    public void terminate(String pMessage, boolean pExitSDK) {
        //showProgressBar(false, null);
        //full of 2 activity: payment gateway and payment channel
        if (GlobalData.getChannelActivityCallBack() != null) {
            if (pExitSDK) {
                GlobalData.getChannelActivityCallBack().onExitAction();
            } else {
                GlobalData.getChannelActivityCallBack().onCallBackAction(mIsShowDialog, pMessage);
            }

            getActivity().finish();

            Log.d(this, "===terminate===GlobalData.getChannelActivityCallBack() != null");
            return;
        }
        // one of 2 activty is destroyed
        else {
            ((BasePaymentActivity) BasePaymentActivity.getCurrentActivity()).recycleActivity();
            Log.d(this, "===terminate===GlobalData.getChannelActivityCallBack() = null");
        }
    }

    protected void showDialogWithCallBack(String pMessage, String pButtonText, ZPWOnEventDialogListener pCallBack) {
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

        if (CErrorValidate.needToTerminateTransaction()) {
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

        getActivity().showInfoDialog(new ZPWOnEventDialogListener() {
            @Override
            public void onOKevent() {
                /***
                 * error networking. if user in pin input pharse,then need to try inputting again
                 */
                if (finalMessage.equals(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error)) && isRequirePinPharse()) {
                    moveToRequirePin();
                }
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
                    showTransactionFailView(GlobalData.getStringResource(GlobalData.getTransProcessingMessage()));
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
                    SDKTransactionAdapter.shared().getTransactionStatus(AdapterBase.this, pZmpTransID, isCheckDataInStatus, null);
                } catch (Exception e) {
                    Log.e(this, e);

                    terminate(GlobalData.getStringResource(RS.string.zpw_string_error_layout), true);
                }
            }
        }, message);
    }

    public void releaseClickSubmit() {
        mMoreClick = true;
        Log.d(this, "====release click submit===");
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
    protected void saveMappedCardToLocal(DMappedCard pMappedCard) throws Exception {
        try {
            Log.d("saveMappedCardToLocal", "pMappedCard=" + pMappedCard);
            String mappedCardList = SharedPreferencesManager.getInstance().getMapCardKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

            if (TextUtils.isEmpty(mappedCardList)) {
                mappedCardList = pMappedCard.getCardKey();
            } else if (!mappedCardList.contains(pMappedCard.getCardKey())) {
                mappedCardList += (Constants.COMMA + pMappedCard.getCardKey());
            }

            SharedPreferencesManager.getInstance().setMapCard(pMappedCard.getCardKey(), GsonUtils.toJsonString(pMappedCard));
            SharedPreferencesManager.getInstance().setMapCardList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, mappedCardList);

            GlobalData.getPaymentInfo().mapBank = pMappedCard;

            //clear checksum cardinfo
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
        } catch (Exception ex) {
            sdkReportErrorOnPharse(Constants.RESULT_PHARSE, ex != null ? ex.getMessage() : GsonUtils.toJsonString(mResponseStatus));
            throw ex;
        }
    }

    /***
     * get map card list
     */
    protected void getMapCardInfoList(boolean pIsShowProgress) {
        if (pIsShowProgress) {
            showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_get_card_info_processing));
        }
        BaseRequest getCardInfoList = new GetMapCardInfoList(this);
        getCardInfoList.makeRequest();
    }

    protected void getBankAccountInfoList(boolean pIsShowProgress) {
        if (pIsShowProgress) {
            showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_get_card_info_processing));
        }
        BaseRequest getCardInfoList = new GetBankAccountList(this);
        getCardInfoList.makeRequest();
    }

    protected boolean isNeedGetMapCardInfoListOnFailTrans(String pMessage) {
        return isTransactionProcessing(pMessage) || isTransactionErrorNetworking(pMessage);
    }

    protected boolean isNeedToGetCardInfoListAfterPayment() {
        //this is zalopay channel
        if (!isCardFlow()) {
            AdapterBase.existedMapCard = false;

            return AdapterBase.existedMapCard;
        }

        //this is card channel
        AdapterBase.existedMapCard = GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel();

        if (!AdapterBase.existedMapCard) {
            try {
                AdapterBase.existedMapCard = isExistedCardNumberOnCache();
            } catch (Exception e) {
                Log.d(this, e);
            }
        }

        return !GlobalData.isWithDrawChannel() && !AdapterBase.existedMapCard;
    }

    /**
     * Check where this card is existed on cache
     *
     * @return
     */

    protected boolean isExistedCardNumberOnCache() throws Exception {
        try {
            if(getGuiProcessor() == null)
            {
                Log.d("isExistedCardNumberOnCache","getGuiProcessor() = null");
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
                strMappedCard = SharedPreferencesManager.getInstance().getMapCardByKey(first6cardno + last4cardno);
            } catch (Exception e) {
                Log.d(this, e);
            }

            if (!TextUtils.isEmpty(strMappedCard) && GsonUtils.fromJsonString(strMappedCard, DMappedCard.class) instanceof DMappedCard) {
                return true;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return false;
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
                                               GlobalData.setResultUpgrade();
                                               terminate(null, true);
                                           }
                                       }, GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update), GlobalData.getStringResource(RS.string.dialog_upgrade_button),
                GlobalData.getStringResource(RS.string.dialog_close_button));
    }

    public void needLinkCardBeforePayment() {
        //save card number to show again when user go to link card again
        try {
            if (getGuiProcessor().isCardLengthMatchIdentifier(getGuiProcessor().getCardNumber())) {
                SharedPreferencesManager.getInstance().setCachedCardNumber(getGuiProcessor().getCardNumber());
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        GlobalData.setResultNeedToLinkCardBeforePayment();

        if (getActivity() != null) {
            getActivity().recycleActivity();
        }
    }

    /**
     * user level 1 payment,we haven't saved card yet.
     * user need to upgrade to level 2 to save card.
     * in this case,need to a dialog to show user know this.
     * Dialog with 2 option: no save card and upgrade to save card.
     * user choose upgrade to save card,sdk need to callback to app to show upgrade interface.
     * after user save card sucessfully,app need to card sdk's SaveCard function
     */
    public void confirmUpgradeLevelAndSaveMapCard() {
        String message = GlobalData.getStringResource(RS.string.zpw_string_confirm_upgrade_level_save_card);

        String bankInfo = null;

        if (!TextUtils.isEmpty(getGuiProcessor().getCardFinder().getDetectedBankName())) {
            bankInfo = getGuiProcessor().getCardFinder().getDetectedBankName();
            bankInfo += " " + GlobalData.getStringResource(RS.string.zpw_string_credit_card_save_label);
            bankInfo += " " + getGuiProcessor().getCardFinder().getTinyCardNumber();

        }

        message = String.format(message, bankInfo);

        getActivity().showNoticeDialog(new ZPWOnEventConfirmDialogListener() {
                                           @Override
                                           public void onCancelEvent() {
                                               //notify to app withou upgrade to save card
                                               GlobalData.setResultSuccess();
                                               terminate(GlobalData.getStringResource(RS.string.zingpaysdk_alert_transaction_success), false);
                                           }

                                           @Override
                                           public void onOKevent() {
                                               /***
                                                * save transaction id+ card info to card.
                                                * this infomation will be compared and send to server when app call save card api.
                                                */
                                               try {
                                                   SharedPreferencesManager.getInstance().setCardInfoTransaction(mTransactionID, GsonUtils.toJsonString(mMapCard));

                                               } catch (Exception e) {
                                                   Log.e(this, e);
                                               }

                                               /***
                                                * notify to merchant to upgrade and save card.
                                                */
                                               try {
                                                   GlobalData.getPaymentInfo().walletTransID = mTransactionID;
                                                   GlobalData.setResultUpgradeAndSave();

                                               } catch (Exception e) {
                                                   Log.e(this, e);
                                               }

                                               terminate(GlobalData.getStringResource(RS.string.zingpaysdk_alert_transaction_success), false);
                                           }
                                       }, message, GlobalData.getStringResource(RS.string.dialog_upgrade_button),
                GlobalData.getStringResource(RS.string.dialog_later_button));
    }

    public void sdkReportErrorOnPharse(int pPharse, String pMessage) {
        String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
        if (!TextUtils.isEmpty(paymentError)) {
            paymentError = String.format(paymentError, pPharse, 200, pMessage);
            try {
                sdkReportError(SDKReport.TRANSACTION_FAIL, paymentError);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public void sdkReportErrorOnTransactionFail() throws Exception {
        try {
            if (PaymentPermission.allowSendLogOnTransactionFail()) {
                String paymentError = GlobalData.getStringResource(RS.string.zpw_sdkreport_error_message);
                if (!TextUtils.isEmpty(paymentError)) {
                    paymentError = String.format(paymentError, Constants.RESULT_PHARSE, 200, GsonUtils.toJsonString(mResponseStatus));
                    sdkReportError(SDKReport.TRANSACTION_FAIL, paymentError);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void sdkReportError(int pErrorCode, String pMessage) throws Exception {
        try {
            String bankCode = null;
            try {
                if (getGuiProcessor() != null) {
                    bankCode = getGuiProcessor().getDetectedBankCode();
                }
            } catch (Exception ex) {
                Log.d(this, ex);
            }
            SDKReport.makeReportError(pErrorCode, mTransactionID, pMessage, bankCode);
        } catch (Exception e) {
            throw e;
        }
    }



    public void sdkReportError(int pErrorCode) throws Exception {
        try {
            String bankCode = null;
            try {
                if (getGuiProcessor() != null) {
                    bankCode = getGuiProcessor().getDetectedBankCode();
                }
            } catch (Exception ex) {
                Log.d(this, ex);
            }
            SDKReport.makeReportError(pErrorCode, mTransactionID, mResponseStatus.toJsonString(), bankCode);
        } catch (Exception e) {
            throw e;
        }
    }

    /***
     * send log to sever no check duplicate request
     * @param pErrorCode
     * @throws Exception
     */
    public void sdkTrustReportError(int pErrorCode) throws Exception {
        try {
            String bankCode = null;
            try {
                if (getGuiProcessor() != null) {
                    bankCode = getGuiProcessor().getDetectedBankCode();
                }
            } catch (Exception ex) {
                Log.d(this, ex);
            }
            TrustSDKReport.makeTrustReportError(pErrorCode, mTransactionID, mResponseStatus.toJsonString(), bankCode);
        } catch (Exception e) {
            throw e;
        }
    }

    protected boolean isDialogFingerPrintShowing() {
        if (getDialogFingerPrint() != null && getDialogFingerPrint().getDialog() != null) {
            return getDialogFingerPrint().getDialog().isShowing();
        }
        return false;
    }

    protected DialogFragment getDialogFingerPrint() {
        return mFingerPrintDialog;
    }

    protected void dismissDialogFingerPrint() {
        if (getDialogFingerPrint() != null) {
            getDialogFingerPrint().dismiss();
            mFingerPrintDialog = null;
            Log.d(this, "===dismissDialogFingerPrint===");
        } else {
            Log.d(this, "===dismissDialogFingerPrint===getDialogFingerPrint()=NULL");
        }
    }
}