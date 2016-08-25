package vn.com.vng.zalopay.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
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

    private int length;
    private boolean mShowPasscode = false;
    private String mHint = "";
    private String mNote = "";

    private LinearLayout mRootView;
    private TextView mTvHint;
    private EditText mEditText;
    private TextView mTvNote;
    private TextView mTvShowHide;
    private ArrayList<TextView> mTextViews;
    private int mTextViewSize = 0;

    private IPasscodeFocusChanged mIPasscodeFocusChanged;

    private IPassCodeMaxLength mIPassCodeMaxLength;

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
            length = typedArray.getInt(R.styleable.PassCodeView_length, getResources().getInteger(R.integer.pin_length));
            mHint = typedArray.getString(R.styleable.PassCodeView_hint);
            mNote = typedArray.getString(R.styleable.PassCodeView_note);
        } finally {
            typedArray.recycle();
        }

        mTextViews = new ArrayList<>();
        mTextViewSize = AndroidUtils.dp(36f);

        View view = LayoutInflater.from(context).inflate(R.layout.passcodeview, this, false);
        mRootView = (LinearLayout) view.findViewById(R.id.root);
        mTvHint = (TextView) view.findViewById(R.id.tvHint);
        if (!TextUtils.isEmpty(mHint)) {
            mTvHint.setText(mHint);
        }
        mTvNote = (TextView) view.findViewById(R.id.tvNote);
        if (TextUtils.isEmpty(mNote)) {
            mTvNote.setVisibility(INVISIBLE);
        } else {
            mTvNote.setText(mNote);
            mTvNote.setVisibility(VISIBLE);
        }
        mEditText = (EditText) view.findViewById(R.id.editText);
        mEditText.addTextChangedListener(this);
        mEditText.setOnFocusChangeListener(this);

        mTvShowHide = (TextView) view.findViewById(R.id.tvShowHide);

        mTvShowHide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowPasscode) {
                    hidePasscode();
                    mTvShowHide.setText(getContext().getResources().getString(R.string.show));
                } else {
                    showPasscode();
                    mTvShowHide.setText(getContext().getResources().getString(R.string.hide));
                }
            }
        });

        initTextView(context);

        addView(view);
    }

    private void initTextView(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mTextViewSize, mTextViewSize);
        params.setMargins(0, 0, AndroidUtils.dp(8f), 0);
        for (int i = 0; i < length; i++) {
            TextView textview = (TextView) LayoutInflater.from(context).inflate(R.layout.passcode_textview, null);
            textview.setSelected(false);
            mTextViews.add(textview);
            mRootView.addView(textview, params);
        }
    }

    public int getMaxLength() {
        return length;
    }

    public void setPassCodeMaxLength(IPassCodeMaxLength passCodeMaxLength) {
        this.mIPassCodeMaxLength = passCodeMaxLength;
    }

    public void removePassCodeMaxLength() {
        this.mIPassCodeMaxLength = null;
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
                textView.setSelected(false);
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
                TextView textView = mTextViews.get(i);
                textView.setText("");
                textView.setSelected(i < inputLength);
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
        mTvNote.setVisibility(INVISIBLE);
    }

    public void showError(String error) {
        if (TextUtils.isEmpty(error)) {
            hideError();
            return;
        }
        mTvNote.setVisibility(VISIBLE);
        mTvNote.setText(error);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        showOrHidePasscode();
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mIPassCodeMaxLength != null && s.length() == length && length > 0) {
            mIPassCodeMaxLength.hasMaxLength();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mTvHint.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        } else {
            mTvHint.setTextColor(ContextCompat.getColor(getContext(), R.color.hint));
        }

        hideError();

        if (mIPasscodeFocusChanged != null) {
            mIPasscodeFocusChanged.onFocusChangedPin(hasFocus);
        }
    }

    public void setHintVisibility(int visibility) {
        mTvHint.setVisibility(visibility);
    }

    public void setButtonHideVisibility(int visibility) {
        mTvShowHide.setVisibility(visibility);
    }

    public boolean requestFocusView() {
        return mEditText.requestFocus();
    }

    public boolean isRequestFocus() {
        return mEditText.isFocused();
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        mEditText.addTextChangedListener(textWatcher);
    }

    public void setPasscodeFocusChanged(IPasscodeFocusChanged listener) {
        mIPasscodeFocusChanged = listener;
    }

    public void removePasscodeFocusChanged() {
        mIPasscodeFocusChanged = null;
    }

}
