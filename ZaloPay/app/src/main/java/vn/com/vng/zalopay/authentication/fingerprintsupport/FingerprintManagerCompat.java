package vn.com.vng.zalopay.authentication.fingerprintsupport;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * Created by hieuvm on 5/4/17.
 * *
 */

public final class FingerprintManagerCompat {

    private Context mContext;

    /**
     * Get a {@link FingerprintManagerCompat} instance for a provided context.
     */
    public static FingerprintManagerCompat from(Context context) {
        return new FingerprintManagerCompat(context);
    }

    private FingerprintManagerCompat(Context context) {
        mContext = context;
    }

    static final FingerprintManagerCompat.FingerprintManagerCompatImpl IMPL;

    static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            IMPL = new Api23FingerprintManagerCompatImpl();
        } else {
            IMPL = new LegacyFingerprintManagerCompatImpl();
        }
    }

    /**
     * Determine if there is at least one fingerprint enrolled.
     *
     * @return true if at least one fingerprint is enrolled, false otherwise
     */
    public boolean hasEnrolledFingerprints() {
        return IMPL.hasEnrolledFingerprints(mContext);
    }

    /**
     * Determine if fingerprint hardware is present and functional.
     *
     * @return true if hardware is present and functional, false otherwise.
     */
    public boolean isHardwareDetected() {
        return IMPL.isHardwareDetected(mContext);
    }

    /**
     * Request authentication of a crypto object. This call warms up the fingerprint hardware
     * and starts scanning for a fingerprint. It terminates when
     * {@link FingerprintManagerCompat.AuthenticationCallback#onAuthenticationError(int, CharSequence)} or
     * {@link FingerprintManagerCompat.AuthenticationCallback#onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult)} is called, at
     * which point the object is no longer valid. The operation can be canceled by using the
     * provided cancel object.
     *
     * @param crypto   object associated with the call or null if none required.
     * @param flags    optional flags; should be 0
     * @param cancel   an object that can be used to cancel authentication
     * @param callback an object to receive authentication events
     * @param handler  an optional handler for events
     */
    public void authenticate(@Nullable FingerprintManagerCompat.CryptoObject crypto, int flags,
                             @Nullable CancellationSignal cancel, @NonNull FingerprintManagerCompat.AuthenticationCallback callback,
                             @Nullable Handler handler) {
        IMPL.authenticate(mContext, crypto, flags, cancel, callback, handler);
    }

    /**
     * A wrapper class for the crypto objects supported by FingerprintManager. Currently the
     * framework supports {@link Signature} and {@link Cipher} objects.
     */
    public static class CryptoObject {

        private final Signature mSignature;
        private final Cipher mCipher;
        private final Mac mMac;

        public CryptoObject(Signature signature) {
            mSignature = signature;
            mCipher = null;
            mMac = null;

        }

        public CryptoObject(Cipher cipher) {
            mCipher = cipher;
            mSignature = null;
            mMac = null;
        }

        public CryptoObject(Mac mac) {
            mMac = mac;
            mCipher = null;
            mSignature = null;
        }

        /**
         * Get {@link Signature} object.
         *
         * @return {@link Signature} object or null if this doesn't contain one.
         */
        public Signature getSignature() {
            return mSignature;
        }

        /**
         * Get {@link Cipher} object.
         *
         * @return {@link Cipher} object or null if this doesn't contain one.
         */
        public Cipher getCipher() {
            return mCipher;
        }

        /**
         * Get {@link Mac} object.
         *
         * @return {@link Mac} object or null if this doesn't contain one.
         */
        public Mac getMac() {
            return mMac;
        }
    }

    /**
     * Container for callback data from {@link FingerprintManagerCompat#authenticate(FingerprintManagerCompat.CryptoObject,
     * int, CancellationSignal, FingerprintManagerCompat.AuthenticationCallback, Handler)}.
     */
    public static final class AuthenticationResult {
        private FingerprintManagerCompat.CryptoObject mCryptoObject;

        public AuthenticationResult(FingerprintManagerCompat.CryptoObject crypto) {
            mCryptoObject = crypto;
        }

        /**
         * Obtain the crypto object associated with this transaction
         *
         * @return crypto object provided to {@link FingerprintManagerCompat#authenticate(
         *FingerprintManagerCompat.CryptoObject, int, CancellationSignal, FingerprintManagerCompat.AuthenticationCallback, Handler)}.
         */
        public FingerprintManagerCompat.CryptoObject getCryptoObject() {
            return mCryptoObject;
        }
    }

    public interface FingerprintManagerCompatImpl {
        boolean hasEnrolledFingerprints(Context context);

        boolean isHardwareDetected(Context context);

        void authenticate(Context context, FingerprintManagerCompat.CryptoObject crypto, int flags,
                          CancellationSignal cancel, FingerprintManagerCompat.AuthenticationCallback callback, Handler handler);
    }


    public static abstract class AuthenticationCallback {
        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         *
         * @param errMsgId  An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
        }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         *
         * @param helpMsgId  An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        }

        /**
         * Called when a fingerprint is recognized.
         *
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() {
        }
    }

}
