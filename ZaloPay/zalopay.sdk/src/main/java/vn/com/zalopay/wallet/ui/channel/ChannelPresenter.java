package vn.com.zalopay.wallet.ui.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.zalopay.feedback.FeedbackCollector;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.feedback.Feedback;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.dialog.ZPWResultCallBackListener;
import vn.com.zalopay.wallet.event.SdkInvalidPaymentInfo;
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkPaymentInfoReadyMessage;
import vn.com.zalopay.wallet.event.SdkSmsMessage;
import vn.com.zalopay.wallet.event.SdkUnlockScreenMessage;
import vn.com.zalopay.wallet.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.helper.BankHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.VersionCallback;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.PaymentPresenter;
import vn.com.zalopay.wallet.ui.channellist.ChannelListActivity;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.AccountLinkWorkFlow;
import vn.com.zalopay.wallet.workflow.WorkFlowFactoryCreator;
import vn.com.zalopay.wallet.workflow.ui.BankCardGuiProcessor;
import vn.com.zalopay.wallet.workflow.ui.CardGuiProcessor;

import static vn.com.zalopay.wallet.constants.Constants.AMOUNT_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.API;
import static vn.com.zalopay.wallet.constants.Constants.BANKCODE_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.BUTTON_LEFT_TEXT_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.CARDNUMBER_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_RESULT_CODE;
import static vn.com.zalopay.wallet.constants.Constants.NOTICE_CONTENT_EXTRA;
import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;
import static vn.com.zalopay.wallet.constants.Constants.SELECTED_PMC_POSITION;
import static vn.com.zalopay.wallet.constants.Constants.STATUS_RESPONSE;

/*
 * Created by chucvv on 6/12/17.
 */

public class ChannelPresenter extends PaymentPresenter<ChannelFragment> {
    @Inject
    public EventBus mBus;
    @Inject
    Context mContext;
    @Inject
    AppInfoStore.Interactor appInfoInteractor;
    AbstractWorkFlow mAbstractWorkFlow = null;
    private onCloseSnackBar mOnCloseSnackBarListener = () -> {
        if (mAbstractWorkFlow != null) {
            mAbstractWorkFlow.checkAndOpenNetworkingSetting();
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
                callback();
                break;
            case 2:
                try {
                    if (mAbstractWorkFlow != null) {
                        mAbstractWorkFlow.handleEventLoadSiteError(new Object());
                    }
                } catch (Exception e) {
                    Timber.w(e);
                }
                break;
        }
    };
    private ZPWResultCallBackListener resultCallBackListener = new ZPWResultCallBackListener() {
        @Override
        public void onResultOk(int pReturnCode, int pData) {
            try {
                Timber.d("onActivityResult data %s", pData);
                Intent intent = new Intent();
                intent.putExtra(SELECTED_PMC_POSITION, pData);
                setResult(MAP_POPUP_RESULT_CODE, intent);
                getViewOrThrow().terminate();
            } catch (Exception e) {
                Timber.w(e);
            }
        }

        @Override
        public void onCancel(int pReturnCode) {
            Timber.d("cancel popup map selection");
            try {
                if (mAbstractWorkFlow == null || mAbstractWorkFlow.getGuiProcessor() == null) {
                    return;
                }
                mAbstractWorkFlow.getGuiProcessor().clearCardNumberAndShowKeyBoard();
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }
    };

    public ChannelPresenter() {
        mPaymentInfoHelper = GlobalData.getPaymentInfoHelper();
        SDKApplication.getApplicationComponent().inject(this);
        Timber.d("call constructor ChannelPresenter");
    }

    private boolean validPaymentInfo() {
        if (mPaymentInfoHelper == null) {
            mPaymentInfoHelper = GlobalData.getPaymentInfoHelper();
        }
        return mPaymentInfoHelper != null;
    }

    public AbstractWorkFlow getWorkFlow() {
        return mAbstractWorkFlow;
    }

    private boolean hasChannelList() {
        ChannelListActivity channelListActivity = BaseActivity.getChannelListActivity();
        return channelListActivity != null && !channelListActivity.isFinishing();
    }

    void setResult(int code, Intent data) throws Exception {
        Activity activity = getViewOrThrow().getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.setResult(code, data);
        }
    }

