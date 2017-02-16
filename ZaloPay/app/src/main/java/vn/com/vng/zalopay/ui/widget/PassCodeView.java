package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
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

import com.zalopay.ui.widget.edittext.NonSelectionActionModeCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 30/05/2016.
 * *
 */
public class PassCodeView extends FrameLayout {

    private int length;
    private boolean mShowPassCode = false;
    private String mHint = "";
    private boolean mErrorEnabled = false;

    @BindView(R.id.root)
    LinearLayout mRootView;

    @BindView(R.id.tvHint)
    TextView mTvHint;

    @BindView(R.id.tvError)
    TextView mTvError;

    @BindView(R.id.editText)
    AppCompatEditText mEditText;

    @BindView(R.id.btnShowHide)
    TextView mTvShowHide;

    private ArrayList<TextView> mTextViews;
    private int mTextViewSize = 0;

    private IPassCodeFocusChanged mIPassCodeFocusChanged;

    private IPassCodeMaxLength mIPassCodeMaxLength;

    private ColorStateList mErrorColorStateList;
    private ColorStateList mOriginalTintList;

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
            mErrorEnabled = typedArray.getBoolean(R.styleable.PassCodeView_errorEnabled, false);
            length = typedArray.getInt(R.styleable.PassCodeView_length, getResources().getInteger(R.integer.pin_length));
            mHint = typedArray.getString(R.styleable.PassCodeView_hint);
            paddingLeft = typedArray.getDimension(R.styleable.PassCodeView_paddingLeft, 0f);
            paddingRight = typedArray.getDimension(R.styleable.PassCodeView_paddingRight, 0f);
        } finally {
            typedArray.recycle();
        }

        mTextViews = new ArrayList<>();
        mTextViewSize = AndroidUtils.dp(36f);
        mErrorColorStateList = ColorStateList.valueOf(Color.RED);
        View view = LayoutInflater.from(context).inflate(R.layout.passcodeview, this, false);
        ButterKnife.bind(this, view);

        if (!TextUtils.isEmpty(mHint)) {
            mTvHint.setText(mHint);
            mTvHint.setVisibility(VISIBLE);
        } else {
            mTvHint.setVisibility(GONE);
        }
        if (mErrorEnabled) {
            mTvError.setVisibility(INVISIBLE);
        } else {
            mTvError.setVisibility(GONE);
        }
        mTvHint.setPadding((int) paddingLeft, 0, 0, 0);
        mRootView.setPadding((int) paddingLeft, 0, 0, 0);
        mEditText.setCustomSelectionActionModeCallback(new NonSelectionActionModeCallback());

        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_focused}, // unfocused
                new int[]{} // default
        };

        int[] colors = new int[]{
                ContextCompat.getColor(context, R.color.hint),
                ContextCompat.getColor(context, R.color.colorPrimary)
        };

        mOriginalTintList = new ColorStateList(states, colors);
        mEditText.setSupportBackgroundTintList(mOriginalTintList);
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
        return mEditText.getText().toString();
    }


    private void hideError() {
        Timber.d("hideError");
        mTvError.setText("");
        if (mErrorEnabled) {
            mTvError.setVisibility(INVISIBLE);
        } else {
            mTvError.setVisibility(GONE);
        }
        mEditText.setSupportBackgroundTintList(mOriginalTintList);
    }

    public void setError(String error) {
        if (TextUtils.isEmpty(error)) {
            hideError();
            return;
        }

        if (error.equals(mTvError.getText().toString())) {
            return;
        }

        mTvError.setText(error);
        mTvError.setVisibility(VISIBLE);
        mEditText.setSupportBackgroundTintList(mErrorColorStateList);
        Timber.d("setError %s", error);
    }

    @OnTextChanged(R.id.editText)
    public void onTextChanged(CharSequence s) {
        hideError();
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
            if (mTvHint.getCurrentTextColor() == ContextCompat.getColor(getContext(), R.color.hint)) {
                mTvHint.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            }
        } else {
            if (mTvHint.getCurrentTextColor() == ContextCompat.getColor(getContext(), R.color.colorPrimary)) {
                mTvHint.setTextColor(ContextCompat.getColor(getContext(), R.color.hint));
            }
        }

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

    public void requestFocusView() {
        Timber.d("requestFocusView");
        if (!mEditText.hasFocus()) {
            Timber.d("requestFocus");
            mEditText.requestFocus();
        }
    }

    public void clearFocusView() {
        mEditText.clearFocus();
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

    public View getPassCodeView() {
        return mRootView;
    }

    public View getButtonShow() {
        return mTvShowHide;
    }

    @Override
    public boolean isFocused() {
        if (mEditText != null) {
            return mEditText.isFocused();
        }
        return super.isFocused();
    }

    public EditText getEditText() {
        return mEditText;
    }
}
