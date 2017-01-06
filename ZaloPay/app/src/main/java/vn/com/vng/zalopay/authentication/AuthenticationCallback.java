package vn.com.vng.zalopay.authentication;

/**
 * Created by hieuvm on 12/26/16.
 */

public abstract class AuthenticationCallback {
    public abstract void onAuthenticated(String password);

    public void onAuthenticationFailure() {
    }

    public void onCancel() {
    }
}
