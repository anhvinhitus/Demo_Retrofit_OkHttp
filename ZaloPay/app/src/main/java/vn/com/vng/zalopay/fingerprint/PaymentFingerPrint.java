package vn.com.vng.zalopay.fingerprint;

import android.content.Context;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.IPaymentFingerPrint;

/**
 * Created by hieuvm on 1/5/17.
 */

public class PaymentFingerPrint implements IPaymentFingerPrint, AuthenticationProvider.Callback {

    private KeyTools mKeyTools;
    private AuthenticationProvider mProvider;
    private IFPCallback mFPCallback;
    private Context mContext;

    public PaymentFingerPrint(Context context) {
        mContext = context;
        mKeyTools = new KeyTools(context, AndroidApplication.instance().getAppComponent().sharedPreferences());
        mProvider = new FingerprintProvider(context, mKeyTools, this);
    }

    @Override
    public void authen(IFPCallback ifpCallback) throws Exception {
        mFPCallback = ifpCallback;
        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            mFPCallback.onComplete("");
            return;
        }
        mProvider.startVerify();
    }

    @Override
    public void stopAuthen() throws Exception {
        mProvider.stopVerify();
    }

    @Override
    public void updatePassword(String s, String s1) throws Exception {
        Timber.d("updatePassword: passwordOld %s passwordNew %s", s, s1);
        if (!TextUtils.isEmpty(s) && !s.equals(s1)) {
            mKeyTools.updatePassword(s1);
        }
    }

    @Override
    public void onAuthenticated(String password) {
        if (mFPCallback != null) {
            mFPCallback.onComplete(password);
        }
    }

    @Override
    public void onError(Throwable e) {
        FPError error = new FPError();
        error.message = e.getMessage();
        mFPCallback.onError(error);
    }
}
