package vn.com.vng.zalopay.protect.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.AuthenticationCallback;
import vn.com.vng.zalopay.authentication.AuthenticationDialog;
import vn.com.vng.zalopay.authentication.FingerprintUtil;
import vn.com.vng.zalopay.authentication.KeyTools;
import vn.com.vng.zalopay.authentication.Stage;
import vn.com.vng.zalopay.data.cache.UserConfig;
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
    KeyTools mKeyTools;

    @Inject
    UserConfig mUserConfig;

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
            showAuthenticationDialog(mContext.getString(R.string.confirm_off_protect_message), new AuthenticationCallback() {
                @Override
                public void onAuthenticated(String password) {
                    setUseProtectAccount(false);
                    if (mView == null) {
                        return;
                    }

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

        boolean useProtect = mUserConfig.isUseProtectAccount();

        mView.setCheckedProtectAccount(useProtect);

        String password = mUserConfig.getEncryptedPassword();
        Timber.d("onViewCreated: password [%s] ", password);

        boolean isFingerprintAuthAvailable = FingerprintUtil.isFingerprintAuthAvailable(mContext);

        mView.setCheckedFingerprint(!TextUtils.isEmpty(password) & isFingerprintAuthAvailable);
    }

    private void enableFingerprint() {
        if (!FingerprintUtil.isKeyguardSecure(mContext)) {
            // user hasn't set up a fingerprint or lock screen.
            mView.showError(mContext.getString(R.string.tutorial_keyguard_secure_disable));
            mView.setCheckedFingerprint(false);
            return;
        }

        if (!FingerprintUtil.isFingerprintAuthAvailable(mContext)) {
            mView.setCheckedFingerprint(false);
            mView.showError(mContext.getString(R.string.tutorial_fingerprint_unavailable));
            return;
        }

        showFingerAuthentication();
    }

    private void showFingerAuthentication() {
        showAuthenticationDialog(mContext.getString(R.string.enter_password_to_continue), new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {

                boolean result = mKeyTools.encrypt(password);
                Timber.d("encrypt cipher result %s", result);
                if (!result) {
                    return;
                }

                if (mView == null) {
                    return;
                }

                mView.setCheckedFingerprint(true);
            }
        });
    }

    private void showAuthenticationDialog(String message, AuthenticationCallback callback) {
        if (mView == null) {
            return;
        }

        AuthenticationDialog fragment = AuthenticationDialog.newInstance();
        fragment.setStage(Stage.PASSWORD);
        fragment.setMessagePassword(message);
        fragment.setAuthenticationCallback(callback);

        fragment.show(((Activity) mView.getContext()).getFragmentManager(), AuthenticationDialog.TAG);
    }

    private void disableFingerprint() {
        showAuthenticationDialog(mContext.getString(R.string.confirm_off_fingerprint_message), new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {
                setUseFingerprint(false);
                if (mView == null) {
                    return;
                }
                mView.setCheckedFingerprint(false);
            }
        });
    }

    void setUseProtectAccount(boolean enable) {
        mUserConfig.useProtectAccount(enable);
    }

    private void setUseFingerprint(boolean enable) {
        if (enable) {
            return;
        }
        mUserConfig.removeFingerprint();
    }
}
