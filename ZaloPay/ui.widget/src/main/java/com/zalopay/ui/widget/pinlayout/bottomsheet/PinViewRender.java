package com.zalopay.ui.widget.pinlayout.bottomsheet;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.R;
import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.pinlayout.checkbox.SmoothCheckBox;
import com.zalopay.ui.widget.pinlayout.encryption.Encryptor;
import com.zalopay.ui.widget.pinlayout.enums.KeyboardButtonEnum;
import com.zalopay.ui.widget.pinlayout.indicator.LoadingIndicatorView;
import com.zalopay.ui.widget.pinlayout.interfaces.IBuilder;
import com.zalopay.ui.widget.pinlayout.interfaces.IFSetDataToView;
import com.zalopay.ui.widget.pinlayout.interfaces.KeyboardButtonClickedListener;
import com.zalopay.ui.widget.pinlayout.view.KeyboardView;
import com.zalopay.ui.widget.pinlayout.view.PinCodeRoundView;

public class PinViewRender extends PinRender implements KeyboardButtonClickedListener, View.OnClickListener {

    public static final String TAG = "PinViewRender";
    private static final int DEFAULT_PIN_LENGTH = 6;

    protected TextView mStepTextView;
    protected PinCodeRoundView mPinCodeRoundView;
    protected KeyboardView mKeyboardView;
    protected ImageView mCancelImageView;
    protected TextView mTextMessage;
    protected int mType = 1;
    protected int mAttempts = 1;
    protected String mPinCode;
    protected String mOldPinCode;
    protected SimpleDraweeView mLogo;
    protected TextView mTextContent;
    private int backgroundResource;
    private boolean isSuccess = false;
    private View mRootView;
    private Context mContext;
    private SmoothCheckBox mSmoothCheckBox;
    private LinearLayout mLayoutCheckBox;
    private LoadingIndicatorView mLoadingIndicatorView;
    IFSetDataToView mIFIfSetDataToView = new IFSetDataToView() {
        @Override
        public void setErrorMessage(Activity pActivity, String pError) {

            if (mLoadingIndicatorView != null && mTextMessage != null) {
                mLoadingIndicatorView.setVisibility(View.GONE);
                mTextMessage.setVisibility(View.VISIBLE);
            }
            onPinCodeError(pActivity);
            setErrorMessageToView(pError);
        }

        @Override
        public void setImage(int pIdImage) {
            //set Image here
            if (mLogo != null) {
                mLogo.setImageResource(pIdImage);
            }
        }

        @Override
        public void setTitle(String pTitle) {
            //set title here
            if (mTextContent != null) {
                mTextContent.setText(pTitle);
            }
        }

        @Override
        public void clearData() {
            mTextMessage.setText(mContext.getText(R.string.pin_code_step_time));
            mBuilder.showLoadding(false);

        }

        @Override
        public void showLoading(boolean pShow) {
            // show loading here
            if (pShow) {
                if (mLoadingIndicatorView != null && mTextMessage != null) {
                    mLoadingIndicatorView.setVisibility(View.VISIBLE);
                    mTextMessage.setVisibility(View.GONE);
                }
            } else {
                if (mLoadingIndicatorView != null && mTextMessage != null) {
                    mLoadingIndicatorView.setVisibility(View.GONE);
                    mTextMessage.setVisibility(View.VISIBLE);
                }
            }


        }
    };

    public PinViewRender(IBuilder pBuilder) {
        super(pBuilder);
    }

    public static IBuilder getBuilder() {
        return new PinBuilder() {
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
            Log.d(getClass().getSimpleName(), "view == null");
            return;
        }
        initLayout(view, pContext);
    }

