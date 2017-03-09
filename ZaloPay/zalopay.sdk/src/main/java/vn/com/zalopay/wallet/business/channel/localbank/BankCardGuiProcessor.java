package vn.com.zalopay.wallet.business.channel.localbank;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.enumeration.EAuthenType;
import vn.com.zalopay.wallet.listener.ZPWOnDetectCardListener;
import vn.com.zalopay.wallet.utils.BitmapUtil;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.adapter.LocalCardFragmentAdapter;
import vn.com.zalopay.wallet.view.adapter.VietComBankAccountListViewAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;

public class BankCardGuiProcessor extends CardGuiProcessor {
    private RadioGroup mInputRadioGroupAuthenType;

    private EAuthenType mAuthenType = EAuthenType.SMS;

    private View mOtpTokenLayoutRootView;
    private View mOtpTockenLayoutView;

    private VPaymentDrawableEditText mOtpAuthenEditText, mTokenAuthenEditText;

    private TextInputLayout mTextLayoutOtp, mTextLayoutToken;

    private AppCompatRadioButton mRadioButtonSms, mRadioButtonToken;

    private View mRadioGroupAuthenSelectionView;

    private RadioGroup mAuthenRadioGroup;

    private ImageView mCaptchaImage;
    private View mCaptchaFrame;
    private WebView mCaptchaWebview;
    private VPaymentDrawableEditText mAccountNameEditText, mAccountPasswordEditText, mOtpWebEditText, mCaptchaWebEditText, mOnlinePasswordEditText;

    //vietcombank account list
    private ListView mAccountListView;
    private VietComBankAccountListViewAdapter mAccountAdapter;

    public BankCardGuiProcessor(AdapterBankCard pAdapterLocalCard) throws Exception {
        super();

        mAdapter = new WeakReference<AdapterBase>(pAdapterLocalCard);

        init();
    }

