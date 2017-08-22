package vn.com.zalopay.wallet.workflow.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.BitmapUtils;
import vn.com.zalopay.utility.SpinnerUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.card.AbstractCardDetector;
import vn.com.zalopay.wallet.helper.RenderHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelFragment;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

/**
 * Created by SinhTT on 14/10/2016.
 */

public class AccountLinkGuiProcessor extends CardGuiProcessor {
    int mPositionSpn;
    private ProgressBar pgbProgress;
    private TextView txtMessage;
    private LoginHolder loginHolder;
    private TextWatcher mLoginEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            checkEnableLoginSubmitButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private RegisterHolder registerHolder;
    private TextWatcher mConfirmCaptchaEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            checkEnableConfirmCaptchaSubmitButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private ConfirmOTPHolder confirmOTPHolder;
    private TextWatcher mConfirmOtpEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            checkEnableConfirmOtpSubmitButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private UnregisterHolder unregisterHolder;
    private TextWatcher mUnRegPassEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            checkEnableUnRegPassSubmitButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private View submitButton;
    private View.OnClickListener mSpinnerButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                int i = view.getId();
                if (i == R.id.footer_confirm_button) {
                    getRegisterHolder().getSpnAccNumberDefault().setSelection(mPositionSpn);
                }
                closeSpinnerView();
            } catch (Exception ex) {
                Timber.d(ex);
            }
        }
    };

    public AccountLinkGuiProcessor(Context pContext, AbstractWorkFlow pAdapter, ChannelFragment pChannelFragment) {
        super(pContext);
        mAdapter = pAdapter;
        mView = pChannelFragment;
        init();
    }

    @Override
    public void useWebView(boolean pIsUseWebView) {
        try {
            getView().setVisible(R.id.zpw_threesecurity_webview, pIsUseWebView);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void showDialogSpinnerView() {
        try {
            getView().setVisible(R.id.zpw_vcb_dialog_spinner, true);
            View view_spinnert = getView().findViewById(R.id.zpw_vcb_dialog_spinner);
            View cancel_button = getView().findViewById(R.id.cancel_button);
            View footer_confirm_button = getView().findViewById(R.id.footer_confirm_button);

            view_spinnert.setOnClickListener(mSpinnerButtonClickListener);
            cancel_button.setOnClickListener(mSpinnerButtonClickListener);
            footer_confirm_button.setOnClickListener(mSpinnerButtonClickListener);
            View v = getView().findViewById(R.id.layout_animation);
            if (v != null) {
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
                v.startAnimation(hyperspaceJumpAnimation);
            }
        } catch (Exception e) {
            Timber.d(e);
        }

    }

    void closeSpinnerView() throws Exception {
        View v = getView().findViewById(R.id.layout_animation);
        if (v != null) {
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
            v.startAnimation(hyperspaceJumpAnimation);
        }
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                getView().setVisible(R.id.zpw_vcb_dialog_spinner, false);
            } catch (Exception e) {
                Timber.d(e);
            }
        }, 300);
    }

    private View getNumberPickerView() {
        try {
            return getView().findViewById(R.id.number_picker);
        } catch (Exception e) {
            Timber.w(e);
        }
        return null;
    }

    public LoginHolder getLoginHolder() {
        return loginHolder;
    }

    public RegisterHolder getRegisterHolder() {
        return registerHolder;
    }

    public ConfirmOTPHolder getConfirmOTPHolder() {
        return confirmOTPHolder;
    }

    public UnregisterHolder getUnregisterHolder() {
        return unregisterHolder;
    }

    private TextView getTxtMessage() {
        return txtMessage;
    }

    private ProgressBar getPgbProgress() {
        return pgbProgress;
    }

    public void hideProgress() {
        if (pgbProgress != null) {
            pgbProgress.setVisibility(View.GONE);
        }
    }

    public void visibleProgress() {
        if (pgbProgress != null) {
            pgbProgress.setVisibility(View.VISIBLE);
        }
    }

    public boolean isProgressVisible() {
        return pgbProgress != null && pgbProgress.getVisibility() == View.VISIBLE;
    }

    public LinearLayout getLlRoot_linear_layout() {
        try {
            return (LinearLayout) getView().findViewById(R.id.ll_layout_rootview);
        } catch (Exception e) {
            Timber.d(e);
        }
        return null;
    }

    public View getLlButton() {
        return submitButton;
    }

    protected void init() {
        try {
            loginHolder = new LoginHolder();
            loginHolder.llLogin = (LinearLayout) getView().findViewById(R.id.zpw_vcb_login);
            loginHolder.imgLogoLinkAcc = (ImageView) getView().findViewById(R.id.img_logo_linkacc);
            loginHolder.edtUsername = (VPaymentDrawableEditText) getView().findViewById(R.id.edt_login_username);
            loginHolder.edtUsername.setGroupText(false);
            loginHolder.edtPassword = (VPaymentDrawableEditText) getView().findViewById(R.id.edt_login_password);
            loginHolder.edtPassword.setGroupText(false);
            loginHolder.edtCaptcha = (VPaymentDrawableEditText) getView().findViewById(R.id.edt_login_captcha);
            loginHolder.btnRefreshCaptcha = (ImageView) getView().findViewById(R.id.refresh_captcha);
            loginHolder.edtCaptcha.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (start < 1) {
                        try {
                            RenderHelper.setTextInputLayoutHint(getLoginHolder().getEdtCaptcha(),
                                    mContext.getResources().getString(R.string.sdk_vcb_linkacc_captcha_hint),
                                    mContext);
                        } catch (Exception e) {
                            Timber.w(e);
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            loginHolder.edtCaptchaTextInputLayout = (TextInputLayout) getView().findViewById(R.id.edt_login_captcha_textInputLayout);
            loginHolder.edtCaptcha.setGroupText(false);
            loginHolder.srvScrollView = (ScrollView) getView().findViewById(R.id.zpw_scrollview_container);
            loginHolder.edtCaptcha.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    try {
                        getAdapter().onClickSubmission();
                    } catch (Exception e) {
                        Timber.w(e);
                    }
                    return true;
                }
                return false;
            });

            loginHolder.imgCaptcha = (ImageView) getView().findViewById(R.id.img_login_captcha);
            loginHolder.webCaptcha = (WebView) getView().findViewById(R.id.web_login_captcha);

            // new gui init confirm info
            registerHolder = new RegisterHolder();
            registerHolder.llRegister = (LinearLayout) getView().findViewById(R.id.zpw_vcb_confirm_link);
            registerHolder.spnWalletType = (Spinner) getView().findViewById(R.id.spn_register_WalletType);
            registerHolder.spnAccNumberDefault = (Spinner) getView().findViewById(R.id.spn_register_AccNumberDefault);
            registerHolder.llAccNumberDefault = (LinearLayout) getView().findViewById(R.id.ll_register_AccNumberDefault);
            registerHolder.ilAccNumberDefault = (TextInputLayout) getView().findViewById(R.id.il_register_AccNumberDefault);
            registerHolder.spnPhoneNumber = (Spinner) getView().findViewById(R.id.spn_register_PhoneNumber);
            registerHolder.edtPhoneNum = (EditText) getView().findViewById(R.id.edt_register_PhoneNumber);
            registerHolder.edtPhoneNum.setKeyListener(null);
            registerHolder.edtAccNumDefault = (EditText) getView().findViewById(R.id.edt_register_AccNumberDefault);
            registerHolder.edtAccNumDefault.setKeyListener(null);
            registerHolder.spnOTPValidType = (Spinner) getView().findViewById(R.id.spn_register_OTPValidType);
            registerHolder.tvPhoneReceiveOTP = (TextView) getView().findViewById(R.id.tv_register_phone_receive_OTP);
            registerHolder.edtCaptcha = (VPaymentDrawableEditText) getView().findViewById(R.id.edt_register_captcha);
            registerHolder.btnRefreshCaptcha = (ImageView) getView().findViewById(R.id.refresh_captcha_register);
            registerHolder.edtCaptcha.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (start < 1) {
                        try {
                            RenderHelper.setTextInputLayoutHint(getRegisterHolder().getEdtCaptcha(),
                                    mContext.getResources().getString(R.string.sdk_vcb_linkacc_captcha_hint), mContext);
                        } catch (Exception e) {
                            Timber.w(e);
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            registerHolder.edtCaptcha.setGroupText(false);
            registerHolder.edtCaptcha.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    try {
                        getAdapter().onClickSubmission();
                    } catch (Exception e) {
                        Timber.w(e);
                    }
                    return true;
                }
                return false;
            });
            registerHolder.imgCaptcha = (ImageView) getView().findViewById(R.id.img_register_captcha);
            registerHolder.webCaptcha = (WebView) getView().findViewById(R.id.web_register_captcha);

            // new gui init confirm otp
            confirmOTPHolder = new ConfirmOTPHolder();
            confirmOTPHolder.llConfirmOTP = (LinearLayout) getView().findViewById(R.id.zpw_vcb_otp);
            confirmOTPHolder.edtConfirmOTP = (VPaymentDrawableEditText) getView().findViewById(R.id.edt_otp_OTP);
            confirmOTPHolder.edtConfirmOTP.setGroupText(false);
            confirmOTPHolder.edtConfirmOTP.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    try {
                        getAdapter().onClickSubmission();
                    } catch (Exception e) {
                        Timber.w(e);
                    }
                    return true;
                }
                return false;
            });

            // new gui init unregister
            unregisterHolder = new UnregisterHolder();
            unregisterHolder.llUnregister = (LinearLayout) getView().findViewById(R.id.zpw_vcb_confirm_unlink);
            unregisterHolder.spnWalletType = (Spinner) getView().findViewById(R.id.spn_unregister_WalletType);
            unregisterHolder.spnPhoneNumber = (Spinner) getView().findViewById(R.id.spn_unregister_PhoneNumber);
            unregisterHolder.edtPhoneNumber = (EditText) getView().findViewById(R.id.edt_unregister_PhoneNumber);
            unregisterHolder.edtPhoneNumber.setKeyListener(null);
            unregisterHolder.edtPassword = (VPaymentDrawableEditText) getView().findViewById(R.id.edt_unregister_password);
            unregisterHolder.edtPassword.setGroupText(false);
            unregisterHolder.edtPassword.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    try {
                        getAdapter().onClickSubmission();
                    } catch (Exception e) {
                        Timber.w(e);
                    }
                    return true;
                }
                return false;
            });

            // message
            txtMessage = (TextView) getView().findViewById(R.id.txt_Message);

            // progress
            pgbProgress = (ProgressBar) getView().findViewById(R.id.pgb_progress);

            submitButton = getView().findViewById(R.id.zpw_vcb_submit);

            getLoginHolder().getEdtUsername().addTextChangedListener(mLoginEditTextWatcher);
            getLoginHolder().getEdtPassword().addTextChangedListener(mLoginEditTextWatcher);
            getLoginHolder().getEdtCaptcha().addTextChangedListener(mLoginEditTextWatcher);
            getLoginHolder().getEdtCaptcha().requestFocus();

            getRegisterHolder().getEdtCaptcha().addTextChangedListener(mConfirmCaptchaEditTextWatcher);
            getRegisterHolder().getEdtCaptcha().requestFocus();
            getConfirmOTPHolder().getEdtConfirmOTP().addTextChangedListener(mConfirmOtpEditTextWatcher);
            getUnregisterHolder().getEdtPassword().addTextChangedListener(mUnRegPassEditTextWatcher);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void setCaptchaImgB64Login(String pB64Encoded) throws Exception {
        if (TextUtils.isEmpty(pB64Encoded)) {
            return;
        }

        Bitmap bitmap = BitmapUtils.b64ToImage(pB64Encoded);
        if (bitmap != null) {
            getLoginHolder().getImgCaptcha().setVisibility(View.VISIBLE);
            getLoginHolder().getWebCaptcha().setVisibility(View.GONE);
            // set Image
            getLoginHolder().getImgCaptcha().setImageBitmap(bitmap);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setCaptchaImgLogin(String pUrl) {
        if (TextUtils.isEmpty(pUrl)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head></head><body style='margin:0;padding:0'><img src='")
                .append(pUrl)
                .append("' style='margin:0;padding:0;' width='120px' alt='' /></body>");
        getLoginHolder().getWebCaptcha().setOnTouchListener((v, event) -> true);

        WebSettings webSettings = getLoginHolder().getWebCaptcha().getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        getLoginHolder().getImgCaptcha().setVisibility(View.GONE);
        getLoginHolder().getWebCaptcha().setVisibility(View.VISIBLE);
        getLoginHolder().getWebCaptcha().setBackgroundColor(Color.TRANSPARENT);
        webSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getLoginHolder().getWebCaptcha().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        getLoginHolder().getWebCaptcha().loadDataWithBaseURL(pUrl, sb.toString(), "text/html", null, null);
    }

    public void setCaptchaImgB64Confirm(String pB64Encoded) {
        if (TextUtils.isEmpty(pB64Encoded))
            return;
        Bitmap bitmap = BitmapUtils.b64ToImage(pB64Encoded);
        if (bitmap != null) {
            getRegisterHolder().getImgCaptcha().setVisibility(View.VISIBLE);
            getRegisterHolder().getWebCaptcha().setVisibility(View.GONE);
            getRegisterHolder().getImgCaptcha().setImageBitmap(bitmap);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setCaptchaImgConfirm(String pUrl) {
        if (TextUtils.isEmpty(pUrl))
            return;
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head></head><body style='margin:0;padding:0'><img src='").append(pUrl)
                .append("' style='margin:0;padding:0;' width='120px' alt='' /></body>");
        getRegisterHolder().getWebCaptcha().setOnTouchListener((v, event) -> true);

        WebSettings webSettings = getRegisterHolder().getWebCaptcha().getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        getRegisterHolder().getImgCaptcha().setVisibility(View.GONE);
        getRegisterHolder().getWebCaptcha().setVisibility(View.VISIBLE);
        getRegisterHolder().getWebCaptcha().setBackgroundColor(Color.TRANSPARENT);
        webSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getRegisterHolder().getWebCaptcha().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        getRegisterHolder().getWebCaptcha().loadDataWithBaseURL(pUrl, sb.toString(),
                "text/html", null, null);
    }

    public boolean isLinkAccOtpPhase() {
        return (getConfirmOTPHolder().getEdtConfirmOTP() != null
                && getConfirmOTPHolder().getEdtConfirmOTP().getVisibility() == View.VISIBLE);
    }

    public void setMessage(String pMessage) {
        if (pMessage == null) {
            return;
        }
        getTxtMessage().setText(pMessage);
    }

    public void resetCaptchaInput() {
        getLoginHolder().getEdtCaptcha().setText("");
    }

    public void resetCaptchaConfirm() {
        getRegisterHolder().getEdtCaptcha().setText("");
    }

    public void resetOtp() {
        getConfirmOTPHolder().getEdtConfirmOTP().setText(null);
    }

    public void setWalletList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getRegisterHolder().getSpnWalletType().setAdapter(adapter);
        }
    }

    public void setAccNumList(ArrayList<String> pList) {
        if (pList != null) {
            getRegisterHolder().getSpnAccNumberDefault().setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList));
            getRegisterHolder().getSpnAccNumberDefault().setClickable(true);
            getRegisterHolder().getSpnAccNumberDefault().setOnTouchListener((view, motionEvent) -> {
                showDialogSpinnerView();
                if (getNumberPickerView() == null) {
                    return true;
                }
                NumberPicker picker = (NumberPicker) getNumberPickerView();
                picker.setMinValue(0);
                picker.setMaxValue(getRegisterHolder().getSpnAccNumberDefault().getCount() - 1);
                picker.setDisplayedValues(SpinnerUtils.getItems(getRegisterHolder().getSpnAccNumberDefault()));
                picker.setWrapSelectorWheel(false);
                picker.setValue(getRegisterHolder().getSpnAccNumberDefault().getSelectedItemPosition());
                picker.setOnScrollListener((numberPicker, i) -> {
                    // selection position i
                    mPositionSpn = numberPicker.getValue();
                });
                return true;
            });
        }
    }

    public void setAccNum(ArrayList<String> pList) {
        if (pList != null) {
            getRegisterHolder().getEdtAccNumDefault().setText(pList.get(0));
        }
    }

    public void setPhoneNumList(List<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getRegisterHolder().getSpnPhoneNumber().setAdapter(adapter);
        }
    }


    public void setPhoneNum(List<String> pList) {
        if (pList != null) {
            getRegisterHolder().getEdtPhoneNum().setText(pList.get(0));
        }
    }

    public void setOtpValidList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getRegisterHolder().getSpnOTPValidType().setAdapter(adapter);
        }
    }

    public void setPhoneReceiveOTP(String pValue) {
        if (pValue != null) {
            getRegisterHolder().getTvPhoneReceiveOTP().setText(pValue);
        }
    }

    public void setProgress(int pValue) {
        if (pValue < 0 && pValue > 100) {
            return;
        }

        getPgbProgress().setProgress(pValue);
    }

    public void setWalletUnRegList(List<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getUnregisterHolder().getSpnWalletType().setAdapter(adapter);
        }
    }

    public void setPhoneNumUnRegList(List<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getUnregisterHolder().getSpnPhoneNumber().setAdapter(adapter);
        }
    }

    public void setPhoneNumUnReg(List<String> pList) {
        if (pList != null) {
            getUnregisterHolder().getEdtPhoneNumber().setText(pList.get(0));
        }
    }

    @Override
    public boolean checkEnableSubmitButton() {
        return false;
    }

    @Override
    protected void populateBankCode() {

    }

    @Override
    protected boolean isATMChannel() {
        return false;
    }

    @Override
    protected boolean canSwitchChannelLinkCard() {
        return false;
    }

    @Override
    protected boolean isOwnChannel() {
        return false;
    }

    @Override
    protected void switchChannel() {

    }

    @Override
    public void moveScrollViewToCurrentFocusView() {
        new Handler().postDelayed(() -> loginHolder.srvScrollView.fullScroll(View.FOCUS_DOWN), 300);
    }

    @Override
    public String getBankCode() {
        try {
            PaymentInfoHelper paymentInfoHelper = getAdapter().getPaymentInfoHelper();
            return paymentInfoHelper != null ? paymentInfoHelper.getLinkAccBankCode() : "";
        } catch (Exception e) {
            Timber.w(e);
        }
        return "";
    }

    public boolean checkValidRequiredEditText(EditText pView) {
        if (pView != null
                && pView.getVisibility() != View.VISIBLE) {
            return true;
        }

        boolean isCheckPattern = true;
        if (pView instanceof VPaymentDrawableEditText
                || pView instanceof VPaymentValidDateEditText) {
            isCheckPattern = ((VPaymentEditText) pView).isValid();
        }
        return isCheckPattern
                && (pView != null && pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText().toString()));
    }

    @Override
    protected boolean needToWarningNotSupportCard() {
        return false;
    }

    @Override
    public AbstractCardDetector getCardFinder() {
        return null;
    }

    @Override
    public boolean isAllowValidateCardNumberByLuhn() {
        return false;
    }

    @Override
    public void continueDetectCardForLinkCard() {

    }

    @Override
    public void setCardDateOnCardView() {

    }

    @Override
    public VPaymentValidDateEditText getCardDateView() throws Exception {
        return null;
    }

    @Override
    protected void actionAfterFinishInputCard() {

    }

    @Override
    protected int validateInputCard() {
        return 0;
    }

    @Override
    protected boolean validateCardNumberLength() {
        return false;
    }

    boolean checkEnableLoginSubmitButton() {
        boolean isLoginCaptcha = checkValidRequiredEditText(getLoginHolder().getEdtCaptcha());
        boolean isLoginName = checkValidRequiredEditText(getLoginHolder().getEdtUsername());
        boolean isLoginPassword = checkValidRequiredEditText(getLoginHolder().getEdtPassword());
        try {
            if (isLoginPassword
                    && isLoginName
                    && isLoginCaptcha) {
                getView().enablePaymentButton();
                getView().changeBgPaymentButton(getAdapter().isFinalStep());
                return true;
            } else {
                getView().disablePaymentButton();
                return false;
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    boolean checkEnableConfirmCaptchaSubmitButton() {
        boolean isConfirmCaptcha = checkValidRequiredEditText(getRegisterHolder().getEdtCaptcha());
        try {
            if (isConfirmCaptcha) {
                getView().enablePaymentButton();
                getView().changeBgPaymentButton(getAdapter().isFinalStep());
                return true;
            } else {
                getView().disablePaymentButton();
                return false;
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    boolean checkEnableConfirmOtpSubmitButton() {
        boolean isConfirmOtp = checkValidRequiredEditText(getConfirmOTPHolder().getEdtConfirmOTP());
        try {
            if (isConfirmOtp) {
                getView().enablePaymentButton();
                getView().changeBgPaymentButton(getAdapter().isFinalStep());
                return true;
            } else {
                getView().disablePaymentButton();
                return false;
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    boolean checkEnableUnRegPassSubmitButton() {
        boolean isUnRegisterPassword = checkValidRequiredEditText(getUnregisterHolder().getEdtPassword());
        try {
            if (isUnRegisterPassword) {
                getView().enablePaymentButton();
                getView().changeBgPaymentButton(getAdapter().isFinalStep());
                return true;
            } else {
                getView().disablePaymentButton();
                return false;
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

}
