package com.zalopay.ui.widget.password.bottomsheet;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

public class PasswordViewRender extends PasswordRender implements KeyboardButtonClickedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "PinViewRender";
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
    private int backgroundResource;
    private boolean isSuccess = false;
    private View mRootView;
    private Context mContext;
    private CheckBox mCheckBox;
    private LinearLayout mMaskLayout;
    private LoadingIndicatorView mLoadingIndicatorView;
    private LinearLayout mLayoutCheckBox;
    private LinearLayout mLayoutContent;
    ISetDataToView mISetDataToView = new ISetDataToView() {
        @Override
        public void setErrorMessage(Activity pActivity, String pError) {

            if (mLoadingIndicatorView != null && mTextMessage != null) {
                mLoadingIndicatorView.setVisibility(View.INVISIBLE);
                mTextMessage.setVisibility(View.VISIBLE);
            }
            onPinCodeError(pActivity);
            setErrorMessageToView(pError);
        }

        @Override
        public void setImage(String pIdImage) {
            //set Image here
            if (mLogo != null) {
                mLogo.setImageURI(pIdImage);
            }
        }

        @Override
        public void setPmcName(String pTitle) {
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
            showFingerPrin(pShow);
        }

        @Override
        public void lockControl(boolean islock) {
            if (mMaskLayout == null) {
                return;
            }
            DisableView(mMaskLayout, !islock);
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
        mContext = pContext;
        if (mBuilder == null) {
            return;
        }
        View view = mBuilder.getView();

        if (view == null) {
            Log.d(TAG, "view == null");
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
        mLogo = (SimpleDraweeView) pWView.findViewById(R.id.ic_content);
        mCheckBox = (CheckBox) pWView.findViewById(R.id.checkbox_fingerprint);
        mLoadingIndicatorView = (LoadingIndicatorView) pWView.findViewById(R.id.indicatorView_pin);
        mMaskLayout = (LinearLayout) pWView.findViewById(R.id.layout_root_view);
        mLayoutCheckBox = (LinearLayout) pWView.findViewById(R.id.layout_checkbox);
        mLayoutContent = (LinearLayout) pWView.findViewById(R.id.layout_content);
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
        showFingerPrin(mBuilder.getFingerPrint());
        setTitleText(pContext);
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

    private void showFingerPrin(boolean pShow) {
        if (mLayoutCheckBox == null) {
            return;
        }
        if (pShow) {
            mLayoutCheckBox.setVisibility(View.VISIBLE);
        } else {
            mLayoutCheckBox.setVisibility(View.GONE);
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
            closePinView();
        }

    }

    @Override
    public void onKeyboardClick(KeyboardButtonEnum keyboardButtonEnum) {
        if (mPinCode.length() < this.getPinLength()) {
            int value = keyboardButtonEnum.getButtonValue();
            if (value == KeyboardButtonEnum.BUTTON_CLEAR.getButtonValue()) {
                if (!mPinCode.isEmpty()) {
                    setPinCode(mPinCode.substring(0, mPinCode.length() - 1));
                } else {
                    setPinCode("");
                }
            } else {
                setPinCode(mPinCode + value);
            }
        }
        if (mPinCode.length() == this.getPinLength()) {
            onPinCodeInputed();
        }
    }

    @Override
    public void onRippleAnimationEnd() {
        Log.d(TAG, "==onRippleAnimationEnd==" + mPinCode.length());
    }

    public void closePinView() {
        mBuilder.getIFControl().clickCancel();
        mPinCode = "";
        mPinCodeRoundView.refresh(mPinCode.length());
        isSuccess = false;
    }

    /**
     * Switch over the {@link #mType} to determine if the password is ok, if we should pass to the next step etc...
     */
    protected void onPinCodeInputed() {
        switch (mType) {
            case 1:
                onPinSuccess();
                break;
            default:
                break;
        }
    }

    public void onPinSuccess() {
        Log.d(TAG, "==onPinSuccess==" + isSuccess);
        if (isSuccess) {
            return;
        }
        mBuilder.getIFPinCallBack().onComplete(Encryptor.sha256(mPinCode));
        isSuccess = true;
    }

    /**
     * Run a shake animation when the password is not valid.
     */
    public void onPinCodeError(final Activity pActivity) {
        isSuccess = false;
        mPinCode = "";
        mPinCodeRoundView.refresh(mPinCode.length());
        Vibrator v = (Vibrator) pActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    public void setErrorMessageToView(String pMessage) {
        if (mTextMessage != null) {
            mTextMessage.setText(pMessage);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTextMessage.setTextColor(mContext.getColor(R.color.holo_red_light));
            } else {
                mTextMessage.setTextColor(mContext.getResources().getColor(R.color.holo_red_light));
            }
        }
    }

    /**
     * When the user has failed a pin challenge
     *
     * @param attempts the number of attempts the user has used
     */
    public void onPinFailure(int attempts) {
        mBuilder.getIFPinCallBack().onError("onPinFailure");
    }

    @Override
    public void onCheckedChanged(CompoundButton checkBoxView, boolean isChecked) {
        if (checkBoxView.getId() == R.id.checkbox_fingerprint) {
            mBuilder.getIFPinCallBack().onCheckedFingerPrint(isChecked);
        }
    }


    private void DisableView(ViewGroup layout, boolean pIsDisable) {
        layout.setEnabled(pIsDisable);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                DisableView((ViewGroup) child, pIsDisable);
            } else {
                child.setClickable(pIsDisable);
            }
        }
    }

}