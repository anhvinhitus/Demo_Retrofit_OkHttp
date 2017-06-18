package vn.com.vng.zalopay.passport.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.zalopay.ui.widget.edittext.ZPEditTextValidate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by hieuvm on 6/14/17.
 * *
 */
public class OnboardingEdtView extends OnboardingView {

    public OnboardingEdtView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OnboardingEdtView(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    @BindView(R.id.inputview)
    CustomTextView mInputView;

    private int mLengthToActiveButton = -1;
    private List<ZPEditTextValidate> mValidators;

    public void assign() {
        super.assign();
        mInputView.addTextChangedListener(new Watcher());
    }

    private class Watcher implements TextWatcher {
        public void afterTextChanged(Editable s) {

        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mConfirmBtnView.setEnabled(s.length() >= mLengthToActiveButton);
        }
    }

    @NonNull
    public String getInputText() {
        return mInputView.getInputText();
    }

    public void setInputText(String text) {
        mInputView.setInputText(text);
    }

    public void setInputLength(int length) {
        mInputView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
    }

    public void setLengthToActiveButton(int length) {
        mLengthToActiveButton = length;
        mConfirmBtnView.setEnabled(getInputText().length() >= mLengthToActiveButton);
    }

    public boolean validate() {
        if (Lists.isEmptyOrNull(mValidators)) {
            return true;
        }

        String text = getInputText();

        boolean isValid = true;
        for (ZPEditTextValidate validator : mValidators) {
            isValid = isValid && validator.isValid(text);
            if (!isValid) {
                setError(validator.getErrorMessage());
                break;
            }
        }


        if (isValid) {
            reset();
        }

        return isValid;
    }

    public void reset() {
        clearError();
        showButton(mShowButton);
    }

    public void addValidator(ZPEditTextValidate validator) {
        if (mValidators == null) {
            mValidators = new ArrayList<>();
        }

        mValidators.add(validator);
    }

    public void clearValidators() {
        if (mValidators != null) {
            mValidators.clear();
        }
    }

    public void addValidator(List<ZPEditTextValidate> validators) {
        if (mValidators == null) {
            mValidators = new ArrayList<>();
        }

        mValidators.addAll(validators);
    }

    public NumberEditable getNumberEditable() {
        return mInputView;
    }
}