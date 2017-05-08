package vn.com.vng.zalopay.authentication.fingerprintsupport;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.os.CancellationSignal;
import android.widget.Toast;

import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by hieuvm on 5/5/17.
 * *
 */

final class SamSungFingerprintManagerCompatImpl implements FingerprintManagerCompat.FingerprintManagerCompatImpl {

    private static final int MSG_AUTH = 1000;
    private final Spass mSpass;
    private final Handler mHandler;

    private SpassFingerprint mSpassFingerprint;
    private boolean needRetryIdentify = false;
    private boolean onReadyIdentify = false;

    private WeakReference<FingerprintManagerCompat.AuthenticationCallback> mCallbackFingerprint;

    SamSungFingerprintManagerCompatImpl(Context context) {
        mHandler = new Handler(mCallback);
        Spass s;
        try {
            s = new Spass();
            s.initialize(context);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception ignored) {
            s = null;
        }

        mSpass = s;
    }

    private Handler.Callback mCallback = msg -> {
        switch (msg.what) {
            case MSG_AUTH:
                startIdentify();
                break;
            default:
                break;
        }
        return true;
    };


    @Override
    public boolean hasEnrolledFingerprints(Context context) {
        try {
            if (!isHardwareDetected(context)) {
                return false;
            }

            if (mSpassFingerprint == null) {
                mSpassFingerprint = new SpassFingerprint(context);
            }

            return mSpassFingerprint.hasRegisteredFinger();
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    @Override
    public boolean isHardwareDetected(Context context) {
        return mSpass != null && mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
    }

    @Override
    public void authenticate(Context context, FingerprintManagerCompat.CryptoObject crypto, int flags, CancellationSignal cancel, FingerprintManagerCompat.AuthenticationCallback callback, Handler handler) {


        Timber.d("authenticate: ");
        mCallbackFingerprint = new WeakReference<>(callback);

        if (!hasEnrolledFingerprints(context)) {
            callback.onAuthenticationFailed();
            return;
        }

        cancelIdentify();

        Timber.d("startIdentify");
        startIdentify();
        cancel.setOnCancelListener(this::cancelIdentify);
    }

    private SpassFingerprint.IdentifyListener mIdentifyListener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            Timber.d("onFinished: %s", eventStatus);

            FingerprintManagerCompat.AuthenticationCallback callback = null;
            if (mCallbackFingerprint != null) {
                callback = mCallbackFingerprint.get();
            }

            if (callback == null) {
                return;
            }

            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                callback.onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(null));
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                callback.onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(null));
            } else if (eventStatus == SpassFingerprint.STATUS_OPERATION_DENIED) {
                callback.onAuthenticationError(FingerprintManagerCompat.STATUS_OPERATION_DENIED, "Authentification is blocked because of fingerprint service internally.");
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED) {
                callback.onAuthenticationError(FingerprintManagerCompat.FINGERPRINT_ERROR_CANCELED, "User cancel this identify.");
            } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                callback.onAuthenticationError(FingerprintManagerCompat.FINGERPRINT_ERROR_TIMEOUT, "The time for identify is finished.");
            } else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
                callback.onAuthenticationHelp(FingerprintManagerCompat.STATUS_QUALITY_FAILED,"Authentification fail for identify");
                needRetryIdentify = true;
            } else {
                callback.onAuthenticationFailed();
                needRetryIdentify = true;
            }
        }

        @Override
        public void onReady() {
            Timber.d("identify is ready");
        }

        @Override
        public void onStarted() {
            Timber.d("identify is Started");
        }

        @Override
        public void onCompleted() {
            Timber.d("identify is completed : needRetry %s", needRetryIdentify);
            onReadyIdentify = false;
            if (needRetryIdentify) {
                needRetryIdentify = false;
                mHandler.sendEmptyMessageDelayed(MSG_AUTH, 100);
            }
        }
    };


    private void startIdentify() {
        if (onReadyIdentify) {
            return;
        }

        onReadyIdentify = true;

        try {
            if (mSpassFingerprint != null) {
                mSpassFingerprint.startIdentify(mIdentifyListener);
            }
        } catch (Exception e) {
            onReadyIdentify = false;
            Timber.w(e, "start Identify fail");
            FingerprintManagerCompat.AuthenticationCallback callback = null;

            if (mCallbackFingerprint != null) {
                callback = mCallbackFingerprint.get();
            }

            if (callback != null) {
                callback.onAuthenticationError(-1, "Fingerprint startIdentify error");
            }
        }
    }


    private void cancelIdentify() {
        try {
            Timber.d("cancel Identify");
            onReadyIdentify = false;
            needRetryIdentify = false;
            mSpassFingerprint.cancelIdentify();
        } catch (IllegalStateException ignore) {
        }
    }
}
