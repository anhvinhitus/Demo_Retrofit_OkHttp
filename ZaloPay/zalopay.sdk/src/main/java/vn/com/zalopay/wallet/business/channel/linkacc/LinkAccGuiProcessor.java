package vn.com.zalopay.wallet.business.channel.linkacc;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.utils.BitmapUtil;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.SpinnerUtils;
import vn.com.zalopay.wallet.utils.ViewUtils;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;

/**
 * Created by SinhTT on 14/10/2016.
 */

public class LinkAccGuiProcessor extends CardGuiProcessor {
    private AdapterBase mAdapter;
    private int mPositionSpn;
    private View.OnFocusChangeListener mOnFocusChangeListenerLoginHolder = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(final View view, boolean hasFocus) {

            if (hasFocus) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), (EditText) view);
                            Log.d(this, "mOnFocusChangeListener");
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 200);
            }
        }
    };
    private ProgressBar pgbProgress;
    private TextView txtMessage;
    private LoginHolder loginHolder;
    ///////LISTENER////////
    // listener EditText
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
    ///////LISTENER////////
    // listener EditText
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

    ///////LISTENER////////
    // listener EditText
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

    private FrameLayout llRoot;
    private View submitButton;
    private boolean isVisibilitySpinner = false;
    private View.OnClickListener mSpinnerButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                int i = view.getId();
                if (i == R.id.footer_confirm_button) {
                    Log.d("OnClick()", "Confirm Button");
                    getRegisterHolder().getSpnAccNumberDefault().setSelection(mPositionSpn);
                } else if (i == R.id.zpw_vcb_dialog_spinner) {
                    Log.d("OnClickListener", "zpw_pay_support_buttom_view");
                } else if (i == R.id.cancel_button) {
                    Log.d("OnClickListener", "cancel_button");
                }

                closeSpinnerView();

            } catch (Exception ex) {
                Log.e(this, ex);
            }
        }
    };

    /***
     * @param pAdapter
     */
    public LinkAccGuiProcessor(AdapterBase pAdapter) {
        mAdapter = pAdapter;
        init();
    }

    protected AdapterBase getAdapter() {
        return mAdapter;
    }

    public void setAccountTest()
    {
        loginHolder.edtUsername.setText("9044060a00");
        loginHolder.edtPassword.setText("minhly2910");
    }
    /***
     * Show showDialogSpinner view
     */
    public void showDialogSpinnerView() {
        try {
            setView(R.id.zpw_vcb_dialog_spinner, true);
            isVisibilitySpinner = true;
            //

            View view_spinnert = mAdapter.getActivity().findViewById(R.id.zpw_vcb_dialog_spinner);
            View cancel_button = mAdapter.getActivity().findViewById(R.id.cancel_button);
            View footer_confirm_button = mAdapter.getActivity().findViewById(R.id.footer_confirm_button);

            view_spinnert.setOnClickListener(mSpinnerButtonClickListener);
            cancel_button.setOnClickListener(mSpinnerButtonClickListener);
            footer_confirm_button.setOnClickListener(mSpinnerButtonClickListener);
            View v = mAdapter.getActivity().findViewById(R.id.layout_animation);
            if (v != null) {
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(mAdapter.getActivity(), R.anim.slide_in_bottom);
                v.startAnimation(hyperspaceJumpAnimation);
            }
        } catch (Exception e) {
            Log.d(this, e);
        }

    }

    public void setView(int pId, boolean pIsVisible) {
        View view = mAdapter.getActivity().findViewById(pId);

        if (view != null) {
            view.setVisibility(pIsVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void closeSpinnerView() {
        View v = mAdapter.getActivity().findViewById(R.id.layout_animation);
        if (v != null) {
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(mAdapter.getActivity(), R.anim.slide_out_bottom);
            v.startAnimation(hyperspaceJumpAnimation);
        }
        isVisibilitySpinner = false;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setView(R.id.zpw_vcb_dialog_spinner, false);
            }
        }, 300);
    }

    public boolean getVisibilitySpinnerView() {
        return isVisibilitySpinner;
    }

    public View getNumberPickerView() {
        return mAdapter.getActivity().findViewById(R.id.number_picker);
    }

    public View getSpinnerView() {
        return ((mAdapter.getActivity().findViewById(R.id.zpw_vcb_dialog_spinner) != null) ? mAdapter.getActivity().findViewById(R.id.zpw_vcb_dialog_spinner) : null);
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

    public LinearLayout getLlRoot_linear_layout() {
        return (LinearLayout) mAdapter.getActivity().findViewById(R.id.ll_layout_rootview);
    }

    public View getLlButton() {
        return submitButton;
    }

    protected void init() {
        // new gui init login
        loginHolder = new LoginHolder();
        loginHolder.llLogin = (LinearLayout) mAdapter.getActivity().findViewById(R.id.zpw_vcb_login);
        loginHolder.imgLogoLinkAcc = (ImageView) mAdapter.getActivity().findViewById(R.id.img_logo_linkacc);
        loginHolder.edtUsername = (VPaymentDrawableEditText) mAdapter.getActivity().findViewById(R.id.edt_login_username);
        loginHolder.edtUsername.setGroupText(false);
        loginHolder.edtPassword = (VPaymentDrawableEditText) mAdapter.getActivity().findViewById(R.id.edt_login_password);
        loginHolder.edtPassword.setGroupText(false);
        loginHolder.edtCaptcha = (VPaymentDrawableEditText) mAdapter.getActivity().findViewById(R.id.edt_login_captcha);
        loginHolder.edtCaptcha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (start < 1) {
                    ViewUtils.setTextInputLayoutHint(getLoginHolder().getEdtCaptcha(),
                            GlobalData.getStringResource(RS.string.zpw_string_linkacc_captcha_hint),
                            mAdapter.getActivity());
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        loginHolder.edtCaptchaTextInputLayout = (TextInputLayout) mAdapter.getActivity().findViewById(R.id.edt_login_captcha_textInputLayout);
        loginHolder.edtCaptcha.setGroupText(false);
        loginHolder.srvScrollView = (ScrollView) mAdapter.getActivity().findViewById(R.id.srv_test_scrollview);
        loginHolder.edtCaptcha.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            }
        });

        loginHolder.imgCaptcha = (ImageView) mAdapter.getActivity().findViewById(R.id.img_login_captcha);
        loginHolder.webCaptcha = (WebView) mAdapter.getActivity().findViewById(R.id.web_login_captcha);

        // new gui init confirm info
        registerHolder = new RegisterHolder();
        registerHolder.llRegister = (LinearLayout) mAdapter.getActivity().findViewById(R.id.zpw_vcb_confirm_link);
        registerHolder.spnWalletType = (Spinner) mAdapter.getActivity().findViewById(R.id.spn_register_WalletType);
        registerHolder.spnAccNumberDefault = (Spinner) mAdapter.getActivity().findViewById(R.id.spn_register_AccNumberDefault);
        registerHolder.llAccNumberDefault = (LinearLayout) mAdapter.getActivity().findViewById(R.id.ll_register_AccNumberDefault);
        registerHolder.ilAccNumberDefault = (TextInputLayout) mAdapter.getActivity().findViewById(R.id.il_register_AccNumberDefault);
        registerHolder.spnPhoneNumber = (Spinner) mAdapter.getActivity().findViewById(R.id.spn_register_PhoneNumber);
        registerHolder.edtPhoneNum = (EditText) mAdapter.getActivity().findViewById(R.id.edt_register_PhoneNumber);
        registerHolder.edtPhoneNum.setKeyListener(null);
        registerHolder.edtAccNumDefault = (EditText) mAdapter.getActivity().findViewById(R.id.edt_register_AccNumberDefault);
        registerHolder.edtAccNumDefault.setKeyListener(null);
        registerHolder.spnOTPValidType = (Spinner) mAdapter.getActivity().findViewById(R.id.spn_register_OTPValidType);
        registerHolder.tvPhoneReceiveOTP = (TextView) mAdapter.getActivity().findViewById(R.id.tv_register_phone_receive_OTP);
        registerHolder.edtCaptcha = (VPaymentDrawableEditText) mAdapter.getActivity().findViewById(R.id.edt_register_captcha);
        registerHolder.edtCaptcha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (start < 1) {
                    ViewUtils.setTextInputLayoutHint(getRegisterHolder().getEdtCaptcha(),
                            GlobalData.getStringResource(RS.string.zpw_string_linkacc_captcha_hint)
                            , mAdapter.getActivity());
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
        registerHolder.edtCaptcha.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            }
        });
        registerHolder.imgCaptcha = (ImageView) mAdapter.getActivity().findViewById(R.id.img_register_captcha);
        registerHolder.webCaptcha = (WebView) mAdapter.getActivity().findViewById(R.id.web_register_captcha);

        // new gui init confirm otp
        confirmOTPHolder = new ConfirmOTPHolder();
        confirmOTPHolder.llConfirmOTP = (LinearLayout) mAdapter.getActivity().findViewById(R.id.zpw_vcb_otp);
        confirmOTPHolder.edtConfirmOTP = (VPaymentDrawableEditText) mAdapter.getActivity().findViewById(R.id.edt_otp_OTP);
        confirmOTPHolder.edtConfirmOTP.setGroupText(false);
        confirmOTPHolder.edtConfirmOTP.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            }
        });

        // new gui init unregister
        unregisterHolder = new UnregisterHolder();
        unregisterHolder.llUnregister = (LinearLayout) mAdapter.getActivity().findViewById(R.id.zpw_vcb_confirm_unlink);
        unregisterHolder.spnWalletType = (Spinner) mAdapter.getActivity().findViewById(R.id.spn_unregister_WalletType);
        unregisterHolder.spnPhoneNumber = (Spinner) mAdapter.getActivity().findViewById(R.id.spn_unregister_PhoneNumber);
        unregisterHolder.edtPhoneNumber = (EditText) mAdapter.getActivity().findViewById(R.id.edt_unregister_PhoneNumber);
        unregisterHolder.edtPhoneNumber.setKeyListener(null);
        unregisterHolder.edtPassword = (VPaymentDrawableEditText) mAdapter.getActivity().findViewById(R.id.edt_unregister_password);
        unregisterHolder.edtPassword.setGroupText(false);
        unregisterHolder.edtPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mAdapter.onClickSubmission();
                    return true;
                }
                return false;
            }
        });

        // message
        txtMessage = (TextView) mAdapter.getActivity().findViewById(R.id.txt_Message);

        // progress
        pgbProgress = (ProgressBar) mAdapter.getActivity().findViewById(R.id.pgb_progress);

        //
        llRoot = (FrameLayout) mAdapter.getActivity().findViewById(R.id.ll_test_rootview);
        submitButton = mAdapter.getActivity().findViewById(R.id.zpw_vcb_submit);

        // set group text
        // TODO: code here

        // set setKeyListener
        // TODO: code here

        //set listener editText
        // TODO: code here
        getLoginHolder().getEdtUsername().addTextChangedListener(mLoginEditTextWatcher);
        getLoginHolder().getEdtPassword().addTextChangedListener(mLoginEditTextWatcher);
        getLoginHolder().getEdtCaptcha().addTextChangedListener(mLoginEditTextWatcher);
        getLoginHolder().getEdtCaptcha().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
        getLoginHolder().getEdtCaptcha().requestFocus();

        getRegisterHolder().getEdtCaptcha().addTextChangedListener(mConfirmCaptchaEditTextWatcher);
        getRegisterHolder().getEdtCaptcha().requestFocus();
        getRegisterHolder().getEdtCaptcha().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
        getConfirmOTPHolder().getEdtConfirmOTP().addTextChangedListener(mConfirmOtpEditTextWatcher);
        getConfirmOTPHolder().getEdtConfirmOTP().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
        getUnregisterHolder().getEdtPassword().addTextChangedListener(mUnRegPassEditTextWatcher);
        getUnregisterHolder().getEdtPassword().setOnFocusChangeListener(mOnFocusChangeListenerLoginHolder);
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

        Bitmap bitmap = BitmapUtil.b64ToImage(pB64Encoded);

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
        getLoginHolder().getWebCaptcha().setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

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

    /***
     * @param pB64Encoded
     */
    public void setCaptchaImgB64Confirm(String pB64Encoded) {
        if (TextUtils.isEmpty(pB64Encoded))
            return;

        Bitmap bitmap = BitmapUtil.b64ToImage(pB64Encoded);

        if (bitmap != null) {
            getRegisterHolder().getImgCaptcha().setVisibility(View.VISIBLE);
            getRegisterHolder().getWebCaptcha().setVisibility(View.GONE);

            // set Image
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
        getRegisterHolder().getWebCaptcha().setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

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

    /***
     * @param pList
     */
    public void setWalletList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mAdapter.getActivity(),
                    android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getRegisterHolder().getSpnWalletType().setAdapter(adapter);
            return;
        }
    }

    /***
     * @param pList
     */
    public void setAccNumList(ArrayList<String> pList) {
        if (pList != null) {
            getRegisterHolder().getSpnAccNumberDefault().setAdapter(new ArrayAdapter<String>(mAdapter.getActivity(),
                    android.R.layout.simple_spinner_item, pList));
            getRegisterHolder().getSpnAccNumberDefault().setClickable(true);
            getRegisterHolder().getSpnAccNumberDefault().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    // TODO: code here to show dialog_spinner

                    showDialogSpinnerView();
                    if (getNumberPickerView() != null) {
                        NumberPicker picker = (NumberPicker) getNumberPickerView();
                        picker.setMinValue(0);
                        picker.setMaxValue(getRegisterHolder().getSpnAccNumberDefault().getCount() - 1);
                        picker.setDisplayedValues(SpinnerUtils.getItems(getRegisterHolder().getSpnAccNumberDefault()));
                        picker.setWrapSelectorWheel(false);
                        picker.setValue(getRegisterHolder().getSpnAccNumberDefault().getSelectedItemPosition());
                        picker.setOnScrollListener(new NumberPicker.OnScrollListener() {
                            @Override
                            public void onScrollStateChange(NumberPicker numberPicker, int i) {
                                // selection position i
                                mPositionSpn = numberPicker.getValue();
                            }
                        });
                    }
                    return true;
                }
            });


            return;
        }
    }

    /***
     * @param pList
     */
    public void setAccNum(ArrayList<String> pList) {
        if (pList != null) {
            getRegisterHolder().getEdtAccNumDefault().setText(pList.get(0));
            return;
        }
    }

    /***
     * @param pList
     */
    public void setPhoneNumList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mAdapter.getActivity(),
                    android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getRegisterHolder().getSpnPhoneNumber().setAdapter(adapter);
            return;
        }
    }

    /***
     * @param pList
     */
    public void setPhoneNum(ArrayList<String> pList) {
        if (pList != null) {
            getRegisterHolder().getEdtPhoneNum().setText(pList.get(0));
            return;
        }
    }

    /***
     * @param pList
     */
    public void setOtpValidList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mAdapter.getActivity(),
                    android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getRegisterHolder().getSpnOTPValidType().setAdapter(adapter);
            return;
        }
    }

    /***
     * @param pValue
     */
    public void setPhoneReceiveOTP(String pValue) {
        if (pValue != null) {
            getRegisterHolder().getTvPhoneReceiveOTP().setText(pValue);
        }
    }

    /***
     * @param pValue
     */
    public void setProgress(int pValue) {
        if (pValue < 0 && pValue > 100) {
            return;
        }

        getPgbProgress().setProgress(pValue);
    }

    /***
     * @param pList
     */
    public void setWalletUnRegList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mAdapter.getActivity(),
                    android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getUnregisterHolder().getSpnWalletType().setAdapter(adapter);
            return;
        }
    }

    /***
     * @param pList
     */
    public void setPhoneNumUnRegList(ArrayList<String> pList) {
        if (pList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mAdapter.getActivity(),
                    android.R.layout.simple_spinner_item, pList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getUnregisterHolder().getSpnPhoneNumber().setAdapter(adapter);
            return;
        }
    }

    /***
     * @param pList
     */
    public void setPhoneNumUnReg(ArrayList<String> pList) {
        if (pList != null) {
            getUnregisterHolder().getEdtPhoneNumber().setText(pList.get(0));
            return;
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
    protected void setWebViewUserAgent() {

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

    /***
     * @return
     */
    public boolean checkEnableLoginSubmitButton() {
        boolean isLoginCaptcha = checkValidRequiredEditText(getLoginHolder().getEdtCaptcha());
        boolean isLoginName = checkValidRequiredEditText(getLoginHolder().getEdtUsername());
        boolean isLoginPassword = checkValidRequiredEditText(getLoginHolder().getEdtPassword());

        if (isLoginPassword && isLoginName && isLoginCaptcha) {
            getAdapter().getActivity().enableSubmitBtn(true);
            return true;
        } else {
            getAdapter().getActivity().enableSubmitBtn(false);
            return false;
        }
    }

    /***
     * @return
     */
    public boolean checkEnableConfirmCaptchaSubmitButton() {
        boolean isConfirmCaptcha = checkValidRequiredEditText(getRegisterHolder().getEdtCaptcha());

        if (isConfirmCaptcha) {
            getAdapter().getActivity().enableSubmitBtn(true);
            return true;
        } else {
            getAdapter().getActivity().enableSubmitBtn(false);
            return false;
        }
    }

    /***
     * @return
     */
    public boolean checkEnableConfirmOtpSubmitButton() {
        boolean isConfirmOtp = checkValidRequiredEditText(getConfirmOTPHolder().getEdtConfirmOTP());

        if (isConfirmOtp) {
            getAdapter().getActivity().enableSubmitBtn(true);
            return true;
        } else {
            getAdapter().getActivity().enableSubmitBtn(false);
            return false;
        }
    }

    /***
     * @return
     */
    public boolean checkEnableUnRegPassSubmitButton() {
        boolean isUnRegisterPassword = checkValidRequiredEditText(getUnregisterHolder().getEdtPassword());

        if (isUnRegisterPassword) {
            getAdapter().getActivity().enableSubmitBtn(true);
            return true;
        } else {
            getAdapter().getActivity().enableSubmitBtn(false);
            return false;
        }
    }

    public class UnregisterHolder {
        private LinearLayout llUnregister;
        private Spinner spnWalletType;
        private Spinner spnPhoneNumber;
        private EditText edtPhoneNumber;
        private VPaymentDrawableEditText edtPassword;

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
        private LinearLayout llConfirmOTP;
        private VPaymentDrawableEditText edtConfirmOTP;

        public LinearLayout getLlConfirmOTP() {
            return llConfirmOTP;
        }

        public EditText getEdtConfirmOTP() {
            return edtConfirmOTP;
        }
    }

    public class RegisterHolder {
        private LinearLayout llRegister;
        private Spinner spnWalletType;
        private Spinner spnAccNumberDefault;
        private Spinner spnPhoneNumber;
        private Spinner spnOTPValidType;
        private TextView tvPhoneReceiveOTP;
        private VPaymentDrawableEditText edtCaptcha;
        private EditText edtPhoneNum;
        private EditText edtAccNumDefault;
        private ImageView imgCaptcha;
        private WebView webCaptcha;
        private LinearLayout llAccNumberDefault;
        private TextInputLayout ilAccNumberDefault;

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
    }
}
