package vn.com.vng.zalopay.passport.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by hieuvm on 6/13/17.
 * *
 */
abstract class OnboardingView extends LinearLayout {

    abstract View onCreateInputView();

    public OnboardingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnboardingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (!isInEditMode()) {
            inflate(context, R.layout.layout_onboarding_view, this);
            initialize(context, attrs);
        }
    }

    public static final int INPUT_VIEW_INDEX = 3;


    private View.OnClickListener mClickListener = null;
    private View.OnClickListener mSecondClickListener = null;

    private String mTitle = null;
    private String mSubTitle = null;
    private String mDescInput = null;
    public Boolean mShowButton = false;
    private String mButtonText = null;
    private Boolean mShowSecButton = false;
    private String mSecondButtonText = null;

    @BindView(R.id.tvTitle)
    TextView mTitleView;

    @BindView(R.id.tvSubTitle)
    TextView mSubTitleView;

    @BindView(R.id.tvDescInput)
    TextView mDescInputView;

    @BindView(R.id.btnPrimary)
    Button mPrimaryBtnView;

    @BindView(R.id.btnSecondary)
    View mSecondaryButton;

    @BindView(R.id.icBtnSecondary)
    IconFont mIconBtnSecondary;

    @BindView(R.id.tvBtnSecondary)
    TextView mTvSecondaryView;

    private void initialize(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        int padding = AndroidUtils.dp(16f);
        setPadding(padding, padding, padding, 0);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OnboardingView);
        mTitle = a.getString(R.styleable.OnboardingView_onb_title);
        mSubTitle = a.getString(R.styleable.OnboardingView_onb_subtitle);
        mDescInput = a.getString(R.styleable.OnboardingView_onb_descriptionInput);
        mShowButton = a.getBoolean(R.styleable.OnboardingView_onb_showButton, mShowButton);
        mButtonText = a.getString(R.styleable.OnboardingView_onb_buttonText);
        mShowSecButton = a.getBoolean(R.styleable.OnboardingView_onb_showSecondButton, false);
        mSecondButtonText = a.getString(R.styleable.OnboardingView_onb_secondButtonText);
        a.recycle();
        addView(onCreateInputView(), INPUT_VIEW_INDEX);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        assign();
    }

    @OnClick(R.id.btnPrimary)
    public void onClick(View v) {
        if (mClickListener != null) {
            mClickListener.onClick(v);
        }
    }

    @OnClick(R.id.btnSecondary)
    public void onSecondClick(View v) {
        if (mSecondClickListener != null) {
            mSecondClickListener.onClick(v);
        }
    }

    public void assign() {
        mTitleView.setText(mTitle);
        mSubTitleView.setText(mSubTitle);
        mPrimaryBtnView.setText(mButtonText);
        mDescInputView.setText(mDescInput);
        showPrimaryButton(mShowButton);
        showSecondaryButton(mShowSecButton);
        mTvSecondaryView.setText(mSecondButtonText);
    }

    public void setOnClick(View.OnClickListener listener) {
        mClickListener = listener;
    }

    public void setOnSecondClick(OnClickListener listener) {
        mSecondClickListener = listener;
    }

    abstract String getInputText();

    abstract void setInputText(String text);

    public void showPrimaryButton(boolean visible) {
        mPrimaryBtnView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void showSecondaryButton(boolean visible) {
        mSecondaryButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setError(@Nullable String msg, Boolean clear) {
        if (TextUtils.isEmpty(msg)) {
            mDescInputView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            mPrimaryBtnView.setEnabled(true);
        } else {
            mDescInputView.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            mPrimaryBtnView.setEnabled(false);
        }

        mDescInputView.setText(msg);

        if (!clear) {
            return;
        }

        setInputText("");
    }

    public void setError(String msg) {
        setError(msg, false);
    }

    public void clearError() {
        if (mDescInputView.getText().equals(mDescInput)) {
            return;
        }

        mDescInputView.setText(mDescInput);
        mDescInputView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    }

    public void reset() {
        clearError();
        showPrimaryButton(mShowButton);
        setInputText("");
    }

    public void setSubTitle(String text) {
        mSubTitleView.setText(text);
    }

    public void setTextSecondaryButton(String text) {
        mTvSecondaryView.setText(text);
    }

    public void setEnableSecondaryButton(Boolean enable) {
        mSecondaryButton.setEnabled(enable);
        mTvSecondaryView.setEnabled(enable);
    }

    public void setIconSecondaryButton(String name) {
        if (TextUtils.isEmpty(name)) {
            mIconBtnSecondary.setVisibility(GONE);
        } else {
            mIconBtnSecondary.setVisibility(View.VISIBLE);
            mIconBtnSecondary.setIcon(name);
        }
    }

    public void setStyleLinkPrimaryButton() {
        mPrimaryBtnView.setBackground(null);
        ViewGroup.LayoutParams layoutParams = mPrimaryBtnView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mPrimaryBtnView.setLayoutParams(layoutParams);
        int padding = AndroidUtils.dp(16);
        mPrimaryBtnView.setPadding(padding, 0, padding, 0);
        mPrimaryBtnView.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_selector), null);
        mPrimaryBtnView.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.txt_blue_selector));
    }
}