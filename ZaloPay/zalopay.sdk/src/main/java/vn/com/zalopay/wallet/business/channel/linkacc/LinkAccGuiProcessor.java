package vn.com.zalopay.wallet.business.channel.linkacc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;

/**
 * Created by SinhTT on 14/10/2016.
 */

public class LinkAccGuiProcessor extends CardGuiProcessor {
    private AdapterBase mAdapter;
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
    ///////LISTENER////////
    // listener EditText
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
    private boolean isVisibilitySpinner = false;
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
                Log.e(this, ex);
            }
        }
    };

    public LinkAccGuiProcessor(Context pContext,AdapterBase pAdapter) {
        super(pContext);
        mAdapter = pAdapter;
        init();
    }

    @Override
    public void useWebView(boolean pIsUseWebView) {
        try {
            mAdapter.getView().setVisible(R.id.zpw_threesecurity_webview, pIsUseWebView);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected AdapterBase getAdapter() {
        return mAdapter;
    }

    public void showDialogSpinnerView() {
        try {
            getAdapter().getView().setVisible(R.id.zpw_vcb_dialog_spinner, true);
            isVisibilitySpinner = true;
            View view_spinnert = mAdapter.getView().findViewById(R.id.zpw_vcb_dialog_spinner);
            View cancel_button = mAdapter.getView().findViewById(R.id.cancel_button);
            View footer_confirm_button = mAdapter.getView().findViewById(R.id.footer_confirm_button);

            view_spinnert.setOnClickListener(mSpinnerButtonClickListener);
            cancel_button.setOnClickListener(mSpinnerButtonClickListener);
            footer_confirm_button.setOnClickListener(mSpinnerButtonClickListener);
            View v = mAdapter.getView().findViewById(R.id.layout_animation);
            if (v != null) {
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(mAdapter.getActivity(), R.anim.slide_in_bottom);
                v.startAnimation(hyperspaceJumpAnimation);
            }
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

    }

    public void closeSpinnerView() throws Exception {
        View v = mAdapter.getView().findViewById(R.id.layout_animation);
        if (v != null) {
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(mAdapter.getActivity(), R.anim.slide_out_bottom);
            v.startAnimation(hyperspaceJumpAnimation);
        }
        isVisibilitySpinner = false;
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            try {
                mAdapter.getView().setVisible(R.id.zpw_vcb_dialog_spinner, false);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }, 300);
    }

    public View getNumberPickerView() {
        try {
            return mAdapter.getView().findViewById(R.id.number_picker);
        } catch (Exception e) {
            Log.e(this, e);
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

    public TextView getTxtMessage() {
        return txtMessage;
    }

    public ProgressBar getPgbProgress() {
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
            return (LinearLayout) mAdapter.getView().findViewById(R.id.ll_layout_rootview);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }

    public View getLlButton() {
        return submitButton;
    }

    protected void init() {
        try {
            loginHolder = new LoginHolder();
            loginHolder.llLogin = (LinearLayout) mAdapter.getView().findViewById(R.id.zpw_vcb_login);
            loginHolder.imgLogoLinkAcc = (ImageView) mAdapter.getView().findViewById(R.id.img_logo_linkacc);
            loginHolder.edtUsername = (VPaymentDrawableEditText) mAdapter.getView().findViewById(R.id.edt_login_username);
            loginHolder.edtUsername.setGroupText(false);
            loginHolder.edtPassword = (VPaymentDrawableEditText) mAdapter.getView().findViewById(R.id.edt_login_password);
            loginHolder.edtPassword.setGroupText(false);
            loginHolder.edtCaptcha = (VPaymentDrawableEditText) mAdapter.getView().findViewById(R.id.edt_login_captcha);
            loginHolder.btnRefreshCaptcha = (ImageView) mAdapter.getView().findViewById(R.id.refresh_captcha);
            loginHolder.edtCaptcha.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (start < 1) {
                        try {
                            getAdapter().getView().setTextInputLayoutHint(getLoginHolder().getEdtCaptcha(),
                                    mContext.getResources().getString(R.string.sdk_vcb_linkacc_captcha_hint),
                                    mContext);
                        } catch (Exception e) {
                            Log.e(this, e);
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
            loginHolder.edtCaptchaTextInputLayout = (TextInputLayout) mAdapter.getView().findViewById(R.id.edt_login_captcha_textInputLayout);
            loginHolder.edtCaptcha.setGroupText(false);
            loginHolder.srvScrollView = (ScrollView) mAdapter.getView().findViewById(R.id.zpw_scrollview_container);
            loginHolder.edtCaptcha.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            });

            loginHolder.imgCaptcha = (ImageView) mAdapter.getView().findViewById(R.id.img_login_captcha);
            loginHolder.webCaptcha = (WebView) mAdapter.getView().findViewById(R.id.web_login_captcha);

            // new gui init confirm info
            registerHolder = new RegisterHolder();
            registerHolder.llRegister = (LinearLayout) mAdapter.getView().findViewById(R.id.zpw_vcb_confirm_link);
            registerHolder.spnWalletType = (Spinner) mAdapter.getView().findViewById(R.id.spn_register_WalletType);
            registerHolder.spnAccNumberDefault = (Spinner) mAdapter.getView().findViewById(R.id.spn_register_AccNumberDefault);
            registerHolder.llAccNumberDefault = (LinearLayout) mAdapter.getView().findViewById(R.id.ll_register_AccNumberDefault);
            registerHolder.ilAccNumberDefault = (TextInputLayout) mAdapter.getView().findViewById(R.id.il_register_AccNumberDefault);
            registerHolder.spnPhoneNumber = (Spinner) mAdapter.getView().findViewById(R.id.spn_register_PhoneNumber);
            registerHolder.edtPhoneNum = (EditText) mAdapter.getView().findViewById(R.id.edt_register_PhoneNumber);
            registerHolder.edtPhoneNum.setKeyListener(null);
            registerHolder.edtAccNumDefault = (EditText) mAdapter.getView().findViewById(R.id.edt_register_AccNumberDefault);
            registerHolder.edtAccNumDefault.setKeyListener(null);
            registerHolder.spnOTPValidType = (Spinner) mAdapter.getView().findViewById(R.id.spn_register_OTPValidType);
            registerHolder.tvPhoneReceiveOTP = (TextView) mAdapter.getView().findViewById(R.id.tv_register_phone_receive_OTP);
            registerHolder.edtCaptcha = (VPaymentDrawableEditText) mAdapter.getView().findViewById(R.id.edt_register_captcha);
            registerHolder.btnRefreshCaptcha = (ImageView) mAdapter.getView().findViewById(R.id.refresh_captcha_register);
            registerHolder.edtCaptcha.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (start < 1) {
                        try {
                            getAdapter().getView().setTextInputLayoutHint(getRegisterHolder().getEdtCaptcha(),
                                    mContext.getResources().getString(R.string.sdk_vcb_linkacc_captcha_hint), mContext);
                        } catch (Exception e) {
                            Log.e(this, e);
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
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            });
            registerHolder.imgCaptcha = (ImageView) mAdapter.getView().findViewById(R.id.img_register_captcha);
            registerHolder.webCaptcha = (WebView) mAdapter.getView().findViewById(R.id.web_register_captcha);

            // new gui init confirm otp
            confirmOTPHolder = new ConfirmOTPHolder();
            confirmOTPHolder.llConfirmOTP = (LinearLayout) mAdapter.getView().findViewById(R.id.zpw_vcb_otp);
            confirmOTPHolder.edtConfirmOTP = (VPaymentDrawableEditText) mAdapter.getView().findViewById(R.id.edt_otp_OTP);
            confirmOTPHolder.edtConfirmOTP.setGroupText(false);
            confirmOTPHolder.edtConfirmOTP.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            });

            // new gui init unregister
            unregisterHolder = new UnregisterHolder();
            unregisterHolder.llUnregister = (LinearLayout) mAdapter.getView().findViewById(R.id.zpw_vcb_confirm_unlink);
            unregisterHolder.spnWalletType = (Spinner) mAdapter.getView().findViewById(R.id.spn_unregister_WalletType);
            unregisterHolder.spnPhoneNumber = (Spinner) mAdapter.getView().findViewById(R.id.spn_unregister_PhoneNumber);
            unregisterHolder.edtPhoneNumber = (EditText) mAdapter.getView().findViewById(R.id.edt_unregister_PhoneNumber);
            unregisterHolder.edtPhoneNumber.setKeyListener(null);
            unregisterHolder.edtPassword = (VPaymentDrawableEditText) mAdapter.getView().findViewById(R.id.edt_unregister_password);
            unregisterHolder.edtPassword.setGroupText(false);
            unregisterHolder.edtPassword.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            });

            // message
            txtMessage = (TextView) mAdapter.getView().findViewById(R.id.txt_Message);

            // progress
            pgbProgress = (ProgressBar) mAdapter.getView().findViewById(R.id.pgb_progress);

            submitButton = mAdapter.getView().findViewById(R.id.zpw_vcb_submit);
        } catch (Exception e) {
            Log.e(this, e);
        }

        getLoginHolder().getEdtUsername().addTextChangedListener(mLoginEditTextWatcher);
        getLoginHolder().getEdtPassword().addTextChangedListener(mLoginEditTextWatcher);
        getLoginHolder().getEdtCaptcha().addTextChangedListener(mLoginEditTextWatcher);
        // getLoginHolder().getEdtCaptcha().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
        getLoginHolder().getEdtCaptcha().requestFocus();

        getRegisterHolder().getEdtCaptcha().addTextChangedListener(mConfirmCaptchaEditTextWatcher);
        getRegisterHolder().getEdtCaptcha().requestFocus();
        //getRegisterHolder().getEdtCaptcha().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
        getConfirmOTPHolder().getEdtConfirmOTP().addTextChangedListener(mConfirmOtpEditTextWatcher);
        //getConfirmOTPHolder().getEdtConfirmOTP().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
        getUnregisterHolder().getEdtPassword().addTextChangedListener(mUnRegPassEditTextWatcher);
        //getUnregisterHolder().getEdtPassword().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
    }


    /***
     * @param pDrawable
     */
    public void setLogoImgLinkAcc(Drawable pDrawable) {
        if (pDrawable != null) {
            getLoginHolder().getImgLogoLinkAcc().setImageResource(android.R.color.transparent);
            // set Image
            getLoginHolder().getImgLogoLinkAcc().setImageDrawable(pDrawable);
        }
    }

    /***
     * @param pB64Encoded
     */
    public void setCaptchaImgB64Login(String pB64Encoded) {
        if (TextUtils.isEmpty(pB64Encoded))
            return;
        Bitmap bitmap = BitmapUtils.b64ToImage(pB64Encoded);
        if (bitmap != null) {
            getLoginHolder().getImgCaptcha().setVisibility(View.VISIBLE);
            getLoginHolder().getWebCaptcha().setVisibility(View.GONE);
            // set Image
            getLoginHolder().getImgCaptcha().setImageBitmap(bitmap);
        }
    }

    /***
     * @param pUrl
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setCaptchaImgLogin(String pUrl) {
        if (TextUtils.isEmpty(pUrl))
            return;
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head></head><body style='margin:0;padding:0'><img src='").append(pUrl)
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
        getLoginHolder().getWebCaptcha().loadDataWithBaseURL(pUrl, sb.toString(),
                "text/html", null, null);
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

    /***
     * @param pUrl
     */
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
        return (getConfirmOTPHolder().getEdtConfirmOTP() != null && getConfirmOTPHolder().getEdtConfirmOTP().getVisibility() == View.VISIBLE);
    }

    /***
     * @param pMessage
     */
    public void setMessage(String pMessage) {
        if (pMessage == null)
            return;

        // set Message
        getTxtMessage().setText(pMessage);
    }

    /***
     * reset form input captcha
     */
    public void resetCaptchaInput() {
        // clear captcha form
        getLoginHolder().getEdtCaptcha().setText("");
    }

    /***
     * reset form input captcha
     */
    public void resetCaptchaConfirm() {
        // clear captcha form
        getRegisterHolder().getEdtCaptcha().setText("");
    }

    public void resetOtp() {
        getConfirmOTPHolder().getEdtConfirmOTP().setText(null);
    }

    /***
     * @param pList
     */
    public void setWalletList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getRegisterHolder().getSpnWalletType().setAdapter(adapter);
        }
    }

    /***
     * @param pList
     */
    public void setAccNumList(ArrayList<String> pList) {
        if (pList != null) {
            getRegisterHolder().getSpnAccNumberDefault().setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, pList));
            getRegisterHolder().getSpnAccNumberDefault().setClickable(true);
            getRegisterHolder().getSpnAccNumberDefault().setOnTouchListener((view, motionEvent) -> {
                showDialogSpinnerView();
                if (getNumberPickerView() != null) {
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
                }
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
    public String getDetectedBankCode() {
        return (mPaymentInfoHelper != null) ? mPaymentInfoHelper.getLinkAccBankCode() : "";
    }

    /***
     * @param pView
     * @return
     */
    public boolean checkValidRequiredEditText(EditText pView) {
        if (pView.getVisibility() != View.VISIBLE) {
            return true;
        }

        boolean isCheckPattern = true;

        if (pView instanceof VPaymentDrawableEditText || pView instanceof VPaymentValidDateEditText)
            isCheckPattern = ((VPaymentEditText) pView).isValid();

        return isCheckPattern && (pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText().toString()));
    }

    @Override
    protected boolean needToWarningNotSupportCard() {
        return false;
    }

    @Override
    public CardCheck getCardFinder() {
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

    @Override
    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        return null;
    }

    public boolean checkEnableLoginSubmitButton() {
        boolean isLoginCaptcha = checkValidRequiredEditText(getLoginHolder().getEdtCaptcha());
        boolean isLoginName = checkValidRequiredEditText(getLoginHolder().getEdtUsername());
        boolean isLoginPassword = checkValidRequiredEditText(getLoginHolder().getEdtPassword());
        try {
            if (isLoginPassword && isLoginName && isLoginCaptcha) {
                getAdapter().getView().enableSubmitBtn();
                getAdapter().getView().changeBgSubmitButton(getAdapter().isFinalStep());
                return true;
            } else {
                getAdapter().getView().disableSubmitBtn();
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public boolean checkEnableConfirmCaptchaSubmitButton() {
        boolean isConfirmCaptcha = checkValidRequiredEditText(getRegisterHolder().getEdtCaptcha());
        try {
            if (isConfirmCaptcha) {
                getAdapter().getView().enableSubmitBtn();
                getAdapter().getView().changeBgSubmitButton(getAdapter().isFinalStep());
                return true;
            } else {
                getAdapter().getView().disableSubmitBtn();
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    /***
     * @return
     */
    public boolean checkEnableConfirmOtpSubmitButton() {
        boolean isConfirmOtp = checkValidRequiredEditText(getConfirmOTPHolder().getEdtConfirmOTP());
        try {
            if (isConfirmOtp) {
                getAdapter().getView().enableSubmitBtn();
                getAdapter().getView().changeBgSubmitButton(getAdapter().isFinalStep());
                return true;
            } else {
                getAdapter().getView().disableSubmitBtn();
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    /***
     * @return
     */
    public boolean checkEnableUnRegPassSubmitButton() {
        boolean isUnRegisterPassword = checkValidRequiredEditText(getUnregisterHolder().getEdtPassword());
        try {
            if (isUnRegisterPassword) {
                getAdapter().getView().enableSubmitBtn();
                getAdapter().getView().changeBgSubmitButton(getAdapter().isFinalStep());
                return true;
            } else {
                getAdapter().getView().disableSubmitBtn();
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public class UnregisterHolder {
        LinearLayout llUnregister;
        Spinner spnWalletType;
        Spinner spnPhoneNumber;
        EditText edtPhoneNumber;
        VPaymentDrawableEditText edtPassword;

        public LinearLayout getLlUnregister() {
            return llUnregister;
        }

        public Spinner getSpnWalletType() {
            return spnWalletType;
        }

        public Spinner getSpnPhoneNumber() {
            return spnPhoneNumber;
        }

        public VPaymentDrawableEditText getEdtPassword() {
            return edtPassword;
        }

        public EditText getEdtPhoneNumber() {
            return edtPhoneNumber;
        }
    }

    public class ConfirmOTPHolder {
        LinearLayout llConfirmOTP;
        VPaymentDrawableEditText edtConfirmOTP;

        public LinearLayout getLlConfirmOTP() {
            return llConfirmOTP;
        }

        public EditText getEdtConfirmOTP() {
            return edtConfirmOTP;
        }
    }

    public class RegisterHolder {
        LinearLayout llRegister;
        Spinner spnWalletType;
        Spinner spnAccNumberDefault;
        Spinner spnPhoneNumber;
        Spinner spnOTPValidType;
        TextView tvPhoneReceiveOTP;
        VPaymentDrawableEditText edtCaptcha;
        EditText edtPhoneNum;
        EditText edtAccNumDefault;
        ImageView imgCaptcha;
        WebView webCaptcha;
        LinearLayout llAccNumberDefault;
        TextInputLayout ilAccNumberDefault;
        ImageView btnRefreshCaptcha;

        public LinearLayout getLlRegister() {
            return llRegister;
        }

        public Spinner getSpnWalletType() {
            return spnWalletType;
        }

        public Spinner getSpnAccNumberDefault() {
            return spnAccNumberDefault;
        }

        public LinearLayout getLlAccNumberDefault() {
            return llAccNumberDefault;
        }

        public TextInputLayout getIlAccNumberDefault() {
            return ilAccNumberDefault;
        }

        public Spinner getSpnPhoneNumber() {
            return spnPhoneNumber;
        }

        public EditText getEdtPhoneNum() {
            return edtPhoneNum;
        }

        public EditText getEdtAccNumDefault() {
            return edtAccNumDefault;
        }

        public Spinner getSpnOTPValidType() {
            return spnOTPValidType;
        }

        public TextView getTvPhoneReceiveOTP() {
            return tvPhoneReceiveOTP;
        }

        public VPaymentDrawableEditText getEdtCaptcha() {
            return edtCaptcha;
        }

        public ImageView getImgCaptcha() {
            return imgCaptcha;
        }

        public WebView getWebCaptcha() {
            return webCaptcha;
        }

        public ImageView getButtonRefreshCaptcha() {
            return btnRefreshCaptcha;
        }
    }

    // Holder: login form
    public class LoginHolder {
        LinearLayout llLogin;
        VPaymentDrawableEditText edtUsername;
        VPaymentDrawableEditText edtPassword;
        VPaymentDrawableEditText edtCaptcha;
        TextInputLayout edtCaptchaTextInputLayout;
        ImageView imgCaptcha;
        WebView webCaptcha;
        ScrollView srvScrollView;
        ImageView imgLogoLinkAcc;

        TextView vcb_note_textview;
        ImageView btnRefreshCaptcha;

        public TextView getVcb_note_textview() {
            return vcb_note_textview;
        }


        public LinearLayout getLlLogin() {
            return llLogin;
        }

        public EditText getEdtUsername() {
            return edtUsername;
        }

        public EditText getEdtPassword() {
            return edtPassword;
        }

        public EditText getEdtCaptcha() {
            return edtCaptcha;
        }

        public TextInputLayout getEdtCaptchaTextInputLayout() {
            return edtCaptchaTextInputLayout;
        }

        public ImageView getImgCaptcha() {
            return imgCaptcha;
        }

        public WebView getWebCaptcha() {
            return webCaptcha;
        }

        public ScrollView getSrvScrollView() {
            return srvScrollView;
        }

        public ImageView getImgLogoLinkAcc() {
            return imgLogoLinkAcc;
        }

        public ImageView getButtonRefreshCaptcha() {
            return btnRefreshCaptcha;
        }
    }
}
