package vn.com.zalopay.wallet.business.channel.localbank;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.functions.Action1;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.BitmapUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.constants.AuthenType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.adapter.LocalCardFragmentAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;

public class BankCardGuiProcessor extends CardGuiProcessor {
    private RadioGroup mInputRadioGroupAuthenType;
    @AuthenType
    private String mAuthenType = AuthenType.OTP;

    private View mOtpTokenLayoutRootView;
    private View mOtpTockenLayoutView;

    private VPaymentDrawableEditText mOtpAuthenEditText, mTokenAuthenEditText;

    private TextInputLayout mTextLayoutOtp, mTextLayoutToken;

    private AppCompatRadioButton mRadioButtonToken;

    private View mRadioGroupAuthenSelectionView;

    private RadioGroup mAuthenRadioGroup;

    private ImageView mCaptchaImage;
    private View mCaptchaFrame;
    private WebView mCaptchaWebview;
    private VPaymentDrawableEditText mAccountNameEditText, mAccountPasswordEditText, mOtpWebEditText, mCaptchaWebEditText, mOnlinePasswordEditText;

    public BankCardGuiProcessor(AdapterBankCard pAdapterLocalCard) {
        super();
        mAdapter = new WeakReference<>(pAdapterLocalCard);
        init(mAdapter.get().getPaymentInfoHelper());
    }

