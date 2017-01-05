package vn.com.vng.zalopay.fingerprint;

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

    private IFPCallback mCallback;
    private Context mContext;
    private KeyTools mKeyTools;

    public PaymentFingerPrint(Context context) {
        mContext = context;
        SharedPreferences mPreferences = ((AndroidApplication) context).getAppComponent().sharedPreferences();
        mKeyTools = new KeyTools(context, mPreferences);
    }

    @Override
    public void authen(Activity activity, IFPCallback callback) throws Exception {

        if (callback == null) {
            Timber.d("Callback is null");
            return;
        }

        mCallback = callback;
        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            mCallback.onComplete("");
            return;
        }

        if (!mKeyTools.isHavePassword()) {
            mCallback.onComplete("");
            return;
        }

        AuthenticationDialog dialog = new AuthenticationDialog();
        dialog.setVisibleSecondButton(false);
        dialog.setAuthenticationCallback(new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {
                if (mCallback != null) {
                    mCallback.onComplete(password);
                }
            }

            @Override
            public void onAuthenticationFailure() {

            }

            @Override
            public void onCancel() {
                if (mCallback != null) {
                    mCallback.onCancel();
                }
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

    public void stopAuthen() throws Exception {

    }
}
