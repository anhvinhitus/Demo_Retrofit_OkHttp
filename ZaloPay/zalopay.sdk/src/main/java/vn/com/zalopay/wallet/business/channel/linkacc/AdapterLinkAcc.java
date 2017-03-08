package vn.com.zalopay.wallet.business.channel.linkacc;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.ZPWNotification;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.business.entity.enumeration.ELinkAccType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannel;
import vn.com.zalopay.wallet.business.entity.linkacc.DLinkAccScriptOutput;
import vn.com.zalopay.wallet.business.entity.linkacc.DResponse;
import vn.com.zalopay.wallet.business.entity.linkacc.DSubmitBankAcc;
import vn.com.zalopay.wallet.business.webview.linkacc.LinkAccWebView;
import vn.com.zalopay.wallet.business.webview.linkacc.LinkAccWebViewClient;
import vn.com.zalopay.wallet.datasource.request.SubmitMapAccount;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ICheckExistBankAccountListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.HashMapUtils;
import vn.com.zalopay.wallet.utils.LayoutUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.OtpUtils;
import vn.com.zalopay.wallet.utils.StringUtil;
import vn.com.zalopay.wallet.utils.VcbUtils;
import vn.com.zalopay.wallet.utils.ViewUtils;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.component.activity.PaymentChannelActivity;
import vn.com.zalopay.wallet.view.custom.topsnackbar.TSnackbar;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/**
 * Created by SinhTT on 14/11/2016.
 */

public class AdapterLinkAcc extends AdapterBase {

    public static String VCB_LOGIN_PAGE = "zpsdk_atm_vcb_login_page";
    public static String VCB_REGISTER_PAGE = "zpsdk_atm_vcb_register_page";
    public static String VCB_UNREGISTER_PAGE = "zpsdk_atm_vcb_unregister_page";
    public static String VCB_REGISTER_COMPLETE_PAGE = "zpsdk_atm_vcb_register_complete_page";
    public static String VCB_UNREGISTER_COMPLETE_PAGE = "zpsdk_atm_vcb_unregister_complete_page";

    private LinkAccGuiProcessor linkAccGuiProcessor;
    private ELinkAccType mLinkAccType;
    private TreeMap<String, String> mHashMapWallet, mHashMapAccNum, mHashMapPhoneNum, mHashMapOTPValid;
    private TreeMap<String, String> mHashMapWalletUnReg, mHashMapPhoneNumUnReg;
    private LinkAccWebViewClient mWebViewProcessor = null;
    private int mNumAllowLoginWrong;
    private Handler mHandler = new Handler();

    public AdapterLinkAcc(PaymentChannelActivity pOwnerActivity) {
        super(pOwnerActivity);
        mLayoutId = SCREEN_LINK_ACC;
        mPageCode = SCREEN_LINK_ACC;
    }

    @Override
    public void init() {
        // init Gui for ATM channel
        linkAccGuiProcessor = new LinkAccGuiProcessor(this);
        this.mGuiProcessor = linkAccGuiProcessor;
        // set button always above keyboard.
        LayoutUtils.setButtonAlwaysAboveKeyboards(
                linkAccGuiProcessor.getLlRoot_linear_layout(),
                linkAccGuiProcessor.getLoginHolder().getSrvScrollView(),
                linkAccGuiProcessor.getLlButton());

        mLinkAccType = GlobalData.getPaymentInfo().linkAccInfo.getLinkAccType();
        DialogManager.showProcessDialog(getActivity(), null);
        try {
            // get bank config
            BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(GlobalData.getPaymentInfo().linkAccInfo.getBankCode()), BankConfig.class);
            if (bankConfig != null && !TextUtils.isEmpty(bankConfig.loginbankurl)) {
                initWebView(bankConfig.loginbankurl);
            } else {
                initWebView(GlobalData.getStringResource(RS.string.zpw_string_vcb_link_login));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get number times allow login wrong.
        mNumAllowLoginWrong = Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_int_vcb_num_times_allow_login_wrong));

