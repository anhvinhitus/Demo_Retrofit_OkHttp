package vn.com.zalopay.wallet.workflow.ui;

import android.support.design.widget.TextInputLayout;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;

/**
 * Register form
 * Created by huuhoa on 7/30/17.
 */
public class RegisterHolder {
    LinearLayout llRegister;
    Spinner spnWalletType;
    Spinner spnAccNumberDefault;
    Spinner spnPhoneNumber;
    Spinner spnOTPValidType;
    TextView tvPhoneReceiveOTP;
    VPaymentDrawableEditText edtCaptcha;
    EditText edtPhoneNum;
    EditText edtAccNumDefault;
    ImageView imgCaptcha;
    WebView webCaptcha;
    LinearLayout llAccNumberDefault;
    TextInputLayout ilAccNumberDefault;
    ImageView btnRefreshCaptcha;

    Spinner getSpnWalletType() {
        return spnWalletType;
    }

    public Spinner getSpnAccNumberDefault() {
        return spnAccNumberDefault;
    }

    public LinearLayout getLlAccNumberDefault() {
        return llAccNumberDefault;
    }

    public TextInputLayout getIlAccNumberDefault() {
        return ilAccNumberDefault;
    }

    Spinner getSpnPhoneNumber() {
        return spnPhoneNumber;
    }

    public EditText getEdtPhoneNum() {
        return edtPhoneNum;
    }

    public EditText getEdtAccNumDefault() {
        return edtAccNumDefault;
    }

    Spinner getSpnOTPValidType() {
        return spnOTPValidType;
    }

    TextView getTvPhoneReceiveOTP() {
        return tvPhoneReceiveOTP;
    }

    public VPaymentDrawableEditText getEdtCaptcha() {
        return edtCaptcha;
    }

    ImageView getImgCaptcha() {
        return imgCaptcha;
    }

    WebView getWebCaptcha() {
        return webCaptcha;
    }

    public ImageView getButtonRefreshCaptcha() {
        return btnRefreshCaptcha;
    }
}
