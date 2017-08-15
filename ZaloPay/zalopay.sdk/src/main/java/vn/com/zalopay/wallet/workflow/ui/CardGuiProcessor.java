package vn.com.zalopay.wallet.workflow.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ConfigLoader;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.CurrencyUtil;
import vn.com.zalopay.utility.PaymentUtils;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.card.AbstractCardDetector;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.constants.BankFlow;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.dialog.BankListDialogFragment;
import vn.com.zalopay.wallet.dialog.CardSupportHelper;
import vn.com.zalopay.wallet.entity.bank.BankAccount;
import vn.com.zalopay.wallet.entity.bank.BankConfig;
import vn.com.zalopay.wallet.entity.bank.PaymentCard;
import vn.com.zalopay.wallet.entity.config.CardRule;
import vn.com.zalopay.wallet.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.helper.BankHelper;
import vn.com.zalopay.wallet.helper.RenderHelper;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.ui.channel.ChannelFragment;
import vn.com.zalopay.wallet.view.adapter.BankSupportAdapter;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.cardview.CreditCardView;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CreditCardFragment;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.BankCardWorkFlow;
import vn.com.zalopay.wallet.workflow.webview.SdkWebView;

import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;

public abstract class CardGuiProcessor extends SingletonBase implements ViewPager.OnPageChangeListener {
    protected Context mContext;
    protected AbstractWorkFlow mAdapter;
    protected ChannelFragment mView;
    protected SdkWebView mWebView;
    protected CreditCardView mCardView;
    View mRootView;
    ScrollView mScrollViewRoot;
    int mLastPageSelected = 0;
    boolean checkValidCardNumberFromBundle = true;
    String mIssueDate;
    CardFragmentBaseAdapter mCardAdapter;
    boolean needToWarningNotSupportCard = true;
    View.OnFocusChangeListener mOnFocusChangeListener = (view, hasFocus) -> {
        if (!hasFocus) {
            return;
        }
        new Handler().postDelayed(() -> {
            try {
                moveScrollViewToCurrentFocusView();
            } catch (Exception e) {
                Timber.w(e);
            }
        }, 100);
    };
    View.OnTouchListener mOnTouchListener = (view, motionEvent) -> {
        if (motionEvent != null
                && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            moveScrollViewToCurrentFocusView();
        }
        return false;
    };
    String mCardNumber;
    int mMaxPagerCount;
    String lastValue = "";
    boolean isInputValidWithWhiteSpace = true;
    ViewPager mViewPager;
    private BankSupportAdapter mBankSupportAdapter;
    private View mLayoutSwitch;
    private int mLengthBeforeChange;
    private View mCurrentFocusView;
    private boolean mUseOtpToken = false;
    private String mCardHolderName;
    private String mExpiry;
    private String mCVV;
    private Button mButtonNext, mButtonPre;
    /*
     * user tap on done on keyboard
     */
    TextView.OnEditorActionListener mEditorActionListener = (v, actionId, event) -> {
        try {
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
        } catch (Exception e) {
            Timber.w(e);
        }
        return false;
    };
    private LinearLayout mDotView;
    private View.OnClickListener mNextButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                if (mViewPager == null || mViewPager.getAdapter() == null) {
                    return;
                }
                mMaxPagerCount = mViewPager.getAdapter().getCount();
                // if last card
                if (mViewPager.getCurrentItem() == mMaxPagerCount - 1) {
                    onDoneTapped();
                } else {
                    showNext();
                }
            } catch (Exception e) {
                Timber.d(e, "Exception on next button clicked");
            }
        }
    };
    private View.OnClickListener mPreviousButtonClick = view -> {
        if (mViewPager == null) {
            return;
        }
        showPrevious();
    };
    private View.OnClickListener mClickOnEditTextListener = view -> {
        try {
            moveScrollViewToCurrentFocusView();
        } catch (Exception e) {
            Timber.d(e);
        }
    };
    private View.OnTouchListener mOnTouchOnCardView = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mViewPager == null) {
                return true;
            }
            int viewID = view.getId();
            if (viewID == R.id.front_card_number) {
                mViewPager.setCurrentItem(0);
            } else if (viewID == R.id.front_card_holder_name) {
                mViewPager.setCurrentItem(mCardAdapter.getCount() - 1);
            } else if (viewID == R.id.front_card_expiry) {
                mViewPager.setCurrentItem(1);
            }
            return false;
        }
    };
    private TextWatcher mCardDetectionTextWatcher = new TextWatcher() {
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
                Timber.d("Input text %s", s);
                //prevent user input if wrong card
                String newValue = s.toString().trim();
                if (mLastLengthCardNumber == newValue.length()) {
                    return;
                }
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
                Timber.d(e);
            }
        }
    };
    private String mLastCharacterCardName = "";
    TextWatcher mEnabledTextWatcher = new TextWatcher() {
        private boolean isValidateOK = false;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                if (getActivity().getCurrentFocus() != null
                        && getActivity().getCurrentFocus().getId() == R.id.edittext_localcard_name) {
                    if (!validCardNameWithWhiteSpace(s.toString())) {
                        try {
                            getCardNameView().setText(lastValue);
                            mCardAdapter.getCardNameFragment().setError(mContext.getResources().getString(R.string.sdk_invalid_whitespace_cardname_mess));
                        } catch (Exception e) {
                            Timber.w(e);
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                Timber.w(e);
            }

            if (isInputValidWithWhiteSpace && isNeedValidateOnTextChange(getCurrentFocusView())) {
                try {
                    isValidateOK = validateInputOnTextChange();
                } catch (Exception e) {
                    Timber.d(e);
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
                try {
                    autoMoveToNextFragment();
                } catch (Exception e) {
                    Timber.d(e, "Exception auto next input");
                }
            }
        }
    };
    private View.OnFocusChangeListener mOnOtpCaptchFocusChangeListener = (view, hasFocus) -> {
        if (!hasFocus) {
            return;
        }
        new Handler().postDelayed(() -> {
            try {
                moveScrollViewToCurrentFocusView();
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }, 100);
    };
    private View.OnClickListener mOnQuestionIconClick = view -> {
        if (supportCard()) {
            return;
        }
        try {
            showSupportCardList();
        } catch (Exception e) {
            Timber.w(e);
        }
    };

    private ZPWOnEventDialogListener mBankMaintenanceDialogListener = () -> new Handler().postDelayed(() -> {
        //focus to edittext again after closing dialog
        try {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(0);
            }
            getCardNumberView().setText(null);
            SdkUtils.focusAndSoftKeyboard(getActivity(), getCardNumberView());
        } catch (Exception e) {
            Timber.w(e, "Exception show maintenance bank dialog");
        }
    }, 400);
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            try {
                Rect r = new Rect();
                mRootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = mRootView.getRootView().getHeight();
                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    getView().visibleBIDVAccountRegisterBtn(false);
                } else {
                    // keyboard is closed
                    getView().visibleBIDVAccountRegisterBtn(true);
                }
            } catch (Exception e) {
                Timber.w(e);
            }
        }
    };

    public CardGuiProcessor(Context context) {
        this.mContext = context;
    }

    public void onDetectCardComplete(Boolean detected) {
        Timber.d("card number %s is detected %s", getCardNumber(), detected);
        try {
            PaymentInfoHelper paymentInfoHelper = getAdapter().getPaymentInfoHelper();
            if (paymentInfoHelper == null) {
                return;
            }
            if (paymentInfoHelper.payByCardMap() || paymentInfoHelper.payByBankAccountMap()) {
                return;
            }
            if (TextUtils.isEmpty(getCardNumber())) {
                getAdapter().setNeedToSwitchChannel(false);
            }
            if (!getAdapter().isNeedToSwitchChannel()) {
                //workout prevent flicker when switch atm and cc
                if (!detected && paymentInfoHelper.isLinkTrans()) {
                    needToWarningNotSupportCard = false;
                }
                onDetectedBank();
                populateTextOnCardView();
                //render view by bank type
                if (detected && getAdapter().isATMFlow()) {
                    showViewByBankType();
                }
            }
            //continue detect if haven't detected card type yet
            if (!detected && paymentInfoHelper.isLinkTrans()) {
                needToWarningNotSupportCard = true;
                continueDetectCardForLinkCard();
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public ChannelActivity getActivity() throws Exception {
        try {
            return (ChannelActivity) getView().getActivity();
        } catch (Exception e) {
            throw new IllegalStateException("activity host invalid");
        }
    }

    public ChannelFragment getView() throws Exception {
        if (mView == null) {
            throw new IllegalAccessException("View is invalid");
        }
        return mView;
    }

    public abstract boolean checkEnableSubmitButton();

    protected abstract void populateBankCode();

    protected abstract boolean isATMChannel();

    protected abstract boolean canSwitchChannelLinkCard();

    protected abstract boolean isOwnChannel();

    protected abstract void switchChannel();

    protected abstract boolean checkValidRequiredEditText(EditText pEditText);

    protected abstract boolean needToWarningNotSupportCard();

    public abstract AbstractCardDetector getCardFinder();

    public abstract boolean isAllowValidateCardNumberByLuhn();

    public abstract void continueDetectCardForLinkCard() throws Exception;

    public abstract void setCardDateOnCardView();

    public abstract VPaymentValidDateEditText getCardDateView() throws Exception;

    protected abstract void actionAfterFinishInputCard();

    protected abstract int validateInputCard() throws Exception;

    protected abstract boolean validateCardNumberLength();

    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        return null;
    }

    protected void init(AbstractWorkFlow pAdapter, ChannelFragment pChannelFragment) {
        try {
            mAdapter = pAdapter;
            mView = pChannelFragment;
            if (mAdapter.isChannelHasInputCard()) {
                initForInputCard();
                if (GlobalData.analyticsTrackerWrapper != null) {
                    GlobalData.analyticsTrackerWrapper
                            .step(ZPPaymentSteps.OrderStep_InputCardInfo)
                            .track();
                }
            } else {
                initForMapCardAndZaloPay();
            }
            mAdapter.setECardFlowType(BankFlow.API);
            getCardFinder();
            setCardDateOnCardView();
            initMutualView();
            setMinHeightSwitchCardButton();
        } catch (Exception e) {
            Timber.d(e, "Exception init card gui processor");
        }
    }

    private void initForInputCard() throws Exception {
        mCardView = (CreditCardView) getView().findViewById(R.id.credit_card_view);
        if (mCardView != null) {
            mCardView.initCardSelector();
            mCardView.setOnClickOnCardView(this);
        }
        mDotView = (LinearLayout) getView().findViewById(R.id.dotView);
        mButtonPre = (Button) getView().findViewById(R.id.previous);
        mButtonNext = (Button) getView().findViewById(R.id.next);
        getView().visibleCardViewNavigateButton(true);
        getView().visibleSubmitButton(false);
        getView().visibleOrderInfo(false);
        getView().visibleCardInfo(true);
        if (mButtonNext != null && mButtonPre != null) {
            mButtonNext.setOnClickListener(mNextButtonClick);
            mButtonPre.setOnClickListener(mPreviousButtonClick);
        }
    }

    private void initForMapCardAndZaloPay() {

    }

    private void initMutualView() throws Exception {
        mLayoutSwitch = getView().findViewById(R.id.zpw_switch_card_button);
        mScrollViewRoot = (ScrollView) getView().findViewById(R.id.zpw_scrollview_container);
        if (mScrollViewRoot != null) {
            OverScrollDecoratorHelper.setUpOverScroll(mScrollViewRoot);
        }
        mRootView = getView().findViewById(R.id.supperRootView);
    }

    protected void initWebView() throws Exception {
        mWebView = (SdkWebView) getView().findViewById(R.id.zpw_threesecurity_webview);
    }

    protected void flipCardView(int pPosition) {
    }

    public void checkForSwitchChannel() throws Exception {
        if (!(getAdapter().needToSwitchChannel() && isOwnChannel())) {
            return;
        }
        getAdapter().resetNeedToSwitchChannel();
        if (canSwitchChannelLinkCard()) {
            switchChannel();
        } else {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(0);
            }
            getView().showDialogWarningLinkCardAndResetCardNumber();
        }
    }

    public void populateCard() throws Exception {
        PaymentCard card = getAdapter().getCard();
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

    protected AbstractWorkFlow getAdapter() throws Exception {
        if (mAdapter == null) {
            throw new IllegalAccessException("Adapter is invalid");
        }
        return mAdapter;
    }

    View getCurrentFocusView() {
        if (mCurrentFocusView == null) {
            try {
                mCurrentFocusView = getActivity().getCurrentFocus();
            } catch (Exception ex) {
                Timber.w(ex, "Exception get current focus view");
            }
        }
        return mCurrentFocusView;
    }

    public void initPager() throws Exception {
        Timber.d("init viewpager");
        if (mViewPager == null) {
            mViewPager = (ViewPager) getView().findViewById(R.id.card_field_container_pager);
        }
        mViewPager.addOnPageChangeListener(this);
        mCardAdapter = onCreateCardFragmentAdapter();
        mViewPager.setOffscreenPageLimit(mCardAdapter.getCount());
        mViewPager.setAdapter(mCardAdapter);
        updateDots();
        //auto show keyboard when the first time start
        showKeyBoardAndResizeButtonsIfNotSwitchChannel();
    }

    boolean isValidCardNumber() {
        try {
            boolean validateLuhn = validateCardNumberLuhn();
            boolean isValidCardNumber = ((TextUtils.isEmpty(getCardNumber())
                    || !validateLuhn
                    || !validateCardNumberLength()
                    || mCardAdapter.getCardNumberFragment().hasError()));
            return !isValidCardNumber;
        } catch (Exception e) {
            Timber.w(e, "Exception check valid card number");
        }
        return false;
    }

    public String warningCardExist() {
        String message = getCardFinder().warningCardExistMessage();
        if (getBankCardFinder().detected()) {
            message = getBankCardFinder().warningCardExistMessage();
        } else if (getCreditCardFinder().detected()) {
            message = getCreditCardFinder().warningCardExistMessage();
        }
        return message;
    }

    private boolean validateCardNumber() {
        try {
            if (isValidCardNumber()) {
                return true;
            }
            //come back last page
            if (mViewPager != null) {
                mViewPager.setCurrentItem(mLastPageSelected);
            }
            String errMes = getCardNumberView().getPatternErrorMessage();
            if (TextUtils.isEmpty(getCardNumber())) {
                errMes = mContext.getResources().getString(R.string.sdk_error_missing_cardnumber_mess);
            } else if (!validateCardNumberLength()) {
                errMes = getCardNumberView().getPatternErrorMessage();
            } else if (preventNextIfLinkCardExisted()) {
                errMes = warningCardExist();
            } else if (!validateCardNumberLuhn()) {
                errMes = mContext.getResources().getString(R.string.sdk_error_luhn_cardnumber_mess);
            }
            showHintError(getCardNumberView(), errMes);
            //disable next button
            disableNext();
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    private void updateLastPagePosition(int pPosition) {
        mLastPageSelected = pPosition;
    }

    /*
     * set cursor to the last position of edittext
     */
    private void moveCursorToLastPositionOnText(int pPosition) {
        try {
            if (mCardAdapter == null) {
                Timber.d("mCardAdapter is null on moveCursorToLastPositionOnText");
                return;
            }
            CreditCardFragment currentFragment = mCardAdapter.getItemAtPosition(pPosition);
            if (currentFragment != null) {
                currentFragment.onSelectText();
            }
        } catch (Exception e) {
            Timber.d(e, "Exception moveCursorToLastPositionOnText");
        }
    }

    private void clearHighLightOnCardView() {
        new Handler().postDelayed(() -> {
            try {
                clearHighLight();
            } catch (Exception e) {
                Timber.d(e);
            }
        }, 100);
    }

    /*
     * prevent navigate if previous fragment have an error
     */
    private boolean preventNavigateIfHasError(int pPosition) {
        boolean hasError = false;
        if (mViewPager == null || mCardAdapter == null) {
            Timber.d("NULL on preventNavigateIfHasError");
            return false;
        }
        if (pPosition < 0 || pPosition > mCardAdapter.getCount()) {
            return false;
        }
        //user swipe to next
        if (mLastPageSelected < pPosition && !mCardAdapter.canNavigateToNext(pPosition)) {
            mViewPager.setCurrentItem(mLastPageSelected);
            hasError = true;
        }
        //user swipe to previous
        else if (mLastPageSelected > pPosition && !mCardAdapter.canNavigateToPrevious(pPosition)) {
            mViewPager.setCurrentItem(mLastPageSelected);
            hasError = true;
        }
        return hasError;
    }

    private boolean isValidCardCVV() {
        try {
            VPaymentEditText cardCVV = getCardCVVView();
            if (cardCVV == null) {
                return true;
            }
            return TextUtils.isEmpty(cardCVV.getText().toString())
                    || cardCVV.isValidPattern()
                    && (cardCVV.getText().toString().length() == 3);
        } catch (Exception e) {
            Timber.d(e);
        }
        return true;
    }

    private boolean validateCardCVV() {
        try {
            boolean isValidCardCVV = isValidCardCVV();
            if (!isValidCardCVV) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(mLastPageSelected);
                }
                String errMes = getCardCVVView() != null ? getCardCVVView().getPatternErrorMessage() : null;
                showHintError(getCardCVVView(), errMes);
            }
            return isValidCardCVV;
        } catch (Exception e) {
            Timber.d(e, "Exception validate card cvv");
        }
        return true;
    }

    private boolean isValidCardDate() {
        try {
            return TextUtils.isEmpty(getCardDateView().getText().toString())
                    || getCardDateView().isValidPattern()
                    && (getCardDateView().getText().toString().length() == 5);
        } catch (Exception e) {
            Timber.d(e, "Exception validate card date");
        }
        return true;
    }

    private boolean validateCardDate() {
        try {
            boolean isValidCardDate = isValidCardDate();
            if (!isValidCardDate) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(mLastPageSelected);
                }
                String errMes = getCardDateView().getPatternErrorMessage();
                showHintError(getCardDateView(), errMes);
            }
            return isValidCardDate;
        } catch (Exception e) {
            Timber.d(e, "Exception validate card date");
        }
        return true;
    }

    /*
     * check formula Luhn card number
     */
    private boolean validateCardNumberLuhn() throws Exception {
        boolean isDetected = getCardFinder().detected();
        if (getAdapter().getPaymentInfoHelper().isLinkTrans() && !isDetected) {
            isDetected = getCreditCardFinder().detected()
                    ? getCreditCardFinder().detected() : getBankCardFinder().detected();
        }
        return !(isAllowValidateCardNumberByLuhn()
                && isDetected)
                || getCardFinder().validCardNumberLuhnFormula(getCardNumber());
    }

    /*
     * show keyboard and focus on current view
     */
    public void onFocusView() {
        try {
            mCurrentFocusView = getActivity().getCurrentFocus();
            if (mCurrentFocusView != null && mCurrentFocusView instanceof EditText) {
                SdkUtils.focusAndSoftKeyboard(getActivity(), (EditText) mCurrentFocusView);
            }
        } catch (Exception e) {
            Timber.d(e, "Exception on focus view");
        }
    }

    public void useWebView(boolean pIsUseWebView) throws Exception {
        if (getView() == null) {
            return;
        }
        if (pIsUseWebView) {
            getView().visibleWebView(true);
            getView().visibleInputCardView(false);
            getView().visibleSubmitButton(false);
            if (CardType.PBIDV.equals(getBankCode())) {
                registerKeyboardEventForBidv();
                getView().showMenuItem();
            }
        } else {
            getView().visibleWebView(false);
            removeKeyboardEventForBidv();
            getView().visibleBIDVAccountRegisterBtn(false);
            getView().visibleInputCardView(true);
            getView().visibleSubmitButton(true);
        }
    }

    private void registerKeyboardEventForBidv() {
        if (mRootView == null) {
            return;
        }
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    private void removeKeyboardEventForBidv() {
        if (mRootView == null) {
            return;
        }
        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    private void showKeyBoardAndResizeButtonsIfNotSwitchChannel() throws Exception {
        if (getAdapter().getPresenter().isSwitchAdapter()) {
            return;
        }
        new Handler().postDelayed(() -> {
            try {
                //dialog process is showing. so when process dialog close, need to show keyboard again.
                if (DialogManager.showingLoadDialog()) {
                    return;
                }
                showKeyBoardOnCardNumberView();
                moveScrollViewToCurrentFocusView();
                getAdapter().getPresenter().setSwitchAdapter(false);
            } catch (Exception e) {
                Timber.d(e);
            }
        }, 300);
    }

    void clearCardNumberAndShowKeyBoard() {
        try {
            mCardAdapter.getCardNumberFragment().clearText();
            mCardView.setCardNumber("");
        } catch (Exception e) {
            Timber.d(e, "Exception clear card number and show keyboard");
        }
        if (mViewPager != null) {
            mViewPager.setCurrentItem(0);
        }
        //auto show keyboard when the first time start
        new Handler().postDelayed(() -> {
            try {
                showKeyBoardAndResizeButtons();
            } catch (Exception e) {
                Timber.d(e);
            }
        }, 500);
    }

    private void showKeyBoardAndResizeButtons() {
        showKeyBoardOnCardNumberView();
        moveScrollViewToCurrentFocusView();
        Timber.d("showKeyBoardAndResizeButtons");
    }

    void onDetectedBank() {
        try {
            if (isInputBankMaintenance()) {
                return;
            }
            String bankName = getDetectedBankName();
            String bankCode = getBankCode();
            setCardNumberHint(bankName);
            MiniPmcTransType miniPmcTransType = null;
            if (TextUtils.isEmpty(bankCode)) {
                getAdapter().setMiniPmcTransType(null);
            } else {
                miniPmcTransType = getAdapter().getConfig(bankCode);
            }
            //check quota amount transaction for each bank
            PaymentInfoHelper paymentInfoHelper = getAdapter().getPaymentInfoHelper();
            if (miniPmcTransType != null && !paymentInfoHelper.isLinkTrans() && (getAdapter() instanceof BankCardWorkFlow)) {
                miniPmcTransType.calculateFee(paymentInfoHelper.getAmount());
                paymentInfoHelper.getOrder().plusChannelFee(miniPmcTransType.totalfee);
                miniPmcTransType.checkPmcOrderAmount(paymentInfoHelper.getAmount());//check amount is support or not
                if (!miniPmcTransType.isAllowPmcQuota()) {
                    CardNumberFragment cardNumberView = mCardAdapter.getCardNumberFragment();
                    String invalidAmountMessage = mContext.getResources().getString(R.string.invalid_order_amount_bank);
                    double amount_total = paymentInfoHelper.getAmountTotal();
                    cardNumberView.setError(String.format(invalidAmountMessage,
                            getBankCardFinder().getShortBankName(),
                            CurrencyUtil.formatCurrency(amount_total)));
                    disableNext();
                    return;
                }
            }
            //limit number of cc link number
            if (isMaxCcLink(bankCode)) {
                return;
            }
            //check bank future feature
            if (getCardFinder().detected() && miniPmcTransType != null && !miniPmcTransType.isVersionSupport(SdkUtils.getAppVersion(mContext))) {
                showWarningBankVersionSupport();
                return;
            }
            //check disable pmc
            if (getCardFinder().detected() && miniPmcTransType != null && miniPmcTransType.isDisable()) {
                showWarningDisablePmc(bankName);
                return;
            }

            //user input bank account
            if (!TextUtils.isEmpty(bankCode) && BankHelper.isBankAccount(bankCode)) {
                showWarningBankAccount();
            }

            //move to next page if detect a card
            if (getCardFinder().detected()) {
                autoMoveToNextFragment();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private boolean isMaxCcLink(String bankCode) {
        try {
            int max_cc_link = ConfigLoader.maxCCLinkNum();
            int currentCcLinkNum = getAdapter().mCurrentCcLinkNumber;
            if (BankHelper.isInternationalBank(bankCode)
                    && currentCcLinkNum >= max_cc_link) {
                String mess = String.format(mContext.getString(R.string.sdk_bank_link_cc_limit_warning), String.valueOf(max_cc_link));
                getView().showNotificationDialog(mess, mContext.getResources().getString(R.string.dialog_agree_button), this::clearCardNumberAndShowKeyBoard);
                return true;
            }
        } catch (Exception e) {
            Timber.d(e, "Exception check max cc link num");
        }
        return false;
    }

    void populateTextOnCardView() {
        if (mCardView != null) {
            mCardView.setCardNumber(getCardNumber());
        }
    }

    void populateTextOnCardViewNoPaintCard() {
        if (mCardView != null) {
            mCardView.setCardNumberNoPaintCard(getCardNumber());
        }
    }

    private void showWarningBankVersionSupport() throws Exception {
        if (!(getCardFinder() instanceof BankDetector)) {
            return;
        }
        BankConfig bankConfig = ((BankDetector) getCardFinder()).getFoundBankConfig();
        if (bankConfig == null) {
            Timber.d("bank config is null");
            return;
        }
        String pMessage = getAdapter().getPaymentInfoHelper().isLinkTrans() ?
                mContext.getResources().getString(R.string.sdk_warning_version_support_linkchannel) :
                mContext.getResources().getString(R.string.sdk_warning_version_support_payment);
        pMessage = String.format(pMessage, bankConfig.getShortBankName());
        getView().showConfirmDialog(pMessage,
                mContext.getResources().getString(R.string.dialog_upgrade_button),
                mContext.getResources().getString(R.string.dialog_retry_input_card_button),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        clearCardNumberAndShowKeyBoard();
                    }

                    @Override
                    public void onOKEvent() {
                        try {
                            PlayStoreUtils.openPlayStoreForUpdate(GlobalData.getMerchantActivity(),
                                    BuildConfig.PACKAGE_IN_PLAY_STORE, "Zalo Pay", "force-app-update", "bank-future");
                            getAdapter().getPresenter().callback();
                        } catch (Exception e) {
                            Timber.w(e);
                        }
                    }
                });
    }

    void callbackOnVCBLink() throws Exception {
        if (getAdapter() == null) {
            return;
        }
        PaymentInfoHelper paymentInfoHelper = getAdapter().getPaymentInfoHelper();
        if (paymentInfoHelper == null) {
            return;
        }
        if (BaseActivity.getActivityCount() >= 2) {
            getAdapter().getPresenter().callbackLink(CardType.PVCB);
        } else {
            //callback bankcode to app , app will direct user to link bank account to right that bank
            BankAccount dBankAccount = new BankAccount();
            dBankAccount.bankcode = BankDetector.getInstance().getDetectBankCode();
            paymentInfoHelper.setMapBank(dBankAccount);
            getAdapter().getPresenter().setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINK_ACCOUNT);
        }
    }

    private void showWarningBankAccount() throws Exception {
        if (getAdapter() == null) {
            return;
        }
        ChannelFragment channelFragment = getView();
        PaymentInfoHelper paymentInfoHelper = getAdapter().getPaymentInfoHelper();
        if (channelFragment == null || paymentInfoHelper == null) {
            return;
        }
        if (!paymentInfoHelper.isLinkTrans()) {
            return;
        }
        channelFragment
                .showConfirmDialog(mContext.getResources().getString(R.string.sdk_vcb_link_warning_mess),
                        mContext.getResources().getString(R.string.dialog_linkaccount_button),
                        mContext.getResources().getString(R.string.dialog_retry_input_card_button),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {
                                clearCardNumberAndShowKeyBoard();
                            }

                            @Override
                            public void onOKEvent() {
                                try {
                                    callbackOnVCBLink();
                                } catch (Exception e) {
                                    Timber.d(e, "Exception callback VCB");
                                }
                            }
                        });
    }

    /*
     * link card channel use this to update found card
     */
    void onDetectedBank(String pBankName, String pBankCode) {
        try {
            setCardNumberHint(pBankName);
            MiniPmcTransType miniPmcTransType = null;
            if (TextUtils.isEmpty(pBankCode)) {
                getAdapter().setMiniPmcTransType(null);
            } else {
                miniPmcTransType = getAdapter().getConfig(pBankCode);
            }

            if (getCardFinder().detected() && miniPmcTransType != null &&
                    !miniPmcTransType.isVersionSupport(SdkUtils.getAppVersion(mContext))) {
                showWarningBankVersionSupport();
                return;
            }
            //check disable pmc
            if (getCardFinder().detected() && miniPmcTransType != null && miniPmcTransType.isDisable()) {
                showWarningDisablePmc(pBankName);
                return;
            }
            //limit number of cc link number
            if (isMaxCcLink(pBankCode)) {
                return;
            }
            //user input bank account
            if (!TextUtils.isEmpty(pBankCode) && BankHelper.isBankAccount(pBankCode)) {
                showWarningBankAccount();
            }
            if (getAdapter().isCCFlow() && getBankCardFinder().detected()) {
                autoMoveToNextFragment();
            } else if (getAdapter().isATMFlow() && getCreditCardFinder().detected()) {
                autoMoveToNextFragment();
            }
        } catch (Exception e) {
            Timber.w(e, "Exception set detect card");
        }
    }

    boolean isUseOtpToken() {
        return mUseOtpToken;
    }

    private String getDetectedBankName() {
        return getCardFinder().getBankName();
    }

    public String getBankCode() {
        return getCardFinder().getDetectBankCode();
    }

    public BankDetector getBankCardFinder() {
        return BankDetector.getInstance();
    }

    public CreditCardDetector getCreditCardFinder() {
        return CreditCardDetector.getInstance();
    }

    private void setMinHeightSwitchCardButton() {
        if (mLayoutSwitch != null) {
            int heightSwitchButton = (int) mContext.getResources().getDimension(R.dimen.switch_card_layout_min_height);
            android.view.ViewGroup.LayoutParams params = mLayoutSwitch.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = heightSwitchButton;
            mLayoutSwitch.setLayoutParams(params);
            mLayoutSwitch.requestLayout();
        }
    }

    /*
     * show/hide issue date
     * after detected a card type
     */
    void showViewByBankType() throws Exception {
        if (!getCardFinder().detected()) {
            return;
        }
        if (!(getCardFinder() instanceof BankDetector)) {
            return;
        }
        BankConfig bank = ((BankDetector) getCardFinder()).getFoundBankConfig();
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

    private void updateDots() {
        if (mDotView == null) {
            Timber.d("NULL on updateDots");
            return;
        }
        final int count = mCardAdapter.getCount();
        if (mDotView.getChildCount() == count) {
            return;
        }
        mDotView.removeAllViews();
        for (int i = 0; i < count; i++) {
            addDot();
        }
        selectDot(mLastPageSelected);
    }

    private void addDot() {
        ImageView dot = new ImageView(mContext);
        dot.setImageResource(R.drawable.dot);
        int dotSize = (int) mContext.getResources().getDimension(R.dimen.dot_size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
        params.setMargins(dotSize, 0, dotSize, 0);
        if (mDotView != null) {
            mDotView.addView(dot, params);
        }
    }

    private void selectDot(int pIndex) {
        if (mDotView == null) {
            return;
        }
        final int childCount = mDotView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mDotView.getChildAt(i);
            if (!(view instanceof ImageView)) {
                continue;
            }
            ImageView imageView = (ImageView) view;
            if (i == pIndex) {
                imageView.setImageResource(R.drawable.dot_color);
            } else {
                //reset previous
                imageView.setImageResource(R.drawable.dot);
            }
        }
    }

    public void resetCardNumberAndShowKeyBoard() {
        try {
            getCardNumberView().setText(null);
            new Handler().postDelayed(() -> {
                try {
                    SdkUtils.focusAndSoftKeyboard(getActivity(), mCardAdapter.getItemAtPosition(0).getEditText());
                } catch (Exception e) {
                    Timber.w(e);
                }
            }, 300);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public void setCardInfo(String pCardNumber) {
        if (mViewPager == null || TextUtils.isEmpty(pCardNumber)) {
            return;
        }
        try {
            SdkUtils.focusAndSoftKeyboard(getActivity(), mCardAdapter.getItemAtPosition(1).getEditText());
            applyFont(getView().findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.sdk_font_medium));
            getCardNumberView().setText(pCardNumber);
            getCardNumberView().formatText(true);
            //reset other
            if (mCardView != null) {
                mCardView.setCVV(null);
                mCardView.setCardHolderName(null);
                mCardView.setCardDate(null);
            }
            new Handler().postDelayed(() -> mViewPager.setCurrentItem(1), 300);
        } catch (Exception e) {
            Timber.w(e, "Exception set card info");
        }
    }

    @CallSuper
    public void dispose() {
        Timber.d("dispose gui processor");
        mCardAdapter = null;
        mBankSupportAdapter = null;
        mAdapter = null;
        mView = null;
        if (mWebView != null) {
            mWebView.release();
        }
    }

    void onDoneTapped() throws Exception {
        int errorFragmentIndex = validateInputCard();
        //there're no error
        if (errorFragmentIndex == -1) {
            clearHighLight();
            //just start submit if intener is online
            if (getAdapter().checkAndOpenNetworkingSetting()) {
                actionAfterFinishInputCard();
            }
        } else {
            //user in last view (card name)
            if (errorFragmentIndex == (mMaxPagerCount - 1)) {
                showHintError(getCardNameView(), mContext.getResources().getString(R.string.sdk_invalid_cardname_mess));
            }
            if (mViewPager != null) {
                mViewPager.setCurrentItem(errorFragmentIndex);
            }
        }
    }

    public void stopWebview() {
        if (mWebView != null) {
            mWebView.stopLoading();
        }
    }

    public void reloadUrl() throws Exception {
        if (mWebView == null) {
            initWebView();
        }
        mWebView.reloadLastUrl();
    }

    public void loadUrl(String pUrl) throws Exception {
        if (mWebView == null) {
            initWebView();
        }
        useWebView(true);
        mWebView.startLoadUrl(pUrl);
    }

    public boolean preventNextIfLinkCardExisted() {
        try {
            return getAdapter().existMapCardOnCache();
        } catch (Exception e) {
            Timber.w(e);
            return false;
        }
    }

    public String getCardName() {
        return mCardHolderName;
    }

    public String getCardNumber() {
        return mCardNumber;
    }

    String getCardCVV() {
        return mCVV;
    }

    String getCardExpire() {
        return mExpiry;
    }

    public VPaymentDrawableEditText getCardNumberView() throws Exception {
        if (mCardAdapter == null || mCardAdapter.getCardNumberFragment() == null) {
            return null;
        }
        return (VPaymentDrawableEditText) mCardAdapter.getCardNumberFragment().getEditText();
    }

    VPaymentDrawableEditText getCardNameView() throws Exception {
        if (mCardAdapter == null || mCardAdapter.getCardNameFragment() == null) {
            return null;
        }
        return (VPaymentDrawableEditText) mCardAdapter.getCardNameFragment().getEditText();
    }

    private VPaymentDrawableEditText getCardCVVView() throws Exception {
        if (mCardAdapter == null || mCardAdapter.getCardCVVFragment() == null) {
            return null;
        }
        return (VPaymentDrawableEditText) mCardAdapter.getCardCVVFragment().getEditText();
    }

    VPaymentValidDateEditText getCardIssueView() throws Exception {
        if (mCardAdapter == null || mCardAdapter.getCardIssueryFragment() == null) {
            return null;
        }
        return (VPaymentValidDateEditText) mCardAdapter.getCardIssueryFragment().getEditText();
    }

    VPaymentValidDateEditText getCardExpiryView() throws Exception {
        if (mCardAdapter == null || mCardAdapter.getCardExpiryFragment() == null) {
            return null;
        }
        return (VPaymentValidDateEditText) mCardAdapter.getCardExpiryFragment().getEditText();
    }

    void showNext() {
        try {
            if (mCardAdapter == null || mViewPager == null) {
                return;
            }
            int max = mCardAdapter.getCount();
            int currentIndex = mViewPager.getCurrentItem();
            //prevent user move to next if input existed card in link card
            if (currentIndex == 0 && preventNextIfLinkCardExisted() && getAdapter().getPaymentInfoHelper().isLinkTrans()) {
                showHintError(getCardNumberView(), warningCardExist());
            }
            //validate card number before move to next page
            if (currentIndex == 0 && !validateCardNumberLuhn()) {
                showHintError(getCardNumberView(), mContext.getResources().getString(R.string.sdk_error_luhn_cardnumber_mess));
                return;
            }
            if (currentIndex + 1 < max) {
                mViewPager.setCurrentItem(currentIndex + 1);
            } else {
                // completed the card entry.
                SdkUtils.hideSoftKeyboard(mContext, getActivity());
            }
            refreshNavigateButton();
        } catch (Exception e) {
            Timber.w(e, "Exception show next");
        }
    }

    private void enableNext() {
        if (!mButtonNext.isEnabled()) {
            mButtonNext.setEnabled(true);
            mButtonNext.setBackgroundResource(R.drawable.button_next);
        }
    }

    private void disableNext() {
        if (mButtonNext.isEnabled()) {
            mButtonNext.setEnabled(false);
            mButtonNext.setBackgroundResource(R.drawable.button_next_disable);
        }
    }

    private void enablePrevious() {
        if (!mButtonPre.isEnabled()) {
            mButtonPre.setEnabled(true);
            mButtonPre.setBackgroundResource(R.drawable.button_pre);
        }
    }

    private void disablePrevious() {
        if (mButtonPre.isEnabled()) {
            mButtonPre.setEnabled(false);
            mButtonPre.setBackgroundResource(R.drawable.button_pre_disable);
        }
    }

    private void refreshNavigateButton() {
        if (mViewPager == null || mViewPager.getAdapter() == null) {
            return;
        }
        int currentIndex = mViewPager.getCurrentItem();
        int max = mViewPager.getAdapter().getCount();
        if (currentIndex == 0) {
            disablePrevious();
        } else if ((max - currentIndex == 1) && mCardAdapter.hasError() > -1) {
            disableNext();
        } else {
            enablePrevious();
            enableNext();
        }
    }

    private void showPrevious() {
        if (mViewPager == null) {
            return;
        }
        int currentIndex = mViewPager.getCurrentItem();
        if (currentIndex - 1 >= 0) {
            mViewPager.setCurrentItem(currentIndex - 1);
        }
        refreshNavigateButton();
    }

    boolean isInputBankMaintenance() {
        boolean isBankDetect = BankDetector.getInstance().detected();
        boolean isCCDetect = CreditCardDetector.getInstance().detected();
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

    private void showMaintenanceBank(String messaage) {
        try {
            getView()
                    .showInfoDialog(messaage,
                            mContext.getString(R.string.dialog_retry_input_card_button),
                            mBankMaintenanceDialogListener);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void showWarningDisablePmc(String pBankName) throws Exception {
        try {
            String mess = getAdapter().getPaymentInfoHelper().isLinkTrans() ?
                    mContext.getResources().getString(R.string.sdk_warning_pmc_transtype_disable_link) :
                    mContext.getResources().getString(R.string.sdk_warning_pmc_transtype_disable_payment);

            String disableBankMessage = String.format(mess, pBankName);
            getView().showInfoDialog(disableBankMessage,
                    mContext.getResources().getString(R.string.dialog_retry_input_card_button),
                    () -> new Handler().postDelayed(() -> {
                        //focus to edittext again after closing dialog
                        try {
                            if (mViewPager != null) {
                                mViewPager.setCurrentItem(0);
                            }
                            getCardNumberView().setText(null);
                            SdkUtils.focusAndSoftKeyboard(getActivity(), getCardNumberView());
                        } catch (Exception e) {
                            Timber.w(e, "Exception show warning disable desc");
                        }
                    }, 400));
        } catch (Exception e) {
            Timber.w(e, "Exception show warning disable desc");
        }
    }

    private void showSupportCardList() throws Exception {
        mBankSupportAdapter = new BankSupportAdapter(getActivity());
        BankListDialogFragment bankDialog = new BankListDialogFragment();
        bankDialog.setAdapter(mBankSupportAdapter);
        bankDialog.setCloseCardSupportDialog(this::clearCardNumberAndShowKeyBoard);
        List<String> items = getListCardSupport(isATMChannel());
        if (Lists.isEmptyOrNull(items)) {
            return;
        }
        mBankSupportAdapter.insertItems(items);
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        if (fragmentManager != null) {
            bankDialog.show(fragmentManager, BankListDialogFragment.class.getSimpleName());
        }
    }

    private ArrayList<String> getListCardSupport(boolean isATMChannel) throws Exception {
        PaymentInfoHelper paymentInfoHelper = getAdapter().getPaymentInfoHelper();
        if (paymentInfoHelper == null) {
            return null;
        }
        if (paymentInfoHelper.isLinkTrans()) {
            return CardSupportHelper.getLinkCardSupport();
        } else {
            return isATMChannel
                    ? CardSupportHelper.getLocalBankSupport() : CardSupportHelper.getCardSupport();
        }
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

    View.OnFocusChangeListener getOnOtpCaptchaFocusChangeListener() {
        return mOnOtpCaptchFocusChangeListener;
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

    /*
     * update error hint on TextIntputLayout
     */
    private void setCardNumberHint(String pMessageHint) throws Exception {
        if (!needToWarningNotSupportCard) {
            return;
        }

        CardNumberFragment cardNumberView = mCardAdapter != null
                ? mCardAdapter.getCardNumberFragment() : null;
        if (cardNumberView == null) {
            Timber.d("NULL on setCardNumberHint");
            return;
        }

        //card not support
        if (TextUtils.isEmpty(pMessageHint) && needToWarningNotSupportCard()) {
            if (!supportCard()) {
                return;
            }
            cardNumberView.setError(mContext.getResources().getString(R.string.sdk_card_not_support));
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

    public void showHintError(EditText pEdittext, String pError) throws Exception {
        RenderHelper.setTextInputLayoutHintError(pEdittext, pError, mContext);
        disablePrevious();
        disableNext();
        if (pEdittext.getId() == R.id.edittext_localcard_number) {
            CardNumberFragment cardNumberView = mCardAdapter != null
                    ? mCardAdapter.getCardNumberFragment() : null;
            if (cardNumberView != null) {
                cardNumberView.hideQuestionIcon();
            }
        } else if (pEdittext.getId() == R.id.edittext_localcard_name) {
            enablePrevious();
        }
    }

    private void clearHintError(EditText pEdittext) {
        try {
            RenderHelper.setTextInputLayoutHint(pEdittext, null, mContext);
            enableNext();
            enablePrevious();
        } catch (Exception e) {
            Timber.w(e, "Exception clear hint error");
        }
    }

    void showKeyBoardOnCardNumberView() {
        try {
            SdkUtils.focusAndSoftKeyboard(getActivity(), getCardNumberView());
        } catch (Exception ignored) {
        }
    }

    public void showKeyBoardOnEditTextAndScroll(final EditText pEditText) {
        new Handler().postDelayed(() -> {
            try {
                SdkUtils.focusAndSoftKeyboard(getActivity(), pEditText);
                moveScrollViewToCurrentFocusView();
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }, 300);
    }

    public void showKeyBoardOnEditText(final EditText pEditText) {
        new Handler().postDelayed(() -> {
            try {
                SdkUtils.focusAndSoftKeyboard(getView().getActivity(), pEditText);
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }, 300);
    }

    boolean supportCard() {
        try {
            if (mCardAdapter != null
                    && mCardAdapter.getCardNumberFragment().hasError()
                    && (mCardAdapter.getCardNumberFragment().getError().equals(mContext.getResources().getString(R.string.sdk_card_not_support))
                    || mCardAdapter.getCardNumberFragment().getError().contains("không hỗ trợ"))) {
                return false;
            }
        } catch (Exception e) {
            Timber.w(e, "Exception check card support");
        }
        return true;
    }

    void clearHighLight() {
        if (mCardView == null) {
            return;
        }
        mCardView.clearHighLightCardNumber();
        mCardView.clearHighLightCardHolderName();
        mCardView.clearHighLightCardDate();
        mCardView.clearHighLightCVV();
    }

    void updateCardInfoAfterTextChange(String pInfo) {
        if (getCurrentFocusView() == null) {
            return;
        }
        if (mCardView == null) {
            return;
        }
        if (getCurrentFocusView().getId() == R.id.edittext_localcard_name) {
            mCardHolderName = PaymentUtils.clearCardName(pInfo.trim());
            mCardView.setCardHolderName(mCardHolderName);
        } else if (getCurrentFocusView().getId() == R.id.edittext_issue_date) {
            mIssueDate = pInfo.trim();
            mCardView.setCardDate(mIssueDate);
        } else if (getCurrentFocusView().getId() == R.id.CreditCardExpiredDate) {
            mExpiry = pInfo.trim();
            mCardView.setCardDate(mExpiry);
        } else if (getCurrentFocusView().getId() == R.id.CreditCardCVV) {
            mCVV = pInfo.trim();
            mCardView.setCVV(mCVV);
        }
    }

    private CardRule getSelectBankCardIdentifier() {
        try {
            CardRule cardIdentifier;
            if (getAdapter().getPaymentInfoHelper().isLinkTrans()) {
                cardIdentifier = getBankCardFinder().getFoundCardRule();
                if (cardIdentifier == null) {
                    cardIdentifier = getCreditCardFinder().getFoundCardRule();
                }
            } else {
                cardIdentifier = getCardFinder().getFoundCardRule();
            }
            return cardIdentifier;
        } catch (Exception e) {
            Timber.d(e, "Exception get select bank identifier");
        }
        return null;
    }

    public boolean isCardLengthMatchIdentifier(String pCardNumber) {
        if (TextUtils.isEmpty(pCardNumber)) {
            return false;
        }
        CardRule cardIdentifier = getSelectBankCardIdentifier();
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

    /*
     * user finish input 1 field,auto swipe to next
     */
    void autoMoveToNextFragment() throws Exception {
        //card number
        View currentFocusView = getCurrentFocusView();
        if (!(currentFocusView instanceof VPaymentEditText)) {
            return;
        }
        EditText editText = null;
        if (currentFocusView.getId() == R.id.edittext_localcard_number
                || currentFocusView.getId() == R.id.CreditCardExpiredDate
                || currentFocusView.getId() == R.id.edittext_issue_date
                || currentFocusView.getId() == R.id.CreditCardCVV) {

            editText = (EditText) currentFocusView;
        }
        int length = 0;
        int maxLength = 0;
        if (editText != null) {
            String text = editText.getText().toString();
            if (editText instanceof VPaymentDrawableEditText) {
                text = ((VPaymentDrawableEditText) editText).getString();
            }
            length = !TextUtils.isEmpty(text) ? text.length() : 0;
            if (currentFocusView instanceof VPaymentValidDateEditText) {
                maxLength = ((VPaymentValidDateEditText) currentFocusView).getMaxLength();
            }
        }

        if (currentFocusView.getId() == R.id.edittext_localcard_number) {
            if (!checkValidCardNumberFromBundle && length == 16) {
                showNext();
                return;
            }
            if (currentFocusView instanceof VPaymentDrawableEditText) {
                VPaymentDrawableEditText numberCard = (VPaymentDrawableEditText) currentFocusView;
                if (isCardLengthMatchIdentifier(numberCard.getString())) {
                    showNext();
                }
            }
        }
        //card expiry || card issue
        else if (length == maxLength &&
                (currentFocusView.getId() == R.id.CreditCardExpiredDate || currentFocusView.getId() == R.id.edittext_issue_date)) {
            showNext();
        }
        //card cvv
        else if (currentFocusView.getId() == R.id.CreditCardCVV && length == 3) {
            showNext();
        }
    }

    void getLengthInputBeforeTextChange() {
        try {
            mCurrentFocusView = getActivity().getCurrentFocus();
            if (getCurrentFocusView() != null && getCurrentFocusView() instanceof VPaymentEditText) {
                mLengthBeforeChange = ((VPaymentEditText) getCurrentFocusView()).getLength();
            }
        } catch (Exception e) {
            Timber.w(e, "Exception get length input");
        }
    }

    boolean isNeedValidateOnTextChange(View view) {
        return !(view != null && (view.getId() == R.id.zpsdk_otp_ctl
                || view.getId() == R.id.zpsdk_captchar_ctl
                || view.getId() == R.id.edittext_otp
                || view.getId() == R.id.edittext_token));
    }

    boolean validateInputOnTextChange() throws Exception {
        if (getAdapter() == null || getActivity() == null) {
            Timber.d("getWorkFlow() == null || getWorkFlow().getActivity() == null");
            return true;
        }

        if (!(getActivity().getCurrentFocus() instanceof VPaymentEditText)) {
            return true;
        }
        VPaymentEditText currentFocusView = (VPaymentEditText) getCurrentFocusView();
        if (currentFocusView == null) {
            return true;
        }
        //card name input
        if (currentFocusView.getId() == R.id.edittext_localcard_name
                && !currentFocusView.isValidInput()) {
            showHintError(currentFocusView, mContext.getResources().getString(R.string.sdk_invalid_cardname_mess));
            return false;
        }
        //empty or input valid
        else if (TextUtils.isEmpty(currentFocusView.getText().toString())
                || (currentFocusView.isValidPattern())) {
            clearHintError(currentFocusView);
            return true;
        }
        //special case for issue day card.
        else if (currentFocusView.getId() == R.id.edittext_issue_date
                || currentFocusView.getId() == R.id.CreditCardExpiredDate) {
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
                Timber.d(e);
            }
        }
        //user delete input
        else if (mLengthBeforeChange > currentFocusView.getLength()) {
            showHintError(currentFocusView, currentFocusView.getPatternErrorMessage());
            return false;
        }
        return true;
    }

    boolean validCardNameWithWhiteSpace(String pCardName) {
        try {
            String newString = (pCardName.length() > 1) ? String.valueOf(pCardName.charAt(pCardName.length() - 1)) : "";
            //check input 2 space
            String VERTICAL_SEPERATOR = " ";
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
            Timber.d(e);
        }
        isInputValidWithWhiteSpace = true;
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        try {
            Timber.d("on page select %s", position);
            if (mCardAdapter == null) {
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
        } catch (Exception e) {
            Timber.w(e, "Exception on page selected %s", position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
