package vn.com.vng.zalopay.authentication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.os.CancellationSignal;
import android.text.TextUtils;

import javax.crypto.Cipher;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.fingerprintsupport.FingerprintManagerCompat;
import vn.com.vng.zalopay.exception.FingerprintException;

/**
 * Created by hieuvm on 1/3/17.
 */

public class FingerprintProvider implements AuthenticationProvider {

    static final long ERROR_TIMEOUT_MILLIS = 1600;

    static final long SUCCESS_DELAY_MILLIS = 1300;

    private FingerprintManagerCompat mFingerprintManagerCompat;
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
        mFingerprintManagerCompat = FingerprintManagerCompat.from(context);

        if (!checkAndroidMVersion()) {
            return;
        }

        try {
            mKeyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
        } catch (Exception ex) {
            Timber.d(ex, " create instance fingerprint api error");
        }
    }

    boolean isFingerprintAuthAvailable() {
        return isHardwarePresent() && hasFingerprintRegistered();
    }

    boolean isHardwarePresent() {
        return mFingerprintManagerCompat.isHardwareDetected();
    }

    boolean hasFingerprintRegistered() {
        return mFingerprintManagerCompat.hasEnrolledFingerprints();
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
    void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        final Cipher c = result.getCryptoObject().getCipher();
        String string = mKeyTools.decrypt(c);
        if (TextUtils.isEmpty(string)) {
            Timber.d("on Authentication succeeded : decrypt empty");
            return;
        }
        mCallback.onAuthenticated(string);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManagerCompat.CryptoObject cryptoObject) throws SecurityException {
        if (!isFingerprintAuthAvailable()) {
            return;
        }

        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        mFingerprintManagerCompat.authenticate(cryptoObject, 0, mCancellationSignal
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
