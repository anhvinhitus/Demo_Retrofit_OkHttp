package vn.com.zalopay.wallet.ui.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.zalopay.feedback.FeedbackCollector;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.factory.AdapterFactory;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.channel.linkacc.AdapterLinkAcc;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardGuiProcessor;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.feedback.Feedback;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.error.ErrorManager;
import vn.com.zalopay.wallet.business.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.Link_Then_Pay;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.dialog.ZPWResultCallBackListener;
import vn.com.zalopay.wallet.event.SdkNetworkEvent;
import vn.com.zalopay.wallet.event.SdkPaymentInfoReadyMessage;
import vn.com.zalopay.wallet.event.SdkSmsMessage;
import vn.com.zalopay.wallet.event.SdkUnlockScreenMessage;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.interactor.VersionCallback;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.pay.PayProxy;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.PaymentPresenter;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

import static vn.com.zalopay.wallet.constants.Constants.API;
import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_RESULT_CODE;
import static vn.com.zalopay.wallet.constants.Constants.PMC_CONFIG;
import static vn.com.zalopay.wallet.constants.Constants.SELECTED_PMC_POSITION;
import static vn.com.zalopay.wallet.constants.Constants.STATUS_RESPONSE;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelPresenter extends PaymentPresenter<ChannelFragment> {
    @Inject
    public EventBus mBus;
    @Inject
    Context mContext;

    boolean mTimerRunning = false;
    AdapterBase mAdapter = null;
    private CountDownTimer mExpireTransTimer;
    private onCloseSnackBar mOnCloseSnackBarListener = () -> {
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
                callback();
                break;
            case 2:
                mAdapter.onEvent(EEventType.ON_BACK_WHEN_LOADSITE, new Object());
                break;
        }
    };

    public ChannelPresenter() {
        try {
            mPaymentInfoHelper = PayProxy.get().getPaymentInfoHelper();
        } catch (Exception e) {
        }
        if (mPaymentInfoHelper == null) {
            mPaymentInfoHelper = GlobalData.paymentInfoHelper;
        }
        SDKApplication.getApplicationComponent().inject(this);
        Timber.d("call constructor ChannelPresenter");
    }

    public AdapterBase getAdapter() {
        return mAdapter;
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
        } else if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
    }

    private boolean canQuit() throws Exception {
        if (getViewOrThrow().visualSupportView()) {
            getViewOrThrow().closeSupportView();
            return true;
        }
        //order is processing
        if (mAdapter != null && mAdapter.processingOrder) {
            Timber.d("can not back, order still request api");
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
            getViewOrThrow().showQuitConfirm(mPaymentInfoHelper.getQuitMessByTrans(mContext), new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                    try {
                        setPaymentStatusAndCallback(PaymentStatus.FAILURE);
                        if (GlobalData.analyticsTrackerWrapper != null) {
                            GlobalData.analyticsTrackerWrapper.trackUserCancel();
                        }
                    } catch (Exception e) {
                        Log.e(this, e);
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
            Timber.d("user tap on UI restart payment transaction countdown");
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

    public void startPayment() {
        try {
            if (mPaymentInfoHelper == null) {
                callback();
                return;
            }
            initTimer();
            getViewOrThrow().setTitle(mPaymentInfoHelper.getTitleByTrans(mContext));
            getViewOrThrow().visiableOrderInfo(!mPaymentInfoHelper.isLinkTrans());
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
            mAdapter = AdapterFactory.create(mContext, this, mMiniPmcTransType, mPaymentInfoHelper, mStatusResponse);
            if (mAdapter == null) {
                onExit(mContext.getResources().getString(R.string.sdk_invalid_payment_data), true);
                return;
            }
            initAdapter();
        } catch (Exception e) {
            Timber.w(e, "Exception on start payment");
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
        String error = TransactionHelper.getMessage(mContext,message.mError);
        boolean showDialog = ErrorManager.shouldShowDialog(mPaymentInfoHelper.getStatus());
        onExit(error, showDialog);
    }

    private boolean allowLink() {
        return PaymentPermission.allowLinkAtm() || PaymentPermission.allowLinkCC();
    }

    private MiniPmcTransType loadLinkConfig(boolean bankLink) {
        return SDKApplication
                .getApplicationComponent()
                .appInfoInteractor()
                .getPmcTranstype(BuildConfig.ZALOPAY_APPID, TransactionType.LINK, bankLink, null);
    }

    private void startLink() {
        Timber.d("start link channel");
        try {
            if (!allowLink() && !mPaymentInfoHelper.isBankAccountTrans()) {
                onExit(mContext.getString(R.string.sdk_error_ban_link), true);
                return;
            }
            mMiniPmcTransType = loadLinkConfig(mPaymentInfoHelper.isBankAccountTrans());
            if (mMiniPmcTransType == null) {
                onExit(mContext.getResources().getString(R.string.sdk_config_invalid), true);
                return;
            }
            mAdapter = AdapterFactory.create(mContext, this, mMiniPmcTransType, mPaymentInfoHelper, mStatusResponse);
            if (mAdapter == null) {
                onExit(mContext.getResources().getString(R.string.sdk_invalid_payment_data), true);
                return;
            }
            initAdapter();
            if (mAdapter instanceof AdapterLinkAcc) {
                ((AdapterLinkAcc) mAdapter).startFlow();
            } else {
                reFillBidvCardNumber();
                showKeyBoard();
            }
        } catch (Exception e) {
            Log.e(this, e);
            onExit(mContext.getResources().getString(R.string.sdk_error_init_data), true);
        }
    }

    public void switchCardLinkAdapter(int pChannelID, final String pCardNumber) {
        if (mAdapter == null) {
            return;
        }
        if (mAdapter.isATMFlow() && pChannelID == BuildConfig.channel_atm) {
            return;
        }
        if (mAdapter.isCCFlow() && pChannelID == BuildConfig.channel_credit_card) {
            return;
        }
        //prevent user move to next if input existed card in link card
        CardGuiProcessor cardGuiProcessor = mAdapter.getGuiProcessor();
        if (cardGuiProcessor != null && cardGuiProcessor.preventNextIfLinkCardExisted()) {
            try {
                cardGuiProcessor.showHintError(cardGuiProcessor.getCardNumberView(), cardGuiProcessor.warningCardExist());
                return;
            } catch (Exception e) {
                Timber.w(e, "Exception switchCardLinkAdapter");
            }
        }
        if (!createLinkAdapter(pChannelID)) {
            return;
        }
        try {
            this.mIsSwitching = true;
            if (mAdapter.isCardFlow()) {
                mAdapter.getGuiProcessor().setCardInfo(pCardNumber);
            }
        } catch (Exception e) {
            Timber.w(e, "Exception on switching channel");
        }
    }

    private boolean createLinkAdapter(int pChannelId) {
        try {
            MiniPmcTransType miniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().
                    getPmcConfigByPmcID(BuildConfig.ZALOPAY_APPID, TransactionType.LINK, pChannelId, null), MiniPmcTransType.class);
            if (miniPmcTransType == null) {
                return false;
            }
            Timber.d("create new adapter pmc id = %s", pChannelId);
            //release old adapter
            if (mAdapter != null) {
                mAdapter.onFinish();
                mAdapter = null;
            }
            mAdapter = AdapterFactory.createByPmc(mContext, this, miniPmcTransType, mPaymentInfoHelper, mStatusResponse);
            initAdapter();
            mMiniPmcTransType = miniPmcTransType;
        } catch (Exception e) {
            Timber.w(e, "Exception on create adapter by channel id %s", pChannelId);
            onExit(mContext.getResources().getString(R.string.sdk_config_invalid), true);
        }
        return true;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onResume() {
        if (mAdapter == null) {
            return;
        }
        if (!mAdapter.isFinalScreen()) {
            showKeyBoard();
        }
        if (ConnectionUtil.isOnline(mContext)) {
            PaymentSnackBar.getInstance().dismiss();
        } else if (!mAdapter.isFinalScreen()) {
            showNetworkOfflineSnackBar(mContext.getResources().getString(R.string.sdk_offline_networking_mess),
                    mContext.getResources().getString(R.string.sdk_turn_on_networking_mess),
                    TSnackbar.LENGTH_INDEFINITE);
        }
        Timber.d("onResume");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.d("onDetach - release adapter - close loading - cancel timer");
        if (mAdapter != null) {
            mAdapter.onFinish();
            mAdapter = null;
        }
        if (DialogManager.isShowingProgressDialog()) {
            DialogManager.closeProcessDialog();
        }
        cancelTransactionExpiredTimer();
    }

    /***
     * focus on current view
     */
    void showKeyBoard() {
        if (mAdapter != null && mAdapter.getGuiProcessor() != null && (mAdapter.isCardFlow() || mAdapter.isLinkAccFlow())) {
            //auto show keyboard
            if (mAdapter.isInputStep() || mAdapter.shouldFocusAfterCloseQuitDialog()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mAdapter.getGuiProcessor().onFocusView();
                            if (!mAdapter.isLinkAccFlow()) {
                                mAdapter.getGuiProcessor().moveScrollViewToCurrentFocusView();//scroll to last view
                            }
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 300);
            } else if (!GlobalData.shouldNativeWebFlow()) {
                mAdapter.getGuiProcessor().moveScrollViewToCurrentFocusView();
            }
        }
    }

    private void initAdapter() throws Exception {
        try {
            if (mAdapter == null) {
                return;
            }
            Timber.d("start init channel %s", mAdapter.getClass().getSimpleName());
            mAdapter.init();
            getViewOrThrow().renderByResource(mAdapter.getPageName());
            getViewOrThrow().marginSubmitButtonTop(false);
            getViewOrThrow().updateCardNumberFont();
        } catch (Exception e) {
            Timber.w(e, "Exception init channel");
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
    private void initTimer() {
        int iTimeToLiveTrans = BuildConfig.transaction_expire_time;
        iTimeToLiveTrans *= 60 * 1000;
        mExpireTransTimer = new CountDownTimer(iTimeToLiveTrans, 1000) {
            public void onTick(long millisUntilFinished) {
                mTimerRunning = true;
            }

            public void onFinish() {
                mTimerRunning = false;
                Timber.d("Timer is onFinish");
                if (mAdapter != null && !mAdapter.isFinalScreen()) {
                    DialogManager.closeAllDialog();
                    mAdapter.showTransactionFailView(mContext.getResources().getString(R.string.sdk_expire_transaction_mess));
                    Timber.d("Moving to expired transaction screen because expiration");
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
            Log.e(this, e);
        }
    }

    public String getTransId() {
        return mAdapter != null ? mAdapter.getTransactionID() : "";
    }

    @Override
    public void callback() {
        try {
            Timber.d("call back result and end sdk");
            setCallBack(Activity.RESULT_OK);
            getViewOrThrow().terminate();
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    public void callbackLinkThenPay(@Link_Then_Pay int bankLink) {
        try {
            Timber.d("call back link then pay");
            Intent intent = new Intent();
            intent.putExtra("bank", bankLink);
            setResult(Constants.LINK_THEN_PAY_RESULT_CODE, intent);
            getViewOrThrow().terminate();
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
    }

    public void onSubmitClick() {
        if (mAdapter == null) {
            callback();
            return;
        }
        mAdapter.onClickSubmission();
    }

    private Feedback collectFeedBack() {
        Feedback feedBack = null;
        try {
            Bitmap mBitmap = SdkUtils.CaptureScreenshot(getViewOrThrow().getActivity());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (mBitmap != null) {
                mBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            }
            byte[] byteArray = stream.toByteArray();
            String transactionTitle = mPaymentInfoHelper.getTitleByTrans(mContext);
            int errorcode = mAdapter.getResponseStatus() != null ? mAdapter.getResponseStatus().returncode : Constants.NULL_ERRORCODE;
            feedBack = new Feedback(byteArray, getViewOrThrow().getFailMess(), transactionTitle, mAdapter.getTransactionID(), errorcode);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return feedBack;
    }

    public void startSupportScreen() throws Exception {
        FeedBackCollector feedBackCollector = FeedBackCollector.shared();
        Feedback feedBack = collectFeedBack();
        if (feedBack != null) {
            FeedbackCollector collector = feedBackCollector.getFeedbackCollector();
            collector.setScreenShot(feedBack.imgByteArray);
            collector.setTransaction(feedBack.category, feedBack.transID, feedBack.errorCode, feedBack.description);
        } else {
            Timber.d("IFeedBack is null");
        }
        feedBackCollector.showDialog(getViewOrThrow().getActivity());
    }

    public void setPaymentStatusAndCallback(@PaymentStatus int pStatus) {
        mPaymentInfoHelper.setResult(pStatus);
        callback();
    }

    void resetCardNumberAndShowKeyBoard() {
        if (mAdapter != null) {
            mAdapter.getGuiProcessor().resetCardNumberAndShowKeyBoard();
        }
    }

    public boolean isSwitchAdapter() {
        return mIsSwitching;
    }

    public void setSwitchAdapter(boolean pSwitching) {
        this.mIsSwitching = pSwitching;
    }

    public void showMapBankDialog(boolean isBIDVBank) {
        if (mPaymentInfoHelper == null) {
            return;
        }
        if (!isBIDVBank) {
            getView().showMapBankDialog(mPaymentInfoHelper.getAmountTotal(), resultCallBackListener);
        } else {
            if (getAdapter().getGuiProcessor() != null) {
                getView().showMapBankBIDVDialog(getAdapter().getGuiProcessor().getCardNumber(), mPaymentInfoHelper.getAmountTotal(), resultCallBackListener);
            }
        }

    }

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
                Log.e(this, e);
            }
        }

        @Override
        public void onCancel(int pReturnCode) {
            Timber.d("cancel popup map selection");
            if (mAdapter != null && mAdapter.getGuiProcessor() != null) {
                mAdapter.getGuiProcessor().clearCardNumberAndShowKeyBoard();
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUnLockScreen(SdkUnlockScreenMessage message) {
        if (mAdapter != null && mAdapter.isCardFlow()) {
            mAdapter.getGuiProcessor().moveScrollViewToCurrentFocusView();
        }
        mBus.removeStickyEvent(SdkUnlockScreenMessage.class);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnPaymentSms(SdkSmsMessage message) {
        if (mAdapter == null) {
            return;
        }
        String sender = message.sender;
        String body = message.message;
        if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(body)) {
            mAdapter.autoFillOtp(sender, body);
        }
        mBus.removeStickyEvent(SdkSmsMessage.class);
        Timber.d("on payment otp event %s", message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkChanged(SdkNetworkEvent message) {
        if (mAdapter.isFinalScreen()) {
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

}
