package vn.com.vng.zalopay.fingerprint;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/**
 * Created by hieuvm on 12/26/16.
 */

final class ProtectAccountPresenter extends AbstractPresenter<IProtectAccountView> {

    @Inject
    public Context mContext;

    private FingerprintUiHelper mFingerprintUiHelper;

    @Inject
    SharedPreferences mSharedPreferences;

    @Inject
    ProtectAccountPresenter(FingerprintUiHelper.FingerprintUiHelperBuilder fingerprintBuilder) {
        mFingerprintUiHelper = fingerprintBuilder.build();
    }

    void useFingerprintToAuthenticate(boolean enable) {
        Timber.d("useFingerprintToAuthenticate: [%s]", enable);
        if (mView == null) {
            return;
        }

        if (enable) {
            enableFingerprint();
        } else {
            disableFingerprint();
        }
    }

    void userProtectAccount(boolean enable) {
        if (mView == null) {
            return;
        }

        if (enable) {
            setUseProtectAccount(true);
            mView.setCheckedProtectAccount(true);
        } else {
            DialogManager.showSweetDialogConfirm((Activity) mView.getContext(), mContext.getString(R.string.confirm_off_protect_message),
                    mContext.getString(R.string.confirm), mContext.getString(R.string.cancel), new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {

                        }

                        @Override
                        public void onOKevent() {
                            setUseProtectAccount(false);
                            setUseFingerprint(false);
                            if (mView == null) {
                                return;
                            }

                            mView.setCheckedFingerprint(false);
                            mView.setCheckedProtectAccount(false);
                        }
                    });
        }
    }

    void onViewCreated() {
        if (mView == null) {
            return;
        }

        if (!mFingerprintUiHelper.isHardwarePresent()) {
            mView.hideFingerprintLayout();
        }

        boolean useProtect = mSharedPreferences.getBoolean(Constants.PREF_USE_PROTECT_PROFILE, true);

        mView.setCheckedProtectAccount(useProtect);

        String password = mSharedPreferences.getString(Constants.PREF_KEY_PASSWORD, "");
        Timber.d("onViewCreated: password [%s] ", password);

        boolean isFingerprintAuthAvailable = mFingerprintUiHelper.isFingerprintAuthAvailable();

        mView.setCheckedFingerprint(useProtect & !TextUtils.isEmpty(password) & isFingerprintAuthAvailable);
    }

    private void enableFingerprint() {
        if (!mFingerprintUiHelper.isKeyguardSecure()) {
            mView.showError("Secure lock screen hasn't set up.\n"
                    + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint");
            mView.setCheckedFingerprint(false);
            return;
        }

        if (!mFingerprintUiHelper.isFingerprintAuthAvailable()) {
            mView.setCheckedFingerprint(false);
            mView.showError("Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint");
            return;
        }

        mView.showFingerAuthentication();
    }

    private void disableFingerprint() {
        DialogManager.showSweetDialogConfirm((Activity) mView.getContext(), mContext.getString(R.string.confirm_off_fingerprint_message),
                mContext.getString(R.string.confirm), mContext.getString(R.string.cancel), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        setUseFingerprint(false);
                        if (mView == null) {
                            return;
                        }
                        mView.setCheckedFingerprint(false);
                    }
                });
    }

    void setUseProtectAccount(boolean enable) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.PREF_USE_PROTECT_PROFILE, enable);
        editor.apply();
    }

    private void setUseFingerprint(boolean enable) {
        if (enable) {
            return;
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(Constants.PREF_KEY_PASSWORD);
        editor.remove(Constants.PREF_KEY_PASSWORD_IV);
        editor.apply();
    }
}