        // show title bar
        if (mLinkAccType.equals(ELinkAccType.LINK))
        {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_link_acc));
        }
        else
        {
            getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_unlink_acc));
        }

    }

    @Override
    public DPaymentChannel getChannelConfig() throws Exception {
        return GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankAccountChannelConfig(), DPaymentChannel.class);
    }

    @Override
    public void onProcessPhrase() {
        if (mPageCode.equals(PAGE_VCB_LOGIN)
                || mPageCode.equals(PAGE_VCB_CONFIRM_LINK)
                || mPageCode.equals(PAGE_VCB_CONFIRM_UNLINK)
                || mPageCode.equals(PAGE_VCB_OTP)) {
            mWebViewProcessor.hit();

            // force virtual keyboard
            forceVirtualKeyboard();
        }
    }

    @Override
    public String getChannelID() {
        if (mConfig != null)
        {
            return String.valueOf(mConfig.pmcid);
        }
        return GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_bankaccount);
    }

    public void forceVirtualKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // call API, submit MapAccount
    private void submitMapAccount(String pAccNum) {
        JSONObject json = new JSONObject();
        try {
            json.put("bankcode", GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank));
            json.put("firstaccountno", StringUtil.getFirstStringWithSize(pAccNum, 6));
            json.put("lastaccountno", StringUtil.getLastStringWithSize(pAccNum, 4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String jsonSt = json.toString();
        SubmitMapAccount submitMapAccount = new SubmitMapAccount(this, jsonSt);
        submitMapAccount.makeRequest();
    }

    // call API, get bankAccount
    private void checkLinkAccountList() {
        // loop to get notification here.
        showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ZPWNotification notification = GlobalData.getNotification();
                if (notification.getType() == Constants.NOTIFY_TYPE.LINKACC) {
                    // receive notify link success
                    showProgressBar(false, null);
                    linkAccSuccess();
                } else {
                    // get & check bankaccount list
                    BankAccountHelper.existBankAccount(true, new ICheckExistBankAccountListener() {
                        @Override
                        public void onCheckExistBankAccountComplete(boolean pExisted) {
                            showProgressBar(false, null);
                            if (pExisted) {
                                linkAccSuccess();
                            } else {
                                linkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_account_notfound_in_server));
                            }
                        }

                        @Override
                        public void onCheckExistBankAccountFail(String pMessage) {
                            showProgressBar(false, null);
                            linkAccFail(pMessage);
                        }
                    }, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank));
                }
            }
        }, Constants.TIMES_DELAY_TO_GET_NOTIFY);
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
            e.printStackTrace();
        }
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.isAllowShowWebviewVCB()) {
            getActivity().findViewById(R.id.webView).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }


        // get bankaccount from cache callback to app
        List<DBankAccount> dBankAccountList = null;
        try {
            dBankAccountList = SharedPreferencesManager.getInstance().getBankAccountList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get & set mapBank
        if (dBankAccountList.size() > 0)
            GlobalData.getPaymentInfo().mapBank = dBankAccountList.get(0);
//        else {
//            // hard code to test
//            DBankAccount dBankAccount = new DBankAccount();
//            dBankAccount.bankcode = GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank);
//            dBankAccount.firstaccountno = "093534";
//            dBankAccount.lastaccountno = "1296";
//            GlobalData.getPaymentInfo().mapBank = dBankAccount;
//        }
    }

    /***
     * Link Account Fail
     *
     * @param pMessage
     */
    private void linkAccFail(String pMessage) {
        mPageCode = PAGE_LINKACC_FAIL;
        getActivity().renderByResource();
        getActivity().showFailView(pMessage, null);
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.isAllowShowWebviewVCB()) {
            getActivity().findViewById(R.id.webView).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }
    }

    // call API, get bankAccount
    private void checkUnlinkAccountList() {
        showProgressBar(true, GlobalData.getStringResource(RS.string.zpw_string_alert_loading_bank));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ZPWNotification notification = GlobalData.getNotification();
                if (notification.getType() == Constants.NOTIFY_TYPE.UNLINKACC) {
                    showProgressBar(false, null);
                    unlinkAccSuccess();
                } else {
                    // get & check bankaccount list
                    BankAccountHelper.existBankAccount(true, new ICheckExistBankAccountListener() {
                        @Override
                        public void onCheckExistBankAccountComplete(boolean pExisted) {
                            showProgressBar(false, null);
                            if (!pExisted) {
                                unlinkAccSuccess();
                            } else {
                                unlinkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_account_in_server));
                            }
                        }

                        @Override
                        public void onCheckExistBankAccountFail(String pMessage) {
                            showProgressBar(false, null);
                            unlinkAccFail(pMessage);
                        }
                    }, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank));
                }
            }
        }, Constants.TIMES_DELAY_TO_GET_NOTIFY);
    }

    /***
     * unlink Account Success
     */
    private void unlinkAccSuccess() {
        mPageCode = PAGE_UNLINKACC_SUCCESS;
        getActivity().renderByResource();
        try {
            getActivity().showPaymentSuccessContent(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.isAllowShowWebviewVCB()) {
            getActivity().findViewById(R.id.webView).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }
    }


    /***
     * unlink account fail
     *
     * @param pMessage
     */
    private void unlinkAccFail(String pMessage) {
        mPageCode = PAGE_UNLINKACC_FAIL;
        // rendering by resource
        getActivity().renderByResource();
        getActivity().showFailView(pMessage, null);
        getActivity().enableSubmitBtn(true);

        // enable web parse. disable webview
        if (GlobalData.isAllowShowWebviewVCB()) {
            getActivity().findViewById(R.id.webView).setVisibility(View.GONE); // disable webview
            getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE); // enable web parse
        }
    }

    @Override
    public void autoFillOtp(String pSender, String pOtp) {
        Log.d(pSender, pOtp);
        if (pSender.equals(GlobalData.getStringResource(RS.string.zpw_string_vcb_otp_sender))) {
            String otp = OtpUtils.getOtp(pOtp,
                    GlobalData.getStringResource(RS.string.zpw_string_vcb_otp_identify),
                    GlobalData.getStringResource(RS.string.zpw_string_vcb_otp_prefixOtp),
                    Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_int_vcb_otp_size)));
            linkAccGuiProcessor.getConfirmOTPHolder().getEdtConfirmOTP().setText(otp);
        }
    }

    @Override
    public Object onEvent(EEventType pEventType, Object... pAdditionParams) {
        // show value progressing
        if (pEventType == EEventType.ON_PROGRESSING) {
            // get value progress  &  show it
            int value = (int) pAdditionParams[0];
            linkAccGuiProcessor.setProgress(value);
            return null;
        }

        // Event: HIT
        if (pEventType == EEventType.ON_HIT) {
            // show processDialog
            DialogManager.showProcessDialog(getActivity(), null);
            return null;
        }

        if (pEventType == EEventType.ON_SUBMIT_LINKACC_COMPLETED) {
            // TODO: code here for submit linkacc complete
            DSubmitBankAcc response = (DSubmitBankAcc) pAdditionParams[0];
            // set transID
            mTransactionID = String.valueOf(response.zptransid);
            return null;
        }

        // Event: RENDER
        if (pEventType == EEventType.ON_REQUIRE_RENDER) {
            if (pAdditionParams == null || pAdditionParams.length == 0) {
                return null; // Error
            }

            // get page.
            String page = (String) pAdditionParams[1];

            // Login page
            if (page.equals(VCB_LOGIN_PAGE)) {
                DialogManager.closeProcessDialog(); // close process dialog

                mPageCode = PAGE_VCB_LOGIN;

                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];

                // set logo
                linkAccGuiProcessor.setLogoImgLinkAcc(getActivity().getResources().getDrawable(R.drawable.ic_zp_vcb));

                // set captcha
                if (!TextUtils.isEmpty(response.otpimg) && response.otpimg.length() > 10) {
                    linkAccGuiProcessor.setCaptchaImgB64Login(response.otpimg);
                } else if (!TextUtils.isEmpty(response.otpimgsrc)) {
                    linkAccGuiProcessor.setCaptchaImgLogin(response.otpimgsrc);
                }


                // set Message
                if (!TextUtils.isEmpty(response.message)) {
                    switch (VcbUtils.getVcbType(response.message)) {
                        case EMPTY_USERNAME:
                        case EMPTY_PASSWORD:
                        case EMPTY_CAPCHA:
                            showMessage(getActivity().getString(R.string.dialog_title_normal), VcbUtils.getVcbType(response.message).toString(), TSnackbar.LENGTH_SHORT);
                            break;
                        case WRONG_USERNAME_PASSWORD:
                            mNumAllowLoginWrong--;
                            if (mNumAllowLoginWrong > 0) {
                                showMessage(GlobalData.getStringResource(RS.string.zpw_string_title_err_login_vcb),
                                        String.format(GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_times_allow),
                                                mNumAllowLoginWrong), TSnackbar.LENGTH_LONG);
                            } else {
                                if (mLinkAccType.equals(ELinkAccType.LINK)) {
                                    linkAccFail(getActivity().getString(R.string.zpw_string_vcb_login_error));
                                } else {
                                    unlinkAccFail(getActivity().getString(R.string.zpw_string_vcb_login_error));
                                }
                                return null;
                            }
                            break;
                        case ACCOUNT_LOCKED:
                            if (mLinkAccType.equals(ELinkAccType.LINK)) {
                                linkAccFail(getActivity().getString(R.string.zpw_string_vcb_bank_locked_account));
                            } else {
                                unlinkAccFail(getActivity().getString(R.string.zpw_string_vcb_bank_locked_account));
                            }
                            return null;
                        case WRONG_CAPTCHA:
                            ViewUtils.setTextInputLayoutHintError(linkAccGuiProcessor.getLoginHolder().getEdtCaptcha(), getActivity().getString(R.string.zpw_string_vcb_error_captcha), getActivity());
                            linkAccGuiProcessor.getLoginHolder().getEdtCaptcha().setText("");
                            linkAccGuiProcessor.getLoginHolder().getEdtCaptcha().requestFocus();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ZPWUtils.focusAndSoftKeyboard(getActivity(), linkAccGuiProcessor.getLoginHolder().getEdtCaptcha());
                                        Log.d(this, "mOnFocusChangeListener Link Acc");
                                    } catch (Exception e) {
                                        Log.e(this, e);
                                    }
                                }
                            }, 100);

                            break;
                        default:
                            break;
                    }
                    linkAccGuiProcessor.setMessage(response.message);
                }
                getActivity().renderByResource();
                getActivity().enableSubmitBtn(false);
                return null;
            }

            // Register page
            if (page.equals(VCB_REGISTER_PAGE)) {
                DialogManager.closeProcessDialog(); // close process dialog
                mPageCode = PAGE_VCB_CONFIRM_LINK;
                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];

                // set captcha
                if (!TextUtils.isEmpty(response.otpimg) && response.otpimg.length() > 10) {
                    linkAccGuiProcessor.setCaptchaImgB64Confirm(response.otpimg);
                } else if (!TextUtils.isEmpty(response.otpimgsrc)) {
                    linkAccGuiProcessor.setCaptchaImgConfirm(response.otpimgsrc);
                }

                // set list wallet
                if (response.walletList != null) {
                    mHashMapWallet = HashMapUtils.JsonArrayToHashMap(response.walletList);
                    ArrayList<String> walletList = HashMapUtils.getKeys(mHashMapWallet);
                    linkAccGuiProcessor.setWalletList(walletList);
                }

                // set list account number
                if (response.accNumList != null) {
                    mHashMapAccNum = HashMapUtils.JsonArrayToHashMap(response.accNumList);
                    // add 2 account to test. HARD CODE
//                    if (mHashMapAccNum != null) {
//                        mHashMapAccNum.put("0421000416723", "0421000416723");
//                    }
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

                // set list phone number
                if (response.phoneNumList != null) {
                    if (response.phoneNumList.size() > 0) {
                        mHashMapPhoneNum = HashMapUtils.JsonArrayToHashMap(response.phoneNumList);
                        ArrayList<String> phoneNum = HashMapUtils.getKeys(mHashMapPhoneNum);
                        linkAccGuiProcessor.setPhoneNumList(phoneNum);
                        linkAccGuiProcessor.setPhoneNum(phoneNum);

                        // MapAccount API. just using for web VCB
                        if (GlobalData.isAllowShowWebviewVCB()) {
                            submitMapAccount(getAccNumValue());
                        }
                    } else {
                        // don't have account link
                        linkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_phonenumber_notfound_register));
                        return null;
                    }
                }

                // set OTP valid type
                if (response.otpValidTypeList != null) {
                    mHashMapOTPValid = HashMapUtils.JsonArrayToHashMap(response.otpValidTypeList);
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
                    if (!GlobalData.isAllowShowWebviewVCB())
                        submitMapAccount(getAccNumValue());

                    getActivity().renderByResource();
                    getActivity().enableSubmitBtn(false);
                    return null;
                } else {
                    // set Message
                    if (!TextUtils.isEmpty(response.message)) {
                        switch (VcbUtils.getVcbType(response.message)) {
                            case EMPTY_CAPCHA:
                                showMessage(getActivity().getString(R.string.dialog_title_normal), VcbUtils.getVcbType(response.message).toString(), TSnackbar.LENGTH_SHORT);
                                break;
                            case WRONG_CAPTCHA:
                                ViewUtils.setTextInputLayoutHintError(linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha(), getActivity().getString(R.string.zpw_string_vcb_error_captcha), getActivity());
                                linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha().setText("");
                                linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha().requestFocus();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ZPWUtils.focusAndSoftKeyboard(getActivity(), linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha());
                                            Log.d(this, "mOnFocusChangeListener Link Acc");
                                        } catch (Exception e) {
                                            Log.e(this, e);
                                        }
                                    }
                                }, 100);
                                break;
                            default:
                                break;
                        }
                    }
                }

                linkAccGuiProcessor.getRegisterHolder().getEdtCaptcha().requestFocus(); // focus captcha confirm
                getActivity().renderByResource();
                getActivity().enableSubmitBtn(false);

                //request permission read/view sms on android 6.0+
                getActivity().requestPermission(getActivity().getApplicationContext());
                return null;
            }

            // Unregister page
            if (page.equals(VCB_UNREGISTER_PAGE)) {
                DialogManager.closeProcessDialog(); // close process dialog
                mPageCode = PAGE_VCB_CONFIRM_UNLINK;
                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];

                // set wallet unregister
                if (response.walletUnRegList != null) {
                    mHashMapWalletUnReg = HashMapUtils.JsonArrayToHashMap(response.walletUnRegList);
                    ArrayList<String> walletList = HashMapUtils.getKeys(mHashMapWalletUnReg);
                    linkAccGuiProcessor.setWalletUnRegList(walletList);
                }

                // set phone number unregister
                if (response.phoneNumUnRegList.size() > 0) {
                    mHashMapPhoneNumUnReg = HashMapUtils.JsonArrayToHashMap(response.phoneNumUnRegList);
                    ArrayList<String> phoneNumList = HashMapUtils.getKeys(mHashMapPhoneNumUnReg);
                    linkAccGuiProcessor.setPhoneNumUnRegList(phoneNumList);
                    linkAccGuiProcessor.setPhoneNumUnReg(phoneNumList);
                } else {
                    // don't have account link
                    unlinkAccFail(GlobalData.getStringResource(RS.string.zpw_string_vcb_phonenumber_notfound_unregister));
                    return null;
                }

                // set Message
                if (!TextUtils.isEmpty(response.message)) {
                    showMessage(GlobalData.getStringResource(RS.string.zpw_string_title_err_login_vcb), response.message, TSnackbar.LENGTH_SHORT);
                }

                linkAccGuiProcessor.getUnregisterHolder().getEdtPassword().requestFocus();
                getActivity().renderByResource();
                getActivity().enableSubmitBtn(false);
                return null;
            }

            // Register complete page
            if (page.equals(VCB_REGISTER_COMPLETE_PAGE)) {

                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];

                // set message
                if (!TextUtils.isEmpty(response.messageResult)) {
                    // SUCCESS. Success register
                    // get & check bankaccount list
                    checkLinkAccountList();
                } else {
                    // FAIL. Fail register
                    if (!TextUtils.isEmpty(response.message)) {
                        DialogManager.closeProcessDialog(); // close process dialog
                        String msgErr = response.message;
                        linkAccFail(msgErr);
                    } else {
                        if (!TextUtils.isEmpty(response.messageTimeout)) {
                            // code here if js time out.
                            // get & check bankaccount list
                            checkLinkAccountList();
                        }
                    }
                }

                return null;
            }

            // Unregister Complete page
            if (page.equals(VCB_UNREGISTER_COMPLETE_PAGE)) {

                DLinkAccScriptOutput response = (DLinkAccScriptOutput) pAdditionParams[0];
                // set message
                if (!TextUtils.isEmpty(response.messageResult)) {
                    // SUCCESS. Success register
                    // get & check bankaccount list
                    checkUnlinkAccountList();
                } else {
                    // FAIL. Fail register
                    if (!TextUtils.isEmpty(response.message)) {
                        DialogManager.closeProcessDialog(); // close process dialog
                        String msgErr = response.message;
                        unlinkAccFail(msgErr);
                    } else {
                        if (!TextUtils.isEmpty(response.messageTimeout)) {
                            // code here if js time out.
                            checkUnlinkAccountList();
                        }
                    }
                }

                return null;
            }
            return null;
        }

        // Event: FAIL
        if (pEventType == EEventType.ON_FAIL) {
            // fail.
            DialogManager.closeProcessDialog();
            if (pAdditionParams == null || pAdditionParams.length == 0) {
                // Error
                return null;
            }

            DResponse response = (DResponse) pAdditionParams[0];
            // show message
            showMessage(GlobalData.getStringResource(RS.string.zpw_string_title_err_login_vcb), response.returnMessage != null ? response.returnMessage : getActivity().getString(R.string.zpw_string_vcb_error_unidentified), TSnackbar.LENGTH_SHORT);
            return null;
        }

        // return null.
        return null;
    }

    private void showMessage(String pTitle, String pMessage, int pDuration) {
        getActivity().showMessageSnackBar(getActivity().findViewById(R.id.zpsdk_header),
                pTitle,
                pMessage, null, pDuration, new onCloseSnackBar() {
                    @Override
                    public void onClose() {

                    }
                });
    }

    protected void initWebView(final String pUrl) {
        // Init:
        if (mWebViewProcessor == null) {
            // Check show WebView in BankList
            if (GlobalData.isAllowShowWebviewVCB()) {
                // show webview && hide web parse
                getActivity().findViewById(R.id.webView).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.GONE);
                mWebViewProcessor = new
                        LinkAccWebViewClient(this, (LinkAccWebView)
                        getActivity().findViewById(R.id.webView));
            } else {
                // hide webview && show web parse
                getActivity().findViewById(R.id.webView).setVisibility(View.GONE);
                getActivity().findViewById(R.id.ll_test_rootview).setVisibility(View.VISIBLE);
                mWebViewProcessor = new LinkAccWebViewClient(this);
            }
        }

        mWebViewProcessor.start(pUrl);
    }

    public String getUserNameValue() {
        Object result = linkAccGuiProcessor.getLoginHolder().getEdtUsername().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";
    }

    public String getPasswordValue() {
        Object result = linkAccGuiProcessor.getLoginHolder().getEdtPassword().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";

    }

    public String getCaptchaLogin() {
        Object result = linkAccGuiProcessor.getLoginHolder().getEdtCaptcha().getText();
        return (result != null && !result.toString().isEmpty()) ? result.toString() : "";
    }

    public String getWalletTypeValue() {
        return GlobalData.getStringResource(RS.string.zpw_vcb_wallet_type);
    }

