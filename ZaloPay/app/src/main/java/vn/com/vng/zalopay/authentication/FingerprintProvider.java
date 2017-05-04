package vn.com.vng.zalopay.authentication;

import android.content.Context;
import android.support.v4.os.CancellationSignal;
import android.text.TextUtils;

import javax.crypto.Cipher;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.fingerprintsupport.FingerprintManagerCompat;
import vn.com.vng.zalopay.exception.FingerprintException;

/**
 * Created by hieuvm on 1/3/17.
 * *
 */

final class FingerprintProvider implements AuthenticationProvider {

    static final long ERROR_TIMEOUT_MILLIS = 1600;

    private final FingerprintManagerCompat mFingerprintManagerCompat;
    private final KeyTools mKeyTools;
    private final Context mContext;


    private CancellationSignal mCancellationSignal;
    private boolean mSelfCancelled;
    private Callback mCallback;

    FingerprintProvider(Context context, KeyTools keyTools, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        this.mKeyTools = keyTools;
        this.mFingerprintManagerCompat = FingerprintManagerCompat.from(context);
    }

    private boolean isFingerprintAuthAvailable() {
        return isHardwarePresent() && hasFingerprintRegistered();
    }

    private boolean isHardwarePresent() {
        return mFingerprintManagerCompat.isHardwareDetected();
    }

    private boolean hasFingerprintRegistered() {
        return mFingerprintManagerCompat.hasEnrolledFingerprints();
    }

    private FingerprintAuthenticationCallback mFingerCallBack;

    private FingerprintAuthenticationCallback getFingerCallBack() {
        if (mFingerCallBack == null) {
            mFingerCallBack = new FingerprintAuthenticationCallback(mContext, this);
        }
        return mFingerCallBack;
    }

    void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (mSelfCancelled) {
            return;
        }

        if (mCallback != null) {
            mCallback.onError(new FingerprintException(errMsgId, errString.toString()));
        }

    }

    void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if (mSelfCancelled) {
            return;
        }

        if (mCallback != null) {
            mCallback.onError(new FingerprintException(helpMsgId, helpString.toString()));
        }

    }

    void onAuthenticationFailed() {
        if (mCallback != null) {
            mCallback.onError(new FingerprintException(-1, mContext.getString(R.string.fingerprint_not_recognized)));
        }
    }

    void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        final Cipher c = result.getCryptoObject().getCipher();
        String string = mKeyTools.decrypt(c);
        if (TextUtils.isEmpty(string)) {
            Timber.d("on Authentication succeeded : decrypt empty");
            return;
        }

        if (mCallback != null) {
            mCallback.onAuthenticated(string);
        }
    }

    private void startListening(FingerprintManagerCompat.CryptoObject cryptoObject) throws SecurityException {
        if (!isFingerprintAuthAvailable()) {
            return;
        }

        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        mFingerprintManagerCompat.authenticate(cryptoObject, 0, mCancellationSignal, getFingerCallBack(), null);

    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void verify(String password) {
        //empty
    }

    @Override
    public void startVerify() {
        stopVerify();

        try {
            if (mKeyTools.initDecryptCipher()) {
                startListening(new FingerprintManagerCompat.CryptoObject(mKeyTools.getDecryptCipher()));
            }
        } catch (Exception ex) {
            Timber.d(ex, "start verify");
        }
    }

    @Override
    public void stopVerify() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }
}
