package vn.com.zalopay.wallet.workflow;

import android.content.Context;
import android.text.TextUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import rx.Subscription;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.PaymentUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.card.AbstractCardDetector;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.ParseWebCode;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.entity.bank.AtmScriptOutput;
import vn.com.zalopay.wallet.entity.config.OtpRule;
import vn.com.zalopay.wallet.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.entity.response.BaseResponse;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.event.SdkAuthenPayerEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteCompleteEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteErrorEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteRenderEvent;
import vn.com.zalopay.wallet.helper.BankHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;
import vn.com.zalopay.wallet.workflow.ui.BankCardGuiProcessor;
import vn.com.zalopay.wallet.workflow.webview.AbstractWebViewClient;
import vn.com.zalopay.wallet.workflow.webview.BankWebViewClient;
import vn.com.zalopay.wallet.workflow.webview.BidvWebViewClient;
import vn.com.zalopay.wallet.workflow.webview.SdkWebView;

import static vn.com.zalopay.wallet.constants.Constants.PAGE_COVER_BANK_AUTHEN;
import static vn.com.zalopay.wallet.constants.Constants.SCREEN_ATM;

public class BankCardWorkFlow extends AbstractWorkFlow {
    private AbstractWebViewClient mWebViewProcessor = null;
    private int numberRetryCaptcha = 0;

    public BankCardWorkFlow(Context pContext, ChannelPresenter pPresenter, MiniPmcTransType pMiniPmcTransType,
                            PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(pContext, SCREEN_ATM, pPresenter, pMiniPmcTransType, paymentInfoHelper, statusResponse);
        GlobalData.cardChannelType = CardChannel.ATM;
    }

    @Override
    public boolean needReloadPmcConfig(String pBankCode) {
        return mMiniPmcTransType == null || !mMiniPmcTransType.bankcode.equals(pBankCode);
    }

    @Override
    public MiniPmcTransType getConfig(String pBankCode) {
        try {
            if (needReloadPmcConfig(pBankCode)) {
                if (mPaymentInfoHelper == null) {
                    return mMiniPmcTransType;
                }
                Timber.d("start reload pmc trans type %s", pBankCode);
                long appId = mPaymentInfoHelper.getAppId();
                boolean bankAccount = BankHelper.isBankAccount(pBankCode);
                mMiniPmcTransType = SDKApplication
                        .getApplicationComponent()
                        .appInfoInteractor()
                        .getPmcTranstype(appId, mPaymentInfoHelper.getTranstype(), bankAccount, false, pBankCode);
                Timber.d("new pmc trans type %s", GsonUtils.toJsonString(mMiniPmcTransType));
            }
        } catch (Exception e) {
            Timber.w(e, "Exception reload pmc config");
        }
        return mMiniPmcTransType;
    }

    @Override
    protected void initializeGuiProcessor() throws Exception {
        this.mGuiProcessor = new BankCardGuiProcessor(mContext, this, getPresenter().getViewOrThrow());
        this.mGuiProcessor.initPager();
    }

    @Override
    public void startParseBankWebsite(String pUrl) {
        if (paymentBIDV()) {
            try {
                SdkWebView webView = (SdkWebView) getView().findViewById(R.id.webviewParser);
                mWebViewProcessor = new BidvWebViewClient(this, webView);
            } catch (Exception e) {
                Timber.d(e);
            }
        } else {
            SdkWebView webView = new SdkWebView(GlobalData.getAppContext());
            mWebViewProcessor = new BankWebViewClient(this, webView);
        }
        if (mWebViewProcessor != null) {
            mWebViewProcessor.start(pUrl);
            mLoadWebStarted = true;
        }
    }

