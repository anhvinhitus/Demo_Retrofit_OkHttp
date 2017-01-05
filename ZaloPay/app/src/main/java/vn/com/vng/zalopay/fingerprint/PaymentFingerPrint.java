package vn.com.vng.zalopay.fingerprint;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.exception.FingerprintException;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;

/**
 * Created by hieuvm on 1/5/17.
 */

public class PaymentFingerPrint implements IPaymentFingerPrint, AuthenticationProvider.Callback {

    private KeyTools mKeyTools;
    private AuthenticationProvider mProvider;
    private IFPCallback mCallback;
    private Context mContext;

    public PaymentFingerPrint(Context context) {
        mContext = context;
        mKeyTools = new KeyTools(context, AndroidApplication.instance().getAppComponent().sharedPreferences());
        mProvider = new FingerprintProvider(context, mKeyTools, this);
    }

    @Override
    public void authen(IFPCallback ifpCallback) throws Exception {
        mCallback = ifpCallback;
        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            mCallback.onComplete("");
            return;
        }
        mProvider.startVerify();
    }

    public void authen(Activity activity, IFPCallback callback) throws Exception {

        if (callback == null) {
            Timber.d("Callback is null");
            return;
        }

        mCallback = callback;
        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            mCallback.onCancel();
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
        });

        dialog.show(activity.getFragmentManager(), AuthenticationDialog.TAG);
    }

    @Override
    public void stopAuthen() throws Exception {
        mProvider.stopVerify();
    }

    @Override
    public void updatePassword(String s, String s1) throws Exception {
        Timber.d("updatePassword: passwordOld %s passwordNew %s", s, s1);
        if (!TextUtils.isEmpty(s1) && !s1.equals(s)) {
            mKeyTools.updatePassword(s1);
        }
    }

    @Override
    public void onAuthenticated(String password) {
        if (mCallback != null) {
            mCallback.onComplete(password);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof FingerprintException) {
            if (((FingerprintException) e).mErrorCode > 0) { // FingerprintManager
                mCallback.onCancel();
                return;
            }
        }

        FPError error = new FPError();
        error.message = e.getMessage();
        mCallback.onError(error);
    }
}
