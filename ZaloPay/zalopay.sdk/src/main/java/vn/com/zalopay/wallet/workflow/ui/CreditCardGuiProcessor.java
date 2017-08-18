package vn.com.zalopay.wallet.workflow.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import rx.Subscription;
import timber.log.Timber;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.card.AbstractCardDetector;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.entity.bank.PaymentCard;
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
        checkValidCardNumberFromBundle = false;
    }

    @Override
    protected void flipCardView(int pPosition) {
        if (mCardView == null) {
            return;
        }
        //flip card side
        if (pPosition == 2) {
            mCardView.showBack();
        } else if ((pPosition == 1
                && mLastPageSelected == 2)
                || (pPosition == 3
                && mLastPageSelected != 0
                && mLastPageSelected != 1)) {
            mCardView.showFront();
        }
    }

    @Override
    public void continueDetectCardForLinkCard() throws Exception {
        Subscription subscription = getBankCardFinder().detectOnAsync(getCardNumber())
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(this::onDetectCardForLinkComplete, Timber::d);
        AbstractWorkFlow workFlow = getAdapter();
        if (workFlow != null) {
            workFlow.mCompositeSubscription.add(subscription);
        }
    }

    private void onDetectCardForLinkComplete(boolean detected) {
        try {
            AbstractWorkFlow workFlow = getAdapter();
            if (workFlow == null) {
                return;
            }
            workFlow.setNeedToSwitchChannel(detected);
            populateTextOnCardView();
            if (detected) {
                onDetectedBank(getBankCardFinder().getBankName(), getBankCardFinder().getDetectBankCode());
                checkValidCardNumberFromBundle = true;
            } else {
                onDetectedBank();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void setCardDateOnCardView() {

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
            Timber.w(e, "Exception actionAfterFinishInputCard");
        }
    }

    @Override
    protected boolean validateCardNumberLength() {
        try {
            return getAdapter().getPaymentInfoHelper().isLinkTrans()
                    && getBankCardFinder().detected()
                    || getCreditCardFinder().isValidCardLength();
        } catch (Exception e) {
            Timber.d(e, "Exception validateCardNumberLength");
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
        if (mCardAdapter == null) {
            return 0;
        }
        int errorFragmentIndex = mCardAdapter.hasError();
        if (errorFragmentIndex > -1) {
            return errorFragmentIndex;
        }

        if (!getCardFinder().isValidCardLength()
                || !getCardFinder().detected()) {
            try {
                return mCardAdapter.getIndexOfFragment(CardNumberFragment.class.getName());
            } catch (Exception e) {
                Timber.d(e);
            }
            return 0;
        }
        if (TextUtils.isEmpty(getCardExpire())) {
            try {
                return mCardAdapter.getIndexOfFragment(CardExpiryFragment.class.getName());
            } catch (Exception e) {
                Timber.d(e);
            }
            return 1;
        }

        if (TextUtils.isEmpty(getCardCVV())
                || getCardCVV().length() < 3) {
            try {
                return mCardAdapter.getIndexOfFragment(CardCVVFragment.class.getName());
            } catch (Exception e) {
                Timber.d(e);
            }
            return 2;
        }

        if (TextUtils.isEmpty(getCardName())
                || getCardName().length() <= 3) {
            try {
                return mCardAdapter.getIndexOfFragment(CardNameFragment.class.getName());
            } catch (Exception e) {
                Timber.d(e);
            }
            return 3;
        }
        return errorFragmentIndex;
    }

    @Override
    protected void populateBankCode() {
        try {
            PaymentCard paymentCard = getAdapter().getCard();
            if (paymentCard != null) {
                paymentCard.setBankcode(BuildConfig.CC_CODE);
            }
        } catch (Exception e) {
            Timber.d(e, "Exception populateBankCode");
        }
    }

    @Override
    public boolean needToWarningNotSupportCard() {
        return needToWarningNotSupportCard
                && (getCardNumber().length() >= Constants.MIN_CC_LENGTH);
    }

    @Override
    public VPaymentValidDateEditText getCardDateView() throws Exception {
        return getCardExpiryView();
    }

    @Override
    protected boolean checkValidRequiredEditText(EditText pView) {
        if (pView != null && pView.getVisibility() != View.VISIBLE) {
            return true;
        }
        boolean isCheckPattern = !(pView instanceof VPaymentDrawableEditText
                || pView instanceof VPaymentValidDateEditText)
                || ((VPaymentEditText) pView).isValid();

        return isCheckPattern
                && (pView != null && pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText().toString()));
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
            Timber.d(e);
        }
        return false;
    }

    @Override
    protected void switchChannel() {
        try {
            Timber.d("start switch to atm adapter");
            getAdapter().getPresenter().switchWorkFlow(BuildConfig.channel_atm, getCardNumber());
        } catch (Exception e) {
            Timber.d(e, "Exception switch atm adapter");
        }
    }

    /*
     * switch to atm input if in linkcard
     */
    @Override
    public void checkForSwitchChannel() {
        try {
            super.checkForSwitchChannel();
        } catch (Exception e) {
            Timber.d(e);
        }
    }
}
