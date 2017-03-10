package vn.com.zalopay.wallet.business.behavior.view;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.component.activity.BasePaymentActivity;
import vn.com.zalopay.wallet.view.custom.pinview.GridPasswordView;

/***
 * password payment class
 */
public class PaymentPassword {
    private static int PIN_LENGTH;
    private onEnterPinListener mPinListener;
    private GridPasswordView gridPasswordView;
    //error view
    private TextView mTextViewError;

    //show:hide pin
    private TextView mSwitchVisiblePinTextView;
    //user input enough 6 character on pinview
    private GridPasswordView.OnPasswordChangedListener mOnPasswordChangeListener = new GridPasswordView.OnPasswordChangedListener() {
        @Override
        public void onTextChanged(String psw) {
            //clear error message
            setErrorPin(null);
        }

        @Override
        public void onInputFinish(String psw) {
            if (psw.length() == PIN_LENGTH) {
                String hashPin = ZPWUtils.sha256(psw);
                GlobalData.setTransactionPin(hashPin);

                if (mPinListener != null) {
                    enableInput(false);
                    mPinListener.onEnterPinComplete();
                }
            }
        }
    };
    //show/hide password pin
    private View.OnClickListener mSwitchPinViewClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (gridPasswordView != null && !TextUtils.isEmpty(gridPasswordView.getPassWord())) {
                gridPasswordView.togglePasswordVisibility();

                mSwitchVisiblePinTextView.setText(gridPasswordView.getPassWordVisibility() ? GlobalData.getStringResource(RS.string.zpw_string_visible_pin_off)
                        : GlobalData.getStringResource(RS.string.zpw_string_visible_pin_on));
            }
        }
    };

    /***
     * constructor
     *
     * @param pParams
     */
    public PaymentPassword(View... pParams) {

        if (pParams != null && pParams.length == 3) {
            gridPasswordView = (GridPasswordView) pParams[0];
            mTextViewError = (TextView) pParams[1];
            mSwitchVisiblePinTextView = (TextView) pParams[2];
            enableInput(true);
            //get pin length from bundle resource
            PIN_LENGTH = BasePaymentActivity.getCurrentActivity() != null ? BasePaymentActivity.getCurrentActivity().getResources().getInteger(R.integer.wallet_pin_length) : 6;
        }
        if (gridPasswordView != null) {
            addEvent();
        }
        if (mSwitchVisiblePinTextView != null)
            mSwitchVisiblePinTextView.setOnClickListener(mSwitchPinViewClicked);

    }

    public void setErrorPin(String pMessage) {
        if (TextUtils.isEmpty(pMessage) && mTextViewError != null) {
            mTextViewError.setVisibility(View.INVISIBLE);
        }
        if (!TextUtils.isEmpty(pMessage) && mTextViewError != null) {
            mTextViewError.setVisibility(View.VISIBLE);
        }
    }

    private void enableInput(boolean pIsEnable) {
        gridPasswordView.setEnabled(pIsEnable);
    }

    public void showSoftKeyBoard() {
        if (gridPasswordView != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    gridPasswordView.forceInputViewGetFocus();
                }
            }, 100);
        }
    }

    public PaymentPassword setOnEnterPinListener(onEnterPinListener pListener) {
        mPinListener = pListener;
        return this;
    }

    public void reset() {
        if (gridPasswordView != null) {
            gridPasswordView.clearPassword();
            enableInput(true);
        }
    }

    private void addEvent() {
        gridPasswordView.setOnPasswordChangedListener(mOnPasswordChangeListener);
    }

    public interface onEnterPinListener {
        void onEnterPinComplete();
    }

}
