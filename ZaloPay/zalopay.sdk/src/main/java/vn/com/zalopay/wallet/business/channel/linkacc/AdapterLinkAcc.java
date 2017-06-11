package vn.com.zalopay.wallet.business.channel.linkacc;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.HashMapUtils;
import vn.com.zalopay.utility.LayoutUtils;
import vn.com.zalopay.utility.PaymentUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.data.VcbUtils;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptOutput;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebView;
import vn.com.zalopay.wallet.business.webview.linkacc.LinkAccWebViewClient;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.task.SubmitMapAccountTask;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;

import static vn.com.zalopay.wallet.constants.BankAccountError.ACCOUNT_LOCKED;
import static vn.com.zalopay.wallet.constants.BankAccountError.EMPTY_CAPCHA;
import static vn.com.zalopay.wallet.constants.BankAccountError.EMPTY_PASSWORD;
import static vn.com.zalopay.wallet.constants.BankAccountError.EMPTY_USERNAME;
import static vn.com.zalopay.wallet.constants.BankAccountError.WRONG_CAPTCHA;
import static vn.com.zalopay.wallet.constants.BankAccountError.WRONG_USERNAME_PASSWORD;

/**
 * Created by SinhTT on 14/11/2016.
 */

public class AdapterLinkAcc extends AdapterBase {

    public static final String VCB_LOGIN_PAGE = "zpsdk_atm_vcb_login_page";
    public static final String VCB_REGISTER_PAGE = "zpsdk_atm_vcb_register_page";
    public static final String VCB_UNREGISTER_PAGE = "zpsdk_atm_vcb_unregister_page";
    public static final String VCB_REGISTER_COMPLETE_PAGE = "zpsdk_atm_vcb_register_complete_page";
    public static final String VCB_UNREGISTER_COMPLETE_PAGE = "zpsdk_atm_vcb_unregister_complete_page";
    public static final String VCB_REFRESH_CAPTCHA = "zpsdk_atm_vcb_refresh_captcha";

    public static final String SCREEN_LINK_ACC = RS.layout.screen__link__acc;

