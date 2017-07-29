package vn.com.zalopay.wallet.workflow.ui;

import android.annotation.TargetApi;
import android.content.Context;
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

import rx.Subscription;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.BitmapUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.workflow.BankCardWorkFlow;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.card.AbstractCardDetector;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.constants.AuthenType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.ui.channel.ChannelFragment;
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
    private View mRadioGroupAuthenSelectionView;
    private RadioGroup mAuthenRadioGroup;
    private ImageView mCaptchaImage;
    private View mCaptchaFrame;
    private WebView mCaptchaWebview;
    private VPaymentDrawableEditText mAccountNameEditText, mAccountPasswordEditText,
            mOtpWebEditText, mCaptchaWebEditText, mOnlinePasswordEditText;

    public BankCardGuiProcessor(Context pContext, BankCardWorkFlow pAdapterLocalCard, ChannelFragment pChannelFragment) {
        super(pContext);
        init(pAdapterLocalCard, pChannelFragment);
    }

    @Override
    protected void init(AbstractWorkFlow pAdapter, ChannelFragment pChannelFragment) {
        super.init(pAdapter, pChannelFragment);
        try {
            getView().findViewById(R.id.bidv_register_btn).setOnClickListener(view -> {
                try {
                    SdkUtils.openWebPage(getActivity(),GlobalData.getStringResource(RS.string.sdk_bidv_bankaccount_register_url));
                } catch (Exception e) {
                    Timber.w(e);
                }
            });
            mOtpTokenLayoutRootView = getView().findViewById(R.id.zpw_content_input_view_root);
            mOtpTokenLayoutRootView.setVisibility(View.GONE);
            mRadioGroupAuthenSelectionView = getView().findViewById(R.id.linearlayout_selection_authen);
            AppCompatRadioButton mRadioButtonSms = (AppCompatRadioButton) getView().findViewById(R.id.radioSelectionSmS);
            AppCompatRadioButton mRadioButtonToken = (AppCompatRadioButton) getView().findViewById(R.id.radioSelectionToken);
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


            mOtpAuthenEditText = (VPaymentDrawableEditText) getView().findViewById(R.id.edittext_otp);
            mTokenAuthenEditText = (VPaymentDrawableEditText) getView().findViewById(R.id.edittext_token);

            mTextLayoutOtp = (TextInputLayout) getView().findViewById(R.id.textLayoutOtp);
            mTextLayoutToken = (TextInputLayout) getView().findViewById(R.id.textLayoutToken);

            mOtpAuthenEditText.setGroupText(false);
            mTokenAuthenEditText.setGroupText(false);

            mOtpTockenLayoutView = getView().findViewById(R.id.linearlayout_authenticate_local_card);

            mInputRadioGroupAuthenType = (RadioGroup) getView().findViewById(R.id.radioGroupAuthenType);
            mAuthenRadioGroup = (RadioGroup) getView().findViewById(R.id.radioGroupAuthenSmsToken);

            mOtpWebEditText = (VPaymentDrawableEditText) getView().findViewById(R.id.zpsdk_otp_ctl);
            mCaptchaWebEditText = (VPaymentDrawableEditText) getView().findViewById(R.id.zpsdk_captchar_ctl);
            mCaptchaImage = (ImageView) getView().findViewById(R.id.zpsdk_captchar_img_ctl);
            mCaptchaFrame = getView().findViewById(R.id.zpsdk_captchar_wv_frame);
            mCaptchaWebview = (WebView) getView().findViewById(R.id.zpsdk_captchar_wv_ctl);

            mAccountNameEditText = (VPaymentDrawableEditText) getView().findViewById(R.id.zpsdk_acc_name_ctl);
            mAccountPasswordEditText = (VPaymentDrawableEditText) getView().findViewById(R.id.zpsdk_acc_password_ctl);

            mOnlinePasswordEditText = (VPaymentDrawableEditText) getView().findViewById(R.id.zpsdk_card_password_ctl);

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
    }

    @Override
    public void setCardDateOnCardView() {
        // Set hint card issue here
    }

    public void continueDetectCardForLinkCard() {
        Subscription subscription = getCreditCardFinder().detectOnAsync(getCardNumber())
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(detected -> {
                    try {
                        getAdapter().setNeedToSwitchChannel(detected);
                        populateTextOnCardView();
                        if (detected) {
                            setDetectedCard(getCreditCardFinder().getBankName(), getCreditCardFinder().getDetectBankCode());
                            checkAutoMoveCardNumberFromBundle = false;
                            getCardView().visibleCardDate();
                            isInputBankMaintenance();
                        } else {
                            setDetectedCard();
                        }
                    } catch (Exception e) {
                        Timber.w(e.getMessage());
                    }
                }, Timber::d);
        try {
            getAdapter().getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    @Override
    public AbstractCardDetector getCardFinder() {
        return getBankCardFinder();
    }

    @Override
    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        try {
            return new LocalCardFragmentAdapter(getActivity().getSupportFragmentManager(),getActivity().getIntent().getExtras());
        } catch (Exception e) {
            Timber.w(e);
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
        return PaymentPermission.allowLuhnATM();
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

        if (!getCardFinder().detected()) {
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
        try {
            if (isUseOtpToken()) {
                getAdapter().setCanEditCardInfo(true);
                moveToAuthenOptionView();
            } else {
                getView().setVisible(R.id.linearlayout_input_local_card, false);
                getView().visibleCardViewNavigateButton(false);
                getView().visibleSubmitButton(true);
                getView().enablePaymentButton();
                getView().visibleOrderInfo(false);
                getAdapter().onClickSubmission();
            }
        } catch (Exception e) {
            Timber.w(e, "Exception action after finish input card info");
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
        try {
            if (!getCardFinder().detected()) {
                return;
            }
            if(!(getCardFinder() instanceof BankDetector)){
                return;
            }
            BankConfig bankConfig = ((BankDetector) getCardFinder()).getFoundBankConfig();
            if(bankConfig == null){
                return;
            }
            getAdapter().getCard().setBankcode(bankConfig.code);
        } catch (Exception e) {
            Timber.w(e, "Exception populate bank code");
        }
    }

    @Override
    public void populateCard() {
        try {
            super.populateCard();
        } catch (Exception e) {
            Timber.w(e, "Exception populate card info");
        }
        if (mOtpTockenLayoutView.getVisibility() == View.VISIBLE) {
            if (mInputRadioGroupAuthenType.getCheckedRadioButtonId() == R.id.radioButtonToken)
                mAuthenType = AuthenType.TOKEN;
        }
        if (mOtpTokenLayoutRootView.getVisibility() == View.VISIBLE) {
            if (mAuthenRadioGroup.getCheckedRadioButtonId() == R.id.radioSelectionToken)
                mAuthenType = AuthenType.TOKEN;
        }
    }

    private void visualOtpToken(boolean pVisible) {
        try {
            getView().setVisible(R.id.linearlayout_authenticate_local_card, pVisible);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    public void goBackInputCard() {
        try {
            getView().setVisible(R.id.linearlayout_input_local_card, true);
            getView().visibleCardViewNavigateButton(true);
            getView().visibleSubmitButton(false);
            getView().disablePaymentButton();
            visualOtpToken(false);
            getAdapter().setCanEditCardInfo(false);
            SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());
        } catch (Exception e) {
            Timber.w(e, "Exception go back input");
        }
    }

    public void showOtpTokenView() {
        try {
            getView().visibleInputCardView(false);
            getView().setText(R.id.zpsdk_btn_submit,
                    mContext.getResources().getString(R.string.sdk_button_submit_text));
            if (getAdapter().getPaymentInfoHelper().payByCardMap()
                    || getAdapter().getPaymentInfoHelper().payByBankAccountMap()) {
                getView().visibleOrderInfo(true);
            }
        } catch (Exception e) {
            Timber.w(e.getMessage());
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
            GlobalData.analyticsTrackerWrapper
                    .step(ZPPaymentSteps.OrderStep_VerifyOtp)
                    .track();
        }
    }

    public boolean isBankOtpPhase() {
        return (mOtpWebEditText != null && mOtpWebEditText.getVisibility() == View.VISIBLE) ||
                (mOtpAuthenEditText != null && mOtpAuthenEditText.getVisibility() == View.VISIBLE);
    }

    public boolean isCoverBankInProcess() {
        View view = null;
        try {
            view = getView().findViewById(R.id.zpw_content_input_root_view_cover_bank);
        } catch (Exception e) {
            Timber.w(e);
        }
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean needToWarningNotSupportCard() {
        return needToWarningNotSupportCard && (getCardNumber().length() >= Constants.MIN_ATM_LENGTH);
    }

    public String getAuthenValue() {
        if (mOtpAuthenEditText.getVisibility() == View.VISIBLE) {
            return mOtpAuthenEditText.getString();
        } else if (mTokenAuthenEditText.getVisibility() == View.VISIBLE) {
            return mTokenAuthenEditText.getString();
        }

        return "";
    }

    public String getOnlinePassword() {
        return mOnlinePasswordEditText.getString();
    }

    private String getIssueDate() {
        return mIssueDate;
    }

    public String getCardMonth() {
        if (!TextUtils.isEmpty(mIssueDate))
            return mIssueDate.split("/")[0];

        return null;
    }

    public String getCardYear() {
        if (!TextUtils.isEmpty(mIssueDate)) {
            return mIssueDate.split("/")[1];
        }
        return null;
    }

    public String getCardPass() {
        return null;
    }

    public String getOtp() {
        if (mOtpWebEditText == null) {
            return null;
        }
        return mOtpWebEditText.getString();
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
        SdkUtils.hideSoftKeyboard(mContext, getAdapter().getActivity());
        getView().visibleOrderInfo(false);
        getView().setVisible(R.id.linearlayout_input_local_card, false);
        getView().visibleCardViewNavigateButton(false);
        getView().visibleSubmitButton(true);
        getView().enablePaymentButton();
        getView().changeBgPaymentButton(getAdapter().isFinalStep());
        visualOtpToken(true);
    }

    private void setCaptchaImage(String pB64Encoded) {
        if (TextUtils.isEmpty(pB64Encoded))
            return;
        Bitmap bitmap = BitmapUtils.b64ToImage(pB64Encoded);
        if (bitmap != null) {
            mCaptchaImage.setImageBitmap(bitmap);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setCaptchaUrl(String pUrl) {
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
        try {
            mCaptchaWebview.loadDataWithBaseURL(((BankCardWorkFlow) getAdapter()).getWebViewProcessor().getCurrentUrl(), sb.toString(),
                    "text/html", null, null);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }

    }

    @Override
    protected boolean checkValidRequiredEditText(EditText pView) {
        if (pView.getVisibility() != View.VISIBLE) {
            return true;
        }
        boolean isCheckPattern = true;
        if (pView instanceof VPaymentDrawableEditText || pView instanceof VPaymentValidDateEditText) {
            isCheckPattern = ((VPaymentEditText) pView).isValid();
        }
        return isCheckPattern && (pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText().toString()));
    }

    /*
     * enable payment button if user input data all ok.
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
                getView().enablePaymentButton();
                getView().changeBgPaymentButton(getAdapter().isFinalStep());
                return true;
            } else {
                getView().disablePaymentButton();
                return false;
            }
        } catch (Exception e) {
            Timber.w(e, "Exception check enable submit ");
        }
        return false;
    }

    @Override
    protected boolean canSwitchChannelLinkCard() {
        try {
            return PaymentPermission.allowLinkCC();
        } catch (Exception e) {
            Log.e(this, e);
            return false;
        }
    }

    @Override
    protected boolean isOwnChannel() {
        try {
            return getAdapter().isATMFlow();
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        return false;
    }

    @Override
    protected void switchChannel() {
        try {
            Timber.d("start switch to cc adapter");
            getAdapter().getPresenter().switchWorkFlow(BuildConfig.channel_credit_card, getCardNumber());
        } catch (Exception e) {
            Timber.w(e, "Exception switch cc adapter");
        }
    }
}
