package vn.com.vng.zalopay.protect.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.AuthenticationCallback;
import vn.com.vng.zalopay.authentication.AuthenticationDialog;
import vn.com.vng.zalopay.authentication.FingerprintUtil;
import vn.com.vng.zalopay.authentication.KeyTools;
import vn.com.vng.zalopay.authentication.Stage;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/**
 * Created by hieuvm on 12/26/16.
 */

final class ProtectAccountPresenter extends AbstractPresenter<IProtectAccountView> {

    @Inject
    public Context mContext;

    @Inject
    SharedPreferences mSharedPreferences;

    @Inject
    KeyTools mKeyTools;

    @Inject
    ProtectAccountPresenter() {
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

        if (!FingerprintUtil.isHardwarePresent(mContext)) {
            mView.hideFingerprintLayout();
        }

        boolean useProtect = mSharedPreferences.getBoolean(Constants.PREF_USE_PROTECT_PROFILE, true);

        mView.setCheckedProtectAccount(useProtect);

        String password = mSharedPreferences.getString(Constants.PREF_KEY_PASSWORD, "");
        Timber.d("onViewCreated: password [%s] ", password);

        boolean isFingerprintAuthAvailable = FingerprintUtil.isFingerprintAuthAvailable(mContext);

        mView.setCheckedFingerprint(useProtect & !TextUtils.isEmpty(password) & isFingerprintAuthAvailable);
    }

    private void enableFingerprint() {
        if (!FingerprintUtil.isKeyguardSecure(mContext)) {
            mView.showError("Secure lock screen hasn't set up.\n"
                    + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint");
            mView.setCheckedFingerprint(false);
            return;
        }

        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            mView.setCheckedFingerprint(false);
            mView.showError("Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint");
            return;
        }

        showFingerAuthentication();
    }

    private void showFingerAuthentication() {
        if (mView == null) {
            return;
        }

        AuthenticationDialog fragment = AuthenticationDialog.newInstance();
        fragment.setStage(Stage.PASSWORD);
        fragment.setAuthenticationCallback(new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {

                boolean result = mKeyTools.encrypt(password);
                Timber.d("encrypt cipher result %s", result);
                if (!result) {
                    return;
                }

                setUseProtectAccount(true);

                if (mView == null) {
                    return;
                }

                mView.setCheckedFingerprint(true);
                mView.setCheckedProtectAccount(true);
            }

            @Override
            public void onAuthenticationFailure() {

            }

            @Override
            public void onCancel() {
                
            }
        });

        fragment.show(((Activity) mView.getContext()).getFragmentManager(), AuthenticationDialog.TAG);
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
