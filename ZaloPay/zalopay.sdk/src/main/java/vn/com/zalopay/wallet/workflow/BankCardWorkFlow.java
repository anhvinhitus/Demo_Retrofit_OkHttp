package vn.com.zalopay.wallet.workflow;

import android.content.Context;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

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
import vn.com.zalopay.wallet.card.BankCardCheck;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.workflow.ui.BankCardGuiProcessor;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.ParseWebCode;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkAuthenPayerEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteCompleteEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteErrorEvent;
import vn.com.zalopay.wallet.event.SdkParseWebsiteRenderEvent;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

import static vn.com.zalopay.wallet.constants.Constants.PAGE_COVER_BANK_AUTHEN;
import static vn.com.zalopay.wallet.constants.Constants.SCREEN_ATM;

public class BankCardWorkFlow extends AbstractWorkFlow {
    private PaymentWebViewClient mWebViewProcessor = null;
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
                boolean bankAccount = BankAccountHelper.isBankAccount(pBankCode);
                mMiniPmcTransType = SDKApplication
                        .getApplicationComponent()
                        .appInfoInteractor()
                        .getPmcTranstype(appId, mPaymentInfoHelper.getTranstype(), bankAccount, pBankCode);
                Timber.d("new pmc trans type %s", GsonUtils.toJsonString(mMiniPmcTransType));
            }
        } catch (Exception e) {
            Timber.w(e, "Exception reload pmc config");
        }
        return mMiniPmcTransType;
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            initializeGuiProcessor();
        }
        if (TransactionHelper.isSecurityFlow(mStatusResponse)) {
            initializeGuiProcessor();
            handleEventGetStatusComplete(mStatusResponse);
            detectCard(mPaymentInfoHelper.getMapBank().getFirstNumber());
        }
    }

    @Override
    protected void initializeGuiProcessor() throws Exception {
        this.mGuiProcessor = new BankCardGuiProcessor(mContext, this, getPresenter().getViewOrThrow());
        this.mGuiProcessor.initPager();
    }

    @Override
    public void startParseBankWebsite(String pUrl) {
        mWebViewProcessor = PaymentWebViewClient.createPaymentWebViewClientByBank(this);
        mWebViewProcessor.start(pUrl);
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
            getGuiProcessor().getCreditCardFinder().reset();
            Subscription subscription = getGuiProcessor().getCardFinder().detectOnAsync(pCardNumber, getGuiProcessor().getOnDetectCardSubscriber());
            try {
                getPresenter().addSubscription(subscription);
            } catch (Exception e) {
                Log.e(this, e);
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public PaymentWebViewClient getWebViewProcessor() {
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
        Timber.d("sender " + pSender + " otp " + pOtp);
        try {
            if (!((BankCardGuiProcessor) getGuiProcessor()).isBankOtpPhase()) {
                Timber.d("user is not in otp phase, skip auto fill otp");
                return;
            }
            List<DOtpReceiverPattern> patternList = getGuiProcessor().getCardFinder().getOtpReceiverPatternList();
            if (patternList != null && patternList.size() > 0) {
                for (DOtpReceiverPattern otpReceiverPattern : patternList) {
                    if (!TextUtils.isEmpty(otpReceiverPattern.sender) && otpReceiverPattern.sender.equalsIgnoreCase(pSender)) {
                        pOtp = pOtp.trim();
                        /*
                         vietinbank has 2 type of sms
                         1. 6 number otp in the fist of content
                         2. 6 number otp in the last of content
                         need extract splited otp by search space ' ' again
                         then compare #validOtp and length otp in config
                         */
                        int index = -1;
                        String validOtp = null;
                        if (otpReceiverPattern.begin) {
                            for (int i = otpReceiverPattern.start; i < pOtp.length(); i++) {
                                if (pOtp.charAt(i) == ' ') {
                                    index = i;
                                    break;
                                }
                            }
                            if (index != -1) {
                                validOtp = pOtp.substring(otpReceiverPattern.start, index);
                            }
                        } else {
                            for (int i = (pOtp.length() - otpReceiverPattern.start) - 1; i >= 0; i--) {
                                if (pOtp.charAt(i) == ' ') {
                                    index = i;
                                    break;
                                }
                            }
                            if (index != -1) {
                                validOtp = pOtp.substring(index, (pOtp.length() - otpReceiverPattern.start));
                            }
                        }
                        if (!TextUtils.isEmpty(validOtp)) {
                            validOtp = validOtp.trim();
                        }
                        //clear whitespace and - character
                        String otp = PaymentUtils.clearOTP(validOtp);
                        Timber.d("otp after split by space " + validOtp);
                        //check it whether length match length of otp in config
                        if (!TextUtils.isEmpty(otp) && otp.length() != otpReceiverPattern.length) {
                            continue;
                        }
                        if ((!otpReceiverPattern.isdigit && TextUtils.isDigitsOnly(otp)) || (otpReceiverPattern.isdigit && !TextUtils.isDigitsOnly(otp))) {
                            continue;
                        }
                        if (CardType.PBIDV.equals(otpReceiverPattern.bankcode)) {
                            getGuiProcessor().bidvAutoFillOtp(otp);
                        }
                        ((BankCardGuiProcessor) getGuiProcessor()).setOtp(otp);
                        getGuiProcessor().getView().setVisible(R.id.txtOtpInstruction, false);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
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

    private void handlerParseWebsiteRender(DAtmScriptOutput response, String pageName) throws Exception {
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
            ((BankCardGuiProcessor) getGuiProcessor()).setCaptchaImage(response.otpimg, response.otpimgsrc);
        }
        if (!TextUtils.isEmpty(pageName)) {
            mPageName = PAGE_COVER_BANK_AUTHEN;
            getGuiProcessor().getView().renderByResource(mPageName);
            mPageName = pageName;
            getGuiProcessor().getView().renderByResource(mPageName, response.staticView, response.dynamicView);
            getGuiProcessor().checkEnableSubmitButton();
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
            showDialogWithCallBack(response.message,
                    GlobalData.getAppContext().getResources().getString(R.string.dialog_close_button), () -> {
                        try {
                            if (((BankCardGuiProcessor) getGuiProcessor()).isCaptchaProcessing()) {
                                //reset otp and show keyboard again
                                ((BankCardGuiProcessor) getGuiProcessor()).resetCaptcha();
                                getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getCaptchaEditText());
                            } else if (((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing()) {
                                //reset otp and show keyboard again
                                ((BankCardGuiProcessor) getGuiProcessor()).resetOtpWeb();
                                getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getOtpWebEditText());
                            }
                        } catch (Exception e) {
                            Timber.w(e.getMessage());
                        }
                    });
        }
        boolean visibleOrderInfo = !GlobalData.isChannelHasInputCard(mPaymentInfoHelper);
        getGuiProcessor().getView().visibleOrderInfo(visibleOrderInfo);
        getGuiProcessor().getView().setVisible(R.id.order_info_line_view, false);
        //set time process for otp and captcha to send log to server.
        if (((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing() && mOtpEndTime == 0) {
            mOtpEndTime = System.currentTimeMillis();
            getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getOtpWebEditText());
        }
        if (((BankCardGuiProcessor) getGuiProcessor()).isCaptchaProcessing() && mCaptchaEndTime == 0) {
            mCaptchaEndTime = System.currentTimeMillis();
            //request permission read/view sms on android 6.0+
            requestReadOtpPermission();
            getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getCaptchaEditText());
            if (GlobalData.analyticsTrackerWrapper != null) {
                GlobalData.analyticsTrackerWrapper
                        .step(ZPPaymentSteps.OrderStep_WebInfoConfirm)
                        .track();
            }
        }

        if (((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing()) {
            getGuiProcessor().getView().setVisible(R.id.txtOtpInstruction, true);
            if (GlobalData.analyticsTrackerWrapper != null) {
                GlobalData.analyticsTrackerWrapper
                        .step(ZPPaymentSteps.OrderStep_WebOtp)
                        .track();
            }
            //testing broadcast otp viettinbak
                    /*
                    new Handler().postDelayed(new Runnable() {
						@Override
						public void run()
						{
							String sender = "VietinBank";
							String body = "Giao dich truc tuyen Viettinbank.... Mat khau: 4556104679";
							//send otp to channel activity
							Intent messageIntent = new Intent();
							messageIntent.setAction(Constants.FILTER_ACTION_BANK_SMS_RECEIVER);
							messageIntent.putExtra(Constants.BANK_SMS_RECEIVER_SENDER, sender);
							messageIntent.putExtra(Constants.BANK_SMS_RECEIVER_BODY,body);
							LocalBroadcastManager.get(GlobalData.getAppContext()).sendBroadcast(messageIntent);
						}
					},5000);
					*/

        }
        getGuiProcessor().getView().renderKeyBoard();
        getGuiProcessor().getView().hideLoading();
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
            Timber.w(e.getMessage());
        }
        return super.isCaptchaStep();
    }

    @Override
    public boolean isOtpStep() {
        try {
            if (getGuiProcessor() instanceof BankCardGuiProcessor) {
                return ((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing() || ((BankCardGuiProcessor) getGuiProcessor()).isOtpAuthenPayerProcessing();
            }
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        return super.isOtpStep();
    }

    @Override
    public void onProcessPhrase() throws Exception {
        //authen payer atm
        if (isAuthenPayerPharse()) {
            if (!checkAndOpenNetworkingSetting()) {
                return;
            }
            if (mPaymentInfoHelper == null || mPaymentInfoHelper.getUserInfo() == null) {
                return;
            }
            showTimeoutProgressDialog(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_authen_otp_mess));
            mOrderProcessing = true;
            SDKTransactionAdapter.shared().authenPayer(mPaymentInfoHelper.getUserInfo(), mTransactionID,
                    ((BankCardGuiProcessor) getGuiProcessor()).getAuthenType(), ((BankCardGuiProcessor) getGuiProcessor()).getAuthenValue());
            if (mOtpEndTime == 0) {
                mOtpBeginTime = System.currentTimeMillis();
            }
            return;
        }
        //web flow
        if (((BankCardGuiProcessor) getGuiProcessor()).isCoverBankInProcess()) {
            if (!checkAndOpenNetworkingSetting()) {
                return;
            }
            showTimeoutProgressDialog(GlobalData.getAppContext().getResources().getString(R.string.sdk_trans_processing_bank_mess));
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
            getGuiProcessor().populateCard();
            transformPaymentCard();
        }
        startSubmitTransaction();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.dispose();
        }
    }

    public boolean existBIDVinMapCardList(String pCardNumber) {
        try {
            if (mPaymentInfoHelper == null) {
                return false;
            }
            if (TextUtils.isEmpty(pCardNumber) || pCardNumber.length() < 6) {
                return false;
            }
            String cardKey = pCardNumber.substring(0, 6) + pCardNumber.substring(pCardNumber.length() - 4, pCardNumber.length());
            MapCard mapCard = mLinkInteractor.getCard(mPaymentInfoHelper.getUserId(), cardKey);
            return mapCard != null;
        } catch (Exception e) {
            Timber.w(e, "Exception check exist card number on map card list");
        }
        return false;
    }

    public boolean existBIDVinMapCardList() {
        if (mPaymentInfoHelper == null) {
            return false;
        }
        try {
            List<MapCard> mapCards = mLinkInteractor.getMapCardList(mPaymentInfoHelper.getUserId());
            if (mapCards == null || mapCards.size() <= 0) {
                return false;
            }
            for (MapCard mappedCard : mapCards) {
                if (CardType.PBIDV.equals(mappedCard.bankcode)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Timber.w(e, "Exception check exist BIDV in map card list");
        }
        return false;
    }

    public boolean preventPaymentBidvCard(String pBankCode, String pCardNumber) throws Exception {

        //have some card bidv in map card list and have this card
        if ((existBIDVinMapCardList() && getGuiProcessor().isCardLengthMatchIdentifier(pCardNumber)
                && existBIDVinMapCardList(pCardNumber)) && getPresenter() != null) {

            getPresenter().showMapBankDialog(true);
            return true;
        }
        //have some card bidv in map card list and but don't have this card
        if (existBIDVinMapCardList() && getGuiProcessor().isCardLengthMatchIdentifier(pCardNumber) && !existBIDVinMapCardList(pCardNumber)) {
            getGuiProcessor().getView().showConfirmDialog(GlobalData.getAppContext().getResources().getString(R.string.zpw_warning_bidv_linkcard_before_payment),
                    GlobalData.getAppContext().getResources().getString(R.string.dialog_linkcard_button),
                    GlobalData.getAppContext().getResources().getString(R.string.dialog_retry_input_card_button),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                            try {
                                getGuiProcessor().clearCardNumberAndShowKeyBoard();
                            } catch (Exception e) {
                                Timber.w(e.getMessage());
                            }
                        }

                        @Override
                        public void onOKEvent() {
                            needLinkCardBeforePayment(pBankCode);
                        }
                    });

            return true;
        }
        //have no any card in map card list
        if (!existBIDVinMapCardList()) {
            getGuiProcessor().getView().showConfirmDialog(GlobalData.getAppContext().getResources().getString(R.string.zpw_warning_bidv_linkcard_before_payment),
                    GlobalData.getAppContext().getResources().getString(R.string.dialog_linkcard_button),
                    GlobalData.getAppContext().getResources().getString(R.string.dialog_retry_input_card_button),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                            try {
                                getGuiProcessor().clearCardNumberAndShowKeyBoard();
                            } catch (Exception e) {
                                Timber.w(e);
                            }
                        }

                        @Override
                        public void onOKEvent() {
                            needLinkCardBeforePayment(pBankCode);
                        }
                    });
            return true;
        }
        return false;
    }

    public boolean paymentBIDV() {
        BankCardCheck atmCardCheck = null;
        try {
            atmCardCheck = getGuiProcessor().getBankCardFinder();
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        return atmCardCheck != null && atmCardCheck.isDetected() && CardType.PBIDV.equals(atmCardCheck.getDetectBankCode());
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