    @Override
    protected void init() throws Exception {
        super.init();

        if (GlobalData.isLinkCardChannel()) {
            getAdapter().getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_credit_card_link));
        } else {
            getAdapter().getActivity().setBarTitle(GlobalData.getStringResource(RS.string.zpw_string_atm_method_name));
        }


        mOtpTokenLayoutRootView = getAdapter().getActivity().findViewById(R.id.zpw_content_input_view_root);
        mOtpTokenLayoutRootView.setVisibility(View.GONE);

        mRadioGroupAuthenSelectionView = getAdapter().getActivity().findViewById(R.id.linearlayout_selection_authen);

        mRadioButtonSms = (AppCompatRadioButton) getAdapter().getActivity().findViewById(R.id.radioSelectionSmS);
        mRadioButtonToken = (AppCompatRadioButton) getAdapter().getActivity().findViewById(R.id.radioSelectionToken);

        mRadioButtonSms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mOtpAuthenEditText.setVisibility(View.VISIBLE);
                    mTextLayoutOtp.setVisibility(View.VISIBLE);
                    mTokenAuthenEditText.setVisibility(View.GONE);
                    mTextLayoutToken.setVisibility(View.GONE);

                    mAuthenType = EAuthenType.SMS;
                }
            }
        });
        mRadioButtonToken.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mOtpAuthenEditText.setVisibility(View.GONE);
                    mTextLayoutOtp.setVisibility(View.GONE);
                    mTokenAuthenEditText.setVisibility(View.VISIBLE);
                    mTextLayoutToken.setVisibility(View.VISIBLE);

                    mAuthenType = EAuthenType.TOKEN;
                }
            }
        });


        mOtpAuthenEditText = (VPaymentDrawableEditText) getAdapter().getActivity().findViewById(R.id.edittext_otp);
        mTokenAuthenEditText = (VPaymentDrawableEditText) getAdapter().getActivity().findViewById(R.id.edittext_token);

        mTextLayoutOtp = (TextInputLayout) getAdapter().getActivity().findViewById(R.id.textLayoutOtp);
        mTextLayoutToken = (TextInputLayout) getAdapter().getActivity().findViewById(R.id.textLayoutToken);

        mOtpAuthenEditText.setGroupText(false);
        mTokenAuthenEditText.setGroupText(false);

        mOtpTockenLayoutView = getAdapter().getActivity().findViewById(R.id.linearlayout_authenticate_local_card);

        mInputRadioGroupAuthenType = (RadioGroup) getAdapter().getActivity().findViewById(R.id.radioGroupAuthenType);
        mAuthenRadioGroup = (RadioGroup) getAdapter().getActivity().findViewById(R.id.radioGroupAuthenSmsToken);

        mOtpWebEditText = (VPaymentDrawableEditText) getAdapter().getActivity().findViewById(R.id.zpsdk_otp_ctl);
        mCaptchaWebEditText = (VPaymentDrawableEditText) getAdapter().getActivity().findViewById(R.id.zpsdk_captchar_ctl);
        mCaptchaImage = (ImageView) getAdapter().getActivity().findViewById(R.id.zpsdk_captchar_img_ctl);
        mCaptchaFrame = getAdapter().getActivity().findViewById(R.id.zpsdk_captchar_wv_frame);
        mCaptchaWebview = (WebView) getAdapter().getActivity().findViewById(R.id.zpsdk_captchar_wv_ctl);

        mAccountNameEditText = (VPaymentDrawableEditText) getAdapter().getActivity().findViewById(R.id.zpsdk_acc_name_ctl);
        mAccountPasswordEditText = (VPaymentDrawableEditText) getAdapter().getActivity().findViewById(R.id.zpsdk_acc_password_ctl);

        mOnlinePasswordEditText = (VPaymentDrawableEditText) getAdapter().getActivity().findViewById(R.id.zpsdk_card_password_ctl);

        mAccountListView = (ListView) getAdapter().getActivity().findViewById(R.id.zpw_account_listview);

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
    }

    @Override
    public void setCardDateOnCardView() {
        if (getCardView() != null)
            getCardView().setHintTextIssue();
    }

    @Override
    protected void setWebViewUserAgent() {
        if (mWebView != null) {
            mWebView.setUserAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
            ;
        }
    }

    @Override
    public void continueDetectCardForLinkCard() {
        Log.d(this, "card number=" + getCardNumber() + "===preparing to detect cc");

        getCreditCardFinder().detectOnOtherThread(getCardNumber(), new ZPWOnDetectCardListener() {
            @Override
            public void onDetectCardComplete(final boolean isDetected) {
                getAdapter().setNeedToSwitchChannel(isDetected);

                populateTextOnCardView();

                if (isDetected) {
                    setDetectedCard(getCreditCardFinder().getDetectedBankName(), getCreditCardFinder().getDetectBankCode());

                    checkAutoMoveCardNumberFromBundle = false;

                    getCardView().visibleCardDate();

                    Log.d(this, "card number=" + getCardNumber() + " detected=" + isDetected + " cc=" + getBankCardFinder().getDetectedBankName());
                    if (isInputBankMaintenance()) {
                        return;
                    }
                } else {
                    setDetectedCard();
                }
            }
        });
    }

    @Override
    public CardCheck getCardFinder() {
        return getBankCardFinder();
    }

    @Override
    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        return new LocalCardFragmentAdapter(getAdapter().getActivity().getSupportFragmentManager(), getAdapter().getActivity().getIntent().getExtras());
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

            moveToAuthenOptionView();
        } else {
            getAdapter().getActivity().setView(R.id.zpsdk_app_info, false);

            getAdapter().getActivity().setView(R.id.linearlayout_input_local_card, false);
            getAdapter().getActivity().visibleCardViewNavigateButton(false);

            getAdapter().getActivity().visibleSubmitButton(true);
            getAdapter().getActivity().enableSubmitBtn(true);

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

    public void showAccountList(ArrayList<String> pAccountList) {
        if (mAccountListView != null) {

            mAccountAdapter = new VietComBankAccountListViewAdapter(getAdapter().getActivity(), R.layout.item__account__listview, pAccountList);

            mAccountListView.setAdapter(mAccountAdapter);

            mAccountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mAccountAdapter.setSelectedIndex(position);
                }
            });

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAccountListView.getLayoutParams();
            params.height = (int) (pAccountList.size() * getAdapter().getActivity().getResources().getDimension(R.dimen.zpw_account_list_item_height));
            mAccountListView.requestLayout();


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAccountAdapter.setSelectedIndex(0);
                }
            }, 500);


            getAdapter().getActivity().enableSubmitBtn(true);
        }
    }

    public int getSelectedAccountIndex() {
        int index = 0;
        if (mAccountAdapter != null)
            index = mAccountAdapter.getSelectedIndex();

        return index;
    }

    public EAuthenType getAuthenType() {
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
                mAuthenType = EAuthenType.TOKEN;
        }
        if (mOtpTokenLayoutRootView.getVisibility() == View.VISIBLE) {
            if (mAuthenRadioGroup.getCheckedRadioButtonId() == R.id.radioSelectionToken)
                mAuthenType = EAuthenType.TOKEN;
        }
    }

    public void visualOtpToken(boolean pVisible) {
        getAdapter().getActivity().setView(R.id.linearlayout_authenticate_local_card, pVisible);
    }

    public void goBackInputCard() {
        if (getAdapter().getActivity() != null) {
            getAdapter().getActivity().setView(R.id.linearlayout_input_local_card, true);
            getAdapter().getActivity().visibleCardViewNavigateButton(true);

            getAdapter().getActivity().visibleSubmitButton(false);
            getAdapter().getActivity().enableSubmitBtn(false);

            visualOtpToken(false);

            getAdapter().setCanEditCardInfo(false);

            try {
                ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public void showOtpTokenView() {
        getAdapter().getActivity().visibleInputCardView(false);
        getAdapter().getActivity().visiblePinView(false);
        getAdapter().getActivity().visibleConfirmView(false);
        getAdapter().getActivity().visibleHeaderInfo();

        getAdapter().getActivity().setText(R.id.zpsdk_btn_submit, GlobalData.getStringResource(RS.string.zpw_button_submit_text));

        mOtpTokenLayoutRootView.setVisibility(View.VISIBLE);

        if (mAuthenType == EAuthenType.SMS) {
            mRadioGroupAuthenSelectionView.setVisibility(View.GONE);
            mTokenAuthenEditText.setVisibility(View.GONE);
            mTextLayoutToken.setVisibility(View.GONE);

            mOtpAuthenEditText.setVisibility(View.VISIBLE);
            mTextLayoutOtp.setVisibility(View.VISIBLE);

            showKeyBoardOnEditTextAndScroll(mOtpAuthenEditText);

        } else if (mAuthenType == EAuthenType.TOKEN) {

            mRadioGroupAuthenSelectionView.setVisibility(View.GONE);
            mOtpAuthenEditText.setVisibility(View.GONE);
            mTextLayoutOtp.setVisibility(View.GONE);

            mTokenAuthenEditText.setVisibility(View.VISIBLE);
            mTextLayoutToken.setVisibility(View.VISIBLE);

            showKeyBoardOnEditTextAndScroll(mTokenAuthenEditText);
        }

        checkEnableSubmitButton();
    }

    public boolean isBankOtpPhase() {
        return (mOtpWebEditText != null && mOtpWebEditText.getVisibility() == View.VISIBLE) ||
                (mOtpAuthenEditText != null && mOtpAuthenEditText.getVisibility() == View.VISIBLE);
    }

    public boolean isCoverBankInProcess() {
        View view = getAdapter().getActivity().findViewById(R.id.zpw_content_input_root_view_cover_bank);
        if (view != null && view.getVisibility() == View.VISIBLE)
            return true;

        return false;
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

    public String getOnlinePassword()
    {
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

    private void moveToAuthenOptionView() {
        getAdapter().getActivity().setView(R.id.zpsdk_app_info, false);

        ZPWUtils.hideSoftKeyboard(GlobalData.getAppContext(), getAdapter().getActivity());
        getAdapter().getActivity().setView(R.id.linearlayout_input_local_card, false);
        getAdapter().getActivity().visibleCardViewNavigateButton(false);

        getAdapter().getActivity().visibleSubmitButton(true);
        getAdapter().getActivity().enableSubmitBtn(true);

        visualOtpToken(true);
    }

    public void setCaptchaImage(String pB64Encoded) {
        if (TextUtils.isEmpty(pB64Encoded))
            return;

        Bitmap bitmap = BitmapUtil.b64ToImage(pB64Encoded);

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
        mCaptchaWebview.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

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
        boolean isOnlinePassword    = checkValidRequiredEditText(mOnlinePasswordEditText);

        boolean isOtp = true;
        boolean isToken = true;

        if (mOtpTokenLayoutRootView.getVisibility() == View.VISIBLE) {
            isOtp = checkValidRequiredEditText(mOtpAuthenEditText);
            isToken = checkValidRequiredEditText(mTokenAuthenEditText);
        }

        if (isOtp && isToken && isCoverBankOtp && isCoverBankCaptcha && isAccountName && isAccountPassword && isOnlinePassword) {
            getAdapter().getActivity().enableSubmitBtn(true);
            return true;
        } else {
            getAdapter().getActivity().enableSubmitBtn(false);
            return false;
        }
    }

    @Override
    protected boolean canSwitchChannelLinkCard() {
        return getAdapter().getActivity().isAllowLinkCardCC();
    }

    @Override
    protected boolean isOwnChannel() {
        return getAdapter().isATMFlow();
    }

    @Override
    protected void switchChannel() {

        Log.d(this, "===switchChannel===");

        getAdapter().getActivity().switchChannel(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_credit_card), getCardNumber());
    }
}
