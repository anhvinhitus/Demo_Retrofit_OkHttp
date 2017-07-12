package vn.com.zalopay.wallet.business.channel.creditcard;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.view.adapter.CardFragmentBaseAdapter;
import vn.com.zalopay.wallet.view.adapter.CreditCardFragmentAdapter;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardCVVFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardExpiryFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNameFragment;
import vn.com.zalopay.wallet.view.custom.cardview.pager.CardNumberFragment;

public class CreditCardGuiProcessor extends CardGuiProcessor {
    public CreditCardGuiProcessor(AdapterCreditCard pAdapterCreditCard) {
        super();
        mAdapter = new WeakReference<>(pAdapterCreditCard);
        init(mAdapter.get().getPaymentInfoHelper());
    }

    protected void init(PaymentInfoHelper paymentInfoHelper) {
        super.init(paymentInfoHelper);
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
        Timber.d("card number=" + getCardNumber() + "===preparing to detect bank");
        Subscription subscription = getBankCardFinder().detectOnAsync(getCardNumber(), new Action1<Boolean>() {
            @Override
            public void call(Boolean detected) {
                getAdapter().setNeedToSwitchChannel(detected);
                populateTextOnCardView();
                if (detected) {
                    setDetectedCard(getBankCardFinder().getBankName(), getBankCardFinder().getDetectBankCode());
                    checkAutoMoveCardNumberFromBundle = true;
                    Timber.d("card number=" + getCardNumber() + " detected=" + detected + " bank=" + getBankCardFinder().getBankName());

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
        return getCreditCardFinder();
    }

    @Override
    protected boolean isATMChannel() {
        return false;
    }

    @Override
    protected void actionAfterFinishInputCard() {
        getAdapter().onClickSubmission();
    }

    @Override
    protected boolean validateCardNumberLength() {
        return mPaymentInfoHelper.isLinkTrans() && getBankCardFinder().isDetected() || getCreditCardFinder().isValidCardLength();

    }

    @Override
    public boolean isAllowValidateCardNumberByLuhn() {
        return GlobalData.getStringResource(RS.string.zpsdk_luhn_check_cc).equalsIgnoreCase(Constants.TRUE);
    }

    @Override
    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        try {
            return new CreditCardFragmentAdapter(getAdapter().getActivity().getSupportFragmentManager(), getAdapter().getActivity().getIntent().getExtras());
        } catch (Exception e) {
            Log.e(this, e);
        }
        return null;
    }

    @Override
    protected int validateInputCard() {
        int errorFragmentIndex = mCardAdapter.hasError();
        if (errorFragmentIndex > -1)
            return errorFragmentIndex;
        if (!getCardFinder().isValidCardLength() || !getCardFinder().isDetected()) {
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
        getAdapter().getCard().setBankcode(BuildConfig.CC_CODE);
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
        if (isValidCardNumber()
                && !TextUtils.isEmpty(getCardCVV())
                && !TextUtils.isEmpty(getCardExpire())) {
            try {
                getAdapter().getView().enableSubmitBtn();
                getAdapter().getView().changeBgSubmitButton(getAdapter().isFinalStep());
            } catch (Exception e) {
                Log.e(this, e);
            }
            return true;
        } else {
            try {
                getAdapter().getView().disableSubmitBtn();
            } catch (Exception e) {
                Log.e(this, e);
            }
            return false;
        }
    }

    @Override
    protected boolean canSwitchChannelLinkCard() {
        try {
            return PaymentPermission.allowLinkAtm();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    @Override
    protected boolean isOwnChannel() {
        return getAdapter().isCCFlow();
    }

    @Override
    protected void switchChannel() {
        Timber.d("===switchAdapter===");
        try {
            getAdapter().getPresenter().switchAdapter(BuildConfig.channel_atm, getCardNumber());
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    /***
     * switch to atm input if in linkcard
     */
    @Override
    public void checkForSwitchChannel() {
        super.checkForSwitchChannel();
        checkAutoMoveCardNumberFromBundle = true;
    }


}
