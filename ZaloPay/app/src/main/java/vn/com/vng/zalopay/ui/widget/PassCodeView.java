package vn.com.vng.zalopay.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 30/05/2016.
 */
public class PassCodeView extends FrameLayout implements TextWatcher, View.OnFocusChangeListener {

    private final int DEFAULT_LENGTH = 6;
    private int length = DEFAULT_LENGTH;
    private boolean mShowPasscode = false;
    private String mHint = "";
    private String mNote = "";

    private LinearLayout mRootView;
    private TextView mTvHint;
    private EditText mEditText;
    private TextView mTvNote;
    private TextInputLayout mTextInputLayout;
    private ArrayList<TextView> mTextViews;
    private int mTextViewSize = 0;

    private IPasscodeFocusChanged mIPasscodeFocusChanged;
    private IPasscodeChanged mIPasscodeChanged;

    public PassCodeView(Context context) {
        super(context);
    }

    public PassCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public PassCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PassCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PassCodeView, 0, 0);
        try {
            length = typedArray.getInt(R.styleable.PassCodeView_length, DEFAULT_LENGTH);
            mHint = typedArray.getString(R.styleable.PassCodeView_hint);
            mNote = typedArray.getString(R.styleable.PassCodeView_note);
        } finally {
            typedArray.recycle();
        }
        mTextViews = new ArrayList<>();
        mTextViewSize = (int) (AndroidUtils.density * 36);
        inflate(context, R.layout.passcodeview, this);
        mRootView = (LinearLayout) this.findViewById(R.id.root);
        mTextInputLayout = (TextInputLayout) this.findViewById(R.id.textInputLayout);
        mTvHint = (TextView) this.findViewById(R.id.tvHint);
        if (!TextUtils.isEmpty(mHint)) {
            mTvHint.setText(mHint);
        }
        mTvNote = (TextView) this.findViewById(R.id.tvNote);
        if (TextUtils.isEmpty(mNote)) {
            mTvNote.setVisibility(GONE);
        } else {
            mTvNote.setText(mNote);
            mTvNote.setVisibility(VISIBLE);
        }
        mEditText = (EditText) this.findViewById(R.id.editText);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
        mEditText.addTextChangedListener(this);
        mEditText.setOnFocusChangeListener(this);
        initTextView();
    }

    private void initTextView() {
        if (length <= 0) {
            return;
        }
        if (mRootView == null) {
            return;
        }

        if (getContext() == null) {
            return;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mTextViewSize, mTextViewSize);
        params.setMargins(0, 0, (int) (AndroidUtils.density * 8), 0);
        for (int i = 0; i < length; i++) {
            TextView textview = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.passcode_textview, null);
            textview.setBackgroundResource(R.drawable.pin_code_round_empty);
            mTextViews.add(textview);
            mRootView.addView(textview, params);
        }
    }

    public void setPasscodeFocusChanged(IPasscodeFocusChanged listener) {
        mIPasscodeFocusChanged = listener;
    }

    public void removePasscodeFocusChanged() {
        mIPasscodeFocusChanged = null;
    }

    public void setPasscodeChanged(IPasscodeChanged listener) {
        mIPasscodeChanged = listener;
    }

    public void removePasscodeChanged() {
        mIPasscodeChanged = null;
    }

    public void showPasscode() {
        mShowPasscode = true;
        showOrHidePasscode();
    }

    public void hidePasscode() {
        mShowPasscode = false;
        showOrHidePasscode();
    }

    private void showOrHidePasscode() {
        if (mShowPasscode) {
            String strInput = mEditText.getText().toString();
            char[] charArray = null;
            if (!TextUtils.isEmpty(strInput)) {
                charArray = strInput.toCharArray();
            }
            for (int i = 0; i < mTextViews.size(); i++) {
                TextView textView = mTextViews.get(i);
                textView.setBackgroundResource(R.drawable.pin_code_round_empty);
                if (charArray != null && i < charArray.length) {
                    textView.setText(String.valueOf(charArray[i]));
                } else {
                    textView.setText("");
                }
            }
        } else {
            String strInput = mEditText.getText().toString();
            int inputLength = 0;
            if (!TextUtils.isEmpty(strInput)) {
                inputLength = strInput.length();
            }
            for (int i = 0; i < mTextViews.size(); i++) {
//                TextView textView = (TextView) mRootView.getChildAt(i);
                TextView textView = mTextViews.get(i);
                textView.setText("");
                if (i < inputLength) {
                    textView.setBackgroundResource(R.drawable.pin_code_round_full);
                } else {
                    textView.setBackgroundResource(R.drawable.pin_code_round_empty);
                }
            }
        }
    }

    public String getText() {
        if (mEditText == null) {
            return "";
        }
        return mEditText.getText().toString();
    }

    public void hideError() {
        mTextInputLayout.setErrorEnabled(false);
        mTextInputLayout.setError(null);
    }

    public void showError(String error) {
        if (TextUtils.isEmpty(error)) {
            hideError();
            return;
        }
        mTvNote.setVisibility(GONE);
        mTextInputLayout.setErrorEnabled(true);
        mTextInputLayout.setError(error);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mIPasscodeChanged != null) {
            mIPasscodeChanged.beforeTextChanged(s, start, count, after);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        showOrHidePasscode();
        if (mIPasscodeChanged != null) {
            mIPasscodeChanged.onTextChanged(s, start, before, count);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mIPasscodeChanged != null) {
            mIPasscodeChanged.afterTextChanged(s);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            hideError();
            mTvNote.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTvHint.setTextColor(getContext().getColor(R.color.colorPrimary));
            } else {
                mTvHint.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        } else {
            mTvNote.setVisibility(View.INVISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTvHint.setTextColor(getContext().getColor(R.color.hint));
            } else {
                mTvHint.setTextColor(getResources().getColor(R.color.hint));
            }
        }
        if (mIPasscodeFocusChanged != null) {
            mIPasscodeFocusChanged.onFocusChangedPin(hasFocus);
        }
    }
}
