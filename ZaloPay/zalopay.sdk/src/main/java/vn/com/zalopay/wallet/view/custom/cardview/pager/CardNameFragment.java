package vn.com.zalopay.wallet.view.custom.cardview.pager;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.workflow.ui.CardGuiProcessor;

/**
 * Card name fragment
 */
public class CardNameFragment extends CreditCardFragment {
    private EditText mCardNameView;

    public CardNameFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle state) {

        View v = inflater.inflate(R.layout.lyt_card_holder_name, group, false);
        mCardNameView = (EditText) v.findViewById(R.id.edittext_localcard_name);

        if (mCardNameView != null) {
            if (SdkUtils.useDefaultKeyBoard(getContext())) {
                mCardNameView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
            mCardNameView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            ((VPaymentEditText) mCardNameView).setGroupText(false);
            onChangeTextHintColor(mCardNameView);
        }

        try {
            CardGuiProcessor cardGuiProcessor = getGuiProcessor();
            if (cardGuiProcessor == null) {
                return v;
            }
            mCardNameView.addTextChangedListener(cardGuiProcessor.getEnabledTextWatcher());
            mCardNameView.setOnFocusChangeListener(cardGuiProcessor.getOnFocusChangeListener());
            mCardNameView.setOnEditorActionListener(cardGuiProcessor.getEditorActionListener());

            //user touch on edittext,show keyboard
            if (mCardNameView instanceof VPaymentEditText && ((VPaymentEditText) mCardNameView).getTextInputLayout() != null) {
                (((VPaymentEditText) mCardNameView).getTextInputLayout()).setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            } else {
                mCardNameView.setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            }
        } catch (Exception e) {
            Timber.w(e);
        }

        return v;
    }

    @Override
    public EditText getEditText() {
        return mCardNameView;
    }

    @Override
    public void setHint(String pMessage) {
        if (mCardNameView != null) {
            setHint(mCardNameView, pMessage);
        }
    }

    @Override
    public String getError() {
        String errorMess = null;

        if (mCardNameView instanceof VPaymentEditText && ((VPaymentEditText) mCardNameView).getTextInputLayout() != null) {
            errorMess = (String) (((VPaymentEditText) mCardNameView).getTextInputLayout()).getHint();
            Object tag = (((VPaymentEditText) mCardNameView).getTextInputLayout()).getTag();
            if (!TextUtils.isEmpty(errorMess)
                    && tag != null
                    && errorMess.equalsIgnoreCase(tag.toString())) {
                errorMess = null;
            }
        }

        return errorMess;
    }

    @Override
    public void setError(String pMessage) {
        if (mCardNameView != null) {
            setErrorHint(mCardNameView, pMessage);
        }
    }

    @Override
    public void clearError() {
        if (mCardNameView != null) {
            setHint(mCardNameView, null);
        }
    }
}
