package vn.com.zalopay.wallet.workflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.OnLoadingDialogTimeoutListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.ISdkErrorContext;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.api.ServiceManager;
import vn.com.zalopay.wallet.api.task.BaseTask;
import vn.com.zalopay.wallet.api.task.CheckOrderStatusFailSubmit;
import vn.com.zalopay.wallet.api.task.SDKReportTask;
import vn.com.zalopay.wallet.api.task.SendLogTask;
import vn.com.zalopay.wallet.api.task.getstatus.GetStatus;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;
import vn.com.zalopay.wallet.constants.BankFlow;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.entity.bank.BankConfig;
import vn.com.zalopay.wallet.entity.bank.MapCard;
import vn.com.zalopay.wallet.entity.bank.PaymentCard;
import vn.com.zalopay.wallet.entity.config.OtpRule;
import vn.com.zalopay.wallet.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.entity.response.SecurityResponse;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.event.SdkAuthenPayerEvent;
import vn.com.zalopay.wallet.event.SdkCheckSubmitOrderEvent;
import vn.com.zalopay.wallet.event.SdkOrderStatusEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteCompleteEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteErrorEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteRenderEvent;
import vn.com.zalopay.wallet.event.SdkSubmitOrderEvent;
import vn.com.zalopay.wallet.event.SdkSuccessTransEvent;
import vn.com.zalopay.wallet.event.SdkWebsite3dsBackEvent;
import vn.com.zalopay.wallet.event.SdkWebsite3dsEvent;
import vn.com.zalopay.wallet.helper.BankHelper;
import vn.com.zalopay.wallet.helper.ErrorCodeHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.helper.ToastHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.helper.WebViewHelper;
import vn.com.zalopay.wallet.interactor.ILinkSourceInteractor;
import vn.com.zalopay.wallet.listener.OnNetworkDialogListener;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelFragment;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;
import vn.com.zalopay.wallet.workflow.ui.BankCardGuiProcessor;
import vn.com.zalopay.wallet.workflow.ui.CardGuiProcessor;

