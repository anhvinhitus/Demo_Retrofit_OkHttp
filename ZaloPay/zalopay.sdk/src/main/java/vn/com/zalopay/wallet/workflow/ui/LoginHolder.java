package vn.com.zalopay.wallet.workflow.ui;

import android.support.design.widget.TextInputLayout;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;

/**
 * Holder: login form
 * Created by huuhoa on 7/30/17.
 */
public class LoginHolder {
    public ImageView btnRefreshCaptcha;
    LinearLayout llLogin;
    VPaymentDrawableEditText edtUsername;
    VPaymentDrawableEditText edtPassword;
    VPaymentDrawableEditText edtCaptcha;
    TextInputLayout edtCaptchaTextInputLayout;
    ImageView imgCaptcha;
    WebView webCaptcha;
    ScrollView srvScrollView;
    ImageView imgLogoLinkAcc;

    public EditText getEdtUsername() {
        return edtUsername;
    }

    public EditText getEdtPassword() {
        return edtPassword;
    }

    public EditText getEdtCaptcha() {
        return edtCaptcha;
    }

    ImageView getImgCaptcha() {
        return imgCaptcha;
    }

    WebView getWebCaptcha() {
        return webCaptcha;
    }

    public ScrollView getSrvScrollView() {
        return srvScrollView;
    }

    ImageView getImgLogoLinkAcc() {
        return imgLogoLinkAcc;
    }

}
