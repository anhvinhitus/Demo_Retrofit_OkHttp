package vn.com.vng.zalopay.authentication;

import android.app.Activity;
import android.app.DialogFragment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.authentication.fingerprintsupport.FingerprintManagerCompat;
import vn.com.vng.zalopay.authentication.secret.KeyTools;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;

/**
 * Created by hieuvm on 1/5/17.
 */

public class PaymentFingerPrint implements IPaymentFingerPrint {

    private final KeyTools mKeyTools;
    private final Navigator mNavigator;

    private final FingerprintManagerCompat mFingerprintManagerCompat;

    public PaymentFingerPrint(AndroidApplication context) {
        mKeyTools = new KeyTools();
        mNavigator = context.getAppComponent().navigator();
        mFingerprintManagerCompat = FingerprintManagerCompat.from(context);
    }

    /**
     * Null khi fingerprint not available
     **/

    @Nullable
    @Override
    public DialogFragment getDialogFingerprintAuthentication(Activity activity, final IFPCallback callback) throws Exception {

        if (callback == null) {
            Timber.e("PaymentCallback is null");
            return null;
        }

        if (!mFingerprintManagerCompat.isFingerprintAvailable()) {
            Timber.d("Fingerprint not available");
            return null;
        }

        if (!mKeyTools.isHavePassword()) {
            Timber.d("Do not use password");
            return null;
        }

        AuthenticationDialog dialog = new AuthenticationDialog();
        dialog.setContentPayment(true);
        dialog.setAuthenticationCallback(new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {
                callback.onComplete(password);
            }

            @Override
            public void onAuthenticationFailure() {
                callback.onError(new FPError());
            }

            @Override
            public void onCancel() {
                callback.onCancel();
            }

            @Override
            public void onShowPassword() {
            }
        });

        return dialog;
    }

    @Override
    public void updatePassword(String s, String s1) throws Exception {
        Timber.d("Update password : passwordOld %s passwordNew %s", s, s1);
        if (!TextUtils.isEmpty(s1) && !s1.equals(s)) {
            mKeyTools.storePassword(s1);
        }
    }

    @Override
    public boolean isFingerPrintAvailable() {
        return mFingerprintManagerCompat.isFingerprintAvailable();
    }

    @Override
    public boolean hasPassword() {
        return mKeyTools.isHavePassword();
    }

    @Override
    public boolean putPassword(String pNewPassword) {
        if (TextUtils.isEmpty(pNewPassword)) {
            return false;
        }
        Timber.d("put new password %s", pNewPassword);
        return mKeyTools.storePassword(pNewPassword);
    }

    public void showSuggestionDialog(Activity activity, String hashPassword) {

        if (activity == null) {
            return;
        }

        mNavigator.showSuggestionDialog(activity, hashPassword);
    }
}
