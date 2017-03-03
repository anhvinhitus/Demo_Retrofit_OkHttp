package vn.com.zalopay.wallet.business.channel.creditcard;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.channel.base.CardCheck;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.listener.ZPWOnDetectCardListener;
import vn.com.zalopay.wallet.utils.Log;
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
    public CreditCardGuiProcessor(AdapterCreditCard pAdapterCreditCard) throws Exception {
        super();

        mAdapter = new WeakReference<AdapterBase>(pAdapterCreditCard);

        init();
    }

    protected void init() throws Exception {

        super.init();

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
        if (getCardView() != null)
            getCardView().setHintTextExpire();
    }

    @Override
    protected void setWebViewUserAgent() {
        if (mWebView != null) {
            //mWebView.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36");
            mWebView.setUserAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
            ;
        }
    }

    @Override
    public void continueDetectCardForLinkCard() {
        Log.d(this, "card number=" + getCardNumber() + "===preparing to detect bank");

        getBankCardFinder().detectOnOtherThread(getCardNumber(), new ZPWOnDetectCardListener() {
            @Override
            public void onDetectCardComplete(boolean isDetected) {

                getAdapter().setNeedToSwitchChannel(isDetected);

                populateTextOnCardView();

                if (isDetected) {
                    setDetectedCard(getBankCardFinder().getDetectedBankName(), getBankCardFinder().getDetectBankCode());

                    checkAutoMoveCardNumberFromBundle = true;

                    Log.d(this, "card number=" + getCardNumber() + " detected=" + isDetected + " bank=" + getBankCardFinder().getDetectedBankName());

                } else {
                    setDetectedCard();
                }
            }
        });
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
        if (GlobalData.isLinkCardChannel() && getBankCardFinder().isDetected())
            return true;

        return getCreditCardFinder().isValidCardLength();
    }

    @Override
    public boolean isAllowValidateCardNumberByLuhn() {
        return GlobalData.getStringResource(RS.string.zpsdk_luhn_check_cc).equalsIgnoreCase(Constants.TRUE);
    }

    @Override
    protected CardFragmentBaseAdapter onCreateCardFragmentAdapter() {
        return new CreditCardFragmentAdapter(getAdapter().getActivity().getSupportFragmentManager(), getAdapter().getActivity().getIntent().getExtras());
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
        getAdapter().getCard().setBankcode(Constants.CCCode);
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

        boolean isCheckPattern = (pView instanceof VPaymentDrawableEditText || pView instanceof VPaymentValidDateEditText) ? ((VPaymentEditText) pView).isValid() : true;

        return isCheckPattern && (pView.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(pView.getText().toString()));
    }

    @Override
    public boolean checkEnableSubmitButton() {
        if (isValidCardNumber()
                && !TextUtils.isEmpty(getCardCVV())
                && !TextUtils.isEmpty(getCardExpire())) {
            getAdapter().getActivity().enableSubmitBtn(true);
            return true;
        } else {
            getAdapter().getActivity().enableSubmitBtn(false);
            return false;
        }
    }

    @Override
    protected boolean canSwitchChannelLinkCard() {
        return getAdapter().getActivity().isAllowLinkCardATM();
    }

    @Override
    protected boolean isOwnChannel() {
        return getAdapter().isCCFlow();
    }

    @Override
    protected void switchChannel() {
        Log.d(this, "===switchChannel===");

        getAdapter().getActivity().switchChannel(GlobalData.getStringResource(RS.string.zingpaysdk_conf_gwinfo_channel_atm), getCardNumber());
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
