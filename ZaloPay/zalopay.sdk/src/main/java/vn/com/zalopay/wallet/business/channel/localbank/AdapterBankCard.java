package vn.com.zalopay.wallet.business.channel.localbank;

import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import rx.Subscription;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.PaymentUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.DAtmScriptOutput;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebViewClient;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.ParseWebCode;
import vn.com.zalopay.wallet.dialog.MapBankPopup;
import vn.com.zalopay.wallet.helper.PaymentStatusHelper;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.transaction.SDKTransactionAdapter;
import vn.com.zalopay.wallet.ui.channel.ChannelPresenter;

import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_REQUEST_CODE;
import static vn.com.zalopay.wallet.constants.Constants.PAGE_COVER_BANK_AUTHEN;
import static vn.com.zalopay.wallet.constants.Constants.SCREEN_ATM;

public class AdapterBankCard extends AdapterBase {
    private PaymentWebViewClient mWebViewProcessor = null;
    private int numberRetryCaptcha = 0;

    public AdapterBankCard(ChannelPresenter pPresenter, MiniPmcTransType pMiniPmcTransType,
                           PaymentInfoHelper paymentInfoHelper, StatusResponse statusResponse) throws Exception {
        super(SCREEN_ATM, pPresenter, pMiniPmcTransType, paymentInfoHelper, statusResponse);
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
                Timber.d("start reload pmc trans type " + pBankCode);
                long appId = mPaymentInfoHelper.getAppId();
                mMiniPmcTransType = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getATMChannelConfig(appId, mPaymentInfoHelper.getTranstype(), pBankCode), MiniPmcTransType.class);
                Log.d(this, "new pmc trans type", mMiniPmcTransType);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return mMiniPmcTransType;
    }

    @Override
    public void init() throws Exception {
        super.init();
        this.mGuiProcessor = new BankCardGuiProcessor(this);
        if (getGuiProcessor() != null && GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            getGuiProcessor().initPager();
        }
        if (TransactionHelper.isSecurityFlow(mResponseStatus)) {
            onEvent(EEventType.ON_GET_STATUS_COMPLETE, mResponseStatus);

            detectCard(mPaymentInfoHelper.getMapBank().getFirstNumber());
        }
    }

    @Override
    public void initWebView(String pUrl) {
        mWebViewProcessor = PaymentWebViewClient.createPaymentWebViewClientByBank(this);
        mWebViewProcessor.start(pUrl);
    }

    @Override
    public void detectCard(String pCardNumber) {
        getGuiProcessor().getCreditCardFinder().reset();
        Subscription subscription = getGuiProcessor().getCardFinder().detectOnAsync(pCardNumber, getGuiProcessor().getOnDetectCardSubscriber());
        try {
            getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Log.e(this, e);
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
        if (!((BankCardGuiProcessor) getGuiProcessor()).isBankOtpPhase()) {
            Timber.d("user is not in otp phase, skip auto fill otp");
            return;
        }
        try {
            List<DOtpReceiverPattern> patternList = getGuiProcessor().getCardFinder().getOtpReceiverPatternList();
            if (patternList != null && patternList.size() > 0) {
                for (DOtpReceiverPattern otpReceiverPattern : patternList) {
                    if (!TextUtils.isEmpty(otpReceiverPattern.sender) && otpReceiverPattern.sender.equalsIgnoreCase(pSender)) {
                        pOtp = pOtp.trim();
                        /***
                         * vietinbank has 2 type of sms
                         * 1. 6 number otp in the fist of content
                         * 2. 6 number otp in the last of content
                         * need extract splited otp by search space ' ' again
                         * then compare #validOtp and length otp in config
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
                        getView().setVisible(R.id.txtOtpInstruction, false);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
        try {
            super.onEvent(pEventType, pAdditionParams);
            if (pEventType == EEventType.ON_ATM_AUTHEN_PAYER_COMPLETE) {
                //check result authen, otp code is 17: wrong otp, other code callback
                if (PaymentStatusHelper.isNeedToGetStatusAfterAuthenPayer(mResponseStatus) && !PaymentStatusHelper.isWrongOtpResponse(mResponseStatus)) {
                    getTransactionStatus(mTransactionID, false, GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
                }
                //retry otp
                else if (PaymentStatusHelper.isWrongOtpResponse(mResponseStatus)) {
                    processWrongOtp();
                } else if (mResponseStatus != null) {
                    showTransactionFailView(mResponseStatus.returnmessage);
                } else if (shouldCheckStatusAgain()) {
                    Timber.d("continue get status because response is null after authen payer");
                    getTransactionStatus(mTransactionID, false, GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
                } else {
                    showTransactionFailView(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
                }
            }
            //flow webview parse website
            else if (pEventType == EEventType.ON_FAIL) {
                //get status again if user input otp
                if (((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing()) {
                    getTransactionStatus(mTransactionID, false, null);
                } else {
                    String failMessage = null;
                    if (mResponseStatus != null) {
                        failMessage = mResponseStatus.returnmessage;
                    }

                    if (!TextUtils.isEmpty(failMessage)) {
                        failMessage = GlobalData.getStringResource(RS.string.zpw_alert_networking_error_parse_website);
                    }
                    showTransactionFailView(failMessage);
                    getView().hideLoading();
                }
                //get website content and send to server
                getWebViewProcessor().getSiteContent();
            }
            //flow webview parse website
            else if (pEventType == EEventType.ON_PAYMENT_COMPLETED) {
                mOtpEndTime = System.currentTimeMillis();
                BaseResponse response = (BaseResponse) pAdditionParams[0];
                if (response.returncode == ParseWebCode.ATM_VERIFY_OTP_SUCCESS) {
                    getTransactionStatus(mTransactionID, false, GlobalData.getStringResource(RS.string.zingpaysdk_alert_get_status));
                } else {
                    showTransactionFailView(response.returnmessage);
                }
            }
            //render webview flow
            else if (pEventType == EEventType.ON_REQUIRE_RENDER) {
                if (isFinalScreen()) {
                    Timber.d("EEventType.ON_REQUIRE_RENDER but in final screen now");
                    return null;
                }
                DAtmScriptOutput response = (DAtmScriptOutput) pAdditionParams[0];
                if (paymentBIDV() && !continueProcessForBidvBank(response.message)) {
                    if (isCaptchaStep()) {
                        showTransactionFailView(response.message);
                    }
                    return null;
                }
                // Reset captcha imediately
                if (!TextUtils.isEmpty(response.otpimg)) {
                    if (numberRetryCaptcha >= Constants.MAX_COUNT_RETRY_CAPTCHA) {
                        String message = response.message;
                        if (TextUtils.isEmpty(message)) {
                            message = GlobalData.getStringResource(RS.string.zpw_alert_captcha_vietcombank_update);
                        }
                        showTransactionFailView(message);
                        return null;
                    }
                    numberRetryCaptcha++;
                    ((BankCardGuiProcessor) getGuiProcessor()).setCaptchaImage(response.otpimg, response.otpimgsrc);
                }
                // re-render from web bank
                if (pAdditionParams.length > 1) {
                    mPageName = PAGE_COVER_BANK_AUTHEN;
                    getView().renderByResource(mPageName);
                    mPageName = (String) pAdditionParams[1];
                    getView().renderByResource(mPageName, response.staticView, response.dynamicView);
                    getGuiProcessor().checkEnableSubmitButton();

                }
                if (!response.isError()) {
                    if (!TextUtils.isEmpty(response.info)) {
                        showDialog(GlobalData.getStringResource(response.info));
                    }
                }
                //has an error on website(wrong captcha,otp)
                else {
                    if (response.message.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_alert_captcha_vietcombank))) {
                        response.message = GlobalData.getStringResource(RS.string.zpw_alert_captcha_vietcombank_update);
                    }
                    showDialogWithCallBack(response.message, GlobalData.getStringResource(RS.string.dialog_close_button), () -> {
                        if (((BankCardGuiProcessor) getGuiProcessor()).isCaptchaProcessing()) {
                            //reset otp and show keyboard again
                            ((BankCardGuiProcessor) getGuiProcessor()).resetCaptcha();
                            getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getCaptchaEditText());
                        } else if (((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing()) {
                            //reset otp and show keyboard again
                            ((BankCardGuiProcessor) getGuiProcessor()).resetOtpWeb();
                            getGuiProcessor().showKeyBoardOnEditTextAndScroll(((BankCardGuiProcessor) getGuiProcessor()).getOtpWebEditText());
                        }
                    });
                }
                boolean visibleOrderInfo = !GlobalData.isChannelHasInputCard(mPaymentInfoHelper);
                getView().visiableOrderInfo(visibleOrderInfo);
                getView().setVisible(R.id.order_info_line_view, false);
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
                    getView().setVisible(R.id.txtOtpInstruction, true);
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
                getView().hideLoading();
                getView().renderKeyBoard();
            }
        } catch (Exception ex) {
            Log.e(this, ex);
            showTransactionFailView(GlobalData.getStringResource(RS.string.zpw_alert_networking_error_check_status));
        }
        return null;
    }

    @Override
    public boolean isInputStep() {
        return getPageName().equals(SCREEN_ATM) || super.isInputStep();

    }

    @Override
    public boolean isCaptchaStep() {
        if (getGuiProcessor() instanceof BankCardGuiProcessor) {
            return ((BankCardGuiProcessor) getGuiProcessor()).isCaptchaProcessing();
        }
        return super.isCaptchaStep();
    }

    @Override
    public boolean isOtpStep() {
        if (getGuiProcessor() instanceof BankCardGuiProcessor) {
            return ((BankCardGuiProcessor) getGuiProcessor()).isOtpWebProcessing() || ((BankCardGuiProcessor) getGuiProcessor()).isOtpAuthenPayerProcessing();
        }
        return super.isOtpStep();
    }

    @Override
    public void onProcessPhrase() throws Exception {
        //authen payer atm
        if (isAuthenPayerPharse()) {
            showLoadindTimeout(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_otp));
            processingOrder = true;
            SDKTransactionAdapter.shared().authenPayer(mTransactionID, ((BankCardGuiProcessor) getGuiProcessor()).getAuthenType(), ((BankCardGuiProcessor) getGuiProcessor()).getAuthenValue());
            if (mOtpEndTime == 0)
                mOtpBeginTime = System.currentTimeMillis();
            return;
        }
        //web flow
        if (((BankCardGuiProcessor) getGuiProcessor()).isCoverBankInProcess()) {
            if (!openSettingNetworking()) {
                return;
            }
            showLoadindTimeout(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_bank));
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
            tranferPaymentCardToMapCard();
        }
        startSubmitTransaction();
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (mWebViewProcessor != null) {
            mWebViewProcessor.dispose();
        }
    }

    public boolean hasBidvBankInMapCardList(String pCardNumber) {
        try {
            if (TextUtils.isEmpty(pCardNumber) || pCardNumber.length() < 6) {
                return false;
            }
            List<MapCard> mappedCardList = SharedPreferencesManager.getInstance().getMapCardList(mPaymentInfoHelper.getUserId());
            MapCard bidvCard = new MapCard();
            bidvCard.first6cardno = pCardNumber.substring(0, 6);
            bidvCard.last4cardno = pCardNumber.substring(pCardNumber.length() - 4, pCardNumber.length());
            return mappedCardList != null && mappedCardList.contains(bidvCard);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public boolean hasBidvBankInMapCardList() {
        try {
            List<MapCard> mappedCardList = SharedPreferencesManager.getInstance().getMapCardList(mPaymentInfoHelper.getUserId());

            if (mappedCardList != null && mappedCardList.size() > 0) {
                for (MapCard mappedCard : mappedCardList) {
                    if (mappedCard.bankcode.equalsIgnoreCase(CardType.PBIDV)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public boolean preventPaymentBidvCard(String pBankCode, String pCardNumber) throws Exception {

        //have some card bidv in map card list and have this card
        if ((hasBidvBankInMapCardList() && getGuiProcessor().isCardLengthMatchIdentifier(pCardNumber)
                && hasBidvBankInMapCardList(pCardNumber))) {
            Intent intentBidv = MapBankPopup.createBidvIntent(getActivity(),
                    getGuiProcessor().getCardNumber(),
                    mPaymentInfoHelper.getAmountTotal());
            getView().startActivityForResult(intentBidv, MAP_POPUP_REQUEST_CODE);
            return true;
        }
        //have some card bidv in map card list and but don't have this card
        if (hasBidvBankInMapCardList() && getGuiProcessor().isCardLengthMatchIdentifier(pCardNumber) && !hasBidvBankInMapCardList(pCardNumber)) {
            getView().showConfirmDialog(GlobalData.getStringResource(RS.string.zpw_warning_bidv_linkcard_before_payment),
                    GlobalData.getStringResource(RS.string.dialog_linkcard_button),
                    GlobalData.getStringResource(RS.string.dialog_retry_input_card_button),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                            getGuiProcessor().clearCardNumberAndShowKeyBoard();
                        }

                        @Override
                        public void onOKEvent() {
                            needLinkCardBeforePayment(pBankCode);
                        }
                    });

            return true;
        }
        //have no any card in map card list
        if (!hasBidvBankInMapCardList()) {
            getView().showConfirmDialog(GlobalData.getStringResource(RS.string.zpw_warning_bidv_linkcard_before_payment),
                    GlobalData.getStringResource(RS.string.dialog_linkcard_button),
                    GlobalData.getStringResource(RS.string.dialog_retry_input_card_button),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                            getGuiProcessor().clearCardNumberAndShowKeyBoard();
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
        BankCardCheck atmCardCheck = getGuiProcessor().getBankCardFinder();
        return atmCardCheck != null && atmCardCheck.isDetected() && CardType.PBIDV.equals(atmCardCheck.getDetectBankCode());
    }

    private boolean continueProcessForBidvBank(String pMessage) {
        boolean isContinue = false;
        if (TextUtils.isEmpty(pMessage)) {
            isContinue = true;
        }
        if (!TextUtils.isEmpty(pMessage) &&
                (pMessage.contains(GlobalData.getStringResource(RS.string.zpw_error_message_bidv_website_wrong_captcha))
                        || pMessage.equals(GlobalData.getStringResource(RS.string.zpw_error_message_bidv_website_wrong_password)))) {
            isContinue = true;
        }
        return isContinue;
    }
}
