package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 8/13/16.
 */
public class ZPTextInputLayout extends TextInputLayout {

    private CharSequence mError;
    private CharSequence mOriginHint;
    private ViewState mCurrentState = ViewState.UNKNOWN;
    private boolean mAutoChangeIconError = false;


    public ZPTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ZPTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setErrorEnabled(false);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        if (getEditText() != null) {
            mOriginHint = getHint();
            // Timber.d("mOriginHint %s", mOriginHint);

            getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Timber.d("beforeTextChanged %s", s);
                    if (TextUtils.isEmpty(s)) {
                        setError("");
                        setHintAnimationEnabled(true);
                    } else {
                        setHintAnimationEnabled(false);
                    }
                }
            });
        }
    }

    @Override
    public void setError(@Nullable CharSequence error) {
        if (TextUtils.equals(mError, error)) {
            return;
        }
        mError = error;

        if (TextUtils.isEmpty(error)) {
            mCurrentState = ViewState.UNKNOWN;
            setHint(mOriginHint);
            setHintTextAppearance(R.style.TextLabelHintDefault);
        } else {
            mCurrentState = ViewState.INVALID;
            setHint(mError);
            setHintTextAppearance(R.style.TextLabelHintDefault_Error);
        }
    }

    private void setRightIcon(int resId) {
        if (getEditText() != null) {
            getEditText().setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0);
        }
    }

    public void setState(ViewState state, int resId) {
        if (mCurrentState == state) {
            return;
        }

        mCurrentState = state;
        int _resId;

        if (state == ViewState.VALID) {
            _resId = resId >= 0 ? resId : R.drawable.ic_checked_mark;
        } else if (state == ViewState.INVALID) {
            _resId = resId >= 0 ? resId : R.drawable.ic_check_fail;
        } else {
            _resId = resId >= 0 ? resId : R.drawable.ic_info;
        }

        setRightIcon(_resId);
    }

    public void setStateWithIconDefault(ViewState state) {
        setState(state, -1);
    }

    public void setStateWithoutIcon(ViewState state) {
        setState(state, 0);
    }

    public ViewState getState() {
        return mCurrentState;
    }

    public boolean isValid() {
        return mCurrentState == ViewState.VALID;
    }

    public boolean isInvalid() {
        return mCurrentState == ViewState.INVALID;
    }

    public boolean isUnknown() {
        return mCurrentState == ViewState.UNKNOWN;
    }

    public String getText() {
        return getEditText().getText().toString();
    }

    public enum ViewState {
        UNKNOWN(0),
        VALID(1),
        INVALID(2);

        int value;

        ViewState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