    public void setCallBack(int pCode) throws Exception {
        if (hasChannelList()) {
            setResult(pCode, new Intent());
        } else if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
    }

    private boolean canQuit() throws Exception {
        if (getViewOrThrow().visualSupportView()) {
            getViewOrThrow().onCloseSupportView();
            return true;
        }
        //order is processing
        if (mAbstractWorkFlow != null && mAbstractWorkFlow.mOrderProcessing) {
            Timber.d("can not back, order still request api");
            return true;
        }
        //get status again if user back when payment in bank's site
        if (mAbstractWorkFlow != null && !mAbstractWorkFlow.isFinalScreen() && mAbstractWorkFlow.isCardFlowWeb() &&
                (mAbstractWorkFlow.isCCFlow() || (mAbstractWorkFlow.isATMFlow() && ((BankCardGuiProcessor) mAbstractWorkFlow.getGuiProcessor()).isOtpWebProcessing()))) {
            getViewOrThrow().showDialogManyOption(dialogManyOptionClick);
            return true;
        }
        if (mAbstractWorkFlow != null && mAbstractWorkFlow.isATMFlow() && mAbstractWorkFlow.isCanEditCardInfo() && mAbstractWorkFlow.hasCardGuiProcessor()) {
            ((BankCardGuiProcessor) mAbstractWorkFlow.getGuiProcessor()).goBackInputCard();
            return true;
        }
        if (mAbstractWorkFlow != null && mAbstractWorkFlow.isZaloPayFlow() && mAbstractWorkFlow.isBalanceErrorPharse()) {
            setPaymentStatusAndCallback(PaymentStatus.FAILURE);
            return false;
        }
        if (mAbstractWorkFlow != null && mAbstractWorkFlow.exitWithoutConfirm() && !isInProgress()) {
            if (mAbstractWorkFlow.isTransactionSuccess()) {
                setPaymentStatusAndCallback(PaymentStatus.SUCCESS);
            } else if (mAbstractWorkFlow.isTransactionFail()) {
                setPaymentStatusAndCallback(PaymentStatus.FAILURE);
            } else {
                setCallBack(Activity.RESULT_CANCELED);
                getViewOrThrow().terminate();
            }
            return false;
        } else {
            String quitMessage = mPaymentInfoHelper != null ? mPaymentInfoHelper.getQuitMessByTrans(mContext) : null;
            if (TextUtils.isEmpty(quitMessage)) {
                return false;
            }
            getViewOrThrow().showQuitConfirm(quitMessage, new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                    try {
                        setPaymentStatusAndCallback(PaymentStatus.FAILURE);
                        if (GlobalData.analyticsTrackerWrapper != null) {
                            GlobalData.analyticsTrackerWrapper.trackUserCancel();
                        }
                    } catch (Exception e) {
                        Timber.w(e);
                    }
                }

                @Override
                public void onOKEvent() {
                    showKeyBoard();
                }
            });
            return true;
        }
    }

    private boolean isInProgress() {
        return DialogManager.showingLoadDialog();
    }

    public boolean onBackPressed() {
        try {
            return canQuit();
        } catch (Exception e) {
            Timber.w(e);
        }
        return false;
    }

    public void pushArgument(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mMiniPmcTransType = bundle.getParcelable(PMC_CONFIG);
        mStatusResponse = bundle.getParcelable(STATUS_RESPONSE);
    }

    public void startPayment() {
        try {
            if (!validPaymentInfo()) {
                return;
            }
            getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans(mContext));
            getViewOrThrow().visibleOrderInfo(!mPaymentInfoHelper.isLinkTrans());
            if (mPaymentInfoHelper.isLinkTrans()) {
                startSubscribePaymentReadyMessage();
                return;
            }
            if (mMiniPmcTransType == null) {
                onExit(mContext.getResources().getString(R.string.sdk_config_invalid), true);
                return;
            }
            Timber.d("start payment channel %s", mMiniPmcTransType);
            getViewOrThrow().renderOrderInfo(mPaymentInfoHelper.getOrder());
            mAbstractWorkFlow = WorkFlowFactoryCreator.create(mContext, this, mMiniPmcTransType, mPaymentInfoHelper, mStatusResponse);
            if (mAbstractWorkFlow == null) {
                onExit(mContext.getResources().getString(R.string.sdk_invalid_payment_data), true);
                return;
            }
            initWorkFlow();
        } catch (Exception e) {
            Timber.d(e, "Exception on start payment");
            onExit(mContext.getResources().getString(R.string.zpw_string_error_layout), true);
        }
    }

    @Override
    protected void onProcessPaymentInfo(SdkPaymentInfoReadyMessage message) throws Exception {
        if (message == null) {
            callback();
            return;
        }
        if (message.mPlatformInfoCallback instanceof VersionCallback) {
            VersionCallback versionCallback = (VersionCallback) message.mPlatformInfoCallback;
            onProcessUpVersionMessage(versionCallback);
            if (versionCallback.forceupdate) {
                return;
            }
        }
        if (message.mErrorType == SdkPaymentInfoReadyMessage.ErrorType.SUCCESS) {
            startLink();
            return;
        }
        Timber.d("payment info on error %s", message.mError.getMessage());
        String error = TransactionHelper.getMessage(mContext, message.mError);
        boolean showDialog = mPaymentInfoHelper != null && ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus());
        onExit(error, showDialog);
    }

    private boolean allowLink() {
        return PaymentPermission.allowLinkAtm() || PaymentPermission.allowLinkCC();
    }

    private MiniPmcTransType loadLinkConfig(boolean bankLink, String pBankCode) {
        boolean internationalBank = BankHelper.isInternationalBank(pBankCode);
        return appInfoInteractor.getPmcTranstype(BuildConfig.ZALOPAY_APPID, TransactionType.LINK, bankLink, internationalBank, null);
    }

    private AbstractWorkFlow createWorkFlow(MiniPmcTransType pmcTransType) {
        if (pmcTransType == null) {
            return null;
        }
        return WorkFlowFactoryCreator.create(mContext, this, pmcTransType, mPaymentInfoHelper, mStatusResponse);
    }

    private void startLink() {
        try {
            if (!validPaymentInfo()) {
                return;
            }
            @CardType String bankCode = mPaymentInfoHelper.getCardTypeLink();
            boolean isBankAccountLink = mPaymentInfoHelper.isBankAccountTrans();
            Timber.d("start link bank %s", bankCode);
            if (!allowLink() && !mPaymentInfoHelper.isBankAccountTrans()) {
                onExit(mContext.getString(R.string.sdk_error_ban_link), true);
                return;
            }
            mMiniPmcTransType = loadLinkConfig(isBankAccountLink, bankCode);
            if (mMiniPmcTransType == null) {
                onExit(mContext.getResources().getString(R.string.sdk_config_invalid), true);
                return;
            }
            mAbstractWorkFlow = createWorkFlow(mMiniPmcTransType);
            if (mAbstractWorkFlow == null) {
                onExit(mContext.getResources().getString(R.string.sdk_invalid_payment_data), true);
                return;
            }
            initWorkFlow();
            if (mAbstractWorkFlow instanceof AccountLinkWorkFlow) {
                ((AccountLinkWorkFlow) mAbstractWorkFlow).startFlow();
            } else {
                showKeyBoard();
            }
        } catch (Exception e) {
            Timber.d(e, "Exception start link");
            onExit(mContext.getResources().getString(R.string.sdk_error_init_data), true);
        }
    }

    public void switchWorkFlow(int pChannelID, final String pCardNumber) throws Exception {
        if (mAbstractWorkFlow == null) {
            return;
        }
        if (mAbstractWorkFlow.isATMFlow() && pChannelID == BuildConfig.channel_atm) {
            return;
        }
        if (mAbstractWorkFlow.isCCFlow() && pChannelID == BuildConfig.channel_credit_card) {
            return;
        }
        //prevent user move to next if input existed card in link card
        CardGuiProcessor cardGuiProcessor = mAbstractWorkFlow.getGuiProcessor();
        if (cardGuiProcessor != null && cardGuiProcessor.preventNextIfLinkCardExisted()) {
            try {
                cardGuiProcessor.showHintError(cardGuiProcessor.getCardNumberView(), cardGuiProcessor.warningCardExist());
                return;
            } catch (Exception e) {
                Timber.w(e, "Exception switchWorkFlow");
            }
        }
        if (!createWorkFlowForLink(pChannelID)) {
            return;
        }
        this.mIsSwitching = true;
        if (mAbstractWorkFlow.isCardFlow() && mAbstractWorkFlow.getGuiProcessor() != null) {
            mAbstractWorkFlow.getGuiProcessor().setCardInfo(pCardNumber);
        }
    }

    private boolean createWorkFlowForLink(int pChannelId) {
        try {
            MiniPmcTransType miniPmcTransType = appInfoInteractor.getPmcTranstype(BuildConfig.ZALOPAY_APPID, TransactionType.LINK, pChannelId, null);
            if (miniPmcTransType == null) {
                return false;
            }
            Timber.d("create new work flow pmc id = %s", pChannelId);
            //release old adapter
            if (mAbstractWorkFlow != null) {
                mAbstractWorkFlow.onDetach();
                mAbstractWorkFlow = null;
            }
            mAbstractWorkFlow = createWorkFlow(miniPmcTransType);
            initWorkFlow();
            mMiniPmcTransType = miniPmcTransType;
        } catch (Exception e) {
            Timber.w(e, "Exception on create adapter by channel id %s", pChannelId);
            onExit(mContext.getResources().getString(R.string.zpw_string_error_layout), true);
        }
        return true;
    }

    @Override
    public void onStart() {
        mBus.register(this);
        if (mAbstractWorkFlow != null) {
            mAbstractWorkFlow.onStart();
        }
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        if (mAbstractWorkFlow != null) {
            mAbstractWorkFlow.onStop();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onResume() {
        if (mAbstractWorkFlow == null) {
            return;
        }
        if (!mAbstractWorkFlow.isFinalScreen()) {
            showKeyBoard();
        }
        if (ConnectionUtil.isOnline(mContext)) {
            PaymentSnackBar.getInstance().dismiss();
        } else if (!mAbstractWorkFlow.isFinalScreen()) {
            showNetworkOfflineSnackBar(mContext.getResources().getString(R.string.sdk_offline_networking_mess),
                    mContext.getResources().getString(R.string.sdk_turn_on_networking_mess),
                    TSnackbar.LENGTH_INDEFINITE);
        }
        Timber.d("onResume");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.d("onDetach - close loading - cancel timer");
        if (mAbstractWorkFlow != null) {
            mAbstractWorkFlow.onDetach();
            mAbstractWorkFlow = null;
        }
        if (DialogManager.showingLoadDialog()) {
            DialogManager.closeLoadDialog();
        }
    }

    void showKeyBoard() {
        try {
            if (mAbstractWorkFlow == null || mAbstractWorkFlow.getGuiProcessor() == null) {
                return;
            }
            //auto show keyboard
            if (mAbstractWorkFlow.isInputStep() || mAbstractWorkFlow.shouldFocusAfterCloseQuitDialog()) {
                new Handler().postDelayed(() -> {
                    try {
                        mAbstractWorkFlow.getGuiProcessor().onFocusView();
                        if (!mAbstractWorkFlow.isLinkAccFlow()) {
                            mAbstractWorkFlow.getGuiProcessor().moveScrollViewToCurrentFocusView();//scroll to last view
                        }
                    } catch (Exception e) {
                        Timber.w(e);
                    }
                }, 300);
            } else if (!PaymentPermission.allowVCBNativeFlow()) {
                mAbstractWorkFlow.getGuiProcessor().moveScrollViewToCurrentFocusView();
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    private void initWorkFlow() throws Exception {
        try {
            if (mAbstractWorkFlow == null) {
                return;
            }
            Timber.d("start init channel %s", mAbstractWorkFlow.getClass().getSimpleName());
            mAbstractWorkFlow.init();
            mAbstractWorkFlow.onStart();
            getViewOrThrow().renderByResource(mAbstractWorkFlow.getPageName());
            getViewOrThrow().updateCardNumberFont();
        } catch (Exception e) {
            Timber.w(e, "Exception init channel");
        }
    }

    private void showNetworkOfflineSnackBar(String message, String btnActionText, int duration) {
        try {
            getViewOrThrow().showSnackBar(message, btnActionText, duration, mOnCloseSnackBarListener);
        } catch (Exception e) {
            Timber.w(e, "Exception showNetworkOfflineSnackBar");
        }
    }

    private void onExit(String pMessage, boolean showDialog) {
        try {
            getViewOrThrow().hideLoading();
        } catch (Exception ignored) {
        }
        //just exit without show dialog.
        if (!showDialog) {
            callback();
            return;
        }
        //continue to show dialog and quit.
        String message = pMessage;
        if (TextUtils.isEmpty(message)) {
            message = mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
        }
        try {
            getViewOrThrow().showError(message);
        } catch (Exception e) {
            Timber.w(e, "Exception onExit");
        }
    }

    public String getTransId() {
        return mAbstractWorkFlow != null ? mAbstractWorkFlow.getTransactionID() : "";
    }

    @Override
    public void callback() {
        try {
            Timber.d("call back result and end sdk");
            setCallBack(Activity.RESULT_OK);
            getViewOrThrow().terminate();
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void callbackLink(@CardType String bankLink) {
        try {
            Timber.d("call back link %s", bankLink);
            Intent intent = TransactionHelper.createLinkIntent(bankLink);
            setResult(Constants.LINK_ACCOUNT_RESULT_CODE, intent);
            getViewOrThrow().terminate();
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void onSubmitClick() {
        if (mAbstractWorkFlow == null) {
            callback();
            return;
        }
        mAbstractWorkFlow.onClickSubmission();
    }

    public void showInstructRegiterBIDV() {
        try {
            SdkUtils.openWebPage(getViewOrThrow().getActivity(), GlobalData.getStringResource(RS.string.sdk_website_instruct_register_bidv_url));
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void showFeedbackDialog() throws Exception {
        if (!validPaymentInfo()) {
            return;
        }
        if (mAbstractWorkFlow == null) {
            return;
        }
        FeedBackCollector feedBackCollector = FeedBackCollector.shared();
        String transTitle = mPaymentInfoHelper.getTitleByTrans(mContext);
        int errorCode = mAbstractWorkFlow.getResponseStatus() != null ? mAbstractWorkFlow.getResponseStatus().returncode : Constants.NULL_ERRORCODE;
        Feedback feedBack = Feedback.collectFeedBack(getViewOrThrow().getActivity(), transTitle,
                getViewOrThrow().getFailMess(), errorCode, mAbstractWorkFlow.getTransactionID());
        if (feedBack != null) {
            FeedbackCollector collector = feedBackCollector.getFeedbackCollector();
            collector.setScreenShot(feedBack.imgByteArray);
            collector.setTransaction(feedBack.category, feedBack.transID, feedBack.errorCode, feedBack.description);
        }
        feedBackCollector.showDialog(getViewOrThrow().getActivity());
    }

    public void setPaymentStatusAndCallback(@PaymentStatus int pStatus) {
        if (!validPaymentInfo()) {
            return;
        }
        mPaymentInfoHelper.setResult(pStatus);
        callback();
    }

    void resetCardNumberAndShowKeyBoard() throws Exception {
        if (mAbstractWorkFlow != null && mAbstractWorkFlow.hasCardGuiProcessor()) {
            mAbstractWorkFlow.getGuiProcessor().resetCardNumberAndShowKeyBoard();
        }
    }

    public boolean isSwitchAdapter() {
        return mIsSwitching;
    }

    public void setSwitchAdapter(boolean pSwitching) {
        this.mIsSwitching = pSwitching;
    }

    public void showMapBankDialog(boolean isBIDVBank) throws Exception {
        if (mPaymentInfoHelper == null) {
            return;
        }
        if (!isBIDVBank) {
            Bundle bundle = new Bundle();
            bundle.putDouble(AMOUNT_EXTRA, mPaymentInfoHelper.getAmountTotal());
            bundle.putString(BUTTON_LEFT_TEXT_EXTRA, mContext.getString(R.string.dialog_retry_input_card_button));
            bundle.putString(BANKCODE_EXTRA, CardType.PVCB);
            getViewOrThrow().showMapBankDialog(bundle, resultCallBackListener);
        } else if (getWorkFlow().getGuiProcessor() != null) {
            Bundle bundle = new Bundle();
            bundle.putDouble(AMOUNT_EXTRA, mPaymentInfoHelper.getAmountTotal());
            bundle.putString(BUTTON_LEFT_TEXT_EXTRA, mContext.getString(R.string.dialog_retry_input_card_button));
            bundle.putString(BANKCODE_EXTRA, CardType.PBIDV);
            bundle.putString(CARDNUMBER_EXTRA, getWorkFlow().getGuiProcessor().getCardNumber());
            bundle.putString(NOTICE_CONTENT_EXTRA, GlobalData.getAppContext().getResources().getString(R.string.zpw_warning_bidv_select_linkcard_payment));
            getViewOrThrow().showMapBankDialog(bundle, resultCallBackListener);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUnLockScreen(SdkUnlockScreenMessage message) {
        try {
            if (mAbstractWorkFlow != null && mAbstractWorkFlow.isCardFlow() && mAbstractWorkFlow.hasCardGuiProcessor()) {
                mAbstractWorkFlow.getGuiProcessor().moveScrollViewToCurrentFocusView();
            }
            mBus.removeStickyEvent(SdkUnlockScreenMessage.class);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentSms(SdkSmsMessage message) {
        if (mAbstractWorkFlow == null) {
            return;
        }
        String sender = message.sender;
        String body = message.message;
        if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(body)) {
            mAbstractWorkFlow.autoFillOtp(sender, body);
        }
        mBus.removeStickyEvent(SdkSmsMessage.class);
        Timber.d("on payment otp event %s", message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkChanged(SdkNetworkEvent message) {
        if (mAbstractWorkFlow != null && mAbstractWorkFlow.isFinalScreen()) {
            Timber.d("onNetworkMessageEvent user is on fail screen...");
            return;
        }
        Timber.d("networking is changed online : %s", message.online);
        //come from api request fail with handshake
        if (message.origin == API) {
            showNetworkOfflineSnackBar(mContext.getResources().getString(R.string.sdk_not_stable_networking_mess),
                    mContext.getResources().getString(R.string.sdk_turn_on_networking_mess), TSnackbar.LENGTH_LONG);
            Timber.d("networking is not stable");
        } else if (!message.online) {
            showNetworkOfflineSnackBar(mContext.getResources().getString(R.string.sdk_offline_networking_mess),
                    mContext.getResources().getString(R.string.sdk_turn_on_networking_mess),
                    TSnackbar.LENGTH_INDEFINITE);
        } else {
            PaymentSnackBar.getInstance().dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvalidPaymentInfo(SdkInvalidPaymentInfo event) {
        try {
            getViewOrThrow().showError(mContext.getResources().getString(R.string.sdk_error_paymentinfo_empty));
        } catch (Exception e) {
            Timber.w(e, "Exception invalid payment info");
        }
    }

}
