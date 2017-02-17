package com.zalopay.ui.widget.edittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.zalopay.ui.widget.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Extension of Android Design Library's {@link TextInputLayout}
 * <p>This class enable you to add validation to the TextInputLayout
 *
 * @author Julian Raj Manandhar
 */
public class ZPTextInputLayout extends TextInputLayout {
    private List<ZPEditTextValidate> mValidators;
    private View.OnFocusChangeListener mOnFocusChangeListener;
    private boolean mIsRequired = false;
    private boolean mAutoValidate = false;
    private boolean mAutoTrimValue = false;

    private Integer mMarginBottom = null;

    public ZPTextInputLayout(Context context) {
        super(context);
        initialize();
    }

    public ZPTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
        initializeCustomAttrs(context, attrs);
    }

    public ZPTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
        initializeCustomAttrs(context, attrs);
    }

    private void initialize() {
        if (!isInEditMode()) {
            mValidators = new ArrayList<>();
            this.post(new Runnable() {
                @Override
                public void run() {
                    if (!getEditText().isInEditMode()) {
                        initializeTextWatcher();
                        initializeFocusChanged();
                    }
                }
            });
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mMarginBottom == null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            mMarginBottom = layoutParams.bottomMargin;
        }
    }

    private void initializeCustomAttrs(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable
                    .ZPTextInputLayout, 0, 0);

            try {
                mAutoTrimValue = typedArray.getBoolean(R.styleable.ZPTextInputLayout_autoTrim, false);
                mAutoValidate = typedArray.getBoolean(R.styleable.ZPTextInputLayout_autoValidate, false);
                mIsRequired = typedArray.getBoolean(R.styleable.ZPTextInputLayout_isRequired, false);
                if (mIsRequired) {
                    initRequiredValidation(context, typedArray);
                }
            } finally {
                typedArray.recycle();
            }
        }
    }

    private void initRequiredValidation(Context context, TypedArray typedArray) {
        String errorMessage = typedArray.getString(R.styleable
                .ZPTextInputLayout_requiredValidationMessage);
        if (errorMessage == null) {
            errorMessage = context.getString(R.string.default_required_validation_message);
        }
        addValidator(new ZPEditTextValidate(errorMessage) {
            @Override
            public boolean isValid(@NonNull CharSequence s) {
                return !TextUtils.isEmpty(s);
            }
        });
    }

    private void initializeTextWatcher() {
        getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAutoValidate) {
                    validate();
                } else {
                    setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initializeFocusChanged() {
        getEditText().setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validate();
                }
                if (mOnFocusChangeListener != null) {
                    mOnFocusChangeListener.onFocusChange(v, hasFocus);
                }
            }
        });
    }

    /**
     * Clears all the validators associated with the {@link ZPTextInputLayout}.
     */
    public void clearValidators() {
        mValidators.clear();
        setErrorEnabled(false);
    }

    /**
     * Associates new {@link ZPEditTextValidate} with the {@link ZPTextInputLayout}.
     *
     * @param pValidator new validator to be associated with the input field
     */
    public void addValidator(ZPEditTextValidate pValidator) {
        mValidators.add(pValidator);
    }

    public void addValidator(List<ZPEditTextValidate> validators) {
        mValidators.addAll(validators);
    }

    /**
     * Enable or disable auto-validation for the {@link ZPTextInputLayout}.
     *
     * @param flag flag to enable or disable auto-validation
     */
    public void autoValidate(boolean flag) {
        mAutoValidate = flag;
    }

    /**
     * @return if auto-validation is enabled
     */
    public boolean isAutoValidated() {
        return mAutoValidate;
    }

    /**
     * Enable or disable auto-trimming of the value of the input field for the
     * {@link ZPTextInputLayout}.
     * <p>Enabling will remove any leading and trailing white space from the value of field.</p>
     * <p>Caution: You may not want to enable this in case of password fields.</p>
     *
     * @param flag flag to enable or disable auto-trimming of value
     */
    public void autoTrimValue(boolean flag) {
        mAutoTrimValue = flag;
    }

    /**
     * @return if auto-trimming input field value is enabled
     */
    public boolean isAutoTrimEnabled() {
        return mAutoTrimValue;
    }

    /**
     * Return a boolean which can be used to check the validity of the field & CHANGE VIEW.
     *
     * @return boolean indicating if the field is valid or not.
     */
    public boolean validate() {
        boolean status = true;
        String text = getText();
        if (!mIsRequired && TextUtils.isEmpty(text)) {
            setError(null);
            setErrorEnabled(false);
            return true;
        }
        for (ZPEditTextValidate validator : mValidators) {
            if (!validator.isValid(text)) {
                setErrorEnabled(true);
                setError(validator.getErrorMessage());
                status = false;
                break;
            }
        }
        if (status) {
            setError(null);
            setErrorEnabled(false);
        }
        return status;
    }

    /**
     * Return a boolean which can be used to check the validity of the field & NOT CHANGE VIEW
     *
     * @return boolean indicating if the field is valid or not.
     */
    public boolean isValid() {
        String text = getText();
        for (ZPEditTextValidate validator : mValidators) {
            if (!validator.isValid(text)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setErrorEnabled(boolean enabled) {
        super.setErrorEnabled(enabled);
        repairAfterAdjustIndicatorPadding(enabled);
    }

    private void repairAfterAdjustIndicatorPadding(boolean enabled) {
        if (enabled && mMarginBottom != null) {
            int paddingBottom = getEditText().getPaddingBottom();
            int temp = mMarginBottom - paddingBottom;
            if (temp < 0) {
                temp = 0;
            }
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin,
                    layoutParams.topMargin,
                    layoutParams.rightMargin,
                    temp);
            setLayoutParams(layoutParams);
        } else {
            if (mMarginBottom != null && mMarginBottom >= 0) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
                layoutParams.setMargins(layoutParams.leftMargin,
                        layoutParams.topMargin,
                        layoutParams.rightMargin,
                        mMarginBottom);
                setLayoutParams(layoutParams);
            }
        }
    }

    /**
     * Return a String value of the input field.
     * <p>This method will remove white spaces if auto-trimming is enabled.</p>
     *
     * @return the value of the input field
     * @see #autoTrimValue(boolean)
     */
    public String getText() {
        if (isAutoTrimEnabled()) {
            return getEditText().getText().toString().trim();
        } else {
            return getEditText().getText().toString();
        }
    }

    public void setText(String text) {
        getEditText().setText(text);
    }

    public void setOnTextChanged(TextWatcher textWatcher) {
        getEditText().addTextChangedListener(textWatcher);
    }

    public void setOnFocusChangeListener(View.OnFocusChangeListener listener) {
        mOnFocusChangeListener = listener;
    }
}
