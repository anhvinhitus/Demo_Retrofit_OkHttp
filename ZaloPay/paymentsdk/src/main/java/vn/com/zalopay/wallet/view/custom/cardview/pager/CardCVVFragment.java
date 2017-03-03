package vn.com.zalopay.wallet.view.custom.cardview.pager;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;

/**
 * Card cc fragment
 */
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
            if (getGuiProcessor() != null) {
                mCardCVVView.addTextChangedListener(getGuiProcessor().getEnabledTextWatcher());
                mCardCVVView.setOnFocusChangeListener(getGuiProcessor().getOnFocusChangeListener());
                mCardCVVView.setOnEditorActionListener(getGuiProcessor().getEditorActionListener());

                //user touch on edittext,show keyboard
                if (mCardCVVView instanceof VPaymentEditText && ((VPaymentEditText) mCardCVVView).getTextInputLayout() instanceof TextInputLayout) {
                    (((VPaymentEditText) mCardCVVView).getTextInputLayout()).setOnClickListener(getGuiProcessor().getClickOnEditTextListener());
                } else
                    mCardCVVView.setOnClickListener(getGuiProcessor().getClickOnEditTextListener());

            }

        } catch (Exception e) {
            vn.com.zalopay.wallet.utils.Log.e(this, e);
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
