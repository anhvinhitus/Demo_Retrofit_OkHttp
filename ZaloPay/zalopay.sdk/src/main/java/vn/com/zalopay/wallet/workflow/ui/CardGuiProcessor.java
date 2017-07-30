package vn.com.zalopay.wallet.workflow.ui;

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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.utility.CurrencyUtil;
import vn.com.zalopay.utility.PaymentUtils;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.business.entity.staticconfig.CardRule;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebView;
import vn.com.zalopay.wallet.card.AbstractCardDetector;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.constants.BankFlow;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Link_Then_Pay;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.dialog.BankListDialogFragment;
import vn.com.zalopay.wallet.dialog.CardSupportHelper;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.RenderHelper;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
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

import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;

public abstract class CardGuiProcessor extends SingletonBase implements ViewPager.OnPageChangeListener {
    protected Context mContext;
    protected WeakReference<AbstractWorkFlow> mAdapter;
    protected WeakReference<ChannelFragment> mView;
    protected PaymentWebView mWebView;
    View mRootView;
    ScrollView mScrollViewRoot;
    int mLastPageSelected = 0;
    boolean checkAutoMoveCardNumberFromBundle = true;
    String mIssueDate;
    CardFragmentBaseAdapter mCardAdapter;
    boolean needToWarningNotSupportCard = true;
    View.OnFocusChangeListener mOnFocusChangeListener = (view, hasFocus) -> {
        if (hasFocus) {
            new Handler().postDelayed(() -> {
                try {
                    moveScrollViewToCurrentFocusView();
                } catch (Exception e) {
                    Timber.w(e);
                }
            }, 100);
        }
    };
    View.OnTouchListener mOnTouchListener = (view, motionEvent) -> {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            moveScrollViewToCurrentFocusView();
        }
        return false;
    };
    String mCardNumber;
    int mMaxPagerCount;
    String lastValue = "";
    boolean isInputValidWithWhiteSpace = true;
    private ViewPager mViewPager;
    private BankSupportAdapter mBankSupportAdapter;
    private View mLayoutSwitch;
    private int mLengthBeforeChange;
    private View mCurrentFocusView;
    private boolean mUseOtpToken = false;
    private CreditCardView mCardView;
    private String mCardHolderName;
    private String mExpiry;
    private String mCVV;
    private Button mButtonNext, mButtonPre;
    /*
     * user tap on done on keyboard
     */
    protected TextView.OnEditorActionListener mEditorActionListener = (v, actionId, event) -> {
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
            Timber.w(e.getMessage());
        }
        return false;
    };
    private LinearLayout mDotView;
    private View.OnClickListener mNextButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
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
            } catch (Exception e) {
                Timber.w(e, "Exception on next button clicked");
            }
        }
    };
    private View.OnClickListener mPreviousButtonClick = view -> {
        if (getViewPager() == null) {
            return;
        }
        showPrevious();
    };
    private View.OnClickListener mClickOnEditTextListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                moveScrollViewToCurrentFocusView();
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };
    private View.OnTouchListener mOnTouchOnCardView = new View.OnTouchListener() {
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
    private String mLastCharacterCardName = "";
    protected TextWatcher mEnabledTextWatcher = new TextWatcher() {
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
    private View.OnFocusChangeListener mOnOtpCaptchFocusChangeListener = (view, hasFocus) -> {
        if (hasFocus) {
            new Handler().postDelayed(() -> {
                try {
                    moveScrollViewToCurrentFocusView();
                } catch (Exception e) {
                    Timber.w(e.getMessage());
                }
            }, 100);
        }
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
            getViewPager().setCurrentItem(0);
            getCardNumberView().setText(null);
            SdkUtils.focusAndSoftKeyboard(getActivity(), getCardNumberView());
        } catch (Exception e) {
            Timber.w(e, "Exception show maintenance bank dialog");
        }
    }, 400);

    public CardGuiProcessor(Context context) {
        this.mContext = context;
    }

    public void onDetectCardComplete(Boolean detected) {
        Timber.d("card number %s is detected %s", getCardNumber(), detected);
        try {
            if (getAdapter().getPaymentInfoHelper().payByCardMap() || getAdapter().getPaymentInfoHelper().payByBankAccountMap()) {
                return;
            }
            if (TextUtils.isEmpty(getCardNumber())) {
                getAdapter().setNeedToSwitchChannel(false);
            }
            if (!getAdapter().isNeedToSwitchChannel()) {
                //workout prevent flicker when switch atm and cc
                if (!detected && getAdapter().getPaymentInfoHelper().isLinkTrans()) {
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
            if (!detected && getAdapter().getPaymentInfoHelper().isLinkTrans()) {
                needToWarningNotSupportCard = true;
                continueDetectCardForLinkCard();
            }
        } catch (Exception e) {
            Timber.w(e.getMessage());
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
        if (mView == null || mView.get() == null) {
            throw new IllegalAccessException("View is invalid");
        }
        return mView.get();
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

    public abstract void continueDetectCardForLinkCard();

    public abstract void setCardDateOnCardView();

    public abstract VPaymentValidDateEditText getCardDateView() throws Exception;

    protected abstract void actionAfterFinishInputCard();

    protected abstract int validateInputCard();

    protected abstract boolean validateCardNumberLength();

    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        return null;
    }

    protected void init(AbstractWorkFlow pAdapter, ChannelFragment pChannelFragment) {
        try {
            mAdapter = new WeakReference<>(pAdapter);
            mView = new WeakReference<>(pChannelFragment);
            if (mAdapter.get().isChannelHasInputCard()) {
                initForInputCard();
                if (GlobalData.analyticsTrackerWrapper != null) {
                    GlobalData.analyticsTrackerWrapper
                            .step(ZPPaymentSteps.OrderStep_InputCardInfo)
                            .track();
                }
            } else {
                initForMapCardAndZaloPay();
            }
            getAdapter().setECardFlowType(BankFlow.API);
            getCardFinder();
            setCardDateOnCardView();
            initMutualView();
            setMinHeightSwitchCardButton();
        } catch (Exception e) {
            Timber.w(e, "Exception init card gui processor");
        }
    }

    private void initForInputCard() throws Exception {
        mCardView = (CreditCardView) getView().findViewById(R.id.credit_card_view);
        if (getCardView() != null) {
            getCardView().initCardSelector();
            getCardView().setOnClickOnCardView(this);
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

    private void initWebView() throws Exception {
        mWebView = (PaymentWebView) getView().findViewById(R.id.zpw_threesecurity_webview);
        if (mWebView != null) {
            mWebView.setPaymentWebViewClient(getAdapter());
        }
    }

    protected void flipCardView(int pPosition) {
    }

    public void checkForSwitchChannel() throws Exception {
        if (getAdapter().needToSwitchChannel() && isOwnChannel()) {
            getAdapter().resetNeedToSwitchChannel();
            if (canSwitchChannelLinkCard()) {
                switchChannel();
            } else {
                getViewPager().setCurrentItem(0);
                getView().showDialogWarningLinkCardAndResetCardNumber();
            }
        }
    }

    public void populateCard() throws Exception {
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

    protected AbstractWorkFlow getAdapter() throws Exception {
        if (mAdapter == null || mAdapter.get() == null) {
            throw new IllegalAccessException("Adapter is invalid");
        }
        return mAdapter.get();
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
        if (getViewPager() == null) {
            mViewPager = (ViewPager) getView().findViewById(R.id.card_field_container_pager);
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
            if (!isValidCardNumber()) {
                try {
                    //come back last page
                    getViewPager().setCurrentItem(mLastPageSelected);
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
                    Log.e(this, e);
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return true;
    }

    private void updateLastPagePosition(int pPosition) {
        mLastPageSelected = pPosition;
    }

    /*
     * set cursor to the last position of edittext
     */
    private void moveCursorToLastPositionOnText(int pPosition) {
        try {
            CreditCardFragment currentFragment = mCardAdapter.getItemAtPosition(pPosition);

            if (currentFragment != null) {
                currentFragment.onSelectText();
            }

        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    private void clearHighLightOnCardView() {
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

    /*
     * prevent navigate if previous fragment have an error
     */
    private boolean preventNavigateIfHasError(int pPosition) {
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

    private boolean isValidCardCVV() {
        try {
            return TextUtils.isEmpty(getCardCVVView().getText().toString())
                    || getCardCVVView().isValidPattern()
                    && (getCardCVVView().getText().toString().length() == 3);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

        return true;
    }

    private boolean validateCardCVV() {
        try {
            boolean isValidCardCVV = isValidCardCVV();
            if (!isValidCardCVV) {
                getViewPager().setCurrentItem(mLastPageSelected);
                String errMes = getCardCVVView().getPatternErrorMessage();
                showHintError(getCardCVVView(), errMes);
            }
            return isValidCardCVV;
        } catch (Exception e) {
            Timber.w(e, "Exception validate card cvv");
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
                getViewPager().setCurrentItem(mLastPageSelected);
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
            isDetected = getCreditCardFinder().detected() ? getCreditCardFinder().detected() : getBankCardFinder().detected();
        }
        return !(isAllowValidateCardNumberByLuhn() && isDetected) || getCardFinder().validCardNumberLuhnFormula(getCardNumber());
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
            Timber.w(e, "Exception on focus view");
        }
    }

    public void useWebView(boolean pIsUseWebView) throws Exception {
        if (pIsUseWebView) {
            getView().visibleWebView(true);
            getView().visibleInputCardView(false);
            getView().visibleSubmitButton(false);
            if(CardType.PBIDV.equals(getDetectedBankCode())){
                registerKeyboardEventForBidv();
            }
        } else {
            getView().visibleWebView(false);
            getView().visibleBIDVAccountRegisterBtn(false);
            getView().visibleInputCardView(true);
            getView().visibleSubmitButton(true);
        }
    }

    private void registerKeyboardEventForBidv(){
        if(mRootView == null){
            return;
        }
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
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
                }
                else {
                    // keyboard is closed
                    getView().visibleBIDVAccountRegisterBtn(true);
                }
            }catch (Exception e){
                Timber.w(e);
            }
        });
    }

    private void showKeyBoardAndResizeButtonsIfNotSwitchChannel() throws Exception {
        if (!getAdapter().getPresenter().isSwitchAdapter()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        //dialog process is showing. so when process dialog close, need to show keyboard again.
                        if (DialogManager.showingLoadDialog()) {
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
            Timber.w(e, "Exception clear card number and show keyboard");
        }
        getViewPager().setCurrentItem(0);
        //auto show keyboard when the first time start
        new Handler().postDelayed(() -> {
            try {
                showKeyBoardAndResizeButtons();
            } catch (Exception e) {
                Timber.w(e.getMessage());
            }
        }, 500);
    }

    private void showKeyBoardAndResizeButtons() {
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

            //bidv card must paid by mapcard
            if (!paymentInfoHelper.isLinkTrans() && (getAdapter() instanceof BankCardWorkFlow)
                    && ((BankCardWorkFlow) getAdapter()).paymentBIDV()
                    && ((BankCardWorkFlow) getAdapter()).preventPaymentBidvCard(bankCode, getCardNumber())) {
                return;
            }

            //user input bank account
            if (!TextUtils.isEmpty(bankCode) && BankAccountHelper.isBankAccount(bankCode) && getAdapter() != null && getActivity() != null) {
                showWarningBankAccount();
            }

            //move to next page if detect a card
            if (getCardFinder().detected()) {
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

    void populateTextOnCardViewNoPaintCard() {
        if (getCardView() != null) {
            getCardView().setCardNumberNoPaintCard(getCardNumber());
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
                        PlayStoreUtils.openPlayStoreForUpdate(GlobalData.getMerchantActivity(), BuildConfig.PACKAGE_IN_PLAY_STORE, "Zalo Pay", "force-app-update", "bank-future");
                        try {
                            getAdapter().getPresenter().callback();
                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                });
    }

    private void showWarningBankAccount() throws Exception {
        ChannelFragment channelFragment = getView();
        if (channelFragment == null || getAdapter().getPaymentInfoHelper() == null) {
            return;
        }
        if (getAdapter().getPaymentInfoHelper().isLinkTrans()) {
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
                                        //callback bankcode to app , app will direct user to link bank account to right that bank
                                        BankAccount dBankAccount = new BankAccount();
                                        dBankAccount.bankcode = BankDetector.getInstance().getDetectBankCode();
                                        getAdapter().getPaymentInfoHelper().setMapBank(dBankAccount);
                                        getAdapter().getPresenter().setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINK_ACCOUNT);
                                    } catch (Exception e) {
                                        Timber.w(e, "Exception switch bank link");
                                    }
                                }
                            });
        } else if (!BankAccountHelper.hasBankAccountOnCache(getAdapter().getPaymentInfoHelper().getUserId(), CardType.PVCB)) {
            channelFragment.showConfirmDialog(mContext.getResources().getString(R.string.sdk_vcb_link_before_payment_warning_mess),
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
                                getAdapter().getPresenter().callbackLinkThenPay(Link_Then_Pay.VCB);
                            } catch (Exception e) {
                                Timber.w(e, "Exception callback then pay VCB");
                            }
                        }
                    });
        } else if (getAdapter().getPresenter() != null) {
            getAdapter().getPresenter().showMapBankDialog(false);
        }
    }

    /*
     * link card channel use this to update found card
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
            //user input bank account
            if (!TextUtils.isEmpty(pBankCode) && BankAccountHelper.isBankAccount(pBankCode)
                    && getAdapter() != null && getActivity() != null) {
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

    public String getDetectedBankCode() {
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

    /***
     * show/hide issue date
     * after detected a card type
     */
    void showViewByBankType() {
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
            Timber.d("mDotView is null");
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
        mDotView.addView(dot, params);
    }

    private void selectDot(int pIndex) {
        if (mDotView == null) {
            return;
        }
        final int childCount = mDotView.getChildCount();
        for (int i = 0; i < childCount; i++) {
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
        if (getViewPager() == null) {
            return;
        }
        try {
            getCardNumberView().setText(null);
            new Handler().postDelayed(() -> {
                try {
                    SdkUtils.focusAndSoftKeyboard(getActivity(), mCardAdapter.getItemAtPosition(0).getEditText());
                } catch (Exception e) {
                    Timber.w(e.getMessage());
                }
            }, 300);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    public void setCardInfo(String pCardNumber) {
        if (getViewPager() != null && !TextUtils.isEmpty(pCardNumber)) {
            try {
                SdkUtils.focusAndSoftKeyboard(getActivity(), mCardAdapter.getItemAtPosition(1).getEditText());
                applyFont(getView().findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.sdk_font_medium));
                getCardNumberView().setText(pCardNumber);
                getCardNumberView().formatText(true);
                //reset other
                getCardView().setCVV(null);
                getCardView().setCardHolderName(null);
                getCardView().setCardDate(null);
                new Handler().postDelayed(() -> getViewPager().setCurrentItem(1), 300);
            } catch (Exception e) {
                Timber.w(e, "Exception set card info");
            }
        }
    }

    @CallSuper
    public void dispose() {
        mCardAdapter = null;
        mBankSupportAdapter = null;
        mAdapter = null;
        mView = null;
        if (mWebView != null) {
            mWebView.getCCWebViewClient().dispose();
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
            getViewPager().setCurrentItem(errorFragmentIndex);
        }
    }

    public void bidvAutoFillOtp(String pOtp) {
        if (mWebView != null) {
            mWebView.getCCWebViewClient().BIDVWebFlowFillOtp(pOtp);
        }
    }

    public void stopWebview() {
        if (mWebView != null) {
            mWebView.stopLoading();
        }
    }

    public void reloadUrl() {
        if (mWebView == null) {
            try {
                initWebView();
            } catch (Exception e) {
                Timber.w(e);
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
            return getAdapter().existMapCardOnCache();
        } catch (Exception e) {
            Timber.w(e.getMessage());
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
        if (mCardAdapter == null) {
            return null;
        }
        return (VPaymentDrawableEditText) mCardAdapter.getCardNumberFragment().getEditText();
    }

    VPaymentDrawableEditText getCardNameView() throws Exception {
        return (VPaymentDrawableEditText) mCardAdapter.getCardNameFragment().getEditText();
    }

    private VPaymentDrawableEditText getCardCVVView() throws Exception {
        return (VPaymentDrawableEditText) mCardAdapter.getCardCVVFragment().getEditText();
    }

    protected VPaymentValidDateEditText getCardIssueView() throws Exception {
        return (VPaymentValidDateEditText) mCardAdapter.getCardIssueryFragment().getEditText();
    }

    protected VPaymentValidDateEditText getCardExpiryView() throws Exception {
        return (VPaymentValidDateEditText) mCardAdapter.getCardExpiryFragment().getEditText();
    }

    void showNext() {
        try {
            int max = mCardAdapter.getCount();
            int currentIndex = getViewPager().getCurrentItem();
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
                getViewPager().setCurrentItem(currentIndex + 1);
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

    private void showPrevious() {
        int currentIndex = getViewPager().getCurrentItem();
        if (currentIndex - 1 >= 0) {
            getViewPager().setCurrentItem(currentIndex - 1);
        }
        refreshNavigateButton();
    }

    boolean isInputBankMaintenance() {
        boolean isBankDetect = BankDetector.getInstance().detected();
        boolean isCCDetect = CreditCardDetector.getInstance().detected();
        if (isBankDetect && GlobalData.shouldUpdateBankFuncbyPayType()) {
            GlobalData.setCurrentBankFunction(BankFunctionCode.PAY_BY_CARD);
            if (BankDetector.getInstance().isBankAccount()) {
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

    private void showMaintenanceBank(String messaage) {
        try {
            getView()
                    .showInfoDialog(messaage,
                            mContext.getString(R.string.dialog_retry_input_card_button),
                            mBankMaintenanceDialogListener);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    private void showWarningDisablePmc(String pBankName) throws Exception {
        String mess = getAdapter().getPaymentInfoHelper().isLinkTrans() ?
                mContext.getResources().getString(R.string.sdk_warning_pmc_transtype_disable_link) :
                mContext.getResources().getString(R.string.sdk_warning_pmc_transtype_disable_payment);
        String disableBankMessage = String.format(mess, pBankName);
        try {
            getView().showInfoDialog(disableBankMessage,
                    mContext.getResources().getString(R.string.dialog_retry_input_card_button),
                    () -> new Handler().postDelayed(() -> {
                        //focus to edittext again after closing dialog
                        try {
                            getViewPager().setCurrentItem(0);
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
        BankListDialogFragment dialog = new BankListDialogFragment();
        dialog.setAdapter(mBankSupportAdapter);
        mBankSupportAdapter.insertItems(getListCardSupport(isATMChannel()));
        getAdapter().getPresenter();
        dialog.show(getActivity().getFragmentManager(), BankListDialogFragment.TAG);
    }

    private ArrayList<String> getListCardSupport(boolean isATMChannel) throws Exception {
        if (getAdapter().getPaymentInfoHelper() != null) {
            if (getAdapter().getPaymentInfoHelper().getTranstype() == TransactionType.LINK) {
                return CardSupportHelper.getLinkCardSupport();
            } else {
                return isATMChannel ? CardSupportHelper.getLocalBankSupport() : CardSupportHelper.getCardSupport();
            }
        }
        return null;
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
            CardNumberFragment cardNumberView = mCardAdapter.getCardNumberFragment();
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
            if (mCardAdapter.getCardNumberFragment().hasError() &&
                    (mCardAdapter.getCardNumberFragment().getError().equals(mContext.getResources().getString(R.string.sdk_card_not_support))
                            || mCardAdapter.getCardNumberFragment().getError().contains("không hỗ trợ"))) {
                return false;
            }
        } catch (Exception e) {
            Timber.w(e, "Exception check card support");
        }
        return true;
    }

    void clearHighLight() {
        getCardView().clearHighLightCardNumber();
        getCardView().clearHighLightCardHolderName();
        getCardView().clearHighLightCardDate();
        getCardView().clearHighLightCVV();
    }

    void updateCardInfoAfterTextChange(String pInfo) {
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
            Timber.w(e, "Exception get select bank identifier");
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
    void autoMoveToNextFragment() {
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

        if (getActivity().getCurrentFocus() instanceof VPaymentEditText) {
            VPaymentEditText currentFocusView = (VPaymentEditText) getCurrentFocusView();
            //card name input
            if (currentFocusView.getId() == R.id.edittext_localcard_name && !currentFocusView.isValidInput()) {
                showHintError(currentFocusView, mContext.getResources().getString(R.string.sdk_invalid_cardname_mess));
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
