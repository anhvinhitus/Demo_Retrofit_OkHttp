package vn.com.zalopay.wallet.business.channel.base;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
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

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.AdapterBankCard;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.channel.pager.interfaces.IPageChangeListener;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.base.DPaymentCard;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardFlowType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.staticconfig.DCardIdentifier;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.business.webview.base.PaymentWebView;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ZPWOnCloseDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnDetectCardListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.PaymentUtils;
import vn.com.zalopay.wallet.utils.ViewUtils;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.adapter.CardSupportAdapter;
import vn.com.zalopay.wallet.view.component.activity.BankListActivity;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.cardview.CreditCardView;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardIssueFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CreditCardFragment;
import vn.com.zalopay.wallet.view.custom.overscroll.OverScrollDecoratorHelper;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/***
 * card processor class
 */
public abstract class CardGuiProcessor extends SingletonBase implements IPageChangeListener {
    public final String VERTICAL_SEPERATOR = " ";
    protected WeakReference<AdapterBase> mAdapter;
    protected CardSupportAdapter cardSupportGridViewAdapter;
    protected ScrollView mScrollViewRoot;
    protected View mLayoutSwitch;
    protected int mLengthBeforeChange;
    protected View mCurrentFocusView;
    protected PaymentWebView mWebView;
    protected boolean mUseOtpToken = false;
    protected boolean mNeedToShowKeyBoardWhenCloseProcessDialog = false;
    /***
     * card view
     ***/
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
    protected View.OnClickListener mNextButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getViewPager() == null) {
                Log.d(this, "===mNextButtonClick mViewAdapter=null===");
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
    protected View.OnClickListener mPreviousButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getViewPager() == null) {
                Log.d(this, "===mPreviousButtonClick mViewAdapter=null===");
                return;
            }

            showPrevious();
        }
    };
    /***
     * user tap on done on keyboard
     */
    protected TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
        }
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

                if (!isCardInputSupport() && mLastLengthCardNumber < newValue.length()) {
                    getCardNumberView().setText(lastValue);

                    getCardNumberView().setSelection(mLastLengthCardNumber);

                    return;
                }

                lastValue = newValue;
                mLastLengthCardNumber = lastValue.length();
                //

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
        protected boolean isValidateOK = false;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (getAdapter().getActivity() != null
                    && getAdapter().getActivity().getCurrentFocus() != null
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

            if (isInputValidWithWhiteSpace && isNeedValidateOnTextChange(getCurrentFocusView())) {
                isValidateOK = validateInputOnTextChange();
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
                Log.d(this, "!isInputValidWithWhiteSpace");
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
    protected View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                moveScrollViewToCurrentFocusView();
            }
            return false;
        }
    };
    protected ZPWOnCloseDialogListener mCloseDialogListener = new ZPWOnCloseDialogListener() {
        @Override
        public void onCloseCardSupportDialog() {
            clearCardNumberAndShowKeyBoard();
        }
    };
    /***
     * click on delete icon / bank support icon
     */
    protected View.OnClickListener mOnQuestionIconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isCardInputSupport()) {
                showSupportCardList();
            }
        }
    };
    /***
     * detect card type listener
     */
    protected ZPWOnDetectCardListener mOnDetectCardListener = new ZPWOnDetectCardListener() {
        @Override
        public void onDetectCardComplete(boolean isDetected) {

            Log.d(this, "card number=" + getCardNumber() + " detected=" + isDetected);

            if (GlobalData.isMapCardChannel() || GlobalData.isMapBankAccountChannel()) {
                return;
            }

            if (TextUtils.isEmpty(getCardNumber()))
                getAdapter().setNeedToSwitchChannel(false);

            if (!getAdapter().isNeedToSwitchChannel()) {
                //workout prevent flicker when switch atm and cc
                if (!isDetected && GlobalData.isLinkCardChannel()) {
                    needToWarningNotSupportCard = false;

                    Log.d(this, "needToWarningNotSupportCard=false");
                }

                setDetectedCard();

                populateTextOnCardView();

                //render view by bank type
                if (isDetected && getAdapter().isATMFlow()) {
                    showViewByBankType();
                }
            }
            //continue detect if haven't detected card type yet
            if (!isDetected && GlobalData.isLinkCardChannel()) {
                needToWarningNotSupportCard = true;
                Log.d(this, "needToWarningNotSupportCard=true");
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

    protected abstract void setWebViewUserAgent();

    public abstract CardCheck getCardFinder();

    public abstract boolean isAllowValidateCardNumberByLuhn();

    public abstract void continueDetectCardForLinkCard();

    public abstract void setCardDateOnCardView();

    public abstract VPaymentValidDateEditText getCardDateView() throws Exception;

    /***
     * action after user input full of info card
     */
    protected abstract void actionAfterFinishInputCard();

    /***
     * validate card info
     *
     * @return index of error fragment
     */
    protected abstract int validateInputCard();

    protected abstract boolean validateCardNumberLength();

    protected abstract CardFragmentBaseAdapter onCreateCardFragmentAdapter();

    protected void init() throws Exception {
        if (GlobalData.isChannelHasInputCard()) {
            initForInputCard();
        } else {
            initForMapCardAndZaloPay();
        }

        getAdapter().setECardFlowType(ECardFlowType.API);

        getAdapter().showFee();

        getCardFinder();

        setCardDateOnCardView();

        initMutualView();

        setMinHeightSwitchCardButton();
    }

    protected void initForInputCard() throws Exception {
        mCardView = (CreditCardView) getAdapter().getActivity().findViewById(R.id.credit_card_view);

        if (getCardView() != null) {
            getCardView().initCardSelector();

            getCardView().setOnClickOnCardView(this);
        }

        mDotView = (LinearLayout) getAdapter().getActivity().findViewById(R.id.dotView);
        mButtonPre = (Button) getAdapter().getActivity().findViewById(R.id.previous);
        mButtonNext = (Button) getAdapter().getActivity().findViewById(R.id.next);

        getAdapter().getActivity().visibleCardViewNavigateButton(true);
        getAdapter().getActivity().visibleSubmitButton(false);
        getAdapter().getActivity().visibleAppInfo(false);
        getAdapter().getActivity().visibleTranferWalletInfo(false);

        getAdapter().getActivity().visibleCardInfo(true);

        if (mButtonNext != null && mButtonPre != null) {
            mButtonNext.setOnClickListener(mNextButtonClick);
            mButtonPre.setOnClickListener(mPreviousButtonClick);
        }
    }

    protected void initForMapCardAndZaloPay() {

    }

    protected void initMutualView() {
        mLayoutSwitch = getAdapter().getActivity().findViewById(R.id.zpw_switch_card_button);

        mScrollViewRoot = (ScrollView) getAdapter().getActivity().findViewById(R.id.zpw_scrollview_container);
        if (mScrollViewRoot != null)
            OverScrollDecoratorHelper.setUpOverScroll(mScrollViewRoot);
    }

    protected void initWebView() {
        mWebView = (PaymentWebView) getAdapter().getActivity().findViewById(R.id.zpw_threesecurity_webview);

        if (mWebView != null) {
            mWebView.setPaymentWebViewClient(getAdapter());

            setWebViewUserAgent();
        }
    }

    protected void flipCardView(int pPosition) {

    }

    public void checkForSwitchChannel() {
        if (getAdapter().needToSwitchChannel() && isOwnChannel()) {
            Log.d(this, "===checkForSwitchChannel===");

            getAdapter().resetNeedToSwitchChannel();

            if (canSwitchChannelLinkCard()) {
                switchChannel();
            } else {
                getViewPager().setCurrentItem(0);
                getAdapter().getActivity().showDialogWarningLinkCardAndResetCardNumber();
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
        Log.d(this, "===init pager===");
        if (getViewPager() == null) {
            mViewPager = (ViewPager) getAdapter().getActivity().findViewById(R.id.card_field_container_pager);
        }

        if (getViewPager() == null) {
            throw new Exception("mViewPager is null");
        }

        getViewPager().addOnPageChangeListener(this);

        mCardAdapter = onCreateCardFragmentAdapter();

        if (mCardAdapter == null) {
            throw new Exception("mCardAdapter is null");
        }

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

    protected boolean validateCardNumber() {
        try {
            if (!isValidCardNumber()) {
                try {
                    //come back last page
                    getViewPager().setCurrentItem(mLastPageSelected);

                    //set message to edittext
                    String errMes = getCardNumberView().getPatternErrorMessage();

                    if (TextUtils.isEmpty(getCardNumber())) {
                        errMes = GlobalData.getStringResource(RS.string.zpw_missing_card_number);
                    } else if (!validateCardNumberLength()) {
                        errMes = getCardNumberView().getPatternErrorMessage();
                    } else if (preventNextIfLinkCardExisted()) {
                        errMes = GlobalData.getStringResource(RS.string.zpw_link_card_existed);
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
            if (TextUtils.isEmpty(getCardCVVView().getText().toString())) {
                return true;
            }

            return getCardCVVView().isValidPattern() && (getCardCVVView().getText().toString().length() == 3);
        } catch (Exception e) {
            Log.d(this, e);
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
            Log.d(this, e);
        }

        return true;
    }

    protected boolean isValidCardDate() {
        try {
            if (TextUtils.isEmpty(getCardDateView().getText().toString())) {
                return true;
            }

            return getCardDateView().isValidPattern() && (getCardDateView().getText().toString().length() == 5);
        } catch (Exception e) {
            Log.d(this, e);
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
            Log.d(this, e);
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

        if (GlobalData.isLinkCardChannel() && !isDetected) {
            isDetected = getCreditCardFinder().isDetected() ? getCreditCardFinder().isDetected() : getBankCardFinder().isDetected();
        }

        if (isAllowValidateCardNumberByLuhn() && isDetected) {
            return getCardFinder().isValidCardLuhn(getCardNumber());
        }

        return true;
    }

    /***
     * show keyboard and focus on current view
     */
    public void onFocusView() {
        mCurrentFocusView = getAdapter().getActivity().getCurrentFocus();

        if (mCurrentFocusView != null && mCurrentFocusView instanceof EditText) {
            ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), (EditText) mCurrentFocusView);

            Log.d(this, "===focusAndSoftKeyboard===" + mCurrentFocusView.toString());
        }
    }

    public void useWebView(boolean pIsUseWebView) {
        if (pIsUseWebView) {
            getAdapter().getActivity().visibleWebView(true);
            getAdapter().getActivity().visibleInputCardView(false);
            getAdapter().getActivity().visibleSubmitButton(false);
        } else {
            getAdapter().getActivity().visibleWebView(false);
            getAdapter().getActivity().visibleInputCardView(true);
            getAdapter().getActivity().visibleSubmitButton(true);
        }
    }

    public boolean isNeedToShowKeyBoardWhenCloseProcessDialog() {
        return mNeedToShowKeyBoardWhenCloseProcessDialog;
    }

    public void showKeyBoardAndResizeButtonsIfNotSwitchChannel() {
        if (!getAdapter().getActivity().isSwitching()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        //dialog process is showing. so when process dialog close, need to show keyboard again.
                        if (DialogManager.isShowingProgressDialog()) {
                            mNeedToShowKeyBoardWhenCloseProcessDialog = true;
                            Log.d(this, "showKeyBoardAndResizeButtonsIfNotSwitchChannel.isShowingProgressDialog");
                            return;
                        }

                        showKeyBoardOnCardNumberView();
                        moveScrollViewToCurrentFocusView();
                        getAdapter().getActivity().setIsSwitching(false);
                        mNeedToShowKeyBoardWhenCloseProcessDialog = false;

                        Log.d(this, "showKeyBoardAndResizeButtonsIfNotSwitchChannel");

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

        Log.d(this, "showKeyBoardAndResizeButtons");
    }

    public void setDetectedCard() {

        try {
            if (isInputBankMaintenance()) {
                return;
            }

            String bankName = getDetectedBankName();
            String bankCode = getDetectedBankCode();

            //bidv card must paid by mapcard
            if(!GlobalData.isLinkCardChannel() && (getAdapter() instanceof AdapterBankCard)
                    && ((AdapterBankCard) getAdapter()).isBidvBankPayment()
                    && ((AdapterBankCard) getAdapter()).preventPaymentBidvCard(getCardNumber()))
            {
                return;
            }

            setCardNumberHint(bankName);

            if (!TextUtils.isEmpty(bankCode)) {
                getCardView().switchCardDateHintByBankCode(bankCode);
            }

            //user input bank account
            if (BankAccountHelper.isBankAccount(bankCode) && validateUserLevelBankAccount() && getAdapter() != null && getAdapter().getActivity() != null) {
                showWarningBankAccount();
            }
        } catch (Exception e) {
            Log.e(this, e);
        }

        //move to next page if detect a card
        if (getCardFinder().isDetected()) {
            autoMoveToNextFragment();
        }
    }

    protected void populateTextOnCardView() {
        getCardView().setCardNumber(getCardNumber());
    }

    protected void populateTextOnCardViewNoPaintCard() {
        getCardView().setCardNumberNoPaintCard(getCardNumber());
    }

    protected boolean validateUserLevelBankAccount() {
        boolean userLevelValid = true;
        try {
            if (GlobalData.getLevel() < Integer.parseInt(GlobalData.getStringResource(RS.string.zpw_string_level_allow_bankaccount))) {
                userLevelValid = false;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }

        if (!userLevelValid && getAdapter() != null && getAdapter().getActivity() != null) {
            String pMessage = GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update_and_linkaccount_before_payment);
            if (BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
                pMessage = GlobalData.getStringResource(RS.string.zpw_string_alert_profilelevel_update_and_before_payby_bankaccount);
            }
            getAdapter().getActivity().confirmUpgradeLevelIfUserInputBankAccount(pMessage, new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                    clearCardNumberAndShowKeyBoard();
                }

                @Override
                public void onOKevent() {
                    if (BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
                        GlobalData.setResultUpgrade();
                    } else {
                        GlobalData.setResultUpLevelLinkAccountAndPayment();
                    }
                    getAdapter().getActivity().recycleActivity();
                }
            });
        }

        return userLevelValid;
    }

    protected void showWarningBankAccount() {
        if (GlobalData.isLinkCardChannel()) {
            getAdapter().getActivity().showConfirmDialog(new ZPWOnEventConfirmDialogListener() {
                                                             @Override
                                                             public void onCancelEvent() {
                                                                 clearCardNumberAndShowKeyBoard();
                                                             }

                                                             @Override
                                                             public void onOKevent() {
                                                                 //callback bankcode to app , app will direct user to link bank account to right that bank
                                                                 DBankAccount dBankAccount = new DBankAccount();
                                                                 dBankAccount.bankcode = BankCardCheck.getInstance().getDetectBankCode();
                                                                 GlobalData.getPaymentInfo().mapBank = dBankAccount;

                                                                 GlobalData.setResultNeedToLinkAccount();
                                                                 getAdapter().getActivity().recycleActivity();
                                                             }
                                                         }, GlobalData.getStringResource(RS.string.zpw_warning_vietcombank_linkbankaccount_not_linkcard),
                    GlobalData.getStringResource(RS.string.dialog_linkaccount_button), GlobalData.getStringResource(RS.string.dialog_retry_input_card_button));
        } else if (!BankAccountHelper.hasBankAccountOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
            getAdapter().getActivity().showConfirmDialog(new ZPWOnEventConfirmDialogListener() {
                                                             @Override
                                                             public void onCancelEvent() {
                                                                 clearCardNumberAndShowKeyBoard();
                                                             }

                                                             @Override
                                                             public void onOKevent() {
                                                                 //callback bankcode to app , app will direct user to link bank account to right that bank
                                                                 DBankAccount dBankAccount = new DBankAccount();
                                                                 dBankAccount.bankcode = BankCardCheck.getInstance().getDetectBankCode();
                                                                 GlobalData.getPaymentInfo().mapBank = dBankAccount;

                                                                 GlobalData.setResultNeedToLinkAccountBeforePayment();
                                                                 getAdapter().getActivity().recycleActivity();
                                                             }
                                                         }, GlobalData.getStringResource(RS.string.zpw_warning_vietcombank_linkcard_before_payment),
                    GlobalData.getStringResource(RS.string.dialog_linkaccount_button), GlobalData.getStringResource(RS.string.dialog_retry_input_card_button));

        } else if (getAdapter() != null && getAdapter().getActivity() != null) {
            getAdapter().getActivity().showSelectionBankAccountDialog();
        }
    }

    /***
     * link card channel use this to update found card
     *
     * @param pBankName
     */
    public void setDetectedCard(String pBankName, String pBankCode) {
        try {
            setCardNumberHint(pBankName);

            if (!TextUtils.isEmpty(pBankCode)) {
                getCardView().switchCardDateHintByBankCode(pBankCode);
            }

            //user input bank account
            if (BankAccountHelper.isBankAccount(pBankCode) && getAdapter() != null && getAdapter().getActivity() != null) {
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
        return getCardFinder().getDetectedBankName();
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

    public void resizeCardView(int decreaseSize) {
        if (getCardView() != null && decreaseSize > 0) {
            float percentWitdh = getCardView().getPercentWitdh();

            if (percentWitdh < 0) {
                percentWitdh = Float.parseFloat(GlobalData.getStringResource(RS.string.percent_ondefault));

                if (ZPWUtils.isTablet(GlobalData.getAppContext()))
                    percentWitdh = Float.parseFloat(GlobalData.getStringResource(RS.string.percent_ontablet));

            }

            int offset = (int) GlobalData.getAppContext().getResources().getDimension(R.dimen.min_button_offset);

            decreaseSize += offset;

            float percentDecrease = (float) decreaseSize / getCardView().getWidth();

            Log.d(this, "===resizeCardView==percentDecrease=" + percentDecrease + "===mCardView.getWidth()=" + getCardView().getWidth());

            getCardView().resize(percentWitdh - percentDecrease);

            getCardView().requestLayout();

            View buttonWrapper = getAdapter().getActivity().findViewById(R.id.zpw_switch_card_button);

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
            if (!TextUtils.isEmpty(bank.otptype) &&
                    bank.otptype.contains(GlobalData.getStringResource(RS.string.sms_option))
                    && bank.otptype.contains(GlobalData.getStringResource(RS.string.token_option))) {
                mUseOtpToken = true;
            } else {
                mUseOtpToken = false;
            }

            if (bank != null) {

                switch (bank.banktype) {
                    case 1:
                        //password

                        break;
                    case 2:
                        //issue day
                        getCardView().visibleCardDate();
                        mCardAdapter.addIssueDateFragment();
                        mCardAdapter.notifyDataSetChanged();

                        updateDots();

                        break;
                    case 4:
                        //expire day
                        getCardView().visibleCardDate();
                        mCardAdapter.addExpireDateFragment();

                        mCardAdapter.notifyDataSetChanged();

                        updateDots();

                        break;

                    default:
                        getCardView().hideCardDate();
                        mCardAdapter.removeFragment(CardIssueFragment.class.getName());
                        mCardAdapter.removeFragment(CardExpiryFragment.class.getName());

                        mCardAdapter.notifyDataSetChanged();

                        updateDots();
                        //none
                        break;
                }
            }
        }
    }

    public void updateDots() {
        if (mDotView == null) {
            Log.d(this, "===mDotView=null===");

            return;
        }

        if (mDotView.getChildCount() == mCardAdapter.getCount()) {
            Log.d(this, "===updateDots===no update dot again");

            return;
        }

        mDotView.removeAllViews();

        for (int i = 0; i < mCardAdapter.getCount(); i++) {
            addDot();
        }

        selectDot(mLastPageSelected);
    }

    public void addDot() {
        ImageView dot = new ImageView(getAdapter().getActivity());

        dot.setImageResource(R.drawable.dot);

        int dotSize = (int) GlobalData.getAppContext().getResources().getDimension(R.dimen.dot_size);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);

        params.setMargins((int) (dotSize), 0, (int) (dotSize), 0);

        mDotView.addView(dot, params);
    }

    public void selectDot(int pIndex) {
        Log.d(this, "===selectDot=" + pIndex);

        if (mDotView == null) {
            Log.d(this, "===mDotView=null===");
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
                            ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), mCardAdapter.getItemAtPosition(0).getEditText());
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
                ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), mCardAdapter.getItemAtPosition(1).getEditText());

                ZPWUtils.applyFont(getAdapter().getActivity().findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium));

                getCardNumberView().setText(pCardNumber);
                getCardNumberView().formatText(true);

                //reset other
                getCardView().setCVV(null);
                getCardView().setCardHolderName(null);
                getCardView().setCardDate(null);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getViewPager().setCurrentItem(1);
                    }
                }, 300);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    public void dispose() {
        mCardAdapter = null;
        cardSupportGridViewAdapter = null;

        if (mWebView != null) {
            mWebView.release();
        }
    }

    /***
     * user go to the last fragment and tap next
     */
    protected void onDoneTapped() {
        int errorFragmentIndex = validateInputCard();

        Log.d(this, "===onDoneTapped===errorFragmentIndex=" + errorFragmentIndex);

        //there're no error
        if (errorFragmentIndex == -1) {
            clearHighLight();

            //just start submit if intener is online
            if (getAdapter().checkNetworkingAndShowRequest()) {
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

    public void reloadUrl() {
        if (mWebView == null) {
            initWebView();
            Log.d(this, "===reloadUrl===mWebView == null");
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
            return getAdapter().isExistedCardNumberOnCache();
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
        if (currentIndex == 0 && preventNextIfLinkCardExisted() && GlobalData.isLinkCardChannel()) {
            try {
                showHintError(getCardNumberView(), GlobalData.getStringResource(RS.string.zpw_link_card_existed));

                return;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }

        //validate card number before move to next page
        if (currentIndex == 0 && !validateCardNumberLuhn()) {
            try {
                showHintError(getCardNumberView(), GlobalData.getStringResource(RS.string.zpw_string_card_error_luhn));

                Log.d(this, "===showNext===validateCardNumberLuhn fail before move to next page");

                return;

            } catch (Exception e) {
                Log.e(this, e);
            }
        }

        if (currentIndex + 1 < max) {
            getViewPager().setCurrentItem(currentIndex + 1);
        } else {
            // completed the card entry.
            ZPWUtils.hideSoftKeyboard(GlobalData.getAppContext(), getAdapter().getActivity());
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

        Log.d(this, "===refreshNavigateButton===currentIndex=" + currentIndex + "===max=" + max);

        if (currentIndex == 0) {
            disablePrevious();

            Log.d(this, "===refreshNavigateButton===mButtonPre=false");
        } else if ((max - currentIndex == 1) && mCardAdapter.hasError() > -1) {
            disableNext();
            Log.d(this, "===refreshNavigateButton===mButtonNext=false");
        } else {
            enablePrevious();
            enableNext();

            Log.d(this, "===refreshNavigateButton===true===true");
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
        if (isBankDetect && GlobalData.getCurrentBankFunction() == EBankFunction.PAY) {
            GlobalData.setCurrentBankFunction(EBankFunction.PAY_BY_CARD);
            if (BankCardCheck.getInstance().isBankAccount()) {
                GlobalData.setCurrentBankFunction(EBankFunction.PAY_BY_BANK_ACCOUNT);
            }
        }

        if ((isBankDetect && BankLoader.getInstance().isBankMaintenance(getBankCardFinder().getDetectBankCode()))) {
            showMaintenanceBank(null);
            return true;
        }
        //detect is cc maintenance?
        if (CreditCardCheck.getInstance().isDetected() && BankLoader.getInstance().isBankMaintenance(getCreditCardFinder().getDetectBankCode())) {
            if (getAdapter().isATMFlow()) {
                showMaintenanceBank(getCreditCardFinder().getDetectBankCode());
            } else {
                showMaintenanceBank(null);
            }
            return true;
        }

        return false;
    }

    protected void showMaintenanceBank(String pBankCode) {
        getAdapter().getActivity().showBankMaintenance(new ZPWOnEventDialogListener() {
            @Override
            public void onOKevent() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //focus to edittext again after closing dialog
                        try {
                            getViewPager().setCurrentItem(0);
                            getCardNumberView().setText(null);
                            ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());

                        } catch (Exception e) {
                            Log.e(this, e);
                        }
                    }
                }, 400);
            }
        }, !TextUtils.isEmpty(pBankCode) ? pBankCode : getDetectedBankCode());
    }

    protected void showSupportCardList() {
        if (cardSupportGridViewAdapter == null)
            cardSupportGridViewAdapter = CardSupportAdapter.createAdapterProxy(isATMChannel());

        BankListActivity.setAdapter(cardSupportGridViewAdapter);
        BankListActivity.setCloseDialogListener(getCloseDialogListener());

        Intent intentBankList = new Intent(getAdapter().getActivity(), BankListActivity.class);
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

    public ZPWOnDetectCardListener getOnDetectCardListener() {
        return mOnDetectCardListener;
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollViewRoot.fullScroll(View.FOCUS_DOWN);
            }
        }, 300);
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
            Log.d(this, "===setCardNumberHint===cardNumberView=NULL");
            return;
        }

        //card not support
        if (TextUtils.isEmpty(pMessageHint) && needToWarningNotSupportCard()) {
            if (!isCardInputSupport()) {
                return;
            }

            cardNumberView.setError(GlobalData.getStringResource(RS.string.zpw_string_card_not_support));

            cardNumberView.showQuestionIcon();

            disableNext();

            Log.d("setCardNumberHint", "===setError===pMessageHint===" + pMessageHint + "===needToWarningNotSupportCard===" + needToWarningNotSupportCard());
        }
        //clear error hint
        else if (TextUtils.isEmpty(pMessageHint)) {
            cardNumberView.clearError();

            enableNext();

            Log.d("setCardNumberHint", "===clearError===");
        }
        //has an error
        else {
            cardNumberView.setHint(pMessageHint);

            enableNext();

            Log.d("setCardNumberHint", "===setHint===pMessageHint===" + pMessageHint);
        }
    }

    public void showHintError(EditText pEdittext, String pError) {
        Log.d(this, "===showHintError===" + pEdittext + "===" + pError);

        ViewUtils.setTextInputLayoutHintError(pEdittext, pError, GlobalData.getAppContext());

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
        Log.d(this, "===clearHintError===" + pEdittext + "===");
        ViewUtils.setTextInputLayoutHint(pEdittext, null, GlobalData.getAppContext());

        enableNext();
        enablePrevious();
    }

    public void showKeyBoardOnCardNumberView() {
        try {
            ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), getCardNumberView());
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void showKeyBoardOnEditText(final EditText pEditText) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {

                    ZPWUtils.focusAndSoftKeyboard(getAdapter().getActivity(), pEditText);
                    moveScrollViewToCurrentFocusView();

                } catch (Exception e) {
                    Log.e(this, e);
                }
            }
        }, 300);
    }

    protected boolean isCardInputSupport() {
        try {
            if (mCardAdapter.getCardNumberFragment().hasError() && mCardAdapter.getCardNumberFragment().getError().equals(GlobalData.getStringResource(RS.string.zpw_string_card_not_support))) {
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
            Log.d(this, "===updateCardInfoAfterTextChange===getCurrentFocusView()=NULL");
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
        DCardIdentifier cardIdentifier = null;

        if (GlobalData.isLinkCardChannel()) {
            cardIdentifier = getBankCardFinder().getCardIdentifier();

            Log.d(this, "===cardIdentifier = getBankCardFinder().getCardIdentifier()");

            if (cardIdentifier == null) {
                cardIdentifier = getCreditCardFinder().getCardIdentifier();
                Log.d(this, "getCreditCardFinder().getCardIdentifier()");
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

                Log.d(this, "===autoMoveToNextFragment===checkAutoMoveCardNumberFromBundle=false");

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
        mCurrentFocusView = getAdapter().getActivity().getCurrentFocus();

        if (getCurrentFocusView() != null && getCurrentFocusView() instanceof VPaymentEditText) {
            mLengthBeforeChange = ((VPaymentEditText) getCurrentFocusView()).getLength();
        }
    }

    protected boolean isNeedValidateOnTextChange(View view) {
        if (view != null && (view.getId() == R.id.zpsdk_otp_ctl
                || view.getId() == R.id.zpsdk_captchar_ctl
                || view.getId() == R.id.edittext_otp
                || view.getId() == R.id.edittext_token))
            return false;
        return true;
    }

    protected boolean validateInputOnTextChange() {
        if (getAdapter() == null || getAdapter().getActivity() == null) {
            Log.d("validateInputOnTextChange", "getAdapter() == null || getAdapter().getActivity() == null");
            return true;
        }

        if (getAdapter().getActivity().getCurrentFocus() instanceof VPaymentEditText) {
            VPaymentEditText currentFocusView = (VPaymentEditText) getCurrentFocusView();

            Log.d(this, "===validateInputOnTextChange===currentFocusView.text=" + currentFocusView.getText().toString());

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

                return isInputValidWithWhiteSpace;

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
        return isInputValidWithWhiteSpace;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {

        Log.d(this, "===onPageSelected===" + position);

        if (mCardAdapter == null) {
            Log.d(this, "===mCardAdapter= null");

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
