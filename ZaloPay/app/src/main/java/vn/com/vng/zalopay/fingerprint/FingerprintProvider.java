package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.text.TextUtils;

import javax.crypto.Cipher;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.exception.FingerprintException;

/**
 * Created by hieuvm on 1/3/17.
 */

public class FingerprintProvider implements AuthenticationProvider {

    static final long ERROR_TIMEOUT_MILLIS = 1600;

    static final long SUCCESS_DELAY_MILLIS = 1300;

    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;

    private KeyTools mKeyTools;

    private CancellationSignal mCancellationSignal;

    boolean mSelfCancelled;

    private Callback mCallback;

    private Stage mStage;

    private Context mContext;

    @TargetApi(Build.VERSION_CODES.M)
    public FingerprintProvider(Context context, KeyTools keyTools, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        this.mKeyTools = keyTools;

        if (!checkAndroidMVersion()) {
            return;
        }

        try {
            mKeyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
            mFingerprintManager = (FingerprintManager) context.getSystemService(Activity.FINGERPRINT_SERVICE);
        } catch (Exception ex) {
            Timber.d(ex, " create instance fingerprint api error");
        }
    }

    public boolean isFingerprintAuthAvailable() {
        if (!checkAndroidMVersion()) {
            return false;
        }

        return isHardwarePresent() && hasFingerprintRegistered();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean isHardwarePresent() {
        if (!checkAndroidMVersion() || mFingerprintManager == null) {
            return false;
        }

        try {
            return mFingerprintManager.isHardwareDetected();
        } catch (SecurityException ignored) {
            return false;
        }
    }

    public boolean isKeyguardSecure() {
        if (!checkAndroidMVersion() || mKeyguardManager == null) {
            return false;
        }

        return mKeyguardManager.isKeyguardSecure();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasFingerprintRegistered() {
        if (!checkAndroidMVersion() || mFingerprintManager == null) {
            return false;
        }

        try {
            return mFingerprintManager.hasEnrolledFingerprints();
        } catch (SecurityException ignored) {
            return false;
        }
    }

    static boolean checkAndroidMVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private FingerprintAuthenticationCallback mFingerCallBack;

    private FingerprintAuthenticationCallback getFingerCallBack() {
        if (mFingerCallBack == null) {
            mFingerCallBack = new FingerprintAuthenticationCallback(mContext, this);
        }
        return mFingerCallBack;
    }

    void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            mCallback.onError(new FingerprintException(errMsgId, errString.toString()));
        }
    }

    void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if (!mSelfCancelled) {
            mCallback.onError(new FingerprintException(helpMsgId, helpString.toString()));
        }
    }

    void onAuthenticationFailed() {
        mCallback.onError(new FingerprintException(-1, mContext.getString(R.string.fingerprint_not_recognized)));
    }

    @TargetApi(Build.VERSION_CODES.M)
    void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        final Cipher c = result.getCryptoObject().getCipher();
        String string = mKeyTools.decrypt(c);
        if (TextUtils.isEmpty(string)) {
            Timber.d("onAuthenticationSucceeded: decrypt empty");
            return;
        }
        mCallback.onAuthenticated(string);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) throws SecurityException {
        if (!isFingerprintAuthAvailable() || mFingerprintManager == null) {
            return;
        }

        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0
                        , getFingerCallBack(), null);
    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void verify(String password) {
        //empty
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void startVerify() {
        stopVerify();

        try {
            if (mKeyTools.initDecryptCipher()) {
                startListening(new FingerprintManager.CryptoObject(mKeyTools.getDecryptCipher()));
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