    public static final String PAGE_VCB_LOGIN = RS.layout.screen__vcb__login;
    public static final String PAGE_VCB_CONFIRM_LINK = RS.layout.screen__vcb__confirm_link;
    public static final String PAGE_VCB_OTP = RS.layout.screen_vcb_otp;
    public static final String PAGE_VCB_CONFIRM_UNLINK = RS.layout.screen__vcb__confirm_unlink;
    public static final String PAGE_LINKACC_SUCCESS = RS.layout.screen__linkacc__success;
    public static final String PAGE_LINKACC_FAIL = RS.layout.screen__linkacc__fail;
    public static final String PAGE_UNLINKACC_SUCCESS = RS.layout.screen__unlinkacc__success;
    public static final String PAGE_UNLINKACC_FAIL = RS.layout.screen__unlinkacc__fail;
    private final Handler mHandler = new Handler();
    public String mUrlReload;
    public boolean mIsLoadingCaptcha = false;
    protected ZPWNotification mNotification;
    private int COUNT_ERROR_PASS = 1;
    private int COUNT_ERROR_CAPTCHA = 1;
    private int COUNT_REFRESH_CAPTCHA_LOGIN = 1;
    private int COUNT_REFRESH_CAPTCHA_REGISTER = 1;
    private int COUNT_RETRY_GET_NUMBERPHONE = 1;
    private LinkAccGuiProcessor linkAccGuiProcessor;
    private TreeMap<String, String> mHashMapAccNum;
    private TreeMap<String, String> mHashMapPhoneNum;
    private TreeMap<String, String> mHashMapPhoneNumUnReg;
    private LinkAccWebViewClient mWebViewProcessor = null;
    private final View.OnClickListener refreshCaptchaLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!isLoadingCaptcha()) {
                Log.d(this, "refreshCaptcha()");
                if (COUNT_REFRESH_CAPTCHA_LOGIN > Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry_password))) {
                    SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
                    linkAccFail(GlobalData.getStringResource(RS.string.zpw_string_refresh_captcha_message_vcb), null);
                    return;
                }
                mIsLoadingCaptcha = true;
                mWebViewProcessor.reload();
                COUNT_REFRESH_CAPTCHA_LOGIN++;
                new Handler().postDelayed(() -> mIsLoadingCaptcha = false, 2000);
            }
        }
    };
    private Action1<BankConfigResponse> bankListSubscriber = new Action1<BankConfigResponse>() {
        @Override
        public void call(BankConfigResponse bankConfigResponse) {
            try {
                hideLoadingDialog();
                // get bank config
                String bankCode = mPaymentInfoHelper.getLinkAccBankCode();
                BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(bankCode), BankConfig.class);
                if (bankConfig == null || !bankConfig.isActive()) {
                    getActivity().onExit(GlobalData.getStringResource(RS.string.zpw_string_bank_not_support), true);
                } else {
                    String loginBankUrl = bankConfig.loginbankurl;
                    if (TextUtils.isEmpty(loginBankUrl)) {
                        loginBankUrl = GlobalData.getStringResource(RS.string.zpw_string_vcb_link_login);
                    }
                    initWebView(loginBankUrl);
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };
    private List<BankAccount> mBankAccountList = null;
    private Action1<Boolean> loadBankAccountSubscriber = new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
            Log.d(this, "load bank account finish");
            hideLoadingDialog();
            loadBankAccountSuccess();
        }
    };
    private Action1<Throwable> loadBankAccountException = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            hideLoadingDialog();
            Log.d(this, "load bank account error", throwable);
            String message = getActivity().getMessage(throwable);
            if (TextUtils.isEmpty(message)) {
                message = GlobalData.getStringResource(RS.string.zpw_alert_network_error_loadmapbankaccountlist);
            }
            if (mPaymentInfoHelper.bankAccountLink()) {
                linkAccFail(message, mTransactionID);
            } else {
                unlinkAccFail(message, mTransactionID);
            }
        }
    };
    protected final Runnable runnableWaitingNotifyLink = () -> {
        // get & check bankaccount list
        UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
        String appVersion = SdkUtils.getAppVersion(GlobalData.getAppContext());
        Subscription subscription = SDKApplication.getApplicationComponent()
                .linkInteractor()
                .getBankAccounts(userInfo.zalopay_userid, userInfo.accesstoken, true, appVersion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loadBankAccountSubscriber, loadBankAccountException);
        getActivity().addSuscription(subscription);
    };
    private int mNumAllowLoginWrong;
    private final View.OnClickListener refreshCaptcha = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isLoadingCaptcha()) {
                if (COUNT_REFRESH_CAPTCHA_REGISTER > Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry_password))) {
                    SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getActivity());
                    linkAccFail(GlobalData.getStringResource(RS.string.zpw_string_refresh_captcha_message_vcb), null);
                    return;
                }
                Log.d(this, "refreshCaptcha()");
                mIsLoadingCaptcha = true;
                mWebViewProcessor.refreshCaptcha();
                COUNT_REFRESH_CAPTCHA_REGISTER++;
                new Handler().postDelayed(() -> mIsLoadingCaptcha = false, 2000);
            }
        }
    };

    public AdapterLinkAcc(PaymentChannelActivity pOwnerActivity, MiniPmcTransType pMiniPmcTransType, PaymentInfoHelper paymentInfoHelper) {
        super(pOwnerActivity, pMiniPmcTransType, paymentInfoHelper);
        mLayoutId = SCREEN_LINK_ACC;
        mPageCode = SCREEN_LINK_ACC;
    }

    private void loadBankAccountSuccess() {
        if (!BankAccountHelper.hasBankAccountOnCache(mPaymentInfoHelper.getUserId(), mPaymentInfoHelper.getLinkAccBankCode())) {
            if (mPaymentInfoHelper.bankAccountLink()) {
                linkAccSuccess();
            } else {
                unlinkAccSuccess();
            }
        } else {
            if (mPaymentInfoHelper.bankAccountLink()) {
                linkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_account_notfound_in_server), mTransactionID);
            } else {
                unlinkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_account_in_server), mTransactionID);
            }
        }
    }

    public boolean exitWithoutConfirm() {
        if (getPageName().equals(PAGE_SUCCESS) || getPageName().equals(PAGE_SUCCESS_SPECIAL)
                || getPageName().equals(PAGE_FAIL) || getPageName().equals(PAGE_FAIL_NETWORKING) || getPageName().equals(PAGE_FAIL_PROCESSING)
                || getPageName().equals(PAGE_LINKACC_SUCCESS) || getPageName().equals(PAGE_LINKACC_SUCCESS)
                || getPageName().equals(PAGE_UNLINKACC_SUCCESS) || getPageName().equals(PAGE_UNLINKACC_FAIL)) {
            mIsExitWithoutConfirm = true;
        }

        return mIsExitWithoutConfirm;
    }

    @Override
    public boolean isFinalStep() {
        boolean finalStep = super.isFinalStep();
        return finalStep && !getPageName().equals(SCREEN_LINK_ACC) && !getPageName().equals(PAGE_VCB_LOGIN) && !getPageName().equals(PAGE_VCB_CONFIRM_LINK)
                && !getPageName().equals(PAGE_LINKACC_SUCCESS) && !getPageName().equals(PAGE_LINKACC_FAIL)
                && !getPageName().equals(PAGE_UNLINKACC_SUCCESS) && !getPageName().equals(PAGE_UNLINKACC_FAIL);
    }

    @Override
    public boolean isTransactionFail() {
        return super.isTransactionFail() || getPageName().equals(PAGE_LINKACC_FAIL)
                || getPageName().equals(PAGE_UNLINKACC_FAIL);
    }

    @Override
    public boolean isLinkAccSuccess() {
        return getPageName().equals(PAGE_LINKACC_SUCCESS) || getPageName().equals(PAGE_UNLINKACC_SUCCESS);
    }

    @Override
    public boolean isFinalScreen() {
        return super.isFinalScreen() || getPageName().equals(PAGE_LINKACC_SUCCESS) || getPageName().equals(PAGE_LINKACC_FAIL)
                || getPageName().equals(PAGE_UNLINKACC_SUCCESS) || getPageName().equals(PAGE_UNLINKACC_FAIL);
    }

    public void startFlow() {
        Log.d(this, "start flow link account");
        visibleLoadingDialog(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        getActivity().loadBankList(bankListSubscriber, getActivity().bankListException);
    }

    @Override
    public void init() throws Exception {
        linkAccGuiProcessor = new LinkAccGuiProcessor(this);
        this.mGuiProcessor = linkAccGuiProcessor;
        // set button always above keyboard.
        LayoutUtils.setButtonAlwaysAboveKeyboards(
                linkAccGuiProcessor.getLlRoot_linear_layout(),
                linkAccGuiProcessor.getLoginHolder().getSrvScrollView(),
                linkAccGuiProcessor.getLlButton());

        // get number times allow login wrong.
        mNumAllowLoginWrong = Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_int_vcb_num_times_allow_login_wrong));

        // show title bar
        if (mPaymentInfoHelper.bankAccountLink()) {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_link_acc));
        } else if (mPaymentInfoHelper.bankAccountUnlink()) {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_unlink_acc));
            try {
                mBankAccountList = SharedPreferencesManager.getInstance().getBankAccountList(mPaymentInfoHelper.getUserId());
            } catch (Exception e) {
                Log.e(this, e);
            }

        } else {
            throw new Exception(GlobalData.getStringResource(RS.string.zpw_error_paymentinfo));
        }
        linkAccGuiProcessor.getLoginHolder().btnRefreshCaptcha.setOnClickListener(refreshCaptchaLogin);
        linkAccGuiProcessor.getRegisterHolder().getButtonRefreshCaptcha().setOnClickListener(refreshCaptcha);
    }

    @Override
    public boolean shouldFocusAfterCloseQuitDialog() {
        return isOtpStep() || isConfirmStep();
    }

    @Override
    public void onProcessPhrase() {
        Log.d(this, "on process phase " + mPageCode);
        if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            getActivity().askToOpenSettingNetwoking();
            Log.d(this, "networking is offline, stop processing click event");
            return;
        }
        if (isLoginStep() || isConfirmStep() || isOtpStep()) {
            mWebViewProcessor.hit();
            Log.d(this, "hit " + mPageCode);
        }


    }

    protected int getDefaultChannelId() {
        return BuildConfig.channel_bankaccount;
    }

    @Override
    public int getChannelID() {
        int channelId = super.getChannelID();
        return channelId != -1 ? channelId : getDefaultChannelId();
    }

    public boolean isLoginStep() {
        return mPageCode.equals(PAGE_VCB_LOGIN);
    }

    public boolean isConfirmStep() {
        return mPageCode.equals(PAGE_VCB_CONFIRM_LINK) || mPageCode.equals(PAGE_VCB_CONFIRM_UNLINK);
    }

    @Override
    public boolean isOtpStep() {
        return mPageCode.equals(PAGE_VCB_OTP);
    }

    public void forceVirtualKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null && view instanceof EditText) {
            linkAccGuiProcessor.showKeyBoardOnEditText((EditText) view);
        }
    }

    // call API, submit MapAccount
    private void submitMapAccount(String pAccNum) {
        JSONObject json = new JSONObject();
        try {
            json.put("bankcode", mPaymentInfoHelper.getLinkAccBankCode());
            json.put("firstaccountno", StringUtil.getFirstStringWithSize(pAccNum, 6));
            json.put("lastaccountno", StringUtil.getLastStringWithSize(pAccNum, 4));
        } catch (JSONException e) {
            Log.e(this, e);
        }

        String jsonSt = json.toString();
        SubmitMapAccountTask submitMapAccount = new SubmitMapAccountTask(this, jsonSt);
        submitMapAccount.makeRequest();
    }

    public void verifyServerAfterParseWebTimeout() {
        checkBankAccount();
    }

    // call API,get bankAccount
    private void checkBankAccount() {
        if (isFinalScreen()) {
            Log.d(this, "stopping reload bank account because user in result screen");
            return;
        }
        visibleLoadingDialog(GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        mHandler.postDelayed(runnableWaitingNotifyLink, Constants.TIMES_DELAY_TO_GET_NOTIFY);
    }

    /***
     * Link Account Success
     */
    private void linkAccSuccess() {
        // set pageCode
        mPageCode = PAGE_LINKACC_SUCCESS;
        getActivity().renderByResource();
        try {
            getActivity().showPaymentSuccessContent(mTransactionID);
        } catch (Exception e) {
            Log.e(this, e);
        }
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.shouldNativeWebFlow()) {
            getActivity().findViewById(R.id.zpw_threesecurity_webview).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }

        try {
            // get bankaccount from cache callback to app
            List<BankAccount> dBankAccountList = SharedPreferencesManager.getInstance().getBankAccountList(mPaymentInfoHelper.getUserId());
            if (dBankAccountList != null && dBankAccountList.size() > 0) {
                mPaymentInfoHelper.setMapBank(dBankAccountList.get(0));
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        //tracking translog id link success
        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Success);
    }

    /***
     * Link Account Fail
     *
     * @param pMessage
     */
    private void linkAccFail(String pMessage, String pTransID) {
        mPageCode = PAGE_LINKACC_FAIL;
        mWebViewProcessor.stop();//stop loading website
        getActivity().renderByResource();
        getActivity().showFailView(pMessage, pTransID);
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.shouldNativeWebFlow()) {
            getActivity().findViewById(R.id.zpw_threesecurity_webview).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }
        PaymentSnackBar.getInstance().dismiss();
        //tracking translogid link fail
        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Fail);
    }

    /***
     * unlink Account Success
     */
    private void unlinkAccSuccess() {
        mPageCode = PAGE_UNLINKACC_SUCCESS;
        mWebViewProcessor.stop();//stop loading website
        getActivity().renderByResource();
        try {
            getActivity().showPaymentSuccessContent(mTransactionID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.shouldNativeWebFlow()) {
            getActivity().findViewById(R.id.zpw_threesecurity_webview).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }
        if (mBankAccountList != null && mBankAccountList.size() > 0) {
            mPaymentInfoHelper.setMapBank(mBankAccountList.get(0));
        }
        //tracking translog id unlink success
        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Success);
    }

    /***
     * unlink account fail
     *
     * @param pMessage
     */
    private void unlinkAccFail(String pMessage, String pTransID) {
        mPageCode = PAGE_UNLINKACC_FAIL;
        // rendering by resource
        getActivity().renderByResource();
        getActivity().showFailView(pMessage, pTransID);
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.shouldNativeWebFlow()) {
            getActivity().findViewById(R.id.zpw_threesecurity_webview).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }
        //tracking translog id unlink fail
        trackingTransactionEvent(ZPPaymentSteps.OrderStepResult_Fail);
    }

    @Override
    public void autoFillOtp(String pSender, String pOtp) {
        Log.d(this, "sender " + pSender + " otp " + pOtp);
        if (!((LinkAccGuiProcessor) getGuiProcessor()).isLinkAccOtpPhase() && !GlobalData.shouldNativeWebFlow()) {
            Log.d(this, "user is not in otp phase, skip auto fill otp");
            return;
        }
        try {
            List<DOtpReceiverPattern> patternList = ResourceManager.getInstance(null).getOtpReceiverPattern(mPaymentInfoHelper.getLinkAccBankCode());
            if (patternList != null && patternList.size() > 0) {
                for (DOtpReceiverPattern otpReceiverPattern : patternList) {
                    Log.d(this, "checking pattern", otpReceiverPattern);
                    if (!TextUtils.isEmpty(otpReceiverPattern.sender) && otpReceiverPattern.sender.equalsIgnoreCase(pSender)) {
                        int start;
                        pOtp = pOtp.trim();
                        //read the begining of sms content
                        if (otpReceiverPattern.begin) {
                            start = otpReceiverPattern.start;
                        }
                        //read otp from the ending of content
                        else {
                            start = pOtp.length() - otpReceiverPattern.length - otpReceiverPattern.start;
                        }

                        String otp = pOtp.substring(start, start + otpReceiverPattern.length);
                        //clear whitespace and - character
                        otp = PaymentUtils.clearOTP(otp);
                        if ((!otpReceiverPattern.isdigit && TextUtils.isDigitsOnly(otp)) || (otpReceiverPattern.isdigit && !TextUtils.isDigitsOnly(otp))) {
                            continue;
                        }
                        if (GlobalData.shouldNativeWebFlow()) {
                            mWebViewProcessor.fillOtpOnWebFlow(otp);
                            Log.d(this, "fill otp into website vcb directly");
                        } else {
                            linkAccGuiProcessor.getConfirmOTPHolder().getEdtConfirmOTP().setText(otp);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected void showFailScreenOnType(String pMessage) {
        if (mPaymentInfoHelper.bankAccountLink()) {
            linkAccFail(pMessage, mTransactionID);
        } else if (mPaymentInfoHelper.bankAccountUnlink()) {
            unlinkAccFail(pMessage, mTransactionID);
        }
    }

    protected boolean isValidPhoneList(List<String> pPhoneVcb) {
        //validate zalopay phone and vcb phone must same
        if (!validate_Phone_Zalopay_Vcb(pPhoneVcb)) {
            String formatMess = GlobalData.getStringResource(RS.string.sdk_error_numberphone_sdk_vcb);
            String message = String.format(formatMess, pPhoneVcb.get(0), maskNumberPhone(mPaymentInfoHelper.getNumberPhone()));
            showFailScreenOnType(message);
            return false;
        }
        return true;
    }

    protected String maskNumberPhone(String pNumberPhone) {
        if (TextUtils.isEmpty(pNumberPhone)) {
            return null;
        }
        int numberOfPrefix = Integer.parseInt(GlobalData.getStringResource(RS.string.prefix_numberphone_vcb));
        int numberOfSuffix = Integer.parseInt(GlobalData.getStringResource(RS.string.suffix_numberphone_vcb));
        String prefixPhone = pNumberPhone.substring(0, numberOfPrefix);
        String suffixPhone = pNumberPhone.substring(pNumberPhone.length() - numberOfSuffix, pNumberPhone.length());
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(prefixPhone).append("***").append(suffixPhone);
        return stringBuffer.toString();
    }

    /***
     * number phone register with zalopay
     * must same with numberphone registered on vcb
     * compare prefix and suffix (3 numbers) together
     * @return
     */
    protected boolean validate_Phone_Zalopay_Vcb(List<String> pPhoneListVcb) {
        if (TextUtils.isEmpty(mPaymentInfoHelper.getNumberPhone())) {
            return true;
        }
        if (pPhoneListVcb == null || pPhoneListVcb.size() <= 0) {
            return true;
        }
        String phoneZalopay = mPaymentInfoHelper.getNumberPhone();
        StringBuilder stringBuffer = new StringBuilder();
        if (phoneZalopay.startsWith("+84")) {
            stringBuffer.append("0");
            stringBuffer.append(phoneZalopay.substring(3));
            phoneZalopay = stringBuffer.toString();
        }
        Log.d(this, "phone in zalopay " + phoneZalopay);
        for (String numberphone : pPhoneListVcb) {
            try {
                int numberOfPrefix = Integer.parseInt(GlobalData.getStringResource(RS.string.prefix_numberphone_vcb));
                int numberOfSuffix = Integer.parseInt(GlobalData.getStringResource(RS.string.suffix_numberphone_vcb));
                String prefixPhoneVcb = numberphone.substring(0, numberOfPrefix);
                String prefixPhoneZalopay = phoneZalopay.substring(0, numberOfPrefix);
                if (!TextUtils.isEmpty(prefixPhoneVcb) && prefixPhoneVcb.equals(prefixPhoneZalopay)) {
                    //continue compare suffix
                    String suffixPhoneVcb = numberphone.substring(numberphone.length() - numberOfSuffix, numberphone.length());
                    String suffixPhoneZalopay = phoneZalopay.substring(phoneZalopay.length() - numberOfSuffix, phoneZalopay.length());
                    if (!TextUtils.isEmpty(suffixPhoneVcb) && suffixPhoneVcb.equals(suffixPhoneZalopay)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        return false;
    }

    protected void visibleLoadingDialog(String pMessage) {
        if (!DialogManager.isShowingProgressDialog()) {
            showProgressBar(true, pMessage);
        }
    }

    protected void hideLoadingDialog() {
        showProgressBar(false, null);
    }

    @Override
    public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
        super.onEvent(pEventType, pAdditionParams);
        // show value progressing
        if (pEventType == EEventType.ON_PROGRESSING) {
            // get value progress  &  show it
            int value = (int) pAdditionParams[0];
            if (value < 100) {
                linkAccGuiProcessor.setProgress(value);
                if (!linkAccGuiProcessor.isProgressVisible()) {
                    linkAccGuiProcessor.visibleProgress();
                }
            } else {
                linkAccGuiProcessor.hideProgress();
            }
            return null;
        }

        // Event: HIT
        if (pEventType == EEventType.ON_HIT) {
            visibleLoadingDialog(GlobalData.getStringResource(RS.string.zingpaysdk_alert_processing_bank));
            return null;
        }

        if (pEventType == EEventType.ON_SUBMIT_LINKACC_COMPLETED) {
            // TODO: code here for submit linkacc complete
            StatusResponse response = (StatusResponse) pAdditionParams[0];
            mResponseStatus = response;
            // set transID
            mTransactionID = String.valueOf(response.zptransid);
            return null;
        }

        // Event: RENDER
        if (pEventType == EEventType.ON_REQUIRE_RENDER) {
            /***
             * sometimes load website timeout
             * user go to result screen, need
             * to prevent switch view if have a callback from website parser
             */
            if (isFinalScreen()) {
                Log.d(this, "call back from parsing website but user in final screen now");
                return null;
            }
            if (pAdditionParams == null || pAdditionParams.length == 0) {
                return null; // Error
            }

            // get page.
            String page = (String) pAdditionParams[1];
            // Login page
            if (page.equals(VCB_LOGIN_PAGE)) {
                Log.d(this, "event login page");
                hideLoadingDialog(); // close process dialog
                //for testing
                if (!SDKApplication.isReleaseBuild()) {
                    linkAccGuiProcessor.setAccountTest();
                }

                mPageCode = PAGE_VCB_LOGIN;

                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];

                if (GlobalData.shouldNativeWebFlow()) {
                    Log.d(this, "user following web flow, skip event login vcb");
                    return pAdditionParams;
                }

                // set captcha
                if (!TextUtils.isEmpty(response.otpimg) && response.otpimg.length() > 10) {
                    linkAccGuiProcessor.setCaptchaImgB64Login(response.otpimg);
                    linkAccGuiProcessor.resetCaptchaInput();
                } else if (!TextUtils.isEmpty(response.otpimgsrc)) {
                    linkAccGuiProcessor.setCaptchaImgLogin(response.otpimgsrc);
                    linkAccGuiProcessor.resetCaptchaInput();
                }
                // set Message
                if (!TextUtils.isEmpty(response.message)) {
                    Log.d(this, response.message);
                    switch (VcbUtils.getVcbType(response.message)) {
                        case EMPTY_USERNAME:
                            break;
                        case EMPTY_PASSWORD:
                            break;
                        case EMPTY_CAPCHA:
                            //showMessage(null, VcbUtils.getVcbType(response.message).toString(), TSnackbar.LENGTH_LONG);
                            break;
                        case WRONG_USERNAME_PASSWORD:
                            mNumAllowLoginWrong--;
                            if (mNumAllowLoginWrong > 0) {
                                showMessage(GlobalData.getStringResource(RS.string.zpw_string_title_err_login_vcb), String.format(GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_times_allow), mNumAllowLoginWrong), TSnackbar.LENGTH_LONG);
                                linkAccGuiProcessor.getLoginHolder().getEdtUsername().selectAll();
                                linkAccGuiProcessor.showKeyBoardOnEditText(linkAccGuiProcessor.getLoginHolder().getEdtUsername());//auto show keyboard
                            } else if (mPaymentInfoHelper.bankAccountLink()) {
                                linkAccFail(getActivity().getString(R.string.zpw_string_vcb_login_error), mTransactionID);
                            } else if (mPaymentInfoHelper.bankAccountUnlink()) {
                                unlinkAccFail(getActivity().getString(R.string.zpw_string_vcb_login_error), mTransactionID);
                            }
                            return null;
                        case ACCOUNT_LOCKED:
                            if (mPaymentInfoHelper.bankAccountLink()) {
                                linkAccFail(getActivity().getString(R.string.zpw_string_vcb_bank_locked_account), mTransactionID);
                            } else {
                                unlinkAccFail(getActivity().getString(R.string.zpw_string_vcb_bank_locked_account), mTransactionID);
                            }
                            return null;
                        case WRONG_CAPTCHA:
                            if (!GlobalData.shouldNativeWebFlow()) {
                                showMessage(getActivity().getString(R.string.dialog_title_normal), response.message, TSnackbar.LENGTH_LONG);
                            }
                            linkAccGuiProcessor.getLoginHolder().getEdtCaptcha().setText(null);
                            linkAccGuiProcessor.showKeyBoardOnEditText(linkAccGuiProcessor.getLoginHolder().getEdtCaptcha());//auto show keyboard
                            return null;
                        default:
                            break;
                    }
                    linkAccGuiProcessor.setMessage(response.message);
                }
                getActivity().renderByResource();
                getActivity().enableSubmitBtn(false);
                linkAccGuiProcessor.showKeyBoardOnEditText(linkAccGuiProcessor.getLoginHolder().getEdtUsername());//auto show keyboard
                return null;
            }

            // Register page
            if (page.equals(VCB_REGISTER_PAGE)) {
                Log.d(this, "event register page");

                // TrackApptransidEvent confirm stage
                if (GlobalData.analyticsTrackerWrapper != null) {
                    GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_WebInfoConfirm, ZPPaymentSteps.OrderStepResult_None, getChannelID());
                }

                hideLoadingDialog();
                mPageCode = PAGE_VCB_CONFIRM_LINK;
                mIsExitWithoutConfirm = false;//mark that will show dialog confirm exit sdk
                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];

                if (GlobalData.shouldNativeWebFlow()) {
                    Log.d(this, "user following web flow, skip event login vcb");
                    return pAdditionParams;
                }
                // set captcha
                if (!TextUtils.isEmpty(response.otpimg) && response.otpimg.length() > 10) {
                    linkAccGuiProcessor.setCaptchaImgB64Confirm(response.otpimg);
                } else if (!TextUtils.isEmpty(response.otpimgsrc)) {
                    linkAccGuiProcessor.setCaptchaImgConfirm(response.otpimgsrc);
                }

                // set list wallet
                if (response.walletList != null) {
                    TreeMap<String, String> mHashMapWallet = HashMapUtils.JsonArrayToHashMap(response.walletList);
                    ArrayList<String> walletList = HashMapUtils.getKeys(mHashMapWallet);
                    linkAccGuiProcessor.setWalletList(walletList);
                }

                // set list account number
                if (response.accNumList != null) {
                    mHashMapAccNum = HashMapUtils.JsonArrayToHashMap(response.accNumList);
                    ArrayList<String> accNum = HashMapUtils.getKeys(mHashMapAccNum);
                    // set accNum into Spinner || Text
                    if (accNum != null && accNum.size() > 1) {
                        linkAccGuiProcessor.setAccNumList(accNum);
                        linkAccGuiProcessor.getRegisterHolder().getLlAccNumberDefault().setVisibility(View.VISIBLE);
                        linkAccGuiProcessor.getRegisterHolder().getIlAccNumberDefault().setVisibility(View.GONE);
                    } else {
                        linkAccGuiProcessor.setAccNum(accNum);
                        linkAccGuiProcessor.getRegisterHolder().getLlAccNumberDefault().setVisibility(View.GONE);
                        linkAccGuiProcessor.getRegisterHolder().getIlAccNumberDefault().setVisibility(View.VISIBLE);
                    }
                }

                if (response.phoneNumList != null) {
                    if (response.phoneNumList.size() <= 0 && COUNT_RETRY_GET_NUMBERPHONE < Constants.VCB_MAX_RETRY_GET_NUMBERPHONE) {
                        mWebViewProcessor.runLastScript();
                        COUNT_RETRY_GET_NUMBERPHONE++;
                        Log.d(this, "run last script again to get number phone list");
                        return null;
                    } else if (response.phoneNumList.size() <= 0) {
                        // don't have account link
                        linkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_phonenumber_notfound_register), mTransactionID);
                    } else {
                        mHashMapPhoneNum = HashMapUtils.JsonArrayToHashMap(response.phoneNumList);

                        List<String> phoneNum = HashMapUtils.getKeys(mHashMapPhoneNum);
                        //validate zalopay phone and vcb phone must same
                        if (!isValidPhoneList(phoneNum)) {
                            return pAdditionParams;
                        }
                        linkAccGuiProcessor.setPhoneNumList(phoneNum);
                        linkAccGuiProcessor.setPhoneNum(phoneNum);

                        // MapAccount API. just using for web VCB
                        if (GlobalData.shouldNativeWebFlow()) {
                            submitMapAccount(getAccNumValue());
                        }
                    }
                }

                // set OTP valid type
                if (response.otpValidTypeList != null) {
                    TreeMap<String, String> mHashMapOTPValid = HashMapUtils.JsonArrayToHashMap(response.otpValidTypeList);
                    ArrayList<String> otpValid = HashMapUtils.getKeys(mHashMapOTPValid);
                    linkAccGuiProcessor.setOtpValidList(otpValid);
                }

                // set phone receive OTP
                if (response.phoneReveiceOTP != null) {
                    linkAccGuiProcessor.setPhoneReceiveOTP(response.phoneReveiceOTP);
                }

                // set message
                if (!TextUtils.isEmpty(response.messageOTP)) {
                    // code here confirm success. get messageResult.
                    mPageCode = PAGE_VCB_OTP;
                    linkAccGuiProcessor.getConfirmOTPHolder().getEdtConfirmOTP().requestFocus();
                    // submit MapAccount for webview VCB parse
                    if (!GlobalData.shouldNativeWebFlow()) {
                        submitMapAccount(getAccNumValue());
                    }

                    getActivity().renderByResource();
                    getActivity().enableSubmitBtn(false);

                    // TrackApptransidEvent AuthenType
                    if (GlobalData.analyticsTrackerWrapper != null) {
                        GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_WebOtp, ZPPaymentSteps.OrderStepResult_None, getChannelID());
                    }

                    return null;
                } else {
                    // set Message
                    if (!TextUtils.isEmpty(response.message)) {
                        switch (VcbUtils.getVcbType(response.message)) {
                            case EMPTY_CAPCHA:
                                showMessage(getActivity().getString(R.string.dialog_title_normal), response.message, TSnackbar.LENGTH_LONG);
                                break;
                            case WRONG_CAPTCHA:
                                if (COUNT_ERROR_CAPTCHA >= Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry_captcha))) {
                                    if (!TextUtils.isEmpty(response.message)) {
                                        hideLoadingDialog(); // close process dialog
                                        String msgErr = response.message;
                                        linkAccFail(msgErr, mTransactionID);
                                        return null;
                                    }

                                } else {
                                    getActivity().setTextInputLayoutHintError(linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha(), getActivity().getString(R.string.zpw_string_vcb_error_captcha), getActivity());
                                    if (!GlobalData.shouldNativeWebFlow()) {
                                        showMessage(getActivity().getString(R.string.dialog_title_normal), response.message, TSnackbar.LENGTH_LONG);
                                    }
                                    linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha().setText(null);
                                    linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha().requestFocus();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                SdkUtils.focusAndSoftKeyboard(getActivity(), linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha());
                                                Log.d(this, "mOnFocusChangeListener Link Acc");
                                            } catch (Exception e) {
                                                Log.e(this, e);
                                            }
                                        }
                                    }, 100);
                                }
                                COUNT_ERROR_CAPTCHA++;
                                break;
                            default:
                                // FAIL. Fail register
                                if (!TextUtils.isEmpty(response.message)) {
                                    hideLoadingDialog(); // close process dialog
                                    String msgErr = response.message;
                                    linkAccFail(msgErr, mTransactionID);
                                    return null;
                                }
                                break;
                        }
                    }

                }

                linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha().requestFocus(); // focus captcha confirm
                getActivity().renderByResource();
                getActivity().enableSubmitBtn(false);

                requestReadOtpPermission();
                return null;
            }

            // Unregister page
            if (page.equals(VCB_UNREGISTER_PAGE)) {
                // get bankaccount from cache callback to app
                Log.d(this, "event on unregister page complete");

                // TrackApptransidEvent AuthenType
                if (GlobalData.analyticsTrackerWrapper != null) {
                    GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_WebInfoConfirm, ZPPaymentSteps.OrderStepResult_None, getChannelID());
                }

                hideLoadingDialog();
                mPageCode = PAGE_VCB_CONFIRM_UNLINK;
                mIsExitWithoutConfirm = false;//mark that will show dialog confirm exit sdk
                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];

                if (GlobalData.shouldNativeWebFlow()) {
                    Log.d(this, "user following web flow, skip event login vcb");
                    return pAdditionParams;
                }
                // set wallet unregister
                if (response.walletUnRegList != null) {
                    TreeMap<String, String> mHashMapWalletUnReg = HashMapUtils.JsonArrayToHashMap(response.walletUnRegList);
                    List<String> walletList = HashMapUtils.getKeys(mHashMapWalletUnReg);
                    linkAccGuiProcessor.setWalletUnRegList(walletList);
                }
                if ((response.phoneNumUnRegList == null || response.phoneNumUnRegList.size() <= 0) && COUNT_RETRY_GET_NUMBERPHONE < Constants.VCB_MAX_RETRY_GET_NUMBERPHONE) {
                    mWebViewProcessor.runLastScript();
                    COUNT_RETRY_GET_NUMBERPHONE++;
                    Log.d(this, "run last script again to get number phone list");
                    return null;
                } else if ((response.phoneNumUnRegList == null || response.phoneNumUnRegList.size() <= 0)) {
                    // don't have account link
                    unlinkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_phonenumber_notfound_unregister), mTransactionID);
                    return null;
                } else {
                    mHashMapPhoneNumUnReg = HashMapUtils.JsonArrayToHashMap(response.phoneNumUnRegList);
                    List<String> phoneNumList = HashMapUtils.getKeys(mHashMapPhoneNumUnReg);
                    if (!isValidPhoneList(phoneNumList)) {
                        return pAdditionParams;
                    }
                    linkAccGuiProcessor.setPhoneNumUnRegList(phoneNumList);
                    linkAccGuiProcessor.setPhoneNumUnReg(phoneNumList);
                }

                // set Message
                if (!TextUtils.isEmpty(response.message)) {
                    showMessage(GlobalData.getStringResource(RS.string.zpw_string_title_err_login_vcb), response.message, TSnackbar.LENGTH_LONG);
                }

                linkAccGuiProcessor.getUnregisterHolder().getEdtPassword().requestFocus();
                getActivity().renderByResource();
                getActivity().enableSubmitBtn(false);
                return null;
            }

            // Register complete page
            if (page.equals(VCB_REGISTER_COMPLETE_PAGE)) {
                Log.d(this, "event on register page complete");
                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];
                // set message
                if (!TextUtils.isEmpty(response.messageResult)) {
                    // SUCCESS. Success register
                    // get & check bankaccount list
                    checkBankAccount();
                } else {
                    // FAIL. Fail register
                    if (!TextUtils.isEmpty(response.message) && COUNT_ERROR_PASS >= Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry_password))) {
                        hideLoadingDialog(); // close process dialog
                        String msgErr = response.message;
                        linkAccFail(msgErr, mTransactionID);
                    } else {
                        if (!TextUtils.isEmpty(response.messageTimeout)) {
                            // code here if js time out.
                            // get & check bankaccount list
                            checkBankAccount();
                        } else {
                            hideLoadingDialog();
                            if (!GlobalData.shouldNativeWebFlow()) {
                                getActivity().showConfirmDialog(new ZPWOnEventConfirmDialogListener() {
                                    @Override
                                    public void onCancelEvent() {
                                        hideLoadingDialog(); // close process dialog
                                        String msgErr = GlobalData.getStringResource(RS.string.zpw_string_cancel_retry_otp);
                                        linkAccFail(msgErr, mTransactionID);
                                    }

                                    @Override
                                    public void onOKevent() {
                                        //retry reload the previous page
                                        if (!TextUtils.isEmpty(mUrlReload)) {
                                            visibleLoadingDialog(GlobalData.getStringResource(RS.string.zpw_loading_website_message));
                                            linkAccGuiProcessor.resetCaptchaConfirm();
                                            linkAccGuiProcessor.resetOtp();
                                            mWebViewProcessor.reloadWebView(mUrlReload);
                                        }
                                    }
                                }, response.message, getActivity().getString(R.string.dialog_retry_button), getActivity().getString(R.string.dialog_close_button));

                            }/* else {
                                showMessage(null, response.message, TSnackbar.LENGTH_LONG);
                                mWebViewProcessor.reloadWebView(mUrlReload);
                            }*/
                        }
                    }

                }
                COUNT_ERROR_PASS++;
                return null;
            }

            try {
                if (pAdditionParams[0] instanceof StatusResponse) {
                    mResponseStatus = (StatusResponse) pAdditionParams[0];
                }
            } catch (Exception e) {
                Log.d(this, e);
            }

            // Unregister Complete page
            if (page.equals(VCB_UNREGISTER_COMPLETE_PAGE)) {
                Log.d(this, "Unregister Complete page");
                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];
                // set message
                if (!TextUtils.isEmpty(response.messageResult)) {
                    // SUCCESS. Success register
                    // get & check bankaccount list
                    checkBankAccount();
                } else {
                    // FAIL. Fail register
                    if (!TextUtils.isEmpty(response.message) && COUNT_ERROR_PASS >= Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_number_retry_password))) {
                        hideLoadingDialog();
                        String msgErr = response.message;
                        unlinkAccFail(msgErr, mTransactionID);
                    } else {
                        if (!TextUtils.isEmpty(response.messageTimeout)) {
                            // code here if js time out.
                            checkBankAccount();
                        } else if (!GlobalData.shouldNativeWebFlow()) {
                            showMessage(null, response.message, TSnackbar.LENGTH_LONG);
                        }
                        hideLoadingDialog();
                        linkAccGuiProcessor.getUnregisterHolder().getEdtPassword().setText(null);
                        forceVirtualKeyboard();
                    }
                }
                COUNT_ERROR_PASS++;
                return null;
            }
            return null;
        }

        // Event: FAIL
        if (pEventType == EEventType.ON_FAIL) {
            // fail.
            Log.d(this, "event on fail");
            hideLoadingDialog();
            //networking is offline
            if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
                showFailScreenOnType(GlobalData.getOfflineMessage(mPaymentInfoHelper));
                return pAdditionParams;
            }

            if (pAdditionParams == null || pAdditionParams.length == 0) {
                return pAdditionParams;
            }
            StatusResponse response = (StatusResponse) pAdditionParams[0];
            showFailScreenOnType(response.returnmessage != null ? response.returnmessage : getActivity().getString(R.string.zpw_string_vcb_error_unidentified));
            return pAdditionParams;
        }
        //event notification from app.
        if (pEventType == EEventType.ON_NOTIFY_BANKACCOUNT) {
            if (isFinalScreen() && isTransactionSuccess()) {
                Log.d(this, "stopping reload bank account from notification because user in success screen");
                return pAdditionParams;
            }
            mNotification = (ZPWNotification) pAdditionParams[0];

            if (mNotification != null) {
                if (mHandler != null) {
                    mHandler.removeCallbacks(runnableWaitingNotifyLink);
                    Log.d(this, "cancelling current notify after getting notify from app...");
                }
                runnableWaitingNotifyLink.run();
            } else {
                Log.d(this, "notification=", mNotification);
                hideLoadingDialog();
            }
        }
        return pAdditionParams;
    }

    protected void showMessage(String pTitle, String pMessage, int pDuration) {
        getActivity().showMessageSnackBar(getActivity().findViewById(R.id.zpsdk_header),
                pTitle,
                pMessage, null, pDuration, () -> {

                });
    }

    protected void initWebView(final String pUrl) {
        // Init:
        if (mWebViewProcessor == null) {
            // Check show WebView in BankList
            if (GlobalData.shouldNativeWebFlow()) {
                // show webview && hide web parse
                getActivity().findViewById(R.id.zpw_threesecurity_webview).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.GONE);
                mWebViewProcessor = new LinkAccWebViewClient(this, (PaymentWebView) getActivity().findViewById(R.id.zpw_threesecurity_webview));
            } else {
                // hide webview && show web parse
                getActivity().findViewById(R.id.zpw_threesecurity_webview).setVisibility(View.GONE);
                getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE);
                visibleLoadingDialog(GlobalData.getStringResource(RS.string.zpw_loading_website_message));//show loading view
                mWebViewProcessor = new LinkAccWebViewClient(this);
            }
        }

        //networking is offline
        if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            showFailScreenOnType(GlobalData.getOfflineMessage(mPaymentInfoHelper));
            return;
        }

        mWebViewProcessor.start(pUrl);
        // TrackApptransidEvent input card info
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_WebLogin, ZPPaymentSteps.OrderStepResult_None, getChannelID());
        }
    }

    public String getUserNameValue() {
        Object result = linkAccGuiProcessor.getLoginHolder().getEdtUsername().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";
    }

    public String getPasswordValue() {
        Object result = linkAccGuiProcessor.getLoginHolder().getEdtPassword().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";

    }

