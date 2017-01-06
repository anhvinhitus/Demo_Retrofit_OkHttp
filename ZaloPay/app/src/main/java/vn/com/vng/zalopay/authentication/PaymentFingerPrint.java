package vn.com.vng.zalopay.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
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
        SharedPreferences mPreferences = context.getAppComponent().sharedPreferences();
        mKeyTools = new KeyTools(context, mPreferences);
    }

    @Override
    public void authen(Activity activity, final IFPCallback callback) throws Exception {

        if (callback == null) {
            Timber.d("Callback is null");
            return;
        }

        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            callback.onComplete("");
            return;
        }

        if (!mKeyTools.isHavePassword()) {
            callback.onComplete("");
            return;
        }

        AuthenticationDialog dialog = new AuthenticationDialog();
        dialog.setContentPayment(true);
        dialog.setAuthenticationCallback(new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {
                callback.onComplete(password);
            }

            @Override
            public void onCancel() {
                callback.onCancel();
            }
        });

        dialog.show(activity.getFragmentManager(), AuthenticationDialog.TAG);
    }

    @Override
    public void updatePassword(String s, String s1) throws Exception {
        Timber.d("updatePassword: passwordOld %s passwordNew %s", s, s1);
        if (!TextUtils.isEmpty(s1) && !s1.equals(s)) {
            mKeyTools.updatePassword(s1);
        }
    }
}
