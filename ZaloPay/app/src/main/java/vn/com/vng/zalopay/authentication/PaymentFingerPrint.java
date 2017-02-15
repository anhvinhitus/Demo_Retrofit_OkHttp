package vn.com.vng.zalopay.authentication;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.zalopay.ui.widget.util.TimeUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;

/**
 * Created by hieuvm on 1/5/17.
 */

public class PaymentFingerPrint implements IPaymentFingerPrint {

    private Context mContext;
    private KeyTools mKeyTools;
    private SharedPreferences mPreferences;

    public PaymentFingerPrint(AndroidApplication context) {
        mContext = context;
        mPreferences = context.getAppComponent().sharedPreferences();
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

    public void showSuggestionDialog(Activity activity, String hashPassword) {

        if (activity == null) {
            return;
        }

        if (!shouldShowSuggestDialog()) {
            return;
        }

        FingerprintSuggestDialog dialog = new FingerprintSuggestDialog();
        dialog.setPassword(hashPassword);
        dialog.show(activity.getFragmentManager(), FingerprintSuggestDialog.TAG);
        mPreferences.edit()
                .putLong(Constants.PREF_LAST_TIME_SHOW_FINGERPRINT_SUGGEST, System.currentTimeMillis())
                .apply();
    }


    private boolean shouldShowSuggestDialog() {

        if (!mPreferences.getBoolean(Constants.PREF_SHOW_FINGERPRINT_SUGGEST, true)) {
            Timber.d("not show fingerprint suggest");
            return false;
        }

        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            Timber.d("fingerprint not available");
            return false;
        }

        if (mKeyTools.isHavePassword()) {
            Timber.d("using fingerprint");
            return false;
        }

        long lastTime = mPreferences.getLong(Constants.PREF_LAST_TIME_SHOW_FINGERPRINT_SUGGEST, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastTime < TimeUtils.DAY) {
            Timber.d("less than one day");
            return false;
        }

        return true;
    }
}