//    public String getAccNumValue() {
//        Object result = linkAccGuiProcessor.getRegisterHolder().getSpnAccNumberDefault().getSelectedItem();
//        return (result != null && !result.toString().isEmpty()) ? mHashMapAccNum.get(result.toString()) : "null";
//    }

    public String getCaptchaLogin() {
        Object result = linkAccGuiProcessor.getLoginHolder().getEdtCaptcha().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";
    }

//    public String getPhoneNumValue() {
//        Object result = linkAccGuiProcessor.getRegisterHolder().getSpnPhoneNumber().getSelectedItem();
//        return (result != null && !result.toString().isEmpty()) ? mHashMapPhoneNum.get(result.toString()) : "null";
//    }

    public String getWalletTypeValue() {
        return GlobalData.getStringResource(RS.string.zpw_vcb_wallet_type);
    }

    public String getAccNumValue() {
        Object result;
        Spinner spinner = linkAccGuiProcessor.getRegisterHolder().getSpnAccNumberDefault();
        SpinnerAdapter spinnerAdapter = spinner.getAdapter();
        if (spinnerAdapter != null && spinnerAdapter.getCount() > 1) {
            result = linkAccGuiProcessor.getRegisterHolder().getSpnAccNumberDefault().getSelectedItem();
        } else result = linkAccGuiProcessor.getRegisterHolder().getEdtAccNumDefault().getText();
        return (result != null && !result.toString().isEmpty()) ? mHashMapAccNum.get(result.toString()) : "null";
    }

    public String getPhoneNumValue() {
        Object result = linkAccGuiProcessor.getRegisterHolder().getEdtPhoneNum().getText();
        return (result != null && !result.toString().isEmpty()) ? mHashMapPhoneNum.get(result.toString()) : "null";
    }

    public String getOTPValidValue() {
        return GlobalData.getStringResource(RS.string.zpw_vcb_value_otp_sms);
    }

    public String getCaptchaConfirm() {
        Object result = linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";
    }

    public String getOTPValue() {
        Object result = linkAccGuiProcessor.getConfirmOTPHolder().getEdtConfirmOTP().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";
    }

    public String getWalletTypeUnRegValue() {
        return GlobalData.getStringResource(RS.string.zpw_vcb_wallet_type);
    }

    public String getPhoneNumUnRegValue() {
        Object result = linkAccGuiProcessor.getUnregisterHolder().getEdtPhoneNumber().getText();
        return (result != null && !result.toString().isEmpty()) ? mHashMapPhoneNumUnReg.get(result.toString()) : "null";
    }

    public String getPasswordUnRegValue() {
        Object result = linkAccGuiProcessor.getUnregisterHolder().getEdtPassword().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "null";
    }

    public ZPWNotification getNotification() {
        return mNotification;
    }

    public boolean isLoadingCaptcha() {
        return mIsLoadingCaptcha;
    }
}
