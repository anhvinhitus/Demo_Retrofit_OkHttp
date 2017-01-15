package vn.com.vng.zalopay.authentication;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;

/**
 * Created by hieuvm on 1/5/17.
 */

public class PaymentFingerPrint implements IPaymentFingerPrint {

    private Context mContext;
    private KeyTools mKeyTools;

    public PaymentFingerPrint(AndroidApplication context) {
        mContext = context;
        mKeyTools = new KeyTools(context.getAppComponent().userConfig());
    }


    /**
     * Null khi fingerprint not available
     **/

    @Nullable
    @Override
    public DialogFragment getDialogFingerprintAuthentication(Activity activity, final IFPCallback callback) throws Exception {

        if (callback == null) {
            Timber.e(new NullPointerException(), "PaymentCallback is null");
            return null;
        }

        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            Timber.d("Fingerprint not Available");
            return null;
        }

        if (!mKeyTools.isHavePassword()) {
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
        });

        return dialog;
    }

    @Override
    public void updatePassword(String s, String s1) throws Exception {
        Timber.d("updatePassword: passwordOld %s passwordNew %s", s, s1);
        if (!TextUtils.isEmpty(s1) && !s1.equals(s)) {
            mKeyTools.updatePassword(s1);
        }
    }
}
