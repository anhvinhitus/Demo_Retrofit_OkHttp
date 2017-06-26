package vn.com.zalopay.wallet.ui.channel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.zalopay.feedback.FeedbackCollector;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StorageUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.factory.AdapterFactory;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.feedback.Feedback;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkDownloadResourceMessage;
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkResourceInitMessage;
import vn.com.zalopay.wallet.event.SdkSmsMessage;
import vn.com.zalopay.wallet.event.SdkStartInitResourceMessage;
import vn.com.zalopay.wallet.event.SdkUnlockScreenMessage;
import vn.com.zalopay.wallet.event.SdkUpVersionMessage;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBank;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.PaymentPresenter;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

import static vn.com.zalopay.wallet.constants.Constants.API;
import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_REQUEST_CODE;
import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_RESULT_CODE;
import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;
import static vn.com.zalopay.wallet.constants.Constants.STATUS_RESPONSE;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelPresenter extends PaymentPresenter<ChannelFragment> {
    @Inject
    public EventBus mBus;
    @Inject
    public IBank mBankInteractor;
    @Inject
    public IAppInfo mAppInfoInteractor;
    public boolean hasAtm;
    public boolean hasCC;
    protected CountDownTimer mExpireTransTimer;
    protected boolean mTimerRunning = false;
    private AdapterBase mAdapter = null;
    protected onCloseSnackBar mOnCloseSnackBarListener = () -> {
        if (mAdapter != null) {
            mAdapter.openSettingNetworking();
        }
    };
    private boolean mIsSwitching = false;
    private MiniPmcTransType mMiniPmcTransType;
    private StatusResponse mStatusResponse;
    private PaymentInfoHelper mPaymentInfoHelper;
    private ZPWOnSweetDialogListener dialogManyOptionClick = pIndex -> {
        switch (pIndex) {
            case 0:
                break;
            case 1:
                callBackThenTerminate();
                break;
            case 2:
                mAdapter.onEvent(EEventType.ON_BACK_WHEN_LOADSITE, new Object());
                break;
        }
    };

    public ChannelPresenter() {
        SDKApplication.getApplicationComponent().inject(this);
        Log.d(this, "call constructor ChannelPresenter");
    }

    @Override
    protected void loadAppInfoOnProcess() {
        try {
            getViewOrThrow().showLoading(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_check_app_info));
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    @Override
    protected void loadAppInfoOnError(Throwable throwable) {
        Log.d(this, "load app info on error", throwable);
        try {
            getViewOrThrow().hideLoading();
            //update payment status depend on api code from server
            if (throwable instanceof RequestException) {
                RequestException requestException = (RequestException) throwable;
                mPaymentInfoHelper.updateTransactionResult(requestException.code);
            }
            String message = TransactionHelper.getMessage(throwable);
            if (TextUtils.isEmpty(message)) {
                message = GlobalData.getStringResource(RS.string.sdk_load_appinfo_error_message);
            }
            getViewOrThrow().showError(message);
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    @Override
    protected void loadAppInfoOnComplete(AppInfo appInfo) {
        try {
            Log.d(this, "load app info success", appInfo);
            if (appInfo == null || !appInfo.isAllow()) {
                getViewOrThrow().showError(GlobalData.getAppContext().getString(R.string.sdk_link_not_allow));
                return;
            }
            if (!loadStaticResource(mPaymentInfoHelper.getUserInfo())) {
                getViewOrThrow().showError(GlobalData.getAppContext().getString(R.string.sdk_error_init_data));
            }
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    public AdapterBase getAdapter() {
        return mAdapter;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentInfoEvent(PaymentInfoHelper paymentInfoHelper) {
        mBus.removeStickyEvent(PaymentInfoHelper.class);
        mPaymentInfoHelper = paymentInfoHelper;
        try {
            startPayment();
        } catch (Exception e) {
            Log.e(this, e);
        }
        prepareLink();
        Log.d(this, "got event payment info from bus", paymentInfoHelper);
    }

    private boolean hasChannelList() {
        return BaseActivity.getChannelListActivity() != null &&
                !BaseActivity.getChannelListActivity().isFinishing();
    }


    private void setResult(int code, Intent data) throws Exception {
        Activity activity = getViewOrThrow().getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.setResult(code, data);
        }
    }

    public void setCallBack(int pCode) throws Exception {
        if (hasChannelList()) {
            setResult(pCode, new Intent());
        } else {
            callback();
        }
    }

    private boolean canQuit() throws Exception {
        if (getViewOrThrow().visualSupportView()) {
            getViewOrThrow().closeSupportView();
            return true;
        }
        //order is processing
        if (mAdapter != null && mAdapter.processingOrder) {
            Log.d(this, "can not back, order still request api");
            return true;
        }
        //get status again if user back when payment in bank's site
        if (mAdapter != null && !mAdapter.isFinalScreen() && mAdapter.isCardFlowWeb() &&
                (mAdapter.isCCFlow() || (mAdapter.isATMFlow() && ((BankCardGuiProcessor) mAdapter.getGuiProcessor()).isOtpWebProcessing()))) {
            getViewOrThrow().showDialogManyOption(dialogManyOptionClick);
            return true;
        }
        if (mAdapter != null && mAdapter.isATMFlow() && mAdapter.isCanEditCardInfo()) {
            ((BankCardGuiProcessor) mAdapter.getGuiProcessor()).goBackInputCard();
            return true;
        }
        if (mAdapter != null && mAdapter.isZaloPayFlow() && mAdapter.isBalanceErrorPharse()) {
            setPaymentStatusAndCallback(PaymentStatus.FAILURE);
            return false;
        }
        if (mAdapter != null && mAdapter.exitWithoutConfirm() && !isInProgress()) {
            if (mAdapter.isTransactionSuccess()) {
                setPaymentStatusAndCallback(PaymentStatus.SUCCESS);
            } else if (mAdapter.isTransactionFail()) {
                setPaymentStatusAndCallback(PaymentStatus.FAILURE);
            } else {
                setCallBack(Activity.RESULT_CANCELED);
                getViewOrThrow().terminate();
            }
            return false;
        } else {
            getViewOrThrow().showQuitConfirm(TransactionHelper.getQuitMessage(mPaymentInfoHelper), new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                    try {
                        setPaymentStatusAndCallback(PaymentStatus.FAILURE);
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                    if (GlobalData.analyticsTrackerWrapper != null) {
                        GlobalData.analyticsTrackerWrapper.trackUserCancel(false);
                    }
                }

                @Override
                public void onOKevent() {
                    showKeyBoardOnFocusingViewAgain();
                }
            });
            return true;
        }
    }

    private boolean isInProgress() {
        return DialogManager.isShowingProgressDialog();
    }

    public boolean onBackPressed() {
        try {
            return canQuit();
        } catch (Exception e) {
            Log.e(this, e);
            return false;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MAP_POPUP_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null) {
                        try {
                            setResult(MAP_POPUP_RESULT_CODE, data);
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d(this, "cancel popup map selection");
                    if (mAdapter != null && mAdapter.getGuiProcessor() != null) {
                        mAdapter.getGuiProcessor().clearCardNumberAndShowKeyBoard();
                    }
                    break;
            }
        }
    }

    private void reFillBidvCardNumber() {
        try {
            String pCardNumber = SharedPreferencesManager.getInstance().pickCachedCardNumber();
            if (!TextUtils.isEmpty(pCardNumber)) {
                mAdapter.getGuiProcessor().setCardInfo(pCardNumber);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void onUserInteraction() {
        if (mTimerRunning && !mAdapter.isFinalScreen()) {
            Log.d(this, "user tap on UI restart payment transaction countdown");
            startTransactionExpiredTimer();
        }
    }

    public void pushArgument(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mMiniPmcTransType = bundle.getParcelable(PMC_CONFIG);
        mStatusResponse = bundle.getParcelable(STATUS_RESPONSE);
    }

    public void startFlow() {
        Log.d(this, "start payment channel", mMiniPmcTransType);
        if (mMiniPmcTransType == null) {
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
            return;
        }
        try {
            mPaymentInfoHelper = PayProxy.get().getPaymentInfoHelper();
        } catch (Exception e) {
            Log.d(this, e);
        }
        if (mPaymentInfoHelper == null) {
            Log.d(this, "this channel not start from gateway channel list");
            return;
        }
        try {
            startPayment();
        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
        }
    }

    private void startPayment() throws Exception {
        Log.d(this, "start payment");
        mAdapter = AdapterFactory.create(this, mMiniPmcTransType, mPaymentInfoHelper, mStatusResponse);
        if (mAdapter == null) {
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
            return;
        }
        initTimer();
        getViewOrThrow().marginSubmitButtonTop(false);
        getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans(GlobalData.getAppContext()));
        //hide header if this is link card.
        if (mPaymentInfoHelper.isCardLinkTrans()) {
            getViewOrThrow().visiableOrderInfo(false);
        } else {
            getViewOrThrow().renderOrderInfo(mPaymentInfoHelper.getOrder());
        }
        initChannel();
    }

    private void prepareLink() {
        //this is link account and the first call
        if (mPaymentInfoHelper.isBankAccountTrans()) {
            try {
                //check static resource whether ready or not
                loadStaticResource(mPaymentInfoHelper.getUserInfo());
            } catch (Exception ex) {
                Log.e(this, ex);
                onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
            }
        }
        //this is link card channel and the first call.
        else if (mPaymentInfoHelper.isCardLinkTrans()) {
            //check profile level permission in table map
            try {
                UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
                int allowATM = userInfo.getPermissionByChannelMap(BuildConfig.channel_atm, TransactionType.LINK);
                int allowCC = userInfo.getPermissionByChannelMap(BuildConfig.channel_credit_card, TransactionType.LINK);
                if (allowATM == Constants.LEVELMAP_INVALID && allowCC == Constants.LEVELMAP_INVALID) {
                    onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
                    return;
                }
                if (allowATM == Constants.LEVELMAP_BAN && allowCC == Constants.LEVELMAP_BAN) {
                    getViewOrThrow().showUpdateLevelDialog(GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update),
                            GlobalData.getStringResource(RS.string.dialog_close_button), new ZPWOnEventConfirmDialogListener() {
                                @Override
                                public void onCancelEvent() {
                                    callBackThenTerminate();
                                }

                                @Override
                                public void onOKevent() {
                                    setPaymentStatusAndCallback(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
                                }
                            });
                    return;
                }
                hasAtm = (allowATM == Constants.LEVELMAP_ALLOW);
                hasCC = (allowCC == Constants.LEVELMAP_ALLOW);
                //switch to cc adapter if link card just allow cc without atm
                if (!hasAtm && hasCC && createChannelAdapter(BuildConfig.channel_credit_card)) {
                    initChannel();
                }
                long appId = BuildConfig.ZALOAPP_ID;
                loadAppInfo(mAppInfoInteractor, appId, TransactionType.LINK, userInfo);
            } catch (Exception ex) {
                Log.e(this, ex);
                onExit(GlobalData.getStringResource(RS.string.zingpaysdk_alert_input_error), true);
            }
        }
    }

    @Override
    public void onStart() {
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
    }

    @Override
    public void onResume() {
        showKeyBoardOnFocusingViewAgain();
        if (ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            PaymentSnackBar.getInstance().dismiss();
        } else if (!mAdapter.isFinalScreen()) {
            showNetworkOfflineSnackBar(GlobalData.getAppContext().getString(R.string.zpw_string_alert_networking_offline),
                    GlobalData.getAppContext().getString(R.string.zpw_string_remind_turn_on_networking),
                    TSnackbar.LENGTH_INDEFINITE);
        }
        Log.d(this, "onResume");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(this, "onDetach - release adapter - close loading - cancel timer");
        if (mAdapter != null) {
            mAdapter.onFinish();
            mAdapter = null;
        }
        if (DialogManager.isShowingProgressDialog()) {
            DialogManager.closeProcessDialog();
        }
        cancelTransactionExpiredTimer();
    }

    @Override
    protected void onUpdateVersion(SdkUpVersionMessage pMessage) {
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onUpVersion(pMessage.forceupdate, pMessage.version, pMessage.message);
        }
        if (pMessage.forceupdate) {
            callBackThenTerminate();
        }
    }

    private void showKeyBoardOnFocusingViewAgain() {
        //focus on editting view again after user resume
        if (getAdapter() != null && getAdapter().getGuiProcessor() != null && (getAdapter().isCardFlow() || getAdapter().isLinkAccFlow())) {
            //auto show keyboard
            if (getAdapter().isInputStep() || getAdapter().shouldFocusAfterCloseQuitDialog()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getAdapter().getGuiProcessor().onFocusView();
                            if (!getAdapter().isLinkAccFlow()) {
                                getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();//scroll to last view
                            }
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 300);
            } else if(!GlobalData.shouldNativeWebFlow()){
                getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();
            }
        }
    }

    private boolean createChannelAdapter(int pChannelId) {
        try {
            Log.d(this, "create new adapter pmc id = " + pChannelId);
            //release old adapter
            if (mAdapter != null) {
                mAdapter.onFinish();
                mAdapter = null;
            }
            int transtype = TransactionType.LINK;
            MiniPmcTransType miniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().
                    getPmcConfigByPmcID(BuildConfig.ZALOAPP_ID, transtype, pChannelId, null), MiniPmcTransType.class);
            if (miniPmcTransType != null) {
                mAdapter = AdapterFactory.createByPmc(this, miniPmcTransType, mPaymentInfoHelper, mStatusResponse);
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getStringResource(RS.string.sdk_config_invalid), true);
        }
        return false;
    }

    private void initChannel() throws Exception {
        Log.d(this, "init channel");
        try {
            mAdapter.init();
        } catch (Exception e) {
            Log.d(this, e);
            return;
        }
        if (!GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            getViewOrThrow().renderByResource(mAdapter.getPageName());
        } else {
            getViewOrThrow().renderResourceAfterDelay(mAdapter.getPageName());
        }
        getViewOrThrow().updateCardNumberFont();
    }

    public synchronized void switchChannel(int pChannelID, final String pCardNumber) {
        if (mAdapter != null && mAdapter.isATMFlow() && pChannelID == BuildConfig.channel_atm)
            return;
        if (mAdapter != null && mAdapter.isCCFlow() && pChannelID == BuildConfig.channel_credit_card)
            return;
        //prevent user move to next if input existed card in link card
        CardGuiProcessor cardGuiProcessor = mAdapter.getGuiProcessor();
        if (cardGuiProcessor != null && cardGuiProcessor.preventNextIfLinkCardExisted()) {
            try {
                cardGuiProcessor.showHintError(cardGuiProcessor.getCardNumberView(), cardGuiProcessor.warningCardExist());
                return;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (!createChannelAdapter(pChannelID)) {
            return;
        }
        setIsSwitching(true);
        try {
            initChannel();
            if (mAdapter.isCardFlow()) {
                mAdapter.getGuiProcessor().setCardInfo(pCardNumber);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void showNetworkOfflineSnackBar(String message, String btnActionText, int duration) {
        try {
            getViewOrThrow().showSnackBar(message, btnActionText, duration, mOnCloseSnackBarListener);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    /***
     * transaction expired in 7minutes
     */
    protected void initTimer() {
        int iTimeToLiveTrans = BuildConfig.transaction_expire_time;
        iTimeToLiveTrans *= 60 * 1000;
        mExpireTransTimer = new CountDownTimer(iTimeToLiveTrans, 1000) {
            public void onTick(long millisUntilFinished) {
                mTimerRunning = true;
            }

            public void onFinish() {
                mTimerRunning = false;
                Log.d(this, "Timer is onFinish");
                if (mAdapter != null && !mAdapter.isFinalScreen()) {
                    DialogManager.closeAllDialog();
                    mAdapter.showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_string_transaction_expired));
                    Log.d(this, "Moving to expired transaction screen because expiration");
                }
            }
        };
    }

    public void startTransactionExpiredTimer() {
        if (mExpireTransTimer != null) {
            mExpireTransTimer.cancel();
            mExpireTransTimer.start();
        }
    }

    public void cancelTransactionExpiredTimer() {
        if (mExpireTransTimer != null) {
            mExpireTransTimer.cancel();
        }
    }

    private void onExit(String pMessage, boolean showDialog) {
        try {
            getViewOrThrow().hideLoading();
        } catch (Exception e) {
            Log.e(this, e);
        }
        //just exit without show dialog.
        if (!showDialog) {
            callBackThenTerminate();
            return;
        }
        //continue to show dialog and quit.
        String message = pMessage;
        if (TextUtils.isEmpty(message)) {
            message = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }
        try {
            getViewOrThrow().showError(message);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public String getTransId() {
        return mAdapter != null ? mAdapter.getTransactionID() : "";
    }

    public void callBackThenTerminate() {
        Log.d(this, "call back result and end sdk - status ", mPaymentInfoHelper.getStatus());
        try {
            setCallBack(Activity.RESULT_OK);
            getViewOrThrow().terminate();
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void onSubmitClick() {
        if (mAdapter != null) {
            mAdapter.onClickSubmission();
        }
    }

    public Feedback collectFeedBack() {
        Feedback feedBack = null;
        try {
            Bitmap mBitmap = SdkUtils.CaptureScreenshot(getViewOrThrow().getActivity());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (mBitmap != null) {
                mBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            }
            byte[] byteArray = stream.toByteArray();
            String transactionTitle = mPaymentInfoHelper.getTitleByTrans(GlobalData.getAppContext());
            if (mPaymentInfoHelper.isCardLinkTrans()) {
                transactionTitle = GlobalData.getStringResource(RS.string.sdk_link_card_title);
            }
            int errorcode = mAdapter.getResponseStatus() != null ? mAdapter.getResponseStatus().returncode : Constants.NULL_ERRORCODE;
            feedBack = new Feedback(byteArray, getViewOrThrow().getFailMess(), transactionTitle, mAdapter.getTransactionID(), errorcode);
        } catch (Exception e) {
            Log.d(this, e);
        } finally {
            return feedBack;
        }
    }

    public void startSupportScreen() throws Exception {
        FeedBackCollector feedBackCollector = FeedBackCollector.shared();
        Feedback feedBack = collectFeedBack();
        if (feedBack != null) {
            FeedbackCollector collector = feedBackCollector.getFeedbackCollector();
            collector.setScreenShot(feedBack.imgByteArray);
            collector.setTransaction(feedBack.category, feedBack.transID, feedBack.errorCode, feedBack.description);
        } else {
            Log.d("support_button", "IFeedBack == null");
        }
        feedBackCollector.showDialog(getViewOrThrow().getActivity());
    }

    public void setPaymentStatusAndCallback(@PaymentStatus int pStatus) {
        mPaymentInfoHelper.setResult(pStatus);
        callBackThenTerminate();
    }

    public void resetCardNumberAndShowKeyBoard() {
        if (mAdapter != null) {
            mAdapter.getGuiProcessor().resetCardNumberAndShowKeyBoard();
        }
    }

    public boolean showBankMaintenance(ZPWOnEventDialogListener pListener, String pBankCode) {
        try {
            int bankFunction = GlobalData.getCurrentBankFunction();
            BankConfig bankConfig = mBankInteractor.getBankConfig(pBankCode);
            if (bankConfig != null && bankConfig.isBankMaintenence(bankFunction)) {
                getViewOrThrow().showInfoDialog(bankConfig.getMaintenanceMessage(bankFunction),
                        GlobalData.getAppContext().getString(R.string.dialog_retry_input_card_button),
                        pListener);
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public boolean isSwitching() {
        return mIsSwitching;
    }

    public void setIsSwitching(boolean pSwitching) {
        this.mIsSwitching = pSwitching;
    }

    private void startLink() {
        Log.d(this, "ready for payment");
        try {
            getViewOrThrow().renderByResource(mAdapter.getPageName());
            getViewOrThrow().hideLoading();
            reFillBidvCardNumber();
            showKeyBoardOnFocusingViewAgain();
            if (mAdapter instanceof AdapterLinkAcc) {
                ((AdapterLinkAcc) mAdapter).startFlow();
            }
        } catch (Exception e) {
            Log.e(this, e);
            onExit(GlobalData.getAppContext().getString(R.string.sdk_error_init_data), true);
        }
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
                    .subscribe(aBoolean -> {
                        try {
                            startLink();
                        } catch (Exception e) {
                            Log.d(this, e);
                        }
                    }, throwable -> {
                        try {
                            getViewOrThrow().showError(GlobalData.getStringResource(RS.string.zpw_generic_error));
                        } catch (Exception e) {
                            Log.d(this, e);
                        }
                        Log.e("load card and bank account error", throwable.getMessage());
                    });
            addSubscription(subscription);
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
            boolean showDialog = ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus());
            if (showDialog) {
                try {
                    getViewOrThrow().showError(message);
                } catch (Exception e) {
                    Log.d(this, e);
                }
            } else {
                try {
                    getViewOrThrow().callbackThenTerminate();
                } catch (Exception e) {
                    Log.d(this, e);
                }
            }
        }
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
        addSubscription(subscription);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUnLockScreenEvent(SdkUnlockScreenMessage pUnlockScreenEventMessage) {
        if (getAdapter() != null && mAdapter.isCardFlow()) {
            getAdapter().getGuiProcessor().moveScrollViewToCurrentFocusView();
        }
        mBus.removeStickyEvent(SdkUnlockScreenMessage.class);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentSmsEvent(SdkSmsMessage pSmsEventMessage) {
        String sender = pSmsEventMessage.sender;
        String body = pSmsEventMessage.message;
        if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(body) && getAdapter() != null) {
            getAdapter().autoFillOtp(sender, body);
        }
        mBus.removeStickyEvent(SdkSmsMessage.class);
        Log.d(this, "on payment otp event " + GsonUtils.toJsonString(pSmsEventMessage));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkEvent(SdkNetworkEvent networkEvent) {
        if (mAdapter.isFinalScreen()) {
            Log.d(this, "onNetworkMessageEvent user is on fail screen...");
            return;
        }
        Log.d(this, "networking is changed ", networkEvent.online);
        //come from api request fail with handshake
        if (networkEvent.origin == API) {
            showNetworkOfflineSnackBar(GlobalData.getAppContext().getString(R.string.zpw_string_alert_networking_not_stable),
                    GlobalData.getAppContext().getString(R.string.zpw_string_remind_turn_on_networking), TSnackbar.LENGTH_LONG);
            Log.d(this, "networking is not stable");
        } else if (!networkEvent.online) {
            showNetworkOfflineSnackBar(GlobalData.getAppContext().getString(R.string.zpw_string_alert_networking_offline),
                    GlobalData.getAppContext().getString(R.string.zpw_string_remind_turn_on_networking),
                    TSnackbar.LENGTH_INDEFINITE);
        } else {
            PaymentSnackBar.getInstance().dismiss();
        }
    }

}