    @Override
    protected void stopLoadWeb() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.stop();
        }
    }

    @Override
    public void detectCard(String pCardNumber) {
        try {
            CreditCardDetector creditCardDetector = getGuiProcessor().getCreditCardFinder();
            if (creditCardDetector != null) {
                creditCardDetector.reset();
            }
            AbstractCardDetector cardDetector = getGuiProcessor().getCardFinder();
            if (cardDetector == null) {
                return;
            }
            Subscription subscription = cardDetector
                    .detectOnAsync(pCardNumber)
                    .compose(SchedulerHelper.applySchedulers())
                    .subscribe(detected -> {
                        try {
                            getGuiProcessor().onDetectCardComplete(detected);
                        } catch (Exception e) {
                            Timber.d(e);
                        }
                    }, Timber::d);
            mCompositeSubscription.add(subscription);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public AbstractWebViewClient getWebViewProcessor() {
        return mWebViewProcessor;
    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_atm;
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }

    @Override
    public void autoFillOtp(String pSender, String pOtp) {
        Timber.d("Sender %s Sms %s", pSender, pOtp);
        try {
            if (TextUtils.isEmpty(pSender) || TextUtils.isEmpty(pOtp)) {
                return;
            }

            if (getGuiProcessor() == null || !(getGuiProcessor().getCardFinder() instanceof BankDetector)) {
                return;
            }
            if (!((BankCardGuiProcessor) getGuiProcessor()).isBankOtpPhase()) {
                Timber.d("user is not in otp phase, skip auto fill otp");
                return;
            }

            List<OtpRule> patternList = ((BankDetector) getGuiProcessor().getCardFinder()).getFoundOtpRules();
            if (patternList == null || patternList.size() <= 0) {
                return;
            }
            for (OtpRule otpReceiverPattern : patternList) {
                if (TextUtils.isEmpty(otpReceiverPattern.sender) || !otpReceiverPattern.sender.equalsIgnoreCase(pSender)) {
                    continue;
                }
                String otp = parseOtp(otpReceiverPattern, pSender, pOtp);
                if (TextUtils.isEmpty(otp)) {
                    continue;
                }
                //clear whitespace and - character
                otp = PaymentUtils.clearOTP(otp);
                Timber.d("otp after split by space %s", otp);
                //check it whether length match length of otp in config
                if (!TextUtils.isEmpty(otp) && otp.length() != otpReceiverPattern.length) {
                    continue;
                }
                if ((!otpReceiverPattern.isdigit && TextUtils.isDigitsOnly(otp))
                        || (otpReceiverPattern.isdigit && !TextUtils.isDigitsOnly(otp))) {
                    continue;
                }
                ((BankCardGuiProcessor) getGuiProcessor()).setOtp(otp);
                getView().setVisible(R.id.txtOtpInstruction, false);
                break;
            }
        } catch (Exception e) {
            Timber.d(e, "Exception autoFillOtp");
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAuthenPayerEvent(SdkAuthenPayerEvent event) {
        mEventBus.removeStickyEvent(SdkAuthenPayerEvent.class);
        handleAuthenPayerComplete(event.response);
        Timber.d("on authen payer complete %s", GsonUtils.toJsonString(event.response));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onParseWebsiteErrorEvent(SdkParseWebsiteErrorEvent event) {
        mEventBus.removeStickyEvent(SdkParseWebsiteErrorEvent.class);
        Timber.d("on parse web error");
        try {
            handleParseWebsiteErrorEvent();
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onParseWebsiteRenderEvent(SdkParseWebsiteRenderEvent event) {
        mEventBus.removeStickyEvent(SdkParseWebsiteRenderEvent.class);
        try {
            handlerParseWebsiteRender(event.response, event.pageName);
        } catch (Exception e) {
            showTransactionFailView(mContext.getResources().getString(R.string.sdk_parsewebsite_error_mess));
            Timber.w(e, "Exception render view parse web");
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onParseEventCompleteEvent(SdkParseWebsiteCompleteEvent event) {
        mEventBus.removeStickyEvent(SdkParseWebsiteCompleteEvent.class);
        handleParseWebsiteComplete(event.response);
        Timber.d("on parse web complete %s", GsonUtils.toJsonString(event.response));
    }

    private void handleAuthenPayerComplete(StatusResponse statusResponse) {
        mOrderProcessing = false;
        mStatusResponse = statusResponse;
        //check result authen, otp code is 17: wrong otp, other code callback
        if (PaymentStatusHelper.isNeedToGetStatusAfterAuthenPayer(mStatusResponse)
                && !PaymentStatusHelper.isWrongOtpResponse(mStatusResponse)) {
            getTransactionStatus(mTransactionID, false,
                    GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_getstatus_mess));
        }
        //retry otp
        else if (PaymentStatusHelper.isWrongOtpResponse(mStatusResponse)) {
            processWrongOtp();
        } else if (mStatusResponse != null) {
            showTransactionFailView(mStatusResponse.returnmessage);
        } else if (shouldCheckStatusAgain()) {
            Timber.d("continue get status because response is null after authen payer");
            getTransactionStatus(mTransactionID, false,
                    GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_getstatus_mess));
        } else {
            showTransactionFailView(GlobalData.getAppContext().getResources().getString(R.string.sdk_payment_generic_error_networking_mess));
        }
    }

    private void handleParseWebsiteErrorEvent() throws Exception {
        //get status again if user input otp
        if (((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing()) {
            getTransactionStatus(mTransactionID, false, null);
        } else {
            String failMessage = null;
            if (mStatusResponse != null) {
                failMessage = mStatusResponse.returnmessage;
            }
            if (!TextUtils.isEmpty(failMessage)) {
                failMessage = GlobalData.getAppContext().getResources().getString(R.string.sdk_parsewebsite_error_mess);
            }
            showTransactionFailView(failMessage);
        }
        //get website content and send to server
        //getWebViewProcessor().getSiteContent();
    }

    private void handleParseWebsiteComplete(BaseResponse response) {
        mOtpEndTime = System.currentTimeMillis();
        if (response.returncode == ParseWebCode.ATM_VERIFY_OTP_SUCCESS) {
            getTransactionStatus(mTransactionID, false, GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_getstatus_mess));
        } else {
            showTransactionFailView(response.returnmessage);
        }
    }

    private void handlerParseWebsiteRender(AtmScriptOutput response, String pageName) throws Exception {
        if (isFinalScreen()) {
            Timber.d("callback render from parse website but in final screen now");
            return;
        }
        if (paymentBIDV() && !continueProcessForBidvBank(response.message)) {
            if (isCaptchaStep()) {
                showTransactionFailView(response.message);
            }
            return;
        }
        BankCardGuiProcessor guiProcessor = (BankCardGuiProcessor) getGuiProcessor();
        // Reset captcha imediately
        if (!TextUtils.isEmpty(response.otpimg)) {
            if (numberRetryCaptcha >= Constants.MAX_COUNT_RETRY_CAPTCHA) {
                String message = response.message;
                if (TextUtils.isEmpty(message)) {
                    message = GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_invalid_captcha_mess);
                }
                showTransactionFailView(message);
                return;
            }
            numberRetryCaptcha++;
            guiProcessor.setCaptchaImage(response.otpimg, response.otpimgsrc);
        }
        if (!TextUtils.isEmpty(pageName)) {
            mPageName = PAGE_COVER_BANK_AUTHEN;
            getView().renderByResource(mPageName);
            mPageName = pageName;
            getView().renderByResource(mPageName, response.staticView, response.dynamicView);
            guiProcessor.checkEnableSubmitButton();
        }
        if (!response.isError()) {
            if (!TextUtils.isEmpty(response.info)) {
                showDialog(GlobalData.getStringResource(response.info));
            }
        }
        //has an error on website(wrong captcha,otp)
        else {
            if (response.message.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_vcb_invalid_captcha))) {
                response.message = GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_invalid_captcha_mess);
            }
            showDialogWithCallBack(response.message, () -> {
                try {
                    if (guiProcessor.isCaptchaProcessing()) {
                        //reset otp and show keyboard again
                        guiProcessor.resetCaptcha();
                        guiProcessor.showKeyBoardOnEditTextAndScroll(guiProcessor.getCaptchaEditText());
                    } else if (guiProcessor.isOtpWebProcessing()) {
                        //reset otp and show keyboard again
                        guiProcessor.resetOtpWeb();
                        guiProcessor.showKeyBoardOnEditTextAndScroll(guiProcessor.getOtpWebEditText());
                    }
                } catch (Exception e) {
                    Timber.w(e);
                }
            });
        }
        boolean visibleOrderInfo = !isChannelHasInputCard();
        getView().visibleOrderInfo(visibleOrderInfo);
        getView().setVisible(R.id.order_info_line_view, false);
        //set time process for otp and captcha to send log to server.
        if (guiProcessor.isOtpWebProcessing() && mOtpEndTime == 0) {
            mOtpEndTime = System.currentTimeMillis();
            guiProcessor.showKeyBoardOnEditTextAndScroll(guiProcessor.getOtpWebEditText());
        }
        if (guiProcessor.isCaptchaProcessing() && mCaptchaEndTime == 0) {
            mCaptchaEndTime = System.currentTimeMillis();
            //request permission read/view sms on android 6.0+
            requestReadOtpPermission();
            guiProcessor.showKeyBoardOnEditTextAndScroll(guiProcessor.getCaptchaEditText());
            if (GlobalData.analyticsTrackerWrapper != null) {
                GlobalData.analyticsTrackerWrapper
                        .step(ZPPaymentSteps.OrderStep_WebInfoConfirm)
                        .track();
            }
        }

        if (guiProcessor.isOtpWebProcessing()) {
            getView().setVisible(R.id.txtOtpInstruction, true);
            if (GlobalData.analyticsTrackerWrapper != null) {
                GlobalData.analyticsTrackerWrapper
                        .step(ZPPaymentSteps.OrderStep_WebOtp)
                        .track();
            }
        }
        getView().renderKeyBoard(RS.layout.screen__card, getBankCode());
        getView().hideLoading();
    }

    @Override
    public boolean isInputStep() {
        return getPageName().equals(SCREEN_ATM) || super.isInputStep();

    }

    @Override
    public boolean isCaptchaStep() {
        try {
            if (getGuiProcessor() instanceof BankCardGuiProcessor) {
                return ((BankCardGuiProcessor) getGuiProcessor()).isCaptchaProcessing();
            }
        } catch (Exception e) {
            Timber.w(e);
        }
        return super.isCaptchaStep();
    }

    @Override
    public boolean isOtpStep() {
        try {
            if (getGuiProcessor() instanceof BankCardGuiProcessor) {
                return ((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing()
                        || ((BankCardGuiProcessor) getGuiProcessor()).isOtpAuthenPayerProcessing();
            }
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        return super.isOtpStep();
    }

    @Override
    public void onProcessPhrase() throws Exception {
        //authen payer atm
        BankCardGuiProcessor guiProcessor = (BankCardGuiProcessor) getGuiProcessor();
        if (isAuthenPayerPharse()) {
            if (!checkAndOpenNetworkingSetting()) {
                return;
            }
            if (mPaymentInfoHelper == null || mPaymentInfoHelper.getUserInfo() == null) {
                return;
            }
            showTimeoutLoading(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_authen_otp_mess));
            mOrderProcessing = true;
            SDKTransactionAdapter.shared().authenPayer(mPaymentInfoHelper.getUserInfo(), mTransactionID,
                    guiProcessor.getAuthenType(), guiProcessor.getAuthenValue());
            if (mOtpEndTime == 0) {
                mOtpBeginTime = System.currentTimeMillis();
            }
            return;
        }
        //web flow
        if (guiProcessor.isCoverBankInProcess()) {
            if (!checkAndOpenNetworkingSetting()) {
                return;
            }
            showTimeoutLoading(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_processing_bank_mess));
            //the first time load captcha
            if (mCaptchaEndTime == 0) {
                mCaptchaBeginTime = System.currentTimeMillis();
            }
            //the first time load captcha
            if (mCaptchaEndTime == 0) {
                mCaptchaBeginTime = System.currentTimeMillis();
            }
            mWebViewProcessor.hit();
            return;
        }
        if (!mPaymentInfoHelper.payByCardMap() && !mPaymentInfoHelper.payByBankAccountMap()) {
            guiProcessor.populateCard();
        }
        startSubmitTransaction();
    }

    @Override
    public void onDetach() {
        if (mWebViewProcessor != null) {
            mWebViewProcessor.dispose();
        }
        super.onDetach();
    }

    private boolean paymentBIDV() {
        BankDetector atmCardCheck = null;
        try {
            atmCardCheck = getGuiProcessor().getBankCardFinder();
        } catch (Exception e) {
            Timber.w(e);
        }
        return atmCardCheck != null
                && atmCardCheck.detected()
                && CardType.PBIDV.equals(atmCardCheck.getDetectBankCode());
    }

    private boolean continueProcessForBidvBank(String pMessage) {
        boolean isContinue = false;
        if (TextUtils.isEmpty(pMessage)) {
            isContinue = true;
        }
        if (!TextUtils.isEmpty(pMessage) &&
                (pMessage.contains(GlobalData.getStringResource(RS.string.sdk_bidv_website_wrong_captcha_mess))
                        || pMessage.equals(GlobalData.getStringResource(RS.string.sdk_bidv_website_wrong_password_mess)))) {
            isContinue = true;
        }
        return isContinue;
    }
}