//    public String getAccNumValue() {
//        Object result = linkAccGuiProcessor.getRegisterHolder().getSpnAccNumberDefault().getSelectedItem();
//        return (result != null && !result.toString().isEmpty()) ? mHashMapAccNum.get(result.toString()) : "null";
//    }

    public String getAccNumValue() {
        Object result;
        Spinner spinner = linkAccGuiProcessor.getRegisterHolder().getSpnAccNumberDefault();
        SpinnerAdapter spinnerAdapter = spinner.getAdapter();
        if (spinnerAdapter != null && spinnerAdapter.getCount() > 1) {
            result = linkAccGuiProcessor.getRegisterHolder().getSpnAccNumberDefault().getSelectedItem();
        } else result = linkAccGuiProcessor.getRegisterHolder().getEdtAccNumDefault().getText();
        return (result != null && !result.toString().isEmpty()) ? mHashMapAccNum.get(result.toString()) : "null";
    }

//    public String getPhoneNumValue() {
//        Object result = linkAccGuiProcessor.getRegisterHolder().getSpnPhoneNumber().getSelectedItem();
//        return (result != null && !result.toString().isEmpty()) ? mHashMapPhoneNum.get(result.toString()) : "null";
//    }

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

    public ELinkAccType getLinkerType() {
        if (mLinkAccType != null)
            return mLinkAccType;
        return null;
    }
}
