package vn.com.zalopay.wallet.workflow.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import rx.Subscription;
import timber.log.Timber;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.card.AbstractCardDetector;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelFragment;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.adapter.CreditCardFragmentAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardCVVFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.CreditCardWorkFlow;

public class CreditCardGuiProcessor extends CardGuiProcessor {
    public CreditCardGuiProcessor(Context pContext, CreditCardWorkFlow pAdapterCreditCard, ChannelFragment pChannelFragment) {
        super(pContext);
        init(pAdapterCreditCard, pChannelFragment);
    }

    @Override
    protected void init(AbstractWorkFlow pAdapter, ChannelFragment pChannelFragment) {
        super.init(pAdapter, pChannelFragment);
        checkAutoMoveCardNumberFromBundle = false;
    }

    @Override
    protected void flipCardView(int pPosition) {
        //flip card side
        if (pPosition == 2) {
            getCardView().showBack();
        } else if ((pPosition == 1 && mLastPageSelected == 2) || (pPosition == 3 && mLastPageSelected != 0 && mLastPageSelected != 1)) {
            getCardView().showFront();
        }
    }

    @Override
    public void setCardDateOnCardView() {
        // Set hint card expiry here
    }

    public void continueDetectCardForLinkCard() {
        try {
            Subscription subscription = getBankCardFinder().detectOnAsync(getCardNumber())
                    .compose(SchedulerHelper.applySchedulers())
                    .subscribe(detected -> {
                        try {
                            getAdapter().setNeedToSwitchChannel(detected);
                        } catch (Exception e) {
                            Timber.w(e.getMessage());
                        }
                        populateTextOnCardView();
                        if (detected) {
                            setDetectedCard(getBankCardFinder().getBankName(), getBankCardFinder().getDetectBankCode());
                            checkAutoMoveCardNumberFromBundle = true;
                        } else {
                            setDetectedCard();
                        }
                    }, Timber::d);
            getAdapter().getPresenter().addSubscription(subscription);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    @Override
    public AbstractCardDetector getCardFinder() {
        return getCreditCardFinder();
    }

    @Override
    protected boolean isATMChannel() {
        return false;
    }

    @Override
    protected void actionAfterFinishInputCard() {
        try {
            getAdapter().onClickSubmission();
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    @Override
    protected boolean validateCardNumberLength() {
        try {
            return getAdapter().getPaymentInfoHelper().isLinkTrans()
                    && getBankCardFinder().detected()
                    || getCreditCardFinder().isValidCardLength();
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean isAllowValidateCardNumberByLuhn() {
        return PaymentPermission.allowLuhnCC();
    }

    @Override
    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        try {
            return new CreditCardFragmentAdapter(getActivity().getSupportFragmentManager(), getActivity().getIntent().getExtras());
        } catch (Exception e) {
            Timber.w(e);
        }
        return null;
    }

    @Override
    protected int validateInputCard() {
        int errorFragmentIndex = mCardAdapter.hasError();
        if (errorFragmentIndex > -1)
            return errorFragmentIndex;
        if (!getCardFinder().isValidCardLength() || !getCardFinder().detected()) {
            try {
                return mCardAdapter.getIndexOfFragment(CardNumberFragment.class.getName());
            } catch (Exception e) {
                Log.e(this, e);
            }
            return 0;
        }
        if (TextUtils.isEmpty(getCardExpire())) {
            try {
                return mCardAdapter.getIndexOfFragment(CardExpiryFragment.class.getName());
            } catch (Exception e) {
                Log.e(this, e);
            }
            return 1;
        }

        if (TextUtils.isEmpty(getCardCVV()) || getCardCVV().length() < 3) {
            try {
                return mCardAdapter.getIndexOfFragment(CardCVVFragment.class.getName());
            } catch (Exception e) {
                Log.e(this, e);
            }
            return 2;
        }

        if (TextUtils.isEmpty(getCardName()) || getCardName().length() <= 3) {
            try {
                return mCardAdapter.getIndexOfFragment(CardNameFragment.class.getName());
            } catch (Exception e) {
                Log.e(this, e);
            }
            return 3;
        }
        return errorFragmentIndex;
    }

    @Override
    protected void populateBankCode() {
        try {
            getAdapter().getCard().setBankcode(BuildConfig.CC_CODE);
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }

    @Override
    public boolean needToWarningNotSupportCard() {
        return needToWarningNotSupportCard && (getCardNumber().length() >= Constants.MIN_CC_LENGTH);
    }

    @Override
    public VPaymentValidDateEditText getCardDateView() throws Exception {
        return getCardExpiryView();
    }

    @Override
    protected boolean checkValidRequiredEditText(EditText pView) {
        if (pView.getVisibility() != View.VISIBLE) {
            return true;
        }

        boolean isCheckPattern = !(pView instanceof VPaymentDrawableEditText || pView instanceof VPaymentValidDateEditText) || ((VPaymentEditText) pView).isValid();

        return isCheckPattern && (pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText().toString()));
    }

    @Override
    public boolean checkEnableSubmitButton() {
        try {
            if (isValidCardNumber()
                    && !TextUtils.isEmpty(getCardCVV())
                    && !TextUtils.isEmpty(getCardExpire())) {
                getView().enablePaymentButton();
                getView().changeBgPaymentButton(getAdapter().isFinalStep());
                return true;
            } else {
                getView().disablePaymentButton();
                return false;
            }
        } catch (Exception e) {
            Timber.w(e, "Exception check enable submit");
        }
        return false;
    }

    @Override
    protected boolean canSwitchChannelLinkCard() {
        try {
            return PaymentPermission.allowLinkAtm();
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        return false;
    }

    @Override
    protected boolean isOwnChannel() {
        try {
            return getAdapter().isCCFlow();
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
        return false;
    }

    @Override
    protected void switchChannel() {
        try {
            Timber.d("start switch to atm adapter");
            getAdapter().getPresenter().switchCardLinkAdapter(BuildConfig.channel_atm, getCardNumber());
        } catch (Exception e) {
            Timber.w(e, "Exception switch atm adapter");
        }
    }

    /*
     * switch to atm input if in linkcard
     */
    @Override
    public void checkForSwitchChannel() {
        try {
            super.checkForSwitchChannel();
            checkAutoMoveCardNumberFromBundle = true;
        } catch (Exception e) {
            Timber.w(e.getMessage());
        }
    }
}
