package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zalopay.ui.widget.edittext.NonSelectionActionModeCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 30/05/2016.
 */
public class PassCodeView extends FrameLayout {

    private int length;
    private boolean mShowPassCode = false;
    private String mHint = "";

    @BindView(R.id.root)
    LinearLayout mRootView;

    @BindView(R.id.tvHint)
    TextView mTvHint;

    @BindView(R.id.editText)
    EditText mEditText;

    @BindView(R.id.btnShowHide)
    Button mTvShowHide;

    private ArrayList<TextView> mTextViews;
    private int mTextViewSize = 0;

    private IPassCodeFocusChanged mIPassCodeFocusChanged;

    private IPassCodeMaxLength mIPassCodeMaxLength;

    public PassCodeView(Context context) {
        this(context, null);
    }

    public PassCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PassCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PassCodeView, 0, 0);
        float paddingLeft = 0;
        float paddingRight = 0;
        try {
            length = typedArray.getInt(R.styleable.PassCodeView_length, getResources().getInteger(R.integer.pin_length));
            mHint = typedArray.getString(R.styleable.PassCodeView_hint);
            paddingLeft = typedArray.getDimension(R.styleable.PassCodeView_paddingLeft, 0f);
            paddingRight = typedArray.getDimension(R.styleable.PassCodeView_paddingRight, 0f);
        } finally {
            typedArray.recycle();
        }

        mTextViews = new ArrayList<>();
        mTextViewSize = AndroidUtils.dp(36f);

        View view = LayoutInflater.from(context).inflate(R.layout.passcodeview, this, false);
        ButterKnife.bind(this, view);

        if (!TextUtils.isEmpty(mHint)) {
            mTvHint.setText(mHint);
        }
        mTvHint.setPadding((int) paddingLeft, 0, (int) paddingRight, 0);
        mRootView.setPadding((int) paddingLeft, 0, (int) paddingRight, 0);
        mEditText.setCustomSelectionActionModeCallback(new NonSelectionActionModeCallback());
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

    public void showPassCode() {
        mShowPassCode = true;
        showOrHidePassCode();
    }

    public void hidePassCode() {
        mShowPassCode = false;
        showOrHidePassCode();
    }

    private void showOrHidePassCode() {
        if (mShowPassCode) {
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
        mTvHint.setText(mHint);
        mTvHint.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    }

    public void showError(String error) {
        if (TextUtils.isEmpty(error)) {
            hideError();
            return;
        }
        mTvHint.setText(error);
        mTvHint.setTextColor(Color.RED);
    }

    @OnTextChanged(R.id.editText)
    public void onTextChanged(CharSequence s) {
        showOrHidePassCode();
    }

    @OnTextChanged(value = R.id.editText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTextChanged(Editable s) {
        if (mIPassCodeMaxLength != null && s.length() == length && length > 0) {
            mIPassCodeMaxLength.hasMaxLength();
        }
    }

    @OnFocusChange(R.id.editText)
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            mTvHint.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        } else {
            mTvHint.setTextColor(ContextCompat.getColor(getContext(), R.color.hint));
        }

        hideError();

        if (mIPassCodeFocusChanged != null) {
            mIPassCodeFocusChanged.onFocusChangedPin(hasFocus);
        }
    }

    @OnClick(R.id.btnShowHide)
    public void onClickShowHide() {
        if (mShowPassCode) {
            hidePassCode();
            mTvShowHide.setText(getContext().getResources().getString(R.string.show));
        } else {
            showPassCode();
            mTvShowHide.setText(getContext().getResources().getString(R.string.hide));
        }
    }

    public boolean requestFocusView() {
        return mEditText.requestFocus();
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        mEditText.addTextChangedListener(textWatcher);
    }

    public void setPassCodeFocusChanged(IPassCodeFocusChanged listener) {
        mIPassCodeFocusChanged = listener;
    }

    public boolean isValid() {
        return getText().length() == getMaxLength();
    }
}
