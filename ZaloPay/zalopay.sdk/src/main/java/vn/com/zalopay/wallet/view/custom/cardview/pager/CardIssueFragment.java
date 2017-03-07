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
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;


public class CardIssueFragment extends CreditCardFragment {

    protected EditText cardIssueView;

    public CardIssueFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle state) {

        View v = inflater.inflate(R.layout.lyt_card_issue, group, false);
        cardIssueView = (EditText) v.findViewById(R.id.edittext_issue_date);

        if (cardIssueView != null) {
            cardIssueView.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            ((VPaymentValidDateEditText) cardIssueView).setCheckPass(true);

            onChangeTextHintColor(cardIssueView);
        }

        try {
            if (getGuiProcessor() != null) {
                cardIssueView.addTextChangedListener(getGuiProcessor().getEnabledTextWatcher());
                cardIssueView.setOnFocusChangeListener(getGuiProcessor().getOnFocusChangeListener());
                cardIssueView.setOnEditorActionListener(getGuiProcessor().getEditorActionListener());

                //user touch on edittext,show keyboard
                if (cardIssueView instanceof VPaymentEditText && ((VPaymentEditText) cardIssueView).getTextInputLayout() instanceof TextInputLayout) {
                    (((VPaymentEditText) cardIssueView)).setOnClickListener(getGuiProcessor().getClickOnEditTextListener());
                } else
                    cardIssueView.setOnClickListener(getGuiProcessor().getClickOnEditTextListener());

            }
        } catch (Exception e) {
            vn.com.zalopay.wallet.utils.Log.e(this, e);
        }
        return v;
    }

    @Override
    public EditText getEditText() {
        return cardIssueView;
    }

    @Override
    public void setHint(String pMessage) {
        if (cardIssueView != null) {
            setHint(cardIssueView, pMessage);
        }
    }

    @Override
    public void clearError() {
        if (cardIssueView != null) {
            setHint(cardIssueView, null);
        }
    }

    @Override
    public String getError() {
        String errorMess = null;

        if (cardIssueView instanceof VPaymentEditText && ((VPaymentEditText) cardIssueView).getTextInputLayout() instanceof TextInputLayout) {
            errorMess = (String) (((VPaymentEditText) cardIssueView).getTextInputLayout()).getHint();

            if (!TextUtils.isEmpty(errorMess) && errorMess.equalsIgnoreCase((((VPaymentEditText) cardIssueView).getTextInputLayout()).getTag().toString()))
                errorMess = null;
        }

        return errorMess;
    }

    @Override
    public void setError(String pMessage) {
        if (cardIssueView != null) {
            setErrorHint(cardIssueView, pMessage);
        }
    }
}