    private void initLayout(View pWView, Context pContext) {

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = pContext.obtainStyledAttributes(attrs);
        backgroundResource = typedArray.getResourceId(0, 0);

        mPinCode = "";
        mOldPinCode = "";
        mStepTextView = (TextView) pWView.findViewById(R.id.pin_code_step_textview);
        mPinCodeRoundView = (PinCodeRoundView) pWView.findViewById(R.id.pin_code_round_view);
        mPinCodeRoundView.setPinLength(this.getPinLength());
        mKeyboardView = (KeyboardView) pWView.findViewById(R.id.pin_code_keyboard_view);
        mCancelImageView = (ImageView) pWView.findViewById(R.id.cancel_action);
        mCancelImageView.setBackgroundResource(backgroundResource);
        mTextMessage = (TextView) pWView.findViewById(R.id.text_time);
        mRootView = pWView.findViewById(R.id.layout_root_view);
        mTextContent = (TextView) pWView.findViewById(R.id.text_content);
        mLogo = (SimpleDraweeView) pWView.findViewById(R.id.ic_content);
        mSmoothCheckBox = (SmoothCheckBox) pWView.findViewById(R.id.checkBox);
        mLayoutCheckBox = (LinearLayout) pWView.findViewById(R.id.layout_checkbox);
        mLoadingIndicatorView = (LoadingIndicatorView) pWView.findViewById(R.id.indicatorView_pin);

        mRootView.setOnClickListener(this);
        mCancelImageView.setOnClickListener(this);
        mKeyboardView.setKeyboardButtonClickedListener(this);
        mTextContent.setText(mBuilder.getTextContent());
        mBuilder.getCallBackToView(mIFIfSetDataToView);
        mLayoutCheckBox.setOnClickListener(this);

        String image_path = mBuilder.getLogoPath();
        if (!TextUtils.isEmpty(image_path)) {
            mLogo.setImageURI(image_path);
        } else {
            mLogo.setVisibility(View.GONE);
        }

        setStepText(pContext);
    }

    private void setStepText(Context pContext) {
        mStepTextView.setText(getStepText(mType, pContext));
    }

    /**
     * Gets the {@link String} to be used in the {@link #mStepTextView} based on {@link #mType}
     *
     * @param reason The {@link #mType} to return a {@link String} for
     * @return The {@link String} for the {@link }
     */
    public String getStepText(int reason, Context pContext) {
        String msg = null;
        switch (reason) {
            case 1:
                msg = pContext.getString(R.string.pin_code_step_unlock);
        }
        return msg;
    }

    /**
     * Gets the number of digits in the pin code.  Subclasses can override this to change the
     * length of the pin.
     *
     * @return the number of digits in the PIN
     */
    public int getPinLength() {
        return PinViewRender.DEFAULT_PIN_LENGTH;
    }

    public void setPinCode(String pinCode) {
        mPinCode = pinCode;
        mPinCodeRoundView.refresh(mPinCode.length());
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.cancel_action || v.getId() == R.id.layout_root_view) {
            closePinView();
        }
        if (v.getId() == R.id.layout_checkbox) {
            if (mSmoothCheckBox.isChecked()) {
                mSmoothCheckBox.setChecked(false, true);
            } else {
                mSmoothCheckBox.setChecked(true, true);
            }
            mBuilder.getIFPinCallBack().onCheckedFingerPrint(mSmoothCheckBox.isChecked());
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
    }

    @Override
    public void onRippleAnimationEnd() {
        if (mPinCode.length() == this.getPinLength()) {
            onPinCodeInputed();
        }
    }

    public void closePinView() {
        mBuilder.getIFPinCallBack().onCancel();

    }

    /**
     * Switch over the {@link #mType} to determine if the password is ok, if we should pass to the next step etc...
     */
    protected void onPinCodeInputed() {
        switch (mType) {
            case 1:
                onPinCodeSuccess();
                break;
            default:
                break;
        }
    }

    protected void onPinCodeSuccess() {
        onPinSuccess(mAttempts);
        mAttempts = 1;
    }

    public void onPinSuccess(int attempts) {

        if (isSuccess) {
            return;
        }
        mBuilder.showLoadding(true);
        isSuccess = true;
        mBuilder.getIFPinCallBack().onComplete(Encryptor.sha256(mPinCode));

        Log.e(TAG, "onPinSuccess!" + attempts);
    }

    /**
     * Run a shake animation when the password is not valid.
     */
    public void onPinCodeError(final Activity pActivity) {
        isSuccess = false;

        onPinFailure(mAttempts++);
        Thread thread = new Thread() {
            public void run() {
                mPinCode = "";
                mPinCodeRoundView.refresh(mPinCode.length());
                Animation animation = AnimationUtils.loadAnimation(
                        pActivity, R.anim.shake);
                mKeyboardView.startAnimation(animation);
            }
        };
        pActivity.runOnUiThread(thread);
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
        Log.e(TAG, "onPinFailure" + attempts);
        mBuilder.getIFPinCallBack().onError("onPinFailure");
    }
}