    @Override
    protected void init(PaymentInfoHelper paymentInfoHelper) {
        super.init(paymentInfoHelper);
        try {
            mOtpTokenLayoutRootView = getAdapter().getView().findViewById(R.id.zpw_content_input_view_root);
            mOtpTokenLayoutRootView.setVisibility(View.GONE);
            mRadioGroupAuthenSelectionView = getAdapter().getView().findViewById(R.id.linearlayout_selection_authen);
            AppCompatRadioButton mRadioButtonSms = (AppCompatRadioButton) getAdapter().getView().findViewById(R.id.radioSelectionSmS);
            mRadioButtonToken = (AppCompatRadioButton) getAdapter().getView().findViewById(R.id.radioSelectionToken);
            mRadioButtonSms.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    mOtpAuthenEditText.setVisibility(View.VISIBLE);
                    mTextLayoutOtp.setVisibility(View.VISIBLE);
                    mTokenAuthenEditText.setVisibility(View.GONE);
                    mTextLayoutToken.setVisibility(View.GONE);

                    mAuthenType = AuthenType.OTP;
                }
            });
            mRadioButtonToken.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    mOtpAuthenEditText.setVisibility(View.GONE);
                    mTextLayoutOtp.setVisibility(View.GONE);
                    mTokenAuthenEditText.setVisibility(View.VISIBLE);
                    mTextLayoutToken.setVisibility(View.VISIBLE);

                    mAuthenType = AuthenType.TOKEN;
                }
            });


            mOtpAuthenEditText = (VPaymentDrawableEditText) getAdapter().getView().findViewById(R.id.edittext_otp);
            mTokenAuthenEditText = (VPaymentDrawableEditText) getAdapter().getView().findViewById(R.id.edittext_token);

            mTextLayoutOtp = (TextInputLayout) getAdapter().getView().findViewById(R.id.textLayoutOtp);
            mTextLayoutToken = (TextInputLayout) getAdapter().getView().findViewById(R.id.textLayoutToken);

            mOtpAuthenEditText.setGroupText(false);
            mTokenAuthenEditText.setGroupText(false);

            mOtpTockenLayoutView = getAdapter().getView().findViewById(R.id.linearlayout_authenticate_local_card);

            mInputRadioGroupAuthenType = (RadioGroup) getAdapter().getView().findViewById(R.id.radioGroupAuthenType);
            mAuthenRadioGroup = (RadioGroup) getAdapter().getView().findViewById(R.id.radioGroupAuthenSmsToken);

            mOtpWebEditText = (VPaymentDrawableEditText) getAdapter().getView().findViewById(R.id.zpsdk_otp_ctl);
            mCaptchaWebEditText = (VPaymentDrawableEditText) getAdapter().getView().findViewById(R.id.zpsdk_captchar_ctl);
            mCaptchaImage = (ImageView) getAdapter().getView().findViewById(R.id.zpsdk_captchar_img_ctl);
            mCaptchaFrame = getAdapter().getView().findViewById(R.id.zpsdk_captchar_wv_frame);
            mCaptchaWebview = (WebView) getAdapter().getView().findViewById(R.id.zpsdk_captchar_wv_ctl);

            mAccountNameEditText = (VPaymentDrawableEditText) getAdapter().getView().findViewById(R.id.zpsdk_acc_name_ctl);
            mAccountPasswordEditText = (VPaymentDrawableEditText) getAdapter().getView().findViewById(R.id.zpsdk_acc_password_ctl);

            mOnlinePasswordEditText = (VPaymentDrawableEditText) getAdapter().getView().findViewById(R.id.zpsdk_card_password_ctl);

            if (mOtpWebEditText != null && mCaptchaWebEditText != null && mAccountNameEditText != null && mAccountPasswordEditText != null && mOnlinePasswordEditText != null) {
                mAccountNameEditText.setGroupText(false);
                mAccountPasswordEditText.setGroupText(false);
                mCaptchaWebEditText.setGroupText(false);
                mOtpWebEditText.setGroupText(false);
                mOnlinePasswordEditText.setGroupText(false);

                mOtpWebEditText.setOnFocusChangeListener(getOnOtpCaptchaFocusChangeListener());
                mOtpWebEditText.addTextChangedListener(mEnabledTextWatcher);
                mOtpWebEditText.setOnEditorActionListener(mEditorActionListener);
                mOtpWebEditText.setOnTouchListener(mOnTouchListener);

                mAccountNameEditText.addTextChangedListener(mEnabledTextWatcher);
                mAccountNameEditText.setOnFocusChangeListener(mOnFocusChangeListener);

                mAccountPasswordEditText.addTextChangedListener(mEnabledTextWatcher);
                mAccountPasswordEditText.setOnFocusChangeListener(mOnFocusChangeListener);

                //mCaptchaWebEditText.setOnFocusChangeListener(getOnOtpCaptchaFocusChangeListener());
                mCaptchaWebEditText.addTextChangedListener(mEnabledTextWatcher);
                mCaptchaWebEditText.setOnEditorActionListener(mEditorActionListener);
                //mCaptchaWebEditText.setOnTouchListener(mOnTouchListener);

                mOnlinePasswordEditText.addTextChangedListener(mEnabledTextWatcher);
                mOnlinePasswordEditText.setOnFocusChangeListener(mOnFocusChangeListener);
                mOnlinePasswordEditText.setOnTouchListener(mOnTouchListener);
            }

            if (mOtpAuthenEditText != null && mTokenAuthenEditText != null) {
                mOtpAuthenEditText.setGroupText(false);
                mTokenAuthenEditText.setGroupText(false);

                mOtpAuthenEditText.addTextChangedListener(mEnabledTextWatcher);
                mOtpAuthenEditText.setOnEditorActionListener(mEditorActionListener);
                mOtpAuthenEditText.setOnFocusChangeListener(getOnOtpCaptchaFocusChangeListener());
                mOtpAuthenEditText.setOnTouchListener(mOnTouchListener);

                mTokenAuthenEditText.addTextChangedListener(mEnabledTextWatcher);
                mTokenAuthenEditText.setOnEditorActionListener(mEditorActionListener);
                mTokenAuthenEditText.setOnFocusChangeListener(getOnOtpCaptchaFocusChangeListener());
                mTokenAuthenEditText.setOnTouchListener(mOnTouchListener);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_InputCardInfo, ZPPaymentSteps.OrderStepResult_None, getAdapter().getChannelID());
        }
    }

    @Override
    public void setCardDateOnCardView() {
       // Set hint card issue here
    }

    public void continueDetectCardForLinkCard() {
        Log.d(this, "card number=" + getCardNumber() + "===preparing to detect cc");
        Subscription subscription = getCreditCardFinder().detectOnAsync(getCardNumber(), new Action1<Boolean>() {
            @Override
            public void call(Boolean detected) {
                getAdapter().setNeedToSwitchChannel(detected);
                populateTextOnCardView();
                if (detected) {
                    setDetectedCard(getCreditCardFinder().getBankName(), getCreditCardFinder().getDetectBankCode());
                    checkAutoMoveCardNumberFromBundle = false;
                    getCardView().visibleCardDate();
                    Log.d(this, "card number=" + getCardNumber() + " detected=" + detected + " cc=" + getBankCardFinder().getBankName());
                    isInputBankMaintenance();
                } else {
                    setDetectedCard();
                }
            }
        });
        try {
            getAdapter().getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    public CardCheck getCardFinder() {
        return getBankCardFinder();
    }

    @Override
    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        try {
            return new LocalCardFragmentAdapter(getAdapter().getActivity().getSupportFragmentManager(), getAdapter().getActivity().getIntent().getExtras());
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }

    @Override
    protected boolean validateCardNumberLength() {
        try {
            return getCardNumberView().isValidPattern();

        } catch (Exception e) {
            Log.e(this, e);
        }

        return true;
    }

    @Override
    public boolean isAllowValidateCardNumberByLuhn() {
        return GlobalData.getStringResource(RS.string.zpsdk_luhn_check_atm).equalsIgnoreCase(Constants.TRUE);
    }

    @Override
    public VPaymentValidDateEditText getCardDateView() throws Exception {
        return getCardIssueView();
    }

    @Override
    protected int validateInputCard() {
        int errorFragmentIndex = mCardAdapter.hasError();

        if (errorFragmentIndex > -1)
            return errorFragmentIndex;

        if (!getCardFinder().isDetected()) {
            try {
                return mCardAdapter.getIndexOfFragment(CardNumberFragment.class.getName());

            } catch (Exception e) {
                Log.e(this, e);
            }

            return 0;
        }

        if (mCardAdapter.hasFragment(CardIssueFragment.class.getName()) && TextUtils.isEmpty(getIssueDate())) {
            try {
                return mCardAdapter.getIndexOfFragment(CardIssueFragment.class.getName());

            } catch (Exception e) {
                Log.e(this, e);
            }

            return 1;
        }


        if (TextUtils.isEmpty(getCardName()) || getCardName().length() <= 3) {
            try {
                return mCardAdapter.getIndexOfFragment(CardNameFragment.class.getName());

            } catch (Exception e) {
                Log.e(this, e);
            }

            return 2;
        }

        return errorFragmentIndex;
    }

    @Override
    protected void actionAfterFinishInputCard() {
        if (isUseOtpToken()) {
            getAdapter().setCanEditCardInfo(true);
            try {
                moveToAuthenOptionView();
            } catch (Exception e) {
                Log.e(this, e);
            }
        } else {
            try {
                getAdapter().getView().setVisible(R.id.linearlayout_input_local_card, false);
                getAdapter().getView().visibleCardViewNavigateButton(false);
                getAdapter().getView().visibleSubmitButton(true);
                getAdapter().getView().enableSubmitBtn();
                getAdapter().getView().visiableOrderInfo(false);
            } catch (Exception e) {
                Log.e(this, e);
            }
            getAdapter().onClickSubmission();
        }
    }

    @Override
    protected boolean isATMChannel() {
        return true;
    }

    public void resetCaptcha() {
        if (mCaptchaWebEditText != null) {
            mCaptchaWebEditText.setText(null);
        }
    }

    public VPaymentDrawableEditText getCaptchaEditText() {
        if (mCaptchaWebEditText != null) {
            return mCaptchaWebEditText;
        }
        return null;
    }

    public VPaymentDrawableEditText getOtpWebEditText() {
        if (mOtpWebEditText != null) {
            return mOtpWebEditText;
        }
        return null;
    }

    public VPaymentDrawableEditText getOtpAuthenPayerEditText() {
        if (mOtpAuthenEditText != null) {
            return mOtpAuthenEditText;
        }
        return null;
    }

    public void resetOtpWeb() {
        if (mOtpAuthenEditText != null) {
            mOtpAuthenEditText.setText(null);
        }
    }

    @AuthenType
    public String getAuthenType() {
        return mAuthenType;
    }

    @Override
    protected void populateBankCode() {
        if (!getCardFinder().isDetected()) {
            Log.d("populateLocalCard.not found", "not detect yet");

            return;
        }

        BankConfig bankConfig = getCardFinder().getDetectBankConfig();

        if (bankConfig != null) {
            getAdapter().getCard().setBankcode(bankConfig.code);
        }
    }

    @Override
    public void populateCard() {
        super.populateCard();

        if (mOtpTockenLayoutView.getVisibility() == View.VISIBLE) {
            if (mInputRadioGroupAuthenType.getCheckedRadioButtonId() == R.id.radioButtonToken)
                mAuthenType = AuthenType.TOKEN;
        }
        if (mOtpTokenLayoutRootView.getVisibility() == View.VISIBLE) {
            if (mAuthenRadioGroup.getCheckedRadioButtonId() == R.id.radioSelectionToken)
                mAuthenType = AuthenType.TOKEN;
        }
    }

    public void visualOtpToken(boolean pVisible) {
        try {
            getAdapter().getView().setVisible(R.id.linearlayout_authenticate_local_card, pVisible);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void goBackInputCard() {
        try {
            getAdapter().getView().setVisible(R.id.linearlayout_input_local_card, true);
            getAdapter().getView().visibleCardViewNavigateButton(true);
            getAdapter().getView().visibleSubmitButton(false);
            getAdapter().getView().disableSubmitBtn();
        } catch (Exception e) {
            Log.e(this, e);
        }
        visualOtpToken(false);
        getAdapter().setCanEditCardInfo(false);
        try {
            SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void showOtpTokenView() {
        try {
            getAdapter().getView().visibleInputCardView(false);
            getAdapter().getView().setText(R.id.zpsdk_btn_submit, GlobalData.getStringResource(RS.string.zpw_button_submit_text));
            if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
                getAdapter().getView().visiableOrderInfo(true);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        mOtpTokenLayoutRootView.setVisibility(View.VISIBLE);
        switch (mAuthenType) {
            case AuthenType.OTP:
                mRadioGroupAuthenSelectionView.setVisibility(View.GONE);
                mTokenAuthenEditText.setVisibility(View.GONE);
                mTextLayoutToken.setVisibility(View.GONE);
                mOtpAuthenEditText.setVisibility(View.VISIBLE);
                mTextLayoutOtp.setVisibility(View.VISIBLE);
                showKeyBoardOnEditTextAndScroll(mOtpAuthenEditText);
                break;
            case AuthenType.TOKEN:
                mRadioGroupAuthenSelectionView.setVisibility(View.GONE);
                mOtpAuthenEditText.setVisibility(View.GONE);
                mTextLayoutOtp.setVisibility(View.GONE);

                mTokenAuthenEditText.setVisibility(View.VISIBLE);
                mTextLayoutToken.setVisibility(View.VISIBLE);
                showKeyBoardOnEditTextAndScroll(mTokenAuthenEditText);
                break;
        }
        checkEnableSubmitButton();
        if (GlobalData.analyticsTrackerWrapper != null) {
            GlobalData.analyticsTrackerWrapper.track(ZPPaymentSteps.OrderStep_VerifyOtp, ZPPaymentSteps.OrderStepResult_None, getAdapter().getChannelID());
        }
    }

    public boolean isBankOtpPhase() {
        return (mOtpWebEditText != null && mOtpWebEditText.getVisibility() == View.VISIBLE) ||
                (mOtpAuthenEditText != null && mOtpAuthenEditText.getVisibility() == View.VISIBLE);
    }

    public boolean isCoverBankInProcess() {
        View view = null;
        try {
            view = getAdapter().getView().findViewById(R.id.zpw_content_input_root_view_cover_bank);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return view != null && view.getVisibility() == View.VISIBLE;

    }

    @Override
    public boolean needToWarningNotSupportCard() {
        return needToWarningNotSupportCard && (getCardNumber().length() >= Constants.MIN_ATM_LENGTH);
    }

    public String getAuthenValue() {
        if (mOtpAuthenEditText.getVisibility() == View.VISIBLE)
            return mOtpAuthenEditText.getString();
        else if (mTokenAuthenEditText.getVisibility() == View.VISIBLE)
            return mTokenAuthenEditText.getString();

        return "";
    }

    public String getOnlinePassword() {
        return mOnlinePasswordEditText.getString();
    }

    public String getIssueDate() {
        return mIssueDate;
    }

    public String getCardMonth() {
        if (!TextUtils.isEmpty(mIssueDate))
            return mIssueDate.split("/")[0];

        return null;
    }

    public String getCardYear() {
        if (!TextUtils.isEmpty(mIssueDate))
            return mIssueDate.split("/")[1];

        return null;
    }

    public String getCardPass() {
        return null;
    }

    public String getOtp() {
        if (mOtpWebEditText != null)
            return mOtpWebEditText.getString();
        return null;
    }

    public void setOtp(String pOtp) {
        if (mOtpWebEditText != null && mOtpWebEditText.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pOtp)) {
            mOtpWebEditText.setText(pOtp);
            mOtpWebEditText.setSelection(pOtp.length());
        } else if (mOtpAuthenEditText != null && mOtpAuthenEditText.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pOtp)) {
            mOtpAuthenEditText.setText(pOtp);
            mOtpAuthenEditText.setSelection(pOtp.length());
        }
    }

    public boolean isCaptchaProcessing() {
        return mCaptchaWebEditText.getVisibility() == View.VISIBLE;
    }

    public boolean isOtpWebProcessing() {
        return mOtpWebEditText.getVisibility() == View.VISIBLE;
    }

    public boolean isOtpAuthenPayerProcessing() {
        return mOtpAuthenEditText.getVisibility() == View.VISIBLE;
    }

    public String getCaptcha() {
        return mCaptchaWebEditText.getString();
    }

    public String getUsername() {
        return mAccountNameEditText.getString();
    }

    public String getPassword() {
        return mAccountPasswordEditText.getString();
    }

    public void setCaptchaImage(String pB64Encoded, String pUrl) {
        if (pB64Encoded.length() > 10) {
            mCaptchaImage.setVisibility(View.VISIBLE);
            mCaptchaFrame.setVisibility(View.GONE);
            setCaptchaImage(pB64Encoded);
            mScrollViewRoot.fullScroll(View.FOCUS_DOWN);
        } else {
            mCaptchaImage.setVisibility(View.GONE);
            mCaptchaFrame.setVisibility(View.VISIBLE);
            setCaptchaUrl(pUrl);
        }
    }

    private void moveToAuthenOptionView() throws Exception {
        SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getAdapter().getActivity());
        getAdapter().getView().visiableOrderInfo(false);
        getAdapter().getView().setVisible(R.id.linearlayout_input_local_card, false);
        getAdapter().getView().visibleCardViewNavigateButton(false);
        getAdapter().getView().visibleSubmitButton(true);
        getAdapter().getView().enableSubmitBtn();
        getAdapter().getView().changeBgSubmitButton(getAdapter().isFinalStep());

        visualOtpToken(true);
    }

    public void setCaptchaImage(String pB64Encoded) {
        if (TextUtils.isEmpty(pB64Encoded))
            return;
        Bitmap bitmap = BitmapUtils.b64ToImage(pB64Encoded);
        if (bitmap != null) {
            mCaptchaImage.setImageBitmap(bitmap);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setCaptchaUrl(String pUrl) {
        if (TextUtils.isEmpty(pUrl))
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head></head><body style='margin:0;padding:0'><img src='").append(pUrl)
                .append("' style='margin:0;padding:0;' width='120px' alt='' /></body>");
        mCaptchaWebview.setOnTouchListener((v, event) -> true);

        WebSettings webSettings = mCaptchaWebview.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        mCaptchaWebview.setBackgroundColor(Color.TRANSPARENT);
        webSettings.setLoadWithOverviewMode(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            mCaptchaWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mCaptchaWebview.loadDataWithBaseURL(((AdapterBankCard) getAdapter()).getWebViewProcessor().getCurrentUrl(), sb.toString(),
                "text/html", null, null);

    }

    @Override
    protected boolean checkValidRequiredEditText(EditText pView) {
        if (pView.getVisibility() != View.VISIBLE) {
            return true;
        }

        boolean isCheckPattern = true;

        if (pView instanceof VPaymentDrawableEditText || pView instanceof VPaymentValidDateEditText)
            isCheckPattern = ((VPaymentEditText) pView).isValid();

        return isCheckPattern && (pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText().toString()));
    }

    /***
     * enable payment button if user input data all ok.
     *
     * @return
     */
    @Override
    public boolean checkEnableSubmitButton() {

        boolean isCoverBankOtp = checkValidRequiredEditText(mOtpWebEditText);
        boolean isCoverBankCaptcha = checkValidRequiredEditText(mCaptchaWebEditText);
        boolean isAccountName = checkValidRequiredEditText(mAccountNameEditText);
        boolean isAccountPassword = checkValidRequiredEditText(mAccountPasswordEditText);
        boolean isOnlinePassword = checkValidRequiredEditText(mOnlinePasswordEditText);

        boolean isOtp = true;
        boolean isToken = true;

        if (mOtpTokenLayoutRootView.getVisibility() == View.VISIBLE) {
            isOtp = checkValidRequiredEditText(mOtpAuthenEditText);
            isToken = checkValidRequiredEditText(mTokenAuthenEditText);
        }
        try {
            if (isOtp && isToken && isCoverBankOtp && isCoverBankCaptcha && isAccountName && isAccountPassword && isOnlinePassword) {
                try {
                    getAdapter().getView().enableSubmitBtn();
                    getAdapter().getView().changeBgSubmitButton(getAdapter().isFinalStep());
                } catch (Exception e) {
                    Log.e(this, e);
                }
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

    @Override
    protected boolean canSwitchChannelLinkCard() {
        try {
            return getAdapter().getPresenter().hasCC;
        } catch (Exception e) {
            Log.e(this, e);
            return false;
        }
    }

    @Override
    protected boolean isOwnChannel() {
        return getAdapter().isATMFlow();
    }

    @Override
    protected void switchChannel() {
        Log.d(this, "===switchChannel===");
        try {
            getAdapter().getPresenter().switchChannel(BuildConfig.channel_credit_card, getCardNumber());
        } catch (Exception e) {
            Log.e(this, e);
        }
    }
}
