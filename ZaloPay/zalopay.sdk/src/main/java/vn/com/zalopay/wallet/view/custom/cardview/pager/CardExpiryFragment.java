package vn.com.zalopay.wallet.view.custom.cardview.pager;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;
import vn.com.zalopay.wallet.workflow.ui.CardGuiProcessor;

/**
 * Card expire
 */
public class CardExpiryFragment extends CreditCardFragment {
    protected EditText cardExpiryView;

    public CardExpiryFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle state) {

        View v = inflater.inflate(R.layout.lyt_card_expiry, group, false);
        cardExpiryView = (EditText) v.findViewById(R.id.CreditCardExpiredDate);

        if (cardExpiryView != null) {
            cardExpiryView.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            ((VPaymentValidDateEditText) cardExpiryView).setCheckDateNow(true);

            onChangeTextHintColor(cardExpiryView);
        }

        try {
            CardGuiProcessor cardGuiProcessor = getGuiProcessor();
            if (cardGuiProcessor == null) {
                return v;
            }
            cardExpiryView.addTextChangedListener(cardGuiProcessor.getEnabledTextWatcher());
            cardExpiryView.setOnFocusChangeListener(cardGuiProcessor.getOnFocusChangeListener());
            cardExpiryView.setOnEditorActionListener(cardGuiProcessor.getEditorActionListener());

            //user touch on edittext,show keyboard
            if (cardExpiryView instanceof VPaymentEditText && ((VPaymentEditText) cardExpiryView).getTextInputLayout() instanceof TextInputLayout) {
                (((VPaymentEditText) cardExpiryView)).setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            } else {
                cardExpiryView.setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            }

        } catch (Exception e) {
            Timber.w(e);
        }

        return v;
    }

    @Override
    public EditText getEditText() {
        return cardExpiryView;
    }

    @Override
    public void setHint(String pMessage) {
        if (cardExpiryView != null) {
            setHint(cardExpiryView, pMessage);
        }
    }

    @Override
    public void clearError() {
        if (cardExpiryView != null) {
            setHint(cardExpiryView, null);
        }
    }

    @Override
    public String getError() {
        String errorMess = null;

        if (cardExpiryView instanceof VPaymentEditText && ((VPaymentEditText) cardExpiryView).getTextInputLayout() instanceof TextInputLayout) {
            errorMess = (String) (((VPaymentEditText) cardExpiryView).getTextInputLayout()).getHint();

            if (!TextUtils.isEmpty(errorMess) && errorMess.equalsIgnoreCase((((VPaymentEditText) cardExpiryView).getTextInputLayout()).getTag().toString())) {
                errorMess = null;
            }
        }

        return errorMess;
    }

    @Override
    public void setError(String pMessage) {
        if (cardExpiryView != null) {
            setErrorHint(cardExpiryView, pMessage);
        }
    }
}
