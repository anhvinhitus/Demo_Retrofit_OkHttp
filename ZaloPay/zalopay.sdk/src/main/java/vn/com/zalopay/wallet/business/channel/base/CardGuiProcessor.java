package vn.com.zalopay.wallet.business.channel.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnCloseDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.lang.ref.WeakReference;

import rx.functions.Action1;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.PaymentUtils;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebView;
import vn.com.zalopay.wallet.constants.BankFlow;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Link_Then_Pay;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.dialog.BankListPopup;
import vn.com.zalopay.wallet.dialog.MapBankPopup;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.adapter.CardSupportAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.cardview.CreditCardView;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CreditCardFragment;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;

import static vn.com.zalopay.wallet.constants.Constants.MAP_POPUP_REQUEST_CODE;
import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;

/***
 * card processor class
 */
public abstract class CardGuiProcessor extends SingletonBase implements ViewPager.OnPageChangeListener {
    public final String VERTICAL_SEPERATOR = " ";
    protected WeakReference<AdapterBase> mAdapter;
    protected CardSupportAdapter cardSupportGridViewAdapter;
    protected ScrollView mScrollViewRoot;
    protected View mLayoutSwitch;
    protected int mLengthBeforeChange;
    protected View mCurrentFocusView;
    protected PaymentWebView mWebView;
    protected boolean mUseOtpToken = false;
    protected int mLastPageSelected = 0;
    protected boolean checkAutoMoveCardNumberFromBundle = true;
    protected CreditCardView mCardView;
    protected String mCardNumber;
    protected String mCardHolderName;
    protected String mIssueDate;
    protected String mExpiry;
    protected String mCVV;
    protected CardFragmentBaseAdapter mCardAdapter;
    protected int mMaxPagerCount;
    protected ViewPager mViewPager;
    protected Button mButtonNext, mButtonPre;
    protected LinearLayout mDotView;
    protected boolean needToWarningNotSupportCard = true;
    protected PaymentInfoHelper mPaymentInfoHelper;
    protected View.OnClickListener mNextButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getViewPager() == null || getViewPager().getAdapter() == null) {
                return;
            }
            mMaxPagerCount = getViewPager().getAdapter().getCount();
            // if last card
            if (getViewPager().getCurrentItem() == mMaxPagerCount - 1) {
                onDoneTapped();
            } else {
                showNext();
            }
        }
    };
    protected View.OnClickListener mPreviousButtonClick = view -> {
        if (getViewPager() == null) {
            return;
        }
        showPrevious();
    };
    /***
     * user tap on done on keyboard
     */
    protected TextView.OnEditorActionListener mEditorActionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (getAdapter().isInputStep()) {
                onDoneTapped();
                return true;
            } else if (checkEnableSubmitButton()) {
                getAdapter().onClickSubmission();
            }
        } else if ((actionId == EditorInfo.IME_ACTION_NEXT) && getAdapter().isInputStep()) {
            showNext();
            return true;
        }
        return false;
    };
    protected View.OnClickListener mClickOnEditTextListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                moveScrollViewToCurrentFocusView();
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };
    protected View.OnTouchListener mOnTouchOnCardView = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int viewID = view.getId();
            if (viewID == R.id.front_card_number) {
                getViewPager().setCurrentItem(0);
            } else if (viewID == R.id.front_card_holder_name) {
                getViewPager().setCurrentItem(mCardAdapter.getCount() - 1);
            } else if (viewID == R.id.front_card_expiry) {
                getViewPager().setCurrentItem(1);
            }
            return false;
        }
    };
    protected TextWatcher mCardDetectionTextWatcher = new TextWatcher() {
        private String lastValue = "";
        private int mLastLengthCardNumber = 0;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //get current focus view and length of input
            getLengthInputBeforeTextChange();
        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                //prevent user input if wrong card
                String newValue = s.toString().trim();
                if (!supportCard() && mLastLengthCardNumber < newValue.length()) {
                    getCardNumberView().setText(lastValue);
                    getCardNumberView().setSelection(mLastLengthCardNumber);
                    return;
                }
                lastValue = newValue;
                mLastLengthCardNumber = lastValue.length();
                mCardNumber = getCardNumberView().getString();
                populateTextOnCardViewNoPaintCard();
                getAdapter().detectCard(mCardNumber);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };
    protected String lastValue = "";
    protected String mLastCharacterCardName = "";
    protected boolean isInputValidWithWhiteSpace = true;
    protected TextWatcher mEnabledTextWatcher = new TextWatcher() {
        private boolean isValidateOK = false;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                if (getAdapter().getActivity().getCurrentFocus() != null
                        && getAdapter().getActivity().getCurrentFocus().getId() == R.id.edittext_localcard_name) {
                    if (!validCardNameWithWhiteSpace(s.toString())) {
                        try {
                            getCardNameView().setText(lastValue);
                            mCardAdapter.getCardNameFragment().setError(GlobalData.getStringResource(RS.string.zpw_alert_cardname_has_whitespace));
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(this, e);
            }

            if (isInputValidWithWhiteSpace && isNeedValidateOnTextChange(getCurrentFocusView())) {
                try {
                    isValidateOK = validateInputOnTextChange();
                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //get current focus view and length of input
            getLengthInputBeforeTextChange();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!isInputValidWithWhiteSpace) {
                Timber.d("!isInputValidWithWhiteSpace");
                return;
            }
            updateCardInfoAfterTextChange(s.toString());
            //whether user input full of information,then need to enable button submit.
            checkEnableSubmitButton();
            if (isValidateOK) {
                autoMoveToNextFragment();
            }
        }
    };
    protected View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(final View view, boolean hasFocus) {
            if (hasFocus) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            moveScrollViewToCurrentFocusView();
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 100);
            }
        }
    };
    protected View.OnFocusChangeListener mOnOtpCaptchFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(final View view, boolean hasFocus) {
            if (hasFocus) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            moveScrollViewToCurrentFocusView();
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 100);
            }
        }
    };
    protected View.OnTouchListener mOnTouchListener = (view, motionEvent) -> {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            moveScrollViewToCurrentFocusView();
        }
        return false;
    };
    protected ZPWOnCloseDialogListener mCloseDialogListener = this::clearCardNumberAndShowKeyBoard;
    protected View.OnClickListener mOnQuestionIconClick = view -> {
        if (!supportCard()) {
            try {
                showSupportCardList();
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };
    private ZPWOnEventDialogListener mBankMaintenanceDialogListener = () -> new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            //focus to edittext again after closing dialog
            try {
                getViewPager().setCurrentItem(0);
                getCardNumberView().setText(null);
                SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }, 400);
    protected Action1<Boolean> mDetectCardSubscriber = new Action1<Boolean>() {
        @Override
        public void call(Boolean detected) {
            Timber.d("card number " + getCardNumber() + " detected " + detected);
            if (mPaymentInfoHelper.payByCardMap() || mPaymentInfoHelper.payByBankAccountMap()) {
                return;
            }
            if (TextUtils.isEmpty(getCardNumber())) {
                getAdapter().setNeedToSwitchChannel(false);
            }
            if (!getAdapter().isNeedToSwitchChannel()) {
                //workout prevent flicker when switch atm and cc
                if (!detected && mPaymentInfoHelper.isLinkTrans()) {
                    needToWarningNotSupportCard = false;
                }
                setDetectedCard();
                populateTextOnCardView();
                //render view by bank type
                if (detected && getAdapter().isATMFlow()) {
                    showViewByBankType();
                }
            }
            //continue detect if haven't detected card type yet
            if (!detected && mPaymentInfoHelper.isLinkTrans()) {
                needToWarningNotSupportCard = true;
                continueDetectCardForLinkCard();
            }
        }
    };

    public abstract boolean checkEnableSubmitButton();

    protected abstract void populateBankCode();

    protected abstract boolean isATMChannel();

    protected abstract boolean canSwitchChannelLinkCard();

    protected abstract boolean isOwnChannel();

    protected abstract void switchChannel();

    protected abstract boolean checkValidRequiredEditText(EditText pEditText);

    protected abstract boolean needToWarningNotSupportCard();

    public abstract CardCheck getCardFinder();

    public abstract boolean isAllowValidateCardNumberByLuhn();

    public abstract void continueDetectCardForLinkCard();

    public abstract void setCardDateOnCardView();

    public abstract VPaymentValidDateEditText getCardDateView() throws Exception;

    protected abstract void actionAfterFinishInputCard();

    protected abstract int validateInputCard();

    protected abstract boolean validateCardNumberLength();

    protected abstract CardFragmentBaseAdapter onCreateCardFragmentAdapter();

    protected void init(PaymentInfoHelper paymentInfoHelper) {
        mPaymentInfoHelper = paymentInfoHelper;
        if (GlobalData.isChannelHasInputCard(mPaymentInfoHelper)) {
            try {
                initForInputCard();
                if (GlobalData.analyticsTrackerWrapper != null) {
                    GlobalData.analyticsTrackerWrapper
                            .step(ZPPaymentSteps.OrderStep_InputCardInfo)
                            .track();
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
        } else {
            initForMapCardAndZaloPay();
        }
        getAdapter().setECardFlowType(BankFlow.API);
        getCardFinder();
        setCardDateOnCardView();
        try {
            initMutualView();
        } catch (Exception e) {
            Log.e(this, e);
        }
        setMinHeightSwitchCardButton();
    }

    protected void initForInputCard() throws Exception {
        mCardView = (CreditCardView) getAdapter().getView().findViewById(R.id.credit_card_view);
        if (getCardView() != null) {
            getCardView().initCardSelector();
            getCardView().setOnClickOnCardView(this);
        }
        mDotView = (LinearLayout) getAdapter().getView().findViewById(R.id.dotView);
        mButtonPre = (Button) getAdapter().getView().findViewById(R.id.previous);
        mButtonNext = (Button) getAdapter().getView().findViewById(R.id.next);
        getAdapter().getView().visibleCardViewNavigateButton(true);
        getAdapter().getView().visibleSubmitButton(false);
        getAdapter().getView().visiableOrderInfo(false);
        getAdapter().getView().visibleCardInfo(true);
        if (mButtonNext != null && mButtonPre != null) {
            mButtonNext.setOnClickListener(mNextButtonClick);
            mButtonPre.setOnClickListener(mPreviousButtonClick);
        }
    }

    protected void initForMapCardAndZaloPay() {

    }

    protected void initMutualView() throws Exception {
        mLayoutSwitch = getAdapter().getView().findViewById(R.id.zpw_switch_card_button);
        mScrollViewRoot = (ScrollView) getAdapter().getView().findViewById(R.id.zpw_scrollview_container);
        if (mScrollViewRoot != null)
            OverScrollDecoratorHelper.setUpOverScroll(mScrollViewRoot);
    }

    protected void initWebView() throws Exception {
        mWebView = (PaymentWebView) getAdapter().getView().findViewById(R.id.zpw_threesecurity_webview);
        if (mWebView != null) {
            mWebView.setPaymentWebViewClient(getAdapter());
        }
    }

    protected void flipCardView(int pPosition) {
    }

    public void checkForSwitchChannel() {
        if (getAdapter().needToSwitchChannel() && isOwnChannel()) {
            getAdapter().resetNeedToSwitchChannel();
            if (canSwitchChannelLinkCard()) {
                switchChannel();
            } else {
                getViewPager().setCurrentItem(0);
                try {
                    getAdapter().getView().showDialogWarningLinkCardAndResetCardNumber();
                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
        }
    }

    public void populateCard() {
        DPaymentCard card = getAdapter().getCard();
        card.setCardholdername(mCardHolderName);
        card.setCardnumber(mCardNumber);
        card.setCvv(mCVV);
        card.setCardvalidfrom(null);
        card.setCardvalidto(null);
        if (!TextUtils.isEmpty(mIssueDate)) {
            card.setCardvalidfrom(mIssueDate.replace("/", ""));
        }

        if (!TextUtils.isEmpty(mExpiry)) {
            card.setCardvalidto(mExpiry.replace("/", ""));
        }
        populateBankCode();
    }

    protected CreditCardView getCardView() {
        return mCardView;
    }

    protected ViewPager getViewPager() {
        return mViewPager;
    }

    protected AdapterBase getAdapter() {
        if (mAdapter != null) {
            return mAdapter.get();
        } else {
            Log.e(this, "===mAdapter=null===");
            return null;
        }
    }

    protected View getCurrentFocusView() {
        if (mCurrentFocusView == null) {
            try {
                mCurrentFocusView = getAdapter().getActivity().getCurrentFocus();
            } catch (Exception ex) {
                Log.e(this, ex);
            }
        }
        return mCurrentFocusView;
    }

    public void initPager() throws Exception {
        Timber.d("init viewpager");
        if (getViewPager() == null) {
            mViewPager = (ViewPager) getAdapter().getView().findViewById(R.id.card_field_container_pager);
        }
        getViewPager().addOnPageChangeListener(this);
        mCardAdapter = onCreateCardFragmentAdapter();
        getViewPager().setOffscreenPageLimit(mCardAdapter.getCount());
        getViewPager().setAdapter(mCardAdapter);
        updateDots();
        //auto show keyboard when the first time start
        showKeyBoardAndResizeButtonsIfNotSwitchChannel();

    }

    protected boolean isValidCardNumber() {
        boolean validateLuhn = validateCardNumberLuhn();
        try {
            boolean isValidCardNumber = ((TextUtils.isEmpty(getCardNumber()) || !validateLuhn || !validateCardNumberLength() || mCardAdapter.getCardNumberFragment().hasError()));
            return !isValidCardNumber;
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    public String warningCardExist() {
        String message = getCardFinder().warningCardExistMessage();
        if (getBankCardFinder().isDetected()) {
            message = getBankCardFinder().warningCardExistMessage();
        } else if (getCreditCardFinder().isDetected()) {
            message = getCreditCardFinder().warningCardExistMessage();
        }
        return message;
    }

    protected boolean validateCardNumber() {
        try {
            if (!isValidCardNumber()) {
                try {
                    //come back last page
                    getViewPager().setCurrentItem(mLastPageSelected);
                    String errMes = getCardNumberView().getPatternErrorMessage();
                    if (TextUtils.isEmpty(getCardNumber())) {
                        errMes = GlobalData.getStringResource(RS.string.zpw_missing_card_number);
                    } else if (!validateCardNumberLength()) {
                        errMes = getCardNumberView().getPatternErrorMessage();
                    } else if (preventNextIfLinkCardExisted()) {
                        errMes = warningCardExist();
                    } else if (!validateCardNumberLuhn()) {
                        errMes = GlobalData.getStringResource(RS.string.zpw_string_card_error_luhn);
                    }
                    showHintError(getCardNumberView(), errMes);
                    //disable next button
                    disableNext();
                } catch (Exception e) {
                    Log.e(this, e);
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return true;
    }

    protected void updateLastPagePosition(int pPosition) {
        mLastPageSelected = pPosition;
    }

    /***
     * set cursor to the last position of edittext
     *
     * @param pPosition
     */
    protected void moveCursorToLastPositionOnText(int pPosition) {
        try {
            CreditCardFragment currentFragment = mCardAdapter.getItemAtPosition(pPosition);

            if (currentFragment != null) {
                currentFragment.onSelectText();
            }

        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected void clearHighLightOnCardView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    clearHighLight();
                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
        }, 100);
    }

    /***
     * prevent navigate if previous fragment have an error
     *
     * @param pPosition
     * @return
     */
    protected boolean preventNavigateIfHasError(int pPosition) {
        boolean hasError = false;

        if (pPosition >= 0 && pPosition < mCardAdapter.getCount()) {
            //user swipe to next
            if (mLastPageSelected < pPosition && !mCardAdapter.canNavigateToNext(pPosition)) {
                getViewPager().setCurrentItem(mLastPageSelected);
                hasError = true;
            }
            //user swipe to previous
            else if (mLastPageSelected > pPosition && !mCardAdapter.canNavigateToPrevious(pPosition)) {
                getViewPager().setCurrentItem(mLastPageSelected);
                hasError = true;
            }
        }

        return hasError;
    }

    protected boolean isValidCardCVV() {
        try {
            return TextUtils.isEmpty(getCardCVVView().getText().toString()) || getCardCVVView().isValidPattern() && (getCardCVVView().getText().toString().length() == 3);

        } catch (Exception e) {
            Timber.d(e != null ? e.getMessage() : "Exception");
        }

        return true;
    }

    protected boolean validateCardCVV() {
        try {
            boolean isValidCardCVV = isValidCardCVV();

            if (!isValidCardCVV) {
                getViewPager().setCurrentItem(mLastPageSelected);

                String errMes = getCardCVVView().getPatternErrorMessage();
                showHintError(getCardCVVView(), errMes);
            }

            return isValidCardCVV;

        } catch (Exception e) {
            Timber.d(e != null ? e.getMessage() : "Exception");
        }

        return true;
    }

    protected boolean isValidCardDate() {
        try {
            return TextUtils.isEmpty(getCardDateView().getText().toString()) || getCardDateView().isValidPattern() && (getCardDateView().getText().toString().length() == 5);

        } catch (Exception e) {
            Timber.d(e != null ? e.getMessage() : "Exception");
        }

        return true;
    }

    protected boolean validateCardDate() {
        try {
            boolean isValidCardDate = isValidCardDate();

            if (!isValidCardDate) {
                getViewPager().setCurrentItem(mLastPageSelected);

                String errMes = getCardDateView().getPatternErrorMessage();
                showHintError(getCardDateView(), errMes);
            }

            return isValidCardDate;

        } catch (Exception e) {
            Timber.d(e != null ? e.getMessage() : "Exception");
        }

        return true;
    }

    /***
     * check formula Luhn card number
     *
     * @return
     */
    protected boolean validateCardNumberLuhn() {
        boolean isDetected = getCardFinder().isDetected();

        if (mPaymentInfoHelper.isLinkTrans() && !isDetected) {
            isDetected = getCreditCardFinder().isDetected() ? getCreditCardFinder().isDetected() : getBankCardFinder().isDetected();
        }

        return !(isAllowValidateCardNumberByLuhn() && isDetected) || getCardFinder().isValidCardLuhn(getCardNumber());

    }

    /***
     * show keyboard and focus on current view
     */
    public void onFocusView() {
        try {
            mCurrentFocusView = getAdapter().getActivity().getCurrentFocus();
            if (mCurrentFocusView != null && mCurrentFocusView instanceof EditText) {
                SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), (EditText) mCurrentFocusView);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void useWebView(boolean pIsUseWebView) {
        if (pIsUseWebView) {
            try {
                getAdapter().getView().visibleWebView(true);
                getAdapter().getView().visibleInputCardView(false);
                getAdapter().getView().visibleSubmitButton(false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                getAdapter().getView().visibleWebView(false);
                getAdapter().getView().visibleInputCardView(true);
                getAdapter().getView().visibleSubmitButton(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showKeyBoardAndResizeButtonsIfNotSwitchChannel() throws Exception {
        if (!getAdapter().getPresenter().isSwitchAdapter()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        //dialog process is showing. so when process dialog close, need to show keyboard again.
                        if (DialogManager.isShowingProgressDialog()) {
                            return;
                        }
                        showKeyBoardOnCardNumberView();
                        moveScrollViewToCurrentFocusView();
                        getAdapter().getPresenter().setSwitchAdapter(false);
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                }
            }, 300);
        }
    }

    public void clearCardNumberAndShowKeyBoard() {
        try {
            mCardAdapter.getCardNumberFragment().clearText();
            getCardView().setCardNumber("");

        } catch (Exception e) {
            Log.e(this, e);
        }

        getViewPager().setCurrentItem(0);
        //auto show keyboard when the first time start
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    showKeyBoardAndResizeButtons();
                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
        }, 500);
    }

    public void showKeyBoardAndResizeButtons() {
        showKeyBoardOnCardNumberView();
        moveScrollViewToCurrentFocusView();

        Timber.d("showKeyBoardAndResizeButtons");
    }

    public void setDetectedCard() {
        try {
            if (isInputBankMaintenance()) {
                return;
            }
            String bankName = getDetectedBankName();
            String bankCode = getDetectedBankCode();
            setCardNumberHint(bankName);
            MiniPmcTransType miniPmcTransType = null;
            if (TextUtils.isEmpty(bankCode)) {
                getAdapter().setMiniPmcTransType(null);
            } else {
                miniPmcTransType = getAdapter().getConfig(bankCode);
            }
            //check quota amount transaction for each bank
            if (miniPmcTransType != null && !mPaymentInfoHelper.isLinkTrans() && (getAdapter() instanceof AdapterBankCard)) {
                miniPmcTransType.calculateFee(mPaymentInfoHelper.getAmount());
                mPaymentInfoHelper.getOrder().plusChannelFee(miniPmcTransType.totalfee);
                miniPmcTransType.checkPmcOrderAmount(mPaymentInfoHelper.getAmount());//check amount is support or not
                if (!miniPmcTransType.isAllowPmcQuota()) {
                    CardNumberFragment cardNumberView = mCardAdapter.getCardNumberFragment();
                    String invalidAmountMessage = GlobalData.getStringResource(RS.string.invalid_order_amount_bank);
                    double amount_total = mPaymentInfoHelper.getAmountTotal();
                    cardNumberView.setError(String.format(invalidAmountMessage, getBankCardFinder().getShortBankName(), StringUtil.formatVnCurrence(String.valueOf(amount_total))));
                    disableNext();
                    return;
                }
            }
            //check bank future feature
            if (getCardFinder().isDetected() && miniPmcTransType != null && !miniPmcTransType.isVersionSupport(SdkUtils.getAppVersion(GlobalData.getAppContext()))) {
                showWarningBankVersionSupport();
                return;
            }
            //check disable pmc
            if (getCardFinder().isDetected() && miniPmcTransType != null && miniPmcTransType.isDisable()) {
                showWarningDisablePmc(bankName);
                return;
            }

            //bidv card must paid by mapcard
            if (!mPaymentInfoHelper.isLinkTrans() && (getAdapter() instanceof AdapterBankCard)
                    && ((AdapterBankCard) getAdapter()).paymentBIDV()
                    && ((AdapterBankCard) getAdapter()).preventPaymentBidvCard(bankCode, getCardNumber())) {
                return;
            }

            //user input bank account
            if (!TextUtils.isEmpty(bankCode) && BankAccountHelper.isBankAccount(bankCode) && getAdapter() != null && getAdapter().getActivity() != null) {
                showWarningBankAccount();
            }

            //move to next page if detect a card
            if (getCardFinder().isDetected()) {
                autoMoveToNextFragment();
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected void populateTextOnCardView() {
        if (getCardView() != null) {
            getCardView().setCardNumber(getCardNumber());
        }
    }

    protected void populateTextOnCardViewNoPaintCard() {
        if (getCardView() != null) {
            getCardView().setCardNumberNoPaintCard(getCardNumber());
        }
    }

    protected void showWarningBankVersionSupport() throws Exception {
        BankConfig bankConfig = getCardFinder().getDetectBankConfig();
        if (bankConfig == null) {
            Timber.d("bank config is null");
            return;
        }
        String pMessage = mPaymentInfoHelper.isLinkTrans() ? GlobalData.getStringResource(RS.string.sdk_warning_version_support_linkchannel) : GlobalData.getStringResource(RS.string.sdk_warning_version_support_payment);
        pMessage = String.format(pMessage, bankConfig.getShortBankName());
        getAdapter().getView().showConfirmDialog(pMessage,
                GlobalData.getStringResource(RS.string.dialog_upgrade_button),
                GlobalData.getStringResource(RS.string.dialog_retry_input_card_button),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        clearCardNumberAndShowKeyBoard();
                    }

                    @Override
                    public void onOKEvent() {
                        PlayStoreUtils.openPlayStoreForUpdate(GlobalData.getMerchantActivity(), BuildConfig.PACKAGE_IN_PLAY_STORE, "Zalo Pay", "force-app-update", "bank-future");
                        try {
                            getAdapter().getPresenter().callback();
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                });
    }

    protected void showWarningBankAccount() {
        if (mPaymentInfoHelper.isLinkTrans()) {
            try {
                getAdapter().getView().showConfirmDialog(GlobalData.getStringResource(RS.string.zpw_warning_vietcombank_linkbankaccount_not_linkcard),
                        GlobalData.getStringResource(RS.string.dialog_linkaccount_button),
                        GlobalData.getStringResource(RS.string.dialog_retry_input_card_button),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {
                                clearCardNumberAndShowKeyBoard();
                            }

                            @Override
                            public void onOKEvent() {
                                //callback bankcode to app , app will direct user to link bank account to right that bank
                                BankAccount dBankAccount = new BankAccount();
                                dBankAccount.bankcode = BankCardCheck.getInstance().getDetectBankCode();
                                mPaymentInfoHelper.setMapBank(dBankAccount);
                                try {
                                    getAdapter().getPresenter().setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINK_ACCOUNT);
                                } catch (Exception e) {
                                    Log.e(this, e);
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(this, e);
            }
        } else if (!BankAccountHelper.hasBankAccountOnCache(mPaymentInfoHelper.getUserId(), CardType.PVCB)) {
            try {
                getAdapter().getView().showConfirmDialog(GlobalData.getStringResource(RS.string.zpw_warning_vietcombank_linkcard_before_payment),
                        GlobalData.getStringResource(RS.string.dialog_linkaccount_button),
                        GlobalData.getStringResource(RS.string.dialog_retry_input_card_button),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {
                                clearCardNumberAndShowKeyBoard();
                            }

                            @Override
                            public void onOKEvent() {
                              /*  //callback bankcode to app , app will direct user to link bank account to right that bank
                                BankAccount dBankAccount = new BankAccount();
                                dBankAccount.bankcode = BankCardCheck.getInstance().getDetectBankCode();
                                mPaymentInfoHelper.setMapBank(dBankAccount);*/
                                try {
                                    getAdapter().getPresenter().callbackLinkThenPay(Link_Then_Pay.VCB);
                                    //getAdapter().getPresenter().setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINK_ACCOUNT_AND_PAYMENT);
                                } catch (Exception e) {
                                    Log.e(this, e);
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(this, e);
            }

        } else if (getAdapter() != null) {
            try {
                Activity activity = getAdapter().getActivity();
                Intent intent = MapBankPopup.createVCBIntent(activity,
                        activity.getString(R.string.dialog_retry_input_card_button),
                        mPaymentInfoHelper.getAmountTotal());
                getAdapter().getView().startActivityForResult(intent, MAP_POPUP_REQUEST_CODE);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    /***
     * link card channel use this to update found card
     * @param pBankName
     */
    public void setDetectedCard(String pBankName, String pBankCode) {
        try {
            setCardNumberHint(pBankName);
            MiniPmcTransType miniPmcTransType = null;
            if (TextUtils.isEmpty(pBankCode)) {
                getAdapter().setMiniPmcTransType(null);
            } else {
                miniPmcTransType = getAdapter().getConfig(pBankCode);
            }

            if (getCardFinder().isDetected() && miniPmcTransType != null && !miniPmcTransType.isVersionSupport(SdkUtils.getAppVersion(GlobalData.getAppContext()))) {
                showWarningBankVersionSupport();
                return;
            }
            //check disable pmc
            if (getCardFinder().isDetected() && miniPmcTransType != null && miniPmcTransType.isDisable()) {
                showWarningDisablePmc(pBankName);
                return;
            }
            //user input bank account
            if (!TextUtils.isEmpty(pBankCode) && BankAccountHelper.isBankAccount(pBankCode) && getAdapter() != null && getAdapter().getActivity() != null) {
                showWarningBankAccount();
            }
        } catch (Exception e) {
            Log.e(this, e);
        }

        if (getAdapter().isCCFlow() && getBankCardFinder().isDetected()) {
            autoMoveToNextFragment();
        } else if (getAdapter().isATMFlow() && getCreditCardFinder().isDetected()) {
            autoMoveToNextFragment();
        }
    }

    protected boolean isUseOtpToken() {
        return mUseOtpToken;
    }

    public String getDetectedBankName() {
        return getCardFinder().getBankName();
    }

    public String getDetectedBankCode() {
        return getCardFinder().getDetectBankCode();
    }

    public BankCardCheck getBankCardFinder() {
        return BankCardCheck.getInstance();
    }

    public CreditCardCheck getCreditCardFinder() {
        return CreditCardCheck.getInstance();
    }

    public void resizeCardView(int decreaseSize) throws Exception {
        if (getCardView() != null && decreaseSize > 0) {
            float percentWitdh = getCardView().getPercentWitdh();
            if (percentWitdh < 0) {
                percentWitdh = Float.parseFloat(GlobalData.getStringResource(RS.string.percent_ondefault));
                if (SdkUtils.isTablet(GlobalData.getAppContext()))
                    percentWitdh = Float.parseFloat(GlobalData.getStringResource(RS.string.percent_ontablet));

            }
            int offset = (int) GlobalData.getAppContext().getResources().getDimension(R.dimen.min_button_offset);
            decreaseSize += offset;
            float percentDecrease = (float) decreaseSize / getCardView().getWidth();
            getCardView().resize(percentWitdh - percentDecrease);
            getCardView().requestLayout();
            View buttonWrapper = getAdapter().getView().findViewById(R.id.zpw_switch_card_button);
            if (buttonWrapper != null) {
                //resize buttons
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) buttonWrapper.getLayoutParams();
                params.bottomMargin = decreaseSize / 2;
                buttonWrapper.setLayoutParams(params);
                buttonWrapper.requestLayout();
            }

        }
    }

    private void setMinHeightSwitchCardButton() {
        if (mLayoutSwitch != null) {
            int heightSwitchButton = (int) GlobalData.getAppContext().getResources().getDimension(R.dimen.switch_card_layout_min_height);
            android.view.ViewGroup.LayoutParams params = mLayoutSwitch.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = heightSwitchButton;
            mLayoutSwitch.setLayoutParams(params);
            mLayoutSwitch.requestLayout();
        }

    }

    /***
     * show/hide issue date
     * after detected a card type
     */
    public void showViewByBankType() {
        if (getCardFinder().isDetected()) {
            BankConfig bank = getCardFinder().getDetectBankConfig();
            //parser otp:token here otp:SMS|token:Token
            mUseOtpToken = !TextUtils.isEmpty(bank.otptype) &&
                    bank.otptype.contains(GlobalData.getStringResource(RS.string.sms_option))
                    && bank.otptype.contains(GlobalData.getStringResource(RS.string.token_option));
            switch (bank.banktype) {
                case 1:
                    //password
                    break;
                case 2:
                    //issue day
                    mCardAdapter.addIssueDateFragment();
                    mCardAdapter.notifyDataSetChanged();
                    updateDots();
                    break;
                case 4:
                    //expire day
                    mCardAdapter.addExpireDateFragment();
                    mCardAdapter.notifyDataSetChanged();
                    updateDots();
                    break;
                default:
                    mCardAdapter.removeFragment(CardIssueFragment.class.getName());
                    mCardAdapter.removeFragment(CardExpiryFragment.class.getName());
                    mCardAdapter.notifyDataSetChanged();
                    updateDots();
                    break;
            }
        }
    }

    public void updateDots() {
        if (mDotView == null) {
            Timber.d("===mDotView=null===");
            return;
        }
        if (mDotView.getChildCount() == mCardAdapter.getCount()) {
            Timber.d("===updateDots===no update dot again");
            return;
        }
        mDotView.removeAllViews();
        for (int i = 0; i < mCardAdapter.getCount(); i++) {
            addDot();
        }
        selectDot(mLastPageSelected);
    }

    public void addDot() {
        ImageView dot = new ImageView(GlobalData.getAppContext());
        dot.setImageResource(R.drawable.dot);
        int dotSize = (int) GlobalData.getAppContext().getResources().getDimension(R.dimen.dot_size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
        params.setMargins(dotSize, 0, dotSize, 0);
        mDotView.addView(dot, params);
    }

    public void selectDot(int pIndex) {
        if (mDotView == null) {
            return;
        }
        for (int i = 0; i < mDotView.getChildCount(); i++) {
            View view = mDotView.getChildAt(i);
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                if (i == pIndex) {
                    imageView.setImageResource(R.drawable.dot_color);
                } else {
                    //reset previous
                    imageView.setImageResource(R.drawable.dot);
                }
            }
        }
    }

    public void resetCardNumberAndShowKeyBoard() {
        if (getViewPager() != null) {
            try {
                getCardNumberView().setText(null);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), mCardAdapter.getItemAtPosition(0).getEditText());
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 300);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public void setCardInfo(String pCardNumber) {
        if (getViewPager() != null && !TextUtils.isEmpty(pCardNumber)) {
            try {
                SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), mCardAdapter.getItemAtPosition(1).getEditText());
                applyFont(getAdapter().getView().findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium));
                getCardNumberView().setText(pCardNumber);
                getCardNumberView().formatText(true);
                //reset other
                getCardView().setCVV(null);
                getCardView().setCardHolderName(null);
                getCardView().setCardDate(null);
                new Handler().postDelayed(() -> getViewPager().setCurrentItem(1), 300);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public void dispose() {
        mCardAdapter = null;
        cardSupportGridViewAdapter = null;
        if (mWebView != null) {
            mWebView.getCCWebViewClient().dispose();
            mWebView.release();
        }
    }

    private void onDoneTapped() {
        int errorFragmentIndex = validateInputCard();
        //there're no error
        if (errorFragmentIndex == -1) {
            clearHighLight();

            //just start submit if intener is online
            if (getAdapter().openSettingNetworking()) {
                actionAfterFinishInputCard();
            }
        } else {
            //user in last view (card name)
            if (errorFragmentIndex == (mMaxPagerCount - 1)) {
                try {
                    showHintError(getCardNameView(), GlobalData.getStringResource(RS.string.zpw_alert_cardname_wrong));

                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
            getViewPager().setCurrentItem(errorFragmentIndex);
        }
    }

    public void bidvAutoFillOtp(String pOtp) {
        if (mWebView != null) {
            mWebView.getCCWebViewClient().BIDVWebFlowFillOtp(pOtp);
        }
    }

    public void reloadUrl() {
        if (mWebView == null) {
            try {
                initWebView();
            } catch (Exception e) {
                Log.e(this, e);
            }
            return;
        }
        mWebView.reloadPaymentUrl();
    }

    public void loadUrl(String pUrl) throws Exception {
        if (mWebView == null) {
            initWebView();
        }
        useWebView(true);
        mWebView.loadPaymentUrl(pUrl);
    }

    public boolean preventNextIfLinkCardExisted() {
        try {
            return getAdapter().existPaymentCardOnCache();
        } catch (Exception e) {
            Log.e(this, e);
            return false;
        }
    }

    public String getCardName() {
        return mCardHolderName;
    }

    public String getCardNumber() {
        return mCardNumber;
    }

    public String getCardCVV() {
        return mCVV;
    }

    public String getCardExpire() {
        return mExpiry;
    }

    public VPaymentDrawableEditText getCardNumberView() throws Exception {
        if (mCardAdapter == null) {
            return null;
        }
        return (VPaymentDrawableEditText) mCardAdapter.getCardNumberFragment().getEditText();
    }

    public VPaymentDrawableEditText getCardNameView() throws Exception {
        return (VPaymentDrawableEditText) mCardAdapter.getCardNameFragment().getEditText();
    }

    public VPaymentDrawableEditText getCardCVVView() throws Exception {
        return (VPaymentDrawableEditText) mCardAdapter.getCardCVVFragment().getEditText();
    }

    public VPaymentValidDateEditText getCardIssueView() throws Exception {
        return (VPaymentValidDateEditText) mCardAdapter.getCardIssueryFragment().getEditText();
    }

    public VPaymentValidDateEditText getCardExpiryView() throws Exception {
        return (VPaymentValidDateEditText) mCardAdapter.getCardExpiryFragment().getEditText();
    }

    public void showNext() {
        int max = mCardAdapter.getCount();
        int currentIndex = getViewPager().getCurrentItem();
        //prevent user move to next if input existed card in link card
        if (currentIndex == 0 && preventNextIfLinkCardExisted() && mPaymentInfoHelper.isLinkTrans()) {
            try {
                showHintError(getCardNumberView(), warningCardExist());
                return;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        //validate card number before move to next page
        if (currentIndex == 0 && !validateCardNumberLuhn()) {
            try {
                showHintError(getCardNumberView(), GlobalData.getStringResource(RS.string.zpw_string_card_error_luhn));
                Timber.d("validdate Luhn fail");
                return;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (currentIndex + 1 < max) {
            getViewPager().setCurrentItem(currentIndex + 1);
        } else {
            // completed the card entry.
            try {
                SdkUtils.hideSoftKeyboard(GlobalData.getAppContext(), getAdapter().getActivity());
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        refreshNavigateButton();
    }

    public void enableNext() {
        if (!mButtonNext.isEnabled()) {
            mButtonNext.setEnabled(true);
            mButtonNext.setBackgroundResource(R.drawable.button_next);
        }
    }

    public void disableNext() {
        if (mButtonNext.isEnabled()) {
            mButtonNext.setEnabled(false);
            mButtonNext.setBackgroundResource(R.drawable.button_next_disable);
        }
    }

    public void enablePrevious() {
        if (!mButtonPre.isEnabled()) {
            mButtonPre.setEnabled(true);
            mButtonPre.setBackgroundResource(R.drawable.button_pre);
        }
    }

    public void disablePrevious() {
        if (mButtonPre.isEnabled()) {
            mButtonPre.setEnabled(false);
            mButtonPre.setBackgroundResource(R.drawable.button_pre_disable);
        }
    }

    public void refreshNavigateButton() {
        if (getViewPager() == null)
            return;
        int currentIndex = getViewPager().getCurrentItem();
        int max = getViewPager().getAdapter().getCount();
        if (currentIndex == 0) {
            disablePrevious();
        } else if ((max - currentIndex == 1) && mCardAdapter.hasError() > -1) {
            disableNext();
        } else {
            enablePrevious();
            enableNext();
        }
    }

    public void showPrevious() {
        int currentIndex = getViewPager().getCurrentItem();
        if (currentIndex - 1 >= 0) {
            getViewPager().setCurrentItem(currentIndex - 1);
        }
        refreshNavigateButton();
    }

    protected boolean isInputBankMaintenance() {
        boolean isBankDetect = BankCardCheck.getInstance().isDetected();
        boolean isCCDetect = CreditCardCheck.getInstance().isDetected();
        if (isBankDetect && GlobalData.shouldUpdateBankFuncbyPayType()) {
            GlobalData.setCurrentBankFunction(BankFunctionCode.PAY_BY_CARD);
            if (BankCardCheck.getInstance().isBankAccount()) {
                GlobalData.setCurrentBankFunction(BankFunctionCode.PAY_BY_BANK_ACCOUNT);
            }
        }
        int bankFunction = GlobalData.getCurrentBankFunction();
        String bankCode = null;
        if (isBankDetect) {
            bankCode = getBankCardFinder().getDetectBankCode();
        } else if (isCCDetect) {
            bankCode = getCreditCardFinder().getDetectBankCode();
        }
        if (TextUtils.isEmpty(bankCode)) {
            return false;
        }
        BankConfig bankConfig = SDKApplication
                .getApplicationComponent()
                .bankListInteractor()
                .getBankConfig(bankCode);
        if (bankConfig == null) {
            return false;
        }
        String mess = bankConfig.getMaintenanceMessage(bankFunction);
        if (bankConfig.isBankMaintenence(bankFunction)) {
            showMaintenanceBank(mess);
            return true;
        }
        return false;
    }

    protected void showMaintenanceBank(String messaage) {
        try {
            getAdapter()
                    .getView()
                    .showInfoDialog(messaage,
                            GlobalData.getAppContext().getString(R.string.dialog_retry_input_card_button),
                            mBankMaintenanceDialogListener);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    protected void showWarningDisablePmc(String pBankName) {
        String mess = mPaymentInfoHelper.isLinkTrans() ? GlobalData.getStringResource(RS.string.sdk_warning_pmc_transtype_disable_link) : GlobalData.getStringResource(RS.string.sdk_warning_pmc_transtype_disable_payment);
        String disableBankMessage = String.format(mess, pBankName);
        try {
            getAdapter().getView().showInfoDialog(disableBankMessage,
                    GlobalData.getStringResource(RS.string.dialog_retry_input_card_button),
                    new ZPWOnEventDialogListener() {
                        @Override
                        public void onOKEvent() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //focus to edittext again after closing dialog
                                    try {
                                        getViewPager().setCurrentItem(0);
                                        getCardNumberView().setText(null);
                                        SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());
                                    } catch (Exception e) {
                                        Log.e(this, e);
                                    }
                                }
                            }, 400);
                        }
                    });
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected void showSupportCardList() throws Exception {
        if (cardSupportGridViewAdapter == null) {
            cardSupportGridViewAdapter = CardSupportAdapter.createAdapterProxy(isATMChannel(), mPaymentInfoHelper.getTranstype());
        }
        BankListPopup.setAdapter(cardSupportGridViewAdapter);
        BankListPopup.setCloseDialogListener(getCloseDialogListener());
        Intent intentBankList = new Intent(getAdapter().getActivity(), BankListPopup.class);
        getAdapter().getActivity().startActivity(intentBankList);
    }

    public View.OnClickListener getOnQuestionIconClick() {
        return mOnQuestionIconClick;
    }

    public TextWatcher getCardDetectionTextWatcher() {
        return mCardDetectionTextWatcher;
    }

    public TextWatcher getEnabledTextWatcher() {
        return mEnabledTextWatcher;
    }

    public View.OnFocusChangeListener getOnFocusChangeListener() {
        return mOnFocusChangeListener;
    }

    public View.OnFocusChangeListener getOnOtpCaptchaFocusChangeListener() {
        return mOnOtpCaptchFocusChangeListener;
    }

    public Action1<Boolean> getOnDetectCardSubscriber() {
        return mDetectCardSubscriber;
    }

    public ZPWOnCloseDialogListener getCloseDialogListener() {
        return mCloseDialogListener;
    }

    public TextView.OnEditorActionListener getEditorActionListener() {
        return mEditorActionListener;
    }

    public View.OnClickListener getClickOnEditTextListener() {
        return mClickOnEditTextListener;
    }

    public View.OnTouchListener getOnTouchOnCardView() {
        return mOnTouchOnCardView;
    }

    public void moveScrollViewToCurrentFocusView() {
        new Handler().postDelayed(() -> mScrollViewRoot.fullScroll(View.FOCUS_DOWN), 300);
    }

    /***
     * update error hint on TextIntputLayout
     *
     * @param pMessageHint
     * @throws Exception
     */
    protected void setCardNumberHint(String pMessageHint) throws Exception {
        if (!needToWarningNotSupportCard) {
            return;
        }

        CardNumberFragment cardNumberView = mCardAdapter.getCardNumberFragment();
        if (cardNumberView == null) {
            Timber.d("setCardNumberHint::cardNumberView is NULL");
            return;
        }

        //card not support
        if (TextUtils.isEmpty(pMessageHint) && needToWarningNotSupportCard()) {
            if (!supportCard()) {
                return;
            }
            cardNumberView.setError(GlobalData.getStringResource(RS.string.zpw_string_card_not_support));
            cardNumberView.showQuestionIcon();
            disableNext();
        }
        //clear error hint
        else if (TextUtils.isEmpty(pMessageHint)) {
            cardNumberView.clearError();
            enableNext();
        }
        //has an error
        else {
            cardNumberView.setHint(pMessageHint);
            enableNext();
        }
    }

    public void showHintError(EditText pEdittext, String pError) {
        try {
            getAdapter().getView().setTextInputLayoutHintError(pEdittext, pError, GlobalData.getAppContext());
        } catch (Exception e) {
            Log.e(this, e);
        }
        disablePrevious();
        disableNext();
        if (pEdittext.getId() == R.id.edittext_localcard_number) {
            try {
                CardNumberFragment cardNumberView = mCardAdapter.getCardNumberFragment();

                if (cardNumberView != null) {
                    cardNumberView.hideQuestionIcon();
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
        } else if (pEdittext.getId() == R.id.edittext_localcard_name) {
            enablePrevious();
        }
    }

    public void clearHintError(EditText pEdittext) {
        try {
            getAdapter().getView().setTextInputLayoutHint(pEdittext, null, GlobalData.getAppContext());
        } catch (Exception e) {
            Log.e(this, e);
        }
        enableNext();
        enablePrevious();
    }

    public void showKeyBoardOnCardNumberView() {
        try {
            SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void showKeyBoardOnEditTextAndScroll(final EditText pEditText) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {

                    SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), pEditText);
                    moveScrollViewToCurrentFocusView();

                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
        }, 300);
    }

    public void showKeyBoardOnEditText(final EditText pEditText) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    SdkUtils.focusAndSoftKeyboard(getAdapter().getActivity(), pEditText);
                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
        }, 300);
    }

    private boolean supportCard() {
        try {
            if (mCardAdapter.getCardNumberFragment().hasError() &&
                    (mCardAdapter.getCardNumberFragment().getError().equals(GlobalData.getStringResource(RS.string.zpw_string_card_not_support))
                            || mCardAdapter.getCardNumberFragment().getError().contains("không hỗ trợ"))) {
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return true;
    }

    protected void clearHighLight() {
        getCardView().clearHighLightCardNumber();
        getCardView().clearHighLightCardHolderName();
        getCardView().clearHighLightCardDate();
        getCardView().clearHighLightCVV();
    }

    protected void updateCardInfoAfterTextChange(String pInfo) {
        if (getCurrentFocusView() == null) {
            return;
        }
        if (getCurrentFocusView().getId() == R.id.edittext_localcard_name) {
            mCardHolderName = PaymentUtils.clearCardName(pInfo.trim());
            getCardView().setCardHolderName(mCardHolderName);
        } else if (getCurrentFocusView().getId() == R.id.edittext_issue_date) {
            mIssueDate = pInfo.trim();
            getCardView().setCardDate(mIssueDate);
        } else if (getCurrentFocusView().getId() == R.id.CreditCardExpiredDate) {
            mExpiry = pInfo.trim();
            getCardView().setCardDate(mExpiry);
        } else if (getCurrentFocusView().getId() == R.id.CreditCardCVV) {
            mCVV = pInfo.trim();
            getCardView().setCVV(mCVV);
        }
    }

    protected DCardIdentifier getSelectBankCardIdentifier() {
        DCardIdentifier cardIdentifier;
        if (mPaymentInfoHelper.isLinkTrans()) {
            cardIdentifier = getBankCardFinder().getCardIdentifier();
            if (cardIdentifier == null) {
                cardIdentifier = getCreditCardFinder().getCardIdentifier();
            }
        } else {
            cardIdentifier = getCardFinder().getCardIdentifier();
        }
        return cardIdentifier;
    }

    public boolean isCardLengthMatchIdentifier(String pCardNumber) {
        if (TextUtils.isEmpty(pCardNumber)) {
            return false;
        }
        DCardIdentifier cardIdentifier = getSelectBankCardIdentifier();
        if (cardIdentifier != null) {
            if (cardIdentifier.isMatchMaxLengthCard(pCardNumber.length())) {

                return true;
            }
        } else {
            //use default
            if (pCardNumber.length() == 16) {
                return true;
            }
        }
        return false;
    }

    /***
     * user finish input 1 field,auto swipe to next
     */
    protected void autoMoveToNextFragment() {
        //card number
        if (getCurrentFocusView() != null && getCurrentFocusView().getId() == R.id.edittext_localcard_number) {
            VPaymentDrawableEditText numberCard = (VPaymentDrawableEditText) getCurrentFocusView();
            if (!checkAutoMoveCardNumberFromBundle && numberCard.getString().length() == 16) {
                showNext();
                return;
            }
            if (isCardLengthMatchIdentifier(numberCard.getString())) {
                showNext();
            }
        }
        //card expiry
        else if (getCurrentFocusView() != null && getCurrentFocusView().getId() == R.id.CreditCardExpiredDate) {
            VPaymentValidDateEditText expiredDateCard = (VPaymentValidDateEditText) getCurrentFocusView();

            if (expiredDateCard.getText().toString().length() == expiredDateCard.getMaxLength()) {
                showNext();
            }
        }
        //card issue
        else if (getCurrentFocusView() != null && getCurrentFocusView().getId() == R.id.edittext_issue_date) {
            VPaymentValidDateEditText issueDateCard = (VPaymentValidDateEditText) getCurrentFocusView();

            if (issueDateCard.getText().toString().length() == issueDateCard.getMaxLength()) {
                showNext();
            }
        }
        //card cvv
        else if (getCurrentFocusView() != null && getCurrentFocusView().getId() == R.id.CreditCardCVV) {
            VPaymentDrawableEditText cvvCard = (VPaymentDrawableEditText) getCurrentFocusView();

            if (cvvCard.getString().length() == 3)
                showNext();
        }
    }

    protected void getLengthInputBeforeTextChange() {
        try {
            mCurrentFocusView = getAdapter().getActivity().getCurrentFocus();
            if (getCurrentFocusView() != null && getCurrentFocusView() instanceof VPaymentEditText) {
                mLengthBeforeChange = ((VPaymentEditText) getCurrentFocusView()).getLength();
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    protected boolean isNeedValidateOnTextChange(View view) {
        return !(view != null && (view.getId() == R.id.zpsdk_otp_ctl
                || view.getId() == R.id.zpsdk_captchar_ctl
                || view.getId() == R.id.edittext_otp
                || view.getId() == R.id.edittext_token));
    }

    protected boolean validateInputOnTextChange() throws Exception {
        if (getAdapter() == null || getAdapter().getActivity() == null) {
            Timber.d("getAdapter() == null || getAdapter().getActivity() == null");
            return true;
        }

        if (getAdapter().getActivity().getCurrentFocus() instanceof VPaymentEditText) {
            VPaymentEditText currentFocusView = (VPaymentEditText) getCurrentFocusView();
            //card name input
            if (currentFocusView.getId() == R.id.edittext_localcard_name && !currentFocusView.isValidInput()) {
                showHintError(currentFocusView, GlobalData.getStringResource(RS.string.zpw_alert_cardname_wrong));
                return false;
            }
            //empty or input valid
            else if (TextUtils.isEmpty(currentFocusView.getText().toString()) || (currentFocusView.isValidPattern())) {
                clearHintError(currentFocusView);
                return true;
            }
            //special case for issue day card.
            else if (currentFocusView.getId() == R.id.edittext_issue_date || currentFocusView.getId() == R.id.CreditCardExpiredDate) {
                try {
                    VPaymentValidDateEditText cardIssueDate = (VPaymentValidDateEditText) currentFocusView;
                    if (cardIssueDate.isInputMaxLength()) {
                        if (cardIssueDate.isValidPattern()) {
                            clearHintError(cardIssueDate);
                            return true;
                        } else {
                            showHintError(cardIssueDate, cardIssueDate.getPatternErrorMessage());
                            return false;
                        }
                    } else if (mLengthBeforeChange > currentFocusView.getLength()) {
                        showHintError(currentFocusView, currentFocusView.getPatternErrorMessage());
                        return false;
                    }
                    disableNext();
                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
            //user delete input
            else if (mLengthBeforeChange > currentFocusView.getLength()) {
                showHintError(currentFocusView, currentFocusView.getPatternErrorMessage());
                return false;
            }
        }
        return true;
    }

    protected boolean validCardNameWithWhiteSpace(String pCardName) {
        try {
            String newString = (pCardName.length() > 1) ? String.valueOf(pCardName.charAt(pCardName.length() - 1)) : "";
            //check input 2 space
            if (mLastCharacterCardName.equals(VERTICAL_SEPERATOR) && newString.equals(VERTICAL_SEPERATOR)) {
                mLastCharacterCardName = "";
                isInputValidWithWhiteSpace = false;
                return false;
            }
            // focus text when input text in cardNameView
            if (getCardNameView() != null && getCardNameView().isFocused()) {
                getCardNameView().setSelection(pCardName.length());
            }
            mLastCharacterCardName = newString;
            lastValue = pCardName;

        } catch (Exception e) {
            Log.e(this, e);
        }
        isInputValidWithWhiteSpace = true;
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(this, "onPageSelecte", position);
        if (mCardAdapter == null) {
            Timber.d("mCardAdapter is null");
            return;
        }
        if (preventNavigateIfHasError(position)) {
            return;
        }
        if (!validateCardNumber()) {
            return;
        }
        if (!validateCardDate()) {
            return;
        }
        if (!validateCardCVV()) {
            return;
        }
        //flip card side
        flipCardView(position);
        selectDot(position);
        updateLastPagePosition(position);
        refreshNavigateButton();
        clearHighLightOnCardView();
        moveCursorToLastPositionOnText(position);
        if (position == 1) {
            checkForSwitchChannel();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
