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
import vn.com.zalopay.wallet.workflow.ui.CardGuiProcessor;

public class CardCVVFragment extends CreditCardFragment {

    protected EditText mCardCVVView;

    public CardCVVFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle state) {
        View v = inflater.inflate(R.layout.lyt_card_cvv, group, false);

        mCardCVVView = (EditText) v.findViewById(R.id.CreditCardCVV);

        if (mCardCVVView != null) {
            mCardCVVView.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

            onChangeTextHintColor(mCardCVVView);
        }

        try {
            CardGuiProcessor cardGuiProcessor = getGuiProcessor();
            if (cardGuiProcessor == null) {
                return v;
            }
            mCardCVVView.addTextChangedListener(cardGuiProcessor.getEnabledTextWatcher());
            mCardCVVView.setOnFocusChangeListener(cardGuiProcessor.getOnFocusChangeListener());
            mCardCVVView.setOnEditorActionListener(cardGuiProcessor.getEditorActionListener());

            //user touch on edittext,show keyboard
            if (mCardCVVView instanceof VPaymentEditText && ((VPaymentEditText) mCardCVVView).getTextInputLayout() instanceof TextInputLayout) {
                (((VPaymentEditText) mCardCVVView).getTextInputLayout()).setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            } else {
                mCardCVVView.setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            }

        } catch (Exception e) {
            Timber.w(e);
        }

        return v;
    }

    @Override
    public EditText getEditText() {
        return mCardCVVView;
    }

    @Override
    public void setHint(String pMessage) {
        if (mCardCVVView != null) {
            setHint(mCardCVVView, pMessage);
        }
    }

    @Override
    public void clearError() {
        if (mCardCVVView != null) {
            setHint(mCardCVVView, null);
        }
    }

    @Override
    public String getError() {
        String errorMess = null;

        if (mCardCVVView instanceof VPaymentEditText && ((VPaymentEditText) mCardCVVView).getTextInputLayout() instanceof TextInputLayout) {
            errorMess = (String) (((VPaymentEditText) mCardCVVView).getTextInputLayout()).getHint();

            if (!TextUtils.isEmpty(errorMess) && errorMess.equalsIgnoreCase((((VPaymentEditText) mCardCVVView).getTextInputLayout()).getTag().toString())) {
                errorMess = null;
            }
        }

        return errorMess;
    }

    @Override
    public void setError(String pMessage) {
        if (mCardCVVView != null) {
            setErrorHint(mCardCVVView, pMessage);
        }
    }
}
