package vn.com.vng.zalopay.fingerprint;

/**
 * Created by hieuvm on 12/26/16.
 */

public interface AuthenticationCallback {
    void onAuthenticated();

    void onAuthenticationFailure();
}
