package vn.com.vng.zalopay.authentication.fingerprintsupport;

import android.content.Context;
import android.os.Handler;
import android.support.v4.os.CancellationSignal;

import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import timber.log.Timber;

/**
 * Created by hieuvm on 5/5/17.
 * *
 */

final class SamSungFingerprintManagerCompatImpl implements FingerprintManagerCompat.FingerprintManagerCompatImpl {

    private final Spass mSpass;
    private SpassFingerprint mSpassFingerprint;

    SamSungFingerprintManagerCompatImpl(Context context) {
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

        if (!hasEnrolledFingerprints(context)) {
            //// TODO: 5/5/17 onFailure
            return;
        }

        try {

            cancelFingerprintRequest(mSpassFingerprint);

            mSpassFingerprint.startIdentify(new SpassFingerprint.IdentifyListener() {
                @Override
                public void onFinished(int status) {
                    Timber.d("onFinished: %s", status);
                    switch (status) {
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                            callback.onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(crypto));
                            break;
                        case SpassFingerprint.STATUS_QUALITY_FAILED:
                            break;
                        case SpassFingerprint.STATUS_SENSOR_FAILED:
                            break;
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
                            callback.onAuthenticationFailed();
                            break;
                        case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                            break;
                        case SpassFingerprint.STATUS_USER_CANCELLED:
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onReady() {

                }

                @Override
                public void onStarted() {

                }

                @Override
                public void onCompleted() {

                }
            });
        } catch (Exception e) {
            Timber.w(e, "Fingerprint startIdentify error");
            // TODO: 5/5/17 onFailure
            return;
        }


        cancel.setOnCancelListener(() -> cancelFingerprintRequest(mSpassFingerprint));

    }

    private void cancelFingerprintRequest(SpassFingerprint spassFingerprint) {
        try {
            spassFingerprint.cancelIdentify();
        } catch (IllegalStateException t) {
            Timber.d(t);
        }
    }
}
