package com.zalopay.ui.widget.password.bottomsheet;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.password.encryption.Encryptor;
import com.zalopay.ui.widget.password.enums.KeyboardButtonEnum;
import com.zalopay.ui.widget.password.indicator.LoadingIndicatorView;
import com.zalopay.ui.widget.password.interfaces.IBuilder;
import com.zalopay.ui.widget.password.interfaces.ISetDataToView;
import com.zalopay.ui.widget.password.interfaces.KeyboardButtonClickedListener;
import com.zalopay.ui.widget.password.view.KeyboardView;
import com.zalopay.ui.widget.password.view.PassCodeRoundView;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class PasswordViewRender extends PasswordRender implements KeyboardButtonClickedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = PasswordViewRender.class.getSimpleName();
    private static final int DEFAULT_PIN_LENGTH = 6;

    protected PassCodeRoundView mPinCodeRoundView;
    protected KeyboardView mKeyboardView;
    protected ImageView mCancelImageView;
    protected TextView mTextMessage;
    protected int mType = 1;
    protected String mPinCode;
    protected SimpleDraweeView mLogo;
    protected TextView mTextViewPmcName;
    protected TextView mTextViewTitle;
    protected TextView mTextViewSupportInfo;
    protected TextView mTextViewOTPInput;
    LoadingIndicatorView mLoadingIndicatorView;
    private int backgroundResource;
    private boolean isSuccess = false;
    private View mRootView;
    private WeakReference<Context> mContext;
    private CheckBox mCheckBox;
    private LinearLayout mLayoutCheckBox;
    private LinearLayout mLayoutSupportInfo;
    ISetDataToView mISetDataToView = new ISetDataToView() {
        @Override
        public void setErrorMessage(String pError) {

            if (mLoadingIndicatorView != null && mTextMessage != null) {
                mLoadingIndicatorView.setVisibility(View.INVISIBLE);
                mTextMessage.setVisibility(View.VISIBLE);
            }
            onPinCodeError();
            setErrorMessageToView(pError);
        }

        @Override
        public void setImagePath(String pIdImage) {
            //set Image here
            if (mLogo != null) {
                mLogo.setImageURI(pIdImage);
            }
        }

        @Override
        public void setContent(String pTitle) {
            //set title here
            if (mTextViewPmcName != null) {
                mTextViewPmcName.setText(pTitle);
            }
        }

        @Override
        public void setTitle(String pTitle) {
            if (mTextViewTitle != null) {
                mTextViewTitle.setText(pTitle);
            }
        }

        @Override
        public void clearData() {
            mBuilder.showLoadding(false);
            if (mTextMessage != null) {
                mTextMessage.setText(null);
            }

        }

        @Override
        public void showLoading(boolean pShow) {
            // show loading here
            if (pShow) {
                if (mLoadingIndicatorView != null && mTextMessage != null) {
                    mLoadingIndicatorView.setVisibility(View.VISIBLE);
                    mTextMessage.setVisibility(View.INVISIBLE);
                }
            } else {
                if (mLoadingIndicatorView != null && mTextMessage != null) {
                    mLoadingIndicatorView.setVisibility(View.INVISIBLE);
                    mTextMessage.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void showFingerPrintCheckBox(boolean pShow) {
            showSuggestFPCheckBox(pShow);
        }

        @Override
        public void showSupportInfo(boolean pShow) {
            showSupportInfoLayout(pShow);
        }

        @Override
        public void lockView(boolean islock) {
            enableView(!islock);
        }

        @Override
        public void resetPasswordInput() {
            onPinCodeError();
        }

        @Override
        public void showOTPInputView() {
            showOTPInput();
        }

        @Override
        public void setOTPValue(String otpValue) {
            setOTP(otpValue);
        }
    };

    public PasswordViewRender(IBuilder pBuilder) {
        super(pBuilder);
    }

    public static IBuilder getBuilder() {
        return new PasswordBuilder() {
            @Override
            public UIBottomSheetDialog.IRender build() {
                return super.build();
            }
        };
    }

    @Override
    public void render(final Context pContext) {
        mContext = new WeakReference<>(pContext);
        if (mBuilder == null) {
            return;
        }
        View view = mBuilder.getView();
        if (view == null) {
            Timber.w("render password - view is null");
            return;
        }
        initLayout(view, pContext);
    }

    private void initLayout(View pWView, Context pContext) {

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = pContext.obtainStyledAttributes(attrs);
        backgroundResource = typedArray.getResourceId(0, 0);
        mPinCode = "";
        mPinCodeRoundView = (PassCodeRoundView) pWView.findViewById(R.id.pin_code_round_view);
        mPinCodeRoundView.setPinLength(this.getPinLength());
        mKeyboardView = (KeyboardView) pWView.findViewById(R.id.pin_code_keyboard_view);
        mCancelImageView = (ImageView) pWView.findViewById(R.id.cancel_action);
        mCancelImageView.setBackgroundResource(backgroundResource);
        mTextMessage = (TextView) pWView.findViewById(R.id.text_error);
        mRootView = pWView.findViewById(R.id.layout_root_view);
        mTextViewPmcName = (TextView) pWView.findViewById(R.id.textview_pmc_name);
        mTextViewTitle = (TextView) pWView.findViewById(R.id.textview_title);
        mTextViewSupportInfo = (TextView) pWView.findViewById(R.id.text_support_info);
        mTextViewOTPInput = (TextView) pWView.findViewById(R.id.tv_otp_input);
        mLogo = (SimpleDraweeView) pWView.findViewById(R.id.ic_content);
        mCheckBox = (CheckBox) pWView.findViewById(R.id.checkbox_fingerprint);
        mLoadingIndicatorView = (LoadingIndicatorView) pWView.findViewById(R.id.indicatorView_pin);
        mLayoutCheckBox = (LinearLayout) pWView.findViewById(R.id.layout_checkbox);
        mLayoutSupportInfo = (LinearLayout) pWView.findViewById(R.id.layout_support_info);
        mRootView.setOnClickListener(this);
        mCancelImageView.setOnClickListener(this);
        mKeyboardView.setKeyboardButtonClickedListener(this);
        mBuilder.getCallBackToView(mISetDataToView);
        mCheckBox.setOnCheckedChangeListener(this);
        String image_path = mBuilder.getLogoPath();

        if (!TextUtils.isEmpty(image_path)) {
            mLogo.setImageURI(image_path);
        } else {
            mLogo.setVisibility(View.GONE);
        }
        showSuggestFPCheckBox(mBuilder.getFingerPrint());
        showSupportInfoLayout(mBuilder.isSupportInfoVisible());
        setTitleText(pContext);
        setSpannedMessageToView(mTextViewSupportInfo,
                pContext.getString(R.string.change_pin_support_info),
                pContext.getString(R.string.change_pin_support_number), false, false,
                ContextCompat.getColor(pContext, R.color.colorPrimary), new ClickableSpanNoUnderline() {
                    @Override
                    public void onClick(View widget) {
//                        navigator.startDialSupport(getContext());
                    }
                });
    }

    private void setTitleText(Context pContext) {

        if (mBuilder != null && !TextUtils.isEmpty(mBuilder.getPmcName())) {
            mTextViewPmcName.setText(mBuilder.getPmcName());
        } else {
            mTextViewPmcName.setVisibility(View.INVISIBLE);
        }

        if (mBuilder != null && !TextUtils.isEmpty(mBuilder.getTitle())) {
            mTextViewTitle.setText(mBuilder.getTitle());
        } else {
            mTextViewTitle.setText(pContext.getString(R.string.pin_code_pay_title_text));
        }
    }

    void showSuggestFPCheckBox(boolean pShow) {
        if (mLayoutCheckBox == null) {
            return;
        }
        if (pShow) {
            mLayoutCheckBox.setVisibility(View.VISIBLE);
        } else {
            mLayoutCheckBox.setVisibility(View.GONE);
        }

    }

    void showSupportInfoLayout(boolean pShow) {
        if (mLayoutSupportInfo == null) {
            return;
        }
        if (pShow) {
            mLayoutSupportInfo.setVisibility(View.VISIBLE);
        } else {
            mLayoutSupportInfo.setVisibility(View.GONE);
        }
    }

    /**
     * Gets the number of digits in the pin code.  Subclasses can override this to change the
     * length of the pin.
     *
     * @return the number of digits in the PIN
     */
    public int getPinLength() {
        return PasswordViewRender.DEFAULT_PIN_LENGTH;
    }

    public void setPinCode(String pinCode) {
        mPinCode = pinCode;
        mPinCodeRoundView.refresh(mPinCode.length());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_action) {
            close();
        }
    }

    @Override
    public void onKeyboardClick(KeyboardButtonEnum keyboardButtonEnum) {
        if (mPinCode.length() < this.getPinLength()) {
            int value = keyboardButtonEnum.getButtonValue();
            int length = mPinCode.length() - 1;
            if (value == KeyboardButtonEnum.BUTTON_CLEAR.getButtonValue()) {
                if (!mPinCode.isEmpty() && length >= 0) {
                    setPinCode(mPinCode.substring(0, length));
                    mTextViewOTPInput.setText(mPinCode.substring(0, length));
                } else {
                    setPinCode("");
                    mTextViewOTPInput.setText("");
                }
            } else {
                String valueDUkel = mPinCode + value;
                setPinCode(valueDUkel);
                mTextViewOTPInput.setText(valueDUkel);
            }
        }
        if (mPinCode.length() == this.getPinLength()) {
            onPasswordComplete();
        }
    }

    @Override
    public void onRippleAnimationEnd() {
    }

    public void close() {
        mPinCode = "";
        mPinCodeRoundView.refresh(mPinCode.length());
        isSuccess = false;
        if (mBuilder != null && mBuilder.getIFControl() != null) {
            mBuilder.getIFControl().onClose();
        }
    }

    /**
     * Switch over the {@link #mType} to determine if the password is ok, if we should pass to the next step etc...
     */
    protected void onPasswordComplete() {
        switch (mType) {
            case 1:
                onPinSuccess();
                break;
            case 2:
                onOTPSuccess();
            default:
                break;
        }
    }

    public void onPinSuccess() {
        if (isSuccess) {
            return;
        }

        Timber.d("onPinSuccess %s", isSuccess);
        isSuccess = true;
        mBuilder.getIFPinCallBack().onComplete(mBuilder.needHashPass() ?
                Encryptor.sha256(mPinCode) :
                mPinCode);
    }

    public void onOTPSuccess() {
        Timber.d("onPinSuccess %s", isSuccess);
        if (isSuccess) {
            return;
        }

        isSuccess = true;
        mBuilder.getIFPinCallBack().onComplete(mPinCode);
    }

    /**
     * Run a shake animation when the password is not valid.
     */
    public void onPinCodeError() {
        isSuccess = false;
        mPinCode = "";
        mPinCodeRoundView.refresh(mPinCode.length());
        if (mContext.get() != null) {
            Vibrator v = (Vibrator) mContext.get().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);// Vibrate for 500 milliseconds
        }
    }

    public void setErrorMessageToView(String pMessage) {
        if (mTextMessage != null) {
            mTextMessage.setText(pMessage);
            if (mContext.get() == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTextMessage.setTextColor(mContext.get().getColor(R.color.holo_red_light));
            } else {
                mTextMessage.setTextColor(mContext.get().getResources().getColor(R.color.holo_red_light));
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton checkBoxView, boolean isChecked) {
        if (checkBoxView.getId() == R.id.checkbox_fingerprint) {
            mBuilder.getIFPinCallBack().onCheckedFingerPrint(isChecked);
        }
    }

    void enableView(boolean enable) {
        mCheckBox.setClickable(enable);
        mCancelImageView.setClickable(enable);
        mKeyboardView.enableInput(enable);
        Timber.d("enable password popup %s", enable);
    }

    void showOTPInput() {
        mTextViewOTPInput.setText("");
        mTextViewOTPInput.setVisibility(View.VISIBLE);
        mPinCodeRoundView.setVisibility(View.GONE);
        mType = 2;
    }

    void setOTP(String otp) {
        mTextViewOTPInput.setText(otp);
        if (otp.length() == this.getPinLength()) {
            showOTPInput();
            onPasswordComplete();
        }
    }

    void setSpannedMessageToView(TextView tv,
                                 String message,
                                 String spannedMessage,
                                 boolean isUnderline,
                                 boolean isMessageBold,
                                 @ColorInt int linkColor,
                                 ClickableSpan clickableSpan) {
        if (tv != null) {
            // set spannable for text view
            int startIndex = message.indexOf("%s");
            int endIndex = startIndex + spannedMessage.length();
            message = String.format(message, spannedMessage);
            Spannable span = Spannable.Factory.getInstance().newSpannable(message);
            // set span color
            span.setSpan(new ForegroundColorSpan(linkColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // set span underline
            if (isUnderline) {
                span.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // set bold message
            if (isMessageBold) {
                span.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            span.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv.setText(span);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private abstract class ClickableSpanNoUnderline extends ClickableSpan {

        private int mLinkColor = 0;

        protected ClickableSpanNoUnderline() {
        }

        protected ClickableSpanNoUnderline(int mLinkColor) {
            super();
            this.mLinkColor = mLinkColor;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            if (mLinkColor != 0) {
                ds.setColor(mLinkColor);
            } else {
                super.updateDrawState(ds);
            }
            ds.setUnderlineText(false);
        }
    }
}