/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *//*


package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import javax.crypto.Cipher;
import javax.inject.Inject;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.AndroidUtils;

*/
/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 *//*

public class FingerprintUiHelper {


    public interface Callback {

        void onAuthenticated(Cipher cipher);

        void onError();
    }

    static final long ERROR_TIMEOUT_MILLIS = 1600;

    static final long SUCCESS_DELAY_MILLIS = 1300;

    @Nullable
    private final FingerprintManager mFingerprintManager;

    @Nullable
    private final KeyguardManager mKeyguardManager;

    private final ImageView mIcon;
    private final TextView mErrorTextView;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    boolean mSelfCancelled;

    */
/**
     * Builder class for {@link FingerprintUiHelper} in which injected fields from Dagger
     * holds its fields and takes other arguments in the {@link #build} method.
     *//*

    public static class FingerprintUiHelperBuilder {
        private final FingerprintManager mFingerPrintManager;
        private final KeyguardManager mKeyguardManager;

        @Inject
        public FingerprintUiHelperBuilder(FingerprintProvider provider) {
            mFingerPrintManager = provider.getFingerprintManager();
            mKeyguardManager = provider.getKeyguardManager();
        }

        public FingerprintUiHelper build() {
            return new FingerprintUiHelper(mFingerPrintManager, mKeyguardManager, null, null,
                    null);
        }

        public FingerprintUiHelper build(ImageView icon, TextView errorTextView, Callback callback) {
            return new FingerprintUiHelper(mFingerPrintManager, mKeyguardManager, icon, errorTextView,
                    callback);
        }
    }

    */
/**
     * Constructor for {@link FingerprintUiHelper}. This method is expected to be called from
     * only the {@link FingerprintUiHelperBuilder} class.
     *//*

    private FingerprintUiHelper(FingerprintManager fingerprintManager, KeyguardManager keyguardManager,
                                ImageView icon, TextView errorTextView, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
        this.mKeyguardManager = keyguardManager;
    }

    public boolean isFingerprintAuthAvailable() {
        if (!checkAndroidMVersion()) {
            return false;
        }

        return isHardwarePresent() && hasFingerprintRegistered();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean isHardwarePresent() {
        if (!checkAndroidMVersion()) {
            return false;
        }

        if (mFingerprintManager == null) {
            return false;
        }

        try {
            return mFingerprintManager.isHardwareDetected();
        } catch (SecurityException ignored) {
            return false;
        }
    }


    public boolean isKeyguardSecure() {
        if (!checkAndroidMVersion()) {
            return false;
        }

        if (mKeyguardManager == null) {
            return false;
        }

        return mKeyguardManager.isKeyguardSecure();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasFingerprintRegistered() {
        if (!checkAndroidMVersion()) {
            return false;
        }

        if (mFingerprintManager == null) {
            return false;
        }

        try {
            return mFingerprintManager.hasEnrolledFingerprints();
        } catch (SecurityException ignored) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) throws SecurityException {
        if (!isFingerprintAuthAvailable()) {
            return;
        }

        if (mFingerprintManager == null) {
            return;
        }

        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0 */
/* flags *//*
, getFingerCallBack(), null);

        if (mIcon != null) {
            mIcon.setImageResource(R.drawable.ic_touch);
        }
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }


    private void showError(CharSequence error) {
        if (mErrorTextView == null || mIcon == null) {
            return;
        }

        //   mIcon.setImageResource(R.drawable.ic_fingerprint_error);

        mErrorTextView.setText(error);
        mErrorTextView.setTextColor(ContextCompat.getColor(mErrorTextView.getContext(), R.color.red));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }


    Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mErrorTextView == null || mIcon == null) {
                return;
            }

            mErrorTextView.setTextColor(ContextCompat.getColor(mErrorTextView.getContext(), R.color.hint));
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(R.string.fingerprint_hint));
            mIcon.setImageResource(R.drawable.ic_touch);
        }
    };


    private FingerprintAuthenticationCallback mFingerCallBack;

    private FingerprintAuthenticationCallback getFingerCallBack() {
        if (mFingerCallBack == null) {
            mFingerCallBack = new FingerprintAuthenticationCallback(this);
        }
        return mFingerCallBack;
    }

    void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            showError(errString);
            AndroidUtils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onError();
                    }
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    void onAuthenticationFailed() {
        if (mIcon == null) {
            return;
        }

        showError(mIcon.getResources().getString(
                R.string.fingerprint_not_recognized));
    }

    @TargetApi(Build.VERSION_CODES.M)
    void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        final Cipher c = result.getCryptoObject().getCipher();
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onAuthenticated(c);
                }
            }
        }, SUCCESS_DELAY_MILLIS);

        if (mErrorTextView == null || mIcon == null) {
            return;
        }

        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        //  mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.colorPrimary, null));
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(R.string.fingerprint_success));

    }

    static boolean checkAndroidMVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
*/