public abstract class AbstractWorkFlow implements ISdkErrorContext {
    final SdkErrorReporter mSdkErrorReporter;
    private final PaymentCard mCard;
    public boolean mOrderProcessing = false;//this is flag prevent user back when user is submitting trans,authen payer,getstatus
    public CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    public int mCurrentCcLinkNumber = 0;
    protected ChannelPresenter mPresenter = null;
    protected PaymentInfoHelper mPaymentInfoHelper;
    protected Context mContext;
    protected EventBus mEventBus;
    CardGuiProcessor mGuiProcessor = null;
    StatusResponse mStatusResponse;
    String mTransactionID;
    String mPageName;
    boolean existTransWithoutConfirm = true;
    //submit log load website to server
    long mCaptchaBeginTime = 0, mCaptchaEndTime = 0;
    long mOtpBeginTime = 0, mOtpEndTime = 0;
    MiniPmcTransType mMiniPmcTransType;
    ILinkSourceInteractor mLinkInteractor;
    int numberOfRetryTimeout = 1;
    SDKTransactionAdapter mTransactionAdapter;
    //check data in response get status api
    boolean isCheckDataInStatus = false;
    boolean mLoadWebStarted = false;
    private boolean isLoadWebTimeout = false;
    private int numberRetryOtp = 0;
    //count of retry check status if submit order fail
    private int mCountCheckStatus = 0;
    //whether show dialog or not?
    private boolean showDialogOnChannelList = true;
    //need to switch to cc or atm
    private boolean mNeedToSwitchChannel = false;
    private boolean mIsOrderSubmit = false;
    OnNetworkDialogListener networkingDialogCloseListener = new OnNetworkDialogListener() {
        @Override
        public void onCloseNetworkingDialog() {
            whetherQuitPaymentOffline();
        }

        @Override
        public void onOpenSettingDialogClicked() {
        }
    };
    private boolean mCanEditCardInfo = false;
    @BankFlow
    private int mECardFlowType;
    OnLoadingDialogTimeoutListener mProgressDialogTimeoutListener = new OnLoadingDialogTimeoutListener() {
        @Override
        public void onProgressTimeout() {
            try {
                WeakReference<Activity> activity = new WeakReference<>(getActivity());
                if (activity.get() == null || activity.get().isFinishing()) {
                    Timber.d("onProgressTimeout - activity is finish");
                    return;
                }
                if (isFinalScreen()) {
                    return;
                }
                //retry load website cc
                if (ConnectionUtil.isOnline(mContext)
                        && isCCFlow() && isLoadWeb() && hasTransId()) {
                    //max retry 3
                    if (numberOfRetryTimeout > Integer.parseInt(GlobalData.getStringResource(RS.string.sdk_retry_number_load_website))) {
                        getOneShotTransactionStatus();
                        return;
                    }
                    numberOfRetryTimeout++;
                    DialogManager.showConfirmDialog(activity.get(),
                            mContext.getResources().getString(R.string.sdk_load_data_timeout_mess),
                            mContext.getResources().getString(R.string.dialog_continue_load_button),
                            mContext.getResources().getString(R.string.dialog_cancel_button),
                            new ZPWOnEventConfirmDialogListener() {
                                @Override
                                public void onCancelEvent() {
                                    getOneShotTransactionStatus();
                                }

                                @Override
                                public void onOKEvent() {
                                    DialogManager.showLoadingDialog(activity.get(), mProgressDialogTimeoutListener);
                                    try {
                                        getGuiProcessor().reloadUrl();
                                    } catch (Exception e) {
                                        Timber.d(e, "Exception reload url");
                                    }
                                }
                            });
                }
                //load web timeout, need to get oneshot to server to check status again
                else if (ConnectionUtil.isOnline(mContext)
                        && isParseWebFlow() && hasTransId()) {
                    getOneShotTransactionStatus();
                } else if (isFinalStep()
                        && mPaymentInfoHelper.isBankAccountTrans()
                        && AbstractWorkFlow.this instanceof AccountLinkWorkFlow) {
                    ((AccountLinkWorkFlow) AbstractWorkFlow.this).verifyServerAfterParseWebTimeout();
                    Timber.d("load website timeout, continue to verify server again to ask for new data list");
                } else if (!isFinalScreen()) {
                    getView().showInfoDialog(mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess),
                            () -> showTransactionFailView(mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess)));
                }
                mSdkErrorReporter.sdkReportError(AbstractWorkFlow.this, SDKReportTask.TIMEOUT_WEBSITE, GsonUtils.toJsonString(mStatusResponse));
            } catch (Exception ex) {
                showTransactionFailView(mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess));
                mSdkErrorReporter.sdkReportError(AbstractWorkFlow.this, SDKReportTask.GENERAL_EXCEPTION, ex.getMessage());
                Timber.w(ex);
            }
        }
    };

    public AbstractWorkFlow(Context pContext, String pPageName, ChannelPresenter pPresenter,
                            MiniPmcTransType pMiniPmcTransType, PaymentInfoHelper paymentInfoHelper,
                            StatusResponse statusResponse) {
        mContext = pContext;
        mPresenter = pPresenter;
        mMiniPmcTransType = pMiniPmcTransType;
        mCard = new PaymentCard();
        mPaymentInfoHelper = paymentInfoHelper;
        mTransactionAdapter = SDKTransactionAdapter.shared().setAdapter(this);
        mStatusResponse = statusResponse;
        mLinkInteractor = SDKApplication.getApplicationComponent().linkInteractor();
        if (mStatusResponse != null) {
            mTransactionID = mStatusResponse.zptransid;
            mPageName = TransactionHelper.getPageName(paymentInfoHelper.getStatus());
            if (TransactionHelper.isSecurityFlow(mStatusResponse)) {
                mPageName = null;
            }
        }
        if (TextUtils.isEmpty(mPageName)) {
            mPageName = pPageName;
        }
        mSdkErrorReporter = SDKApplication.sdkErrorReporter();
        mEventBus = SDKApplication.getApplicationComponent().eventBus();
    }

    public void onStart() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    public void onStop() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    public void showTimeoutLoading(String pTitle) {
        try {
            getView().showLoading(pTitle, mProgressDialogTimeoutListener);
        } catch (Exception e) {
            Timber.w(e, "Exception show loading dialog");
        }
    }

    public String getTransactionID() {
        return mTransactionID;
    }

    public PaymentInfoHelper getPaymentInfoHelper() {
        if (mPaymentInfoHelper == null) {
            mPaymentInfoHelper = GlobalData.getPaymentInfoHelper();
        }
        return mPaymentInfoHelper;
    }

    public void setMiniPmcTransType(MiniPmcTransType mMiniPmcTransType) {
        this.mMiniPmcTransType = mMiniPmcTransType;
    }

    public void requestReadOtpPermission() {
        try {
            getActivity().requestPermission(mContext);//request permission read/view sms on android 6.0+
        } catch (Exception e) {
            Timber.w(e, "Exception on request read otp permission");
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

    public StatusResponse getResponseStatus() {
        return mStatusResponse;
    }

    public void init() throws Exception {
        if (hasTransId()) {
            existTransWithoutConfirm = false;
            if (isTransactionSuccess()) {
                showTransactionSuccessView();
            } else if (!TransactionHelper.isSecurityFlow(mStatusResponse)) {
                showTransactionFailView(mStatusResponse.returnmessage);
            }
        }
        if (isChannelHasInputCard()) {
            initializeGuiProcessor();
            if (mPaymentInfoHelper != null) {
                mCurrentCcLinkNumber = BankHelper.getMaxCCLinkNumber(mPaymentInfoHelper.getUserId());
            }
        }
        if (TransactionHelper.isSecurityFlow(mStatusResponse)) {
            initializeGuiProcessor();
            getView().visibleCardNumberInput(false);
            handleEventGetStatusComplete(mStatusResponse);
            //detectCard(mPaymentInfoHelper.getMapBank().getFirstNumber());
        }
        Timber.d("start adapter with page name %s", mPageName);
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

    @NonNull
    public String getPageName() {
        return (mPageName != null) ? mPageName : "";
    }

    public boolean isBalanceErrorPharse() {
        return getPageName().equals(Constants.PAGE_BALANCE_ERROR);
    }

    public boolean isChannelHasInputCard() {
        if (mPaymentInfoHelper == null) {
            return false;
        }
        boolean isTransactionHasInputCard = !mPaymentInfoHelper.payByCardMap()
                && !mPaymentInfoHelper.payByBankAccountMap()
                && !mPaymentInfoHelper.isWithDrawTrans();
        return isTransactionHasInputCard && !isZaloPayFlow();
    }

    public boolean isAuthenPayerPharse() {
        return getPageName().equals(Constants.PAGE_AUTHEN);
    }

    public void onDetach() {
        Timber.d("onDetach - release gui processor - release pmc config - release presenter");
        if (mGuiProcessor != null) {
            mGuiProcessor.dispose();
            mGuiProcessor = null;
        }
        mCompositeSubscription.clear();
        mMiniPmcTransType = null;
        mPresenter = null;
        clearStickyEvent();
        onStop();
    }

    private void clearStickyEvent() {
        mEventBus.removeStickyEvent(SdkSubmitOrderEvent.class);
        mEventBus.removeStickyEvent(SdkOrderStatusEvent.class);
        mEventBus.removeStickyEvent(SdkAuthenPayerEvent.class);
        mEventBus.removeStickyEvent(SdkSuccessTransEvent.class);

        mEventBus.removeStickyEvent(SdkParseWebsiteRenderEvent.class);
        mEventBus.removeStickyEvent(SdkParseWebsiteCompleteEvent.class);
        mEventBus.removeStickyEvent(SdkParseWebsiteErrorEvent.class);

        mEventBus.removeStickyEvent(SdkWebsite3dsEvent.class);
        mEventBus.removeStickyEvent(SdkWebsite3dsBackEvent.class);
    }

    public void detectCard(String pCardNumber) {
    }

    protected void startParseBankWebsite(String pRedirectUrl) {
        Timber.d("start load parse web %s", pRedirectUrl);
    }

    protected void stopLoadWeb() {
    }

    protected void initializeGuiProcessor() throws Exception {
    }

    private void endingCountTimeLoadCaptchaOtp() {
        if (mCaptchaEndTime == 0) {
            mCaptchaBeginTime = System.currentTimeMillis();
        }
        if (mOtpEndTime == 0) {
            mOtpBeginTime = System.currentTimeMillis();
        }
    }

    public boolean isLoadWebTimeout() {
        return isLoadWebTimeout;
    }

    public void setLoadWebTimeout(boolean loadWebTimeout) {
        isLoadWebTimeout = loadWebTimeout;
    }

    @BankFlow
    private int getECardFlowType() {
        return mECardFlowType;
    }

    public void setECardFlowType(@BankFlow int mECardFlowType) {
        this.mECardFlowType = mECardFlowType;
    }

    public boolean isCardFlowWeb() {
        return isParseWebFlow() || isLoadWeb();
    }

    private boolean isParseWebFlow() {
        return getECardFlowType() == BankFlow.PARSEWEB;
    }

    private boolean isLoadWeb() {
        return getECardFlowType() == BankFlow.LOADWEB;
    }

    public boolean isCardFlow() {
        return isATMFlow() || isCCFlow();
    }

    public boolean isATMFlow() {
        return this instanceof BankCardWorkFlow;
    }

    public boolean isLinkAccFlow() {
        return this instanceof AccountLinkWorkFlow;
    }

    public boolean isCCFlow() {
        return this instanceof CreditCardWorkFlow;
    }

    public boolean isZaloPayFlow() {
        return this instanceof ZaloPayWorkFlow;
    }

    private boolean isOrderSubmit() {
        return mIsOrderSubmit;
    }

    public boolean isNeedToSwitchChannel() {
        return mNeedToSwitchChannel;
    }

    public void setNeedToSwitchChannel(boolean pNeedToSwitchChannel) {
        this.mNeedToSwitchChannel = pNeedToSwitchChannel;
    }

    public ChannelPresenter getPresenter() throws Exception {
        if (mPresenter == null) {
            throw new IllegalAccessException("Presenter is invalid");
        }
        return mPresenter;
    }

    public ChannelActivity getActivity() throws Exception {
        return (ChannelActivity) getView().getActivity();
    }

    public CardGuiProcessor getGuiProcessor() throws Exception {
        if (mGuiProcessor == null) {
            throw new IllegalAccessException("GuiProcessor is invalid");
        }
        return mGuiProcessor;
    }

    public ChannelFragment getView() throws Exception {
        if (mGuiProcessor != null) {
            return mGuiProcessor.getView();
        }
        return getPresenter().getViewOrThrow();
    }

    void startSubmitTransaction() {
        if (!checkAndOpenNetworkingSetting()) {
            return;
        }
        if (mPaymentInfoHelper == null || mPaymentInfoHelper.getUserInfo() == null) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_paymentinfo_invalid_user_id_mess));
            return;
        }
        mOrderProcessing = true;
        mIsOrderSubmit = true;
        mCanEditCardInfo = false;
        try {
            getView().showLoading(mContext.getResources().getString(R.string.sdk_trans_submit_order_mess));
            mTransactionAdapter.startTransaction(getChannelID(), mPaymentInfoHelper.getUserInfo(), getCard(), mPaymentInfoHelper);
        } catch (Exception e) {
            Timber.w(e, "Exception submit order");
            showTransactionFailView(mContext.getResources().getString(R.string.zpw_string_error_layout));
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
        return !getPageName().equals(Constants.SCREEN_ATM)
                && !getPageName().equals(Constants.SCREEN_CC)
                && !getPageName().equals(Constants.PAGE_SUCCESS)
                && !getPageName().equals(Constants.PAGE_FAIL)
                && !getPageName().equals(Constants.PAGE_FAIL_NETWORKING)
                && !getPageName().equals(Constants.PAGE_FAIL_PROCESSING);
    }

    void processWrongOtp() {
        numberRetryOtp++;
        //over number of retry
        if (numberRetryOtp > Integer.parseInt(GlobalData.getStringResource(RS.string.sdk_number_retry_otp))) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_error_retry_otp_mess));
            return;
        }
        showDialogWithCallBack(mStatusResponse.returnmessage, () -> {
            //reset otp and show keyboard again
            if (isCardFlow()) {
                try {
                    BankCardGuiProcessor cardGuiProcessor = (BankCardGuiProcessor) getGuiProcessor();
                    cardGuiProcessor.resetOtpWeb();
                    cardGuiProcessor.showKeyBoardOnEditTextAndScroll(cardGuiProcessor.getOtpAuthenPayerEditText());
                } catch (Exception e) {
                    Timber.w(e);
                }
            }
        });
    }

    public void autoFillOtp(String pSender, String pOtp) {
    }

    boolean shouldCheckStatusAgain() {
        return mStatusResponse == null
                && ConnectionUtil.isOnline(mContext)
                && hasTransId();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onWebsite3dsComplete(SdkWebsite3dsEvent event) {
        mEventBus.removeStickyEvent(SdkWebsite3dsEvent.class);
        //ending timer loading site
        mOtpEndTime = System.currentTimeMillis();
        mCaptchaEndTime = System.currentTimeMillis();
        getTransactionStatus(mTransactionID, false, mContext.getResources().getString(R.string.sdk_trans_getstatus_mess));
        Timber.d("on website 3ds onCloseCompleted");
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSubmitOrderComplete(SdkSubmitOrderEvent event) {
        mEventBus.removeStickyEvent(SdkSubmitOrderEvent.class);
        try {
            mOrderProcessing = false;
            //server is maintenance
            if (PaymentStatusHelper.isServerInMaintenance(mStatusResponse)) {
                getView().showMaintenanceServiceDialog(mStatusResponse.returnmessage);
                return;
            }
            handleEventSubmitOrderCompleted(event.response);
            Timber.d("on submit order onCloseCompleted %s", GsonUtils.toJsonString(event.response));
        } catch (Exception e) {
            Timber.w(e, "Exception on submit order onCloseCompleted");
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onOrderStatusComplete(SdkOrderStatusEvent event) {
        mEventBus.removeStickyEvent(SdkOrderStatusEvent.class);
        try {
            mOrderProcessing = false;
            handleEventGetStatusComplete(event.response);
            //testOtp();
            Timber.d("on status order onCloseCompleted %s", GsonUtils.toJsonString(event.response));
        } catch (Exception e) {
            Timber.w(e, "Exception on status order onCloseCompleted");
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCheckSubmitOrderComplete(SdkCheckSubmitOrderEvent event) {
        mEventBus.removeStickyEvent(SdkCheckSubmitOrderEvent.class);
        try {
            mOrderProcessing = false;
            handleEventCheckStatusSubmitComplete(event.response);
            Timber.d("on check submit order onCloseCompleted %s", GsonUtils.toJsonString(event.response));
        } catch (Exception e) {
            Timber.w(e, "Exception on checksubmit order onCloseCompleted");
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onWebsite3dsBackEvent(SdkWebsite3dsBackEvent event) {
        mEventBus.removeStickyEvent(SdkWebsite3dsBackEvent.class);
        try {
            handleEventLoadSiteError(event.info);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public void handleEventLoadSiteError(Object firstParam) throws Exception {
        if (isLoadWeb() || isParseWebFlow()) {
            stopLoadWeb();
        }
        if (!ConnectionUtil.isOnline(mContext)) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess));
            return;
        }
        //ending timer loading site
        mOtpEndTime = System.currentTimeMillis();
        mCaptchaEndTime = System.currentTimeMillis();

        WebViewHelper webViewError = null;
        if (firstParam instanceof WebViewHelper) {
            webViewError = (WebViewHelper) firstParam;
        }
        if (webViewError != null && webViewError.code == WebViewHelper.SSL_ERROR) {
            showTransactionFailView(webViewError.getFriendlyMessage());
            return;
        }

        if (isCCFlow() || (isATMFlow()
                && ((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing())) {
            isLoadWebTimeout = true;
            getTransactionStatus(mTransactionID, false, mContext.getResources().getString(R.string.sdk_trans_getstatus_mess));
        } else {
            String mess = (webViewError != null) ? webViewError.getFriendlyMessage() :
                    mContext.getResources().getString(R.string.sdk_errormess_end_transaction);
            showTransactionFailView(mess);
        }
    }

    private void handleEventCheckStatusSubmitComplete(StatusResponse statusResponse) {
        mStatusResponse = statusResponse;
        if (mStatusResponse != null) {
            mTransactionID = mStatusResponse.zptransid;
        }
        if (mPaymentInfoHelper == null || mPaymentInfoHelper.getOrder() == null) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_invalid_payment_data));
            return;
        }
        AbstractOrder order = mPaymentInfoHelper.getOrder();
        //order haven't submitted to server yet.
        //need to retry check 30 times to server
        if (PaymentStatusHelper.isTransactionNotSubmit(mStatusResponse)) {
            try {
                mCountCheckStatus++;
                if (mCountCheckStatus == Constants.TRANS_STATUS_MAX_RETRY) {
                    showTransactionFailView(mContext.getResources().getString(R.string.sdk_trans_order_not_submit_mess));
                } else if (order != null) {
                    //retry again
                    checkOrderSubmitStatus(order.apptransid, mContext.getResources().getString(R.string.sdk_trans_getstatus_mess));
                }
            } catch (Exception e) {
                showTransactionFailView(mContext.getResources().getString(R.string.sdk_fail_trans_status));
                Timber.w(e, "Exception handle check order status");
            }
            return;
        }
        if (TransactionHelper.isOrderProcessing(mStatusResponse)) {
            getTransactionStatus(mTransactionID, true, null);
        } else {
            checkTransactionStatus(mStatusResponse);
        }
    }

    private void handleEventGetStatusComplete(StatusResponse statusResponse) throws Exception {
        mStatusResponse = statusResponse;
        getView().visibleCardViewNavigateButton(false);
        getView().visibleSubmitButton(true);
        //error
        if (mStatusResponse == null) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_trans_fail_check_status_mess));
            return;
        }
        if (mLoadWebStarted) {
            showTransactionFailView(mStatusResponse.returnmessage);
            return;
        }
        //retry otp
        if (PaymentStatusHelper.isWrongOtpResponse(mStatusResponse)) {
            processWrongOtp();
            return;
        }
        if (TransactionHelper.isSecurityFlow(mStatusResponse)) {
            SecurityResponse dataResponse = GsonUtils.fromJsonString(mStatusResponse.data, SecurityResponse.class);
            //flow 3ds (atm + cc)
            handleEventFlow3DS_Atm_cc(dataResponse);
        } else if (TransactionHelper.isOrderProcessing(mStatusResponse)) {
            askToRetryGetStatus(mTransactionID);
        } else {
            checkTransactionStatus(mStatusResponse);
        }
    }

    private void handleEventFlow3DS_Atm_cc(SecurityResponse dataResponse) throws Exception {
        String bankCode = getBankCode();
        if (TextUtils.isEmpty(bankCode)) {
            showTransactionFailView(mContext.getString(R.string.sdk_error_init_data));
            return;
        }
        CardGuiProcessor guiProcessor = null;
        try {
            guiProcessor = getGuiProcessor();
        } catch (Exception e) {
            Timber.d(e);
        }
        if (guiProcessor == null) {
            return;
        }
        if (PaymentStatusHelper.is3DSResponse(dataResponse)) {
            //no link for parsing
            if (TextUtils.isEmpty(dataResponse.redirecturl)) {
                showTransactionFailView(mContext.getResources().getString(R.string.sdk_error_empty_url_mess));
                mSdkErrorReporter.sdkReportErrorOnPharse(this, Constants.STATUS_PHARSE, GsonUtils.toJsonString(mStatusResponse));
                return;
            }
            //flow cover parse web (vietinbank)
            BankConfig bankConfig = SDKApplication
                    .getApplicationComponent()
                    .bankListInteractor()
                    .getBankConfig(bankCode);
            if (bankConfig == null) {
                showTransactionFailView(mContext.getString(R.string.sdk_load_bankconfig_error_mess));
                return;
            }
            Timber.d("start flow 3ds banconfig %s", GsonUtils.toJsonString(bankConfig));
            if (isCardFlow() && bankConfig.isParseWebsite()) {
                setECardFlowType(BankFlow.PARSEWEB);
                showTimeoutLoading(mContext.getResources().getString(R.string.sdk_trans_processing_bank_mess));
                startParseBankWebsite(dataResponse.redirecturl);
                endingCountTimeLoadCaptchaOtp();
            }
            //flow load web 3ds of cc
            else {
                handleEventLoadWeb3DS(dataResponse.redirecturl);
            }
        } else if (PaymentStatusHelper.isOtpResponse(dataResponse)) {
            //otp flow
            mPageName = Constants.PAGE_AUTHEN;
            if (guiProcessor instanceof BankCardGuiProcessor) {
                ((BankCardGuiProcessor) guiProcessor).showOtpTokenView();
            }
            getView().hideLoading();
            getView().renderKeyBoard(RS.layout.screen__card, bankCode);
            getView().setVisible(R.id.order_info_line_view, false);
            //request permission read/view sms on android 6.0+
            if (guiProcessor instanceof BankCardGuiProcessor
                    && ((BankCardGuiProcessor) guiProcessor).isOtpAuthenPayerProcessing()) {
                requestReadOtpPermission();
            }
        } else {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_undefine_error));
        }
    }

    private void handleEventLoadWeb3DS(String redirecturl) {
        try {
            setECardFlowType(BankFlow.LOADWEB);
            getGuiProcessor().loadUrl(redirecturl);
            getView().hideLoading();
            mLoadWebStarted = true;
            //begin count timer loading site until finish transaction
            mOtpBeginTime = System.currentTimeMillis();
            mCaptchaBeginTime = System.currentTimeMillis();
        } catch (Exception e) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_error_init_data));
            mSdkErrorReporter.sdkReportErrorOnPharse(this, Constants.STATUS_PHARSE, e.getMessage());
            Timber.w(e, "Exception handle load 3ds");
        }
    }

    private void handleEventSubmitOrderCompleted(StatusResponse response) {
        if (response == null) {
            //offline
            if (!ConnectionUtil.isOnline(mContext)) {
                showTransactionFailView(mContext.getResources().getString(R.string.sdk_trans_networking_offine_mess));
                return;
            }
            AbstractOrder order = mPaymentInfoHelper.getOrder();
            if (order == null) {
                showTransactionFailView(mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess));
            } else {
                checkOrderSubmitStatus(order.apptransid,
                        mContext.getResources().getString(R.string.sdk_trans_getstatus_mess));
            }
            return;
        }
        mStatusResponse = response;
        mTransactionID = mStatusResponse.zptransid;
        if (TransactionHelper.isOrderProcessing(mStatusResponse)) {
            getTransactionStatus(mTransactionID, true, null);//get status transaction
        } else {
            checkTransactionStatus(mStatusResponse);//check status
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEventNotifyTransactionFinish(SdkSuccessTransEvent event) {
        Timber.d("processing result payment from notification");
        if (isTransactionSuccess()) {
            Timber.d("transaction is finish, skipping process notification");
            return;
        }
        if (!isTransactionInProgress()) {
            Timber.d("transaction is ending, skipping process notification");
            return;
        }
        if (event == null) {
            Timber.d("stopping processing result payment from notification because of empty event");
            return;
        }
        if (!Constants.TRANSACTION_SUCCESS_NOTIFICATION_TYPES.contains(event.notification_type)) {
            Timber.d("notification type is not accepted for this kind of transaction");
            return;
        }
        try {
            if (TextUtils.isEmpty(mTransactionID) || !mTransactionID.equals(String.valueOf(event.transid))) {
                Timber.d("Transaction id not same");
                return;
            }
            ServiceManager.shareInstance().cancelRequest();//cancel current request
            GetStatus.cancelRetryTimer();//cancel timer retry get status
            DialogManager.closeAllDialog();//close dialog
            if (mStatusResponse != null) {
                mStatusResponse.returncode = 1;
                mStatusResponse.isprocessing = false;
                mStatusResponse.returnmessage = mContext.getResources().getString(R.string.sdk_trans_success_mess);
            }
            if (mPaymentInfoHelper != null) {
                mPaymentInfoHelper.updateOrderTime(event.trans_time);
            }
            showTransactionSuccessView();
            Timber.d("trans success from notification");
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    /*
     * check networking is on/off
     * if off then open dialog networking for requesting open network again
     */
    public boolean checkAndOpenNetworkingSetting() {
        try {
            boolean isNetworkingOpen = ConnectionUtil.isOnline(mContext);
            if (!isNetworkingOpen) {
                getView().showOpenSettingNetwokingDialog(networkingDialogCloseListener);
            }
            return isNetworkingOpen;
        } catch (Exception e) {
            Timber.w(e, "Exception check networking");
        }
        return false;
    }

    private boolean shouldSendLogToServer() {
        Timber.d("captcha %s ms, otp %s ms", (mCaptchaEndTime - mCaptchaBeginTime), (mOtpEndTime - mOtpBeginTime));
        return ((mCaptchaEndTime - mCaptchaBeginTime) >= 0) || ((mOtpEndTime - mOtpBeginTime) > 0);
    }

    private void sendLogTransaction() {
        try {
            if (!shouldSendLogToServer()) {
                return;
            }
            BaseTask sendLogTask = new SendLogTask(mPaymentInfoHelper.getUserInfo(), getChannelID(),
                    mTransactionID, mCaptchaBeginTime, mCaptchaEndTime, mOtpBeginTime, mOtpEndTime);
            sendLogTask.makeRequest();
        } catch (Exception e) {
            Timber.w(e, "Exception send log to loading time website (captcha - otp)");
        }
    }

    public void onClickSubmission() {
        try {
            Timber.d("page name %s", mPageName);
            SdkUtils.hideSoftKeyboard(mContext, getActivity());
            //fail transaction
            if (isTransactionFail()) {
                terminate(null, true);
                return;
            }
            //pay successfully
            if (isTransactionSuccess()) {
                finishTransaction();
                return;
            }
            onProcessPhrase();
        } catch (Exception ex) {
            showTransactionFailView(mContext.getResources().getString(R.string.zpw_string_error_layout));
            Timber.w(ex, "Exception click submit");
        }
    }

    public PaymentCard getCard() {
        return mCard;
    }

    public boolean isFinalScreen() {
        return getPageName().equals(Constants.PAGE_FAIL) || getPageName().equals(Constants.PAGE_SUCCESS)
                || getPageName().equals(Constants.PAGE_FAIL_NETWORKING)
                || getPageName().equals(Constants.PAGE_FAIL_PROCESSING);
    }

    public boolean isTransactionFail() {
        return TransactionHelper.isTransFail(getPageName());
    }

    public boolean isTransactionSuccess() {
        return isPaymentSuccess() || isLinkAccSuccess();
    }

    public boolean isPaymentSuccess() {
        return getPageName().equals(Constants.PAGE_SUCCESS);
    }

    public boolean isLinkAccSuccess() {
        return false;
    }

    /*
     * after show network error dialog.
     * close sdk if user is submitted order
     */
    void whetherQuitPaymentOffline() {
        boolean isNeedCloseSDK = isOrderSubmit() || isLinkAccFlow();
        if (isNeedCloseSDK && !ConnectionUtil.isOnline(mContext)) {
            try {
                SdkUtils.hideSoftKeyboard(mContext, getActivity());
            } catch (Exception ignored) {
            }
            String offlineMessage = mPaymentInfoHelper != null ? mPaymentInfoHelper.getOfflineMessage(mContext) :
                    mContext.getResources().getString(R.string.sdk_trans_networking_offine_mess);
            showTransactionFailView(offlineMessage);
        }
    }

    public boolean exitWithoutConfirm() {
        if (getPageName().equals(Constants.PAGE_SUCCESS)
                || getPageName().equals(Constants.PAGE_FAIL)
                || getPageName().equals(Constants.PAGE_FAIL_NETWORKING)
                || getPageName().equals(Constants.PAGE_FAIL_PROCESSING)) {
            existTransWithoutConfirm = true;
        }
        return existTransWithoutConfirm;
    }

    boolean hasTransId() {
        return !TextUtils.isEmpty(mTransactionID);
    }

    void getTransactionStatus(String pTransID, boolean pCheckData, String pMessage) {
        existTransWithoutConfirm = false;
        mOrderProcessing = true;
        isCheckDataInStatus = pCheckData;
        getStatusStrategy(pTransID, pCheckData, pMessage);
    }

    private void getStatusStrategy(String pTransID, boolean pCheckData, String pMessage) {
        try {
            getView()
                    .showLoading(TextUtils.isEmpty(pMessage) ?
                            mContext.getResources().getString(R.string.sdk_trans_getstatus_mess) :
                            pMessage);
            mTransactionAdapter.getTransactionStatus(pTransID, pCheckData, pMessage);
        } catch (Exception e) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess));
        }
    }

    private void checkTransactionStatus(StatusResponse pStatusResponse) {
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
                showTransactionFailView(mContext.getResources().getString(R.string.sdk_trans_fail_check_status_mess));
            }
            getView().hideLoading();
        } catch (Exception e) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_trans_fail_check_status_mess));
            Timber.w(e, "Exception check trans status");
        }
    }

    private void showFailScreen(String pMessage) {
        String message = pMessage;
        if (TextUtils.isEmpty(message)) {
            message = mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
        }
        String appName = TransactionHelper.getAppNameByTranstype(mContext, mPaymentInfoHelper.getTranstype());
        if (TextUtils.isEmpty(appName)) {
            AppInfo appInfo = TransactionHelper.getAppInfoCache(mPaymentInfoHelper.getAppId());
            appName = appInfo != null ? appInfo.appname : null;
        }
        try {
            String title = mPaymentInfoHelper.getFailTitleByTrans(mContext);
            boolean isLink = mPaymentInfoHelper.isLinkTrans();
            getView().renderFail(isLink, message, mTransactionID, mPaymentInfoHelper.getOrder(),
                    appName, mStatusResponse, true, title);
        } catch (Exception e) {
            Timber.w(e, "Exception show fail screen");
        }
    }

    /*
     * networking occur an error on the way,
     * client haven't get response from server,need to check to server
     */
    private void checkOrderSubmitStatus(final String pAppTransID, String pMessage) {
        try {
            getView().showLoading(pMessage);
            if (mPaymentInfoHelper == null) {
                showTransactionFailView(mContext.getResources().getString(R.string.sdk_invalid_payment_data));
                return;
            }
            BaseTask getStatusTask = new CheckOrderStatusFailSubmit(pAppTransID,
                    mPaymentInfoHelper.getAppId(), mPaymentInfoHelper.getUserInfo());
            getStatusTask.makeRequest();
        } catch (Exception ex) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_trans_fail_check_status_mess));
            Timber.w(ex, "Exception check order submit status");
        }
    }

    private void finishTransaction() {
        try {
            getPresenter()
                    .setPaymentStatusAndCallback(PaymentStatus.SUCCESS);
        } catch (Exception e) {
            Timber.w(e, "Exception finish trans");
        }
    }

    private void saveMapCardToLocal() {
        try {
            if (mCard == null) {
                return;
            }
            MapCard mapCard = new MapCard(mCard);
            saveMapCard(mapCard);
        } catch (Exception e) {
            Timber.w(e, "Exception save map card to local");
        }
    }

    /*
     * show success view base
     */
    private synchronized void showTransactionSuccessView() {
        showDialogOnChannelList = false;
        existTransWithoutConfirm = true;
        try {
            mGuiProcessor.useWebView(false);
        } catch (Exception e) {
            Timber.d(e, "Exception hide webview");
        }
        try {
            getView().dismissShowingView();
        } catch (Exception e) {
            Timber.d(e, "Exception dismiss loading view");
        }
        GlobalData.extraJobOnPaymentCompleted(mStatusResponse, getBankCode(), true);
        //save payment card for show on channel list later
        String userId = mPaymentInfoHelper != null ? mPaymentInfoHelper.getUserId() : null;
        if (!TextUtils.isEmpty(userId)) {
            String paymentCard = null;
            if (mCard != null && mCard.isValid()) {
                paymentCard = mCard.getCardKey();
            }
            if (TextUtils.isEmpty(paymentCard)) {
                paymentCard = mPaymentInfoHelper.getMapBank() != null ? mPaymentInfoHelper.getMapBank().getKey() : null;
            }
            SDKApplication.getApplicationComponent()
                    .bankListInteractor().setPaymentBank(userId, paymentCard);
        }
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }
        //if this is redpacket,then close sdk and callback to app
        boolean isRedPacket = mPaymentInfoHelper != null && mPaymentInfoHelper.isRedPacket();
        if (isRedPacket) {
            finishTransaction();
            return;
        }
        renderSuccessInformation();
        try {
            //change button text for whitelist app id
            long appId = -1;
            if (mPaymentInfoHelper != null) {
                appId = mPaymentInfoHelper.getAppId();
            }
            getView().handleSpecialAppResult(appId);
        } catch (Exception e) {
            Timber.d(e);
        }
        //update password fingerprint
        try {
            if (GlobalData.mShowFingerPrintToast) {
                ToastHelper.showToastUpdatePassword(getActivity());
            }
        } catch (Exception e) {
            Timber.d(e);
        }

        if (mPaymentInfoHelper.isLinkTrans()) {
            try {
                saveMapCardToLocal();
                reloadMapCard();
            } catch (Exception e) {
                Timber.w(e, "Exception reload map card on link");
            }
        }
    }

    private void renderSuccessInformation() {
        try {
            mPageName = Constants.PAGE_SUCCESS;
            getView().renderByResource(mPageName);
            AppInfo appInfo = TransactionHelper.getAppInfoCache(mPaymentInfoHelper.getAppId());
            String appName = TransactionHelper.getAppNameByTranstype(mContext, mPaymentInfoHelper.getTranstype());
            if (TextUtils.isEmpty(appName)) {
                appName = appInfo != null ? appInfo.appname : null;
            }
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            boolean isTransfer = mPaymentInfoHelper.isMoneyTranferTrans();
            UserInfo receiverInfo = mPaymentInfoHelper.getMoneyTransferReceiverInfo();
            String title = mPaymentInfoHelper.getSuccessTitleByTrans(mContext);
            boolean isLink = mPaymentInfoHelper.isLinkTrans();
            getView().renderSuccess(isLink, mTransactionID, userInfo, mPaymentInfoHelper.getOrder(),
                    appName, null, isLink, isTransfer, receiverInfo, title);
        } catch (Exception e) {
            Timber.w(e, "Exception render success info");
        }
    }

    private boolean isTransactionInProgress() {
        return mStatusResponse != null && mStatusResponse.isprocessing;
    }

    void showTransactionFailView(String pMessage) {
        showDialogOnChannelList = false;
        existTransWithoutConfirm = true;
        try {
            getView().dismissShowingView();
        } catch (Exception e) {
            Timber.d(e);
        }
        GlobalData.extraJobOnPaymentCompleted(mStatusResponse, getBankCode(), true);
        try {
            mGuiProcessor.useWebView(false);
        } catch (Exception e) {
            Timber.d(e, "Exception hide webview");
        }

        mPageName = Constants.PAGE_FAIL;
        if (TransactionHelper.isTransactionProcessing(mContext, pMessage, mPaymentInfoHelper.getTranstype())) {
            mPageName = Constants.PAGE_FAIL_PROCESSING;
        } else if (TransactionHelper.isTransNetworkError(mContext, pMessage)) {
            mPageName = Constants.PAGE_FAIL_NETWORKING;
            mPaymentInfoHelper.updateResultNetworkingError(mContext, pMessage); //update payment status to no internet to app know
        }

        int status = mPaymentInfoHelper.getStatus();
        if (status != PaymentStatus.TOKEN_EXPIRE && status != PaymentStatus.USER_LOCK) {
            mPaymentInfoHelper.setResult(mPageName.equals(Constants.PAGE_FAIL_PROCESSING)
                    ? PaymentStatus.NON_STATE : PaymentStatus.FAILURE);
        }
        try {
            getView().renderByResource(mPageName);
            showFailScreen(pMessage);
        } catch (Exception e) {
            Timber.d(e, "Exception show trans fail");
        }
        //send log captcha, otp
        if (isCardFlowWeb()) {
            sendLogTransaction();
        }
        //send log
        try {
            mSdkErrorReporter.sdkReportErrorOnTransactionFail(this,
                    GsonUtils.toJsonString(mStatusResponse));
        } catch (Exception e) {
            Timber.d(e, "Exception send error log");
        }
        reloadMapListOnResponseMessage(pMessage);
    }

    private void reloadMapListOnResponseMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (!message.equalsIgnoreCase(mContext.getResources().getString(R.string.sdk_error_mess_exist_mapcard))) {
            return;
        }
        try {
            reloadMapCard();
        } catch (Exception e) {
            Timber.d(e, "Exception reload map card");
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
            Timber.d(e, "Exception terminate");
        }
        Timber.d("callback transaction");
    }

    void showDialogWithCallBack(String pMessage, ZPWOnEventDialogListener pCallBack) {
        try {
            getView().hideLoading();
            getView().showInfoDialog(pMessage, pCallBack);
        } catch (Exception e) {
            Timber.w(e, "Exception show showDialogWithCallBack");
        }
    }

    protected void showDialog(String pMessage) {
        try {
            getView().hideLoading();
        } catch (Exception e) {
            Timber.w(e);
        }
        if (ErrorCodeHelper.needToTerminateTransaction(mPaymentInfoHelper.getStatus())) {
            terminate(pMessage, true);
            return;
        }
        if (!TextUtils.isEmpty(pMessage)) {
            pMessage = mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
        }
        try {
            getView().showInfoDialog(pMessage);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    private void askToRetryGetStatus(final String pZmpTransID) throws Exception {
        try {
            getView().hideLoading();
        } catch (Exception e) {
            Timber.w(e);
        }
        if (isFinalScreen()) {
            Timber.d("user in fail screen - skip retry get status");
            return;
        }
        String message = mContext.getResources().getString(R.string.sdk_trans_retry_getstatus_mess);
        getView().showRetryDialog(message, new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                showTransactionFailView(TransactionHelper.getTransProcessingMessage(mPaymentInfoHelper.getTranstype()));
            }

            @Override
            public void onOKEvent() {
                try {
                    getView().showLoading(mContext.getResources().getString(R.string.sdk_trans_getstatus_mess));
                } catch (Exception e) {
                    Timber.w(e);
                }
                /*
                 * if bank bypass opt, no need to check data when get status
                 * if bank not by pass opt, need to check data to determinate 3ds or api.
                 */
                try {
                    mTransactionAdapter.getTransactionStatus(pZmpTransID, isCheckDataInStatus, null);
                } catch (Exception e) {
                    Timber.w(e);
                    terminate(mContext.getResources().getString(R.string.zpw_string_error_layout), true);
                }
            }
        });
    }

    /*
     * link card
     * auto save map card to local storage
     * make sure that reset card info checksum
     */
    private void saveMapCard(MapCard mapCard) throws Exception {
        try {
            if (mPaymentInfoHelper == null || mapCard == null) {
                return;
            }
            Timber.d("start save map card to storage %s", mapCard);
            String userId = mPaymentInfoHelper.getUserId();
            if (TextUtils.isEmpty(userId)) {
                return;
            }
            mLinkInteractor.putCard(userId, mapCard);
            //clear card info checksum for forcing reload api later
            mLinkInteractor.clearCheckSum();
            mPaymentInfoHelper.setMapBank(mapCard);
        } catch (Exception ex) {
            mSdkErrorReporter.sdkReportErrorOnPharse(this, Constants.RESULT_PHARSE, ex.getMessage());
            throw ex;
        }
    }

    private void reloadMapCard() throws Exception {
        try {
            mLinkInteractor.clearCheckSum();
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            String appVersion = SdkUtils.getAppVersion(mContext);
            Subscription subscription = SDKApplication.getApplicationComponent()
                    .linkInteractor()
                    .getCards(userInfo.zalopay_userid, userInfo.accesstoken, false, appVersion)
                    .compose(SchedulerHelper.applySchedulers())
                    .subscribe(aBoolean -> Timber.d("Reload map card list success"),
                            throwable -> Timber.d(throwable, "Exception reload map card list"));
            mCompositeSubscription.add(subscription);
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean existMapCardOnCache() {
        try {
            String cardNumber = getGuiProcessor().getCardNumber();
            if (TextUtils.isEmpty(cardNumber) || cardNumber.length() <= 6) {
                return false;
            }
            if (mPaymentInfoHelper == null) {
                return false;
            }
            String first6cardno = cardNumber.substring(0, 6);
            String last4cardno = cardNumber.substring(cardNumber.length() - 4);
            MapCard mapCard = mLinkInteractor.getCard(mPaymentInfoHelper.getUserId(), first6cardno + last4cardno);
            return mapCard != null;
        } catch (Exception e) {
            Timber.w(e, "Exception check exist map card on cache");
        }
        return false;
    }

    /*
     * get status 1 oneshot to check status again in load website is timeout
     */
    void getOneShotTransactionStatus() {
        isLoadWebTimeout = true;
        getStatusStrategy(mTransactionID, false, null);
    }

    @Override
    public boolean hasCardGuiProcessor() {
        return mGuiProcessor != null;
    }

    @Override
    public String getBankCode() {
        try {
            if (mPaymentInfoHelper != null && mPaymentInfoHelper.getMapBank() != null) {
                return mPaymentInfoHelper.getMapBank().bankcode;
            }
            if (mGuiProcessor != null) {
                return mGuiProcessor.getBankCode();
            }
        } catch (Exception e) {
            Timber.w(e);
        }
        return "";
    }

    @Override
    public String getTransactionId() {
        return mTransactionID;
    }

    @Override
    public UserInfo getUserInfo() {
        return mPaymentInfoHelper.getUserInfo();
    }
}
