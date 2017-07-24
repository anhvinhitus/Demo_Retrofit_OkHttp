package vn.com.vng.zalopay.protect.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.password.interfaces.IPasswordCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.AuthenticationCallback;
import vn.com.vng.zalopay.authentication.AuthenticationPassword;
import vn.com.vng.zalopay.authentication.fingerprintsupport.FingerprintManagerCompat;
import vn.com.vng.zalopay.authentication.secret.KeyTools;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.user.UserBaseActivity;
import vn.com.vng.zalopay.utils.PasswordUtil;
import vn.com.vng.zalopay.utils.ToastUtil;

/**
 * Created by hieuvm on 12/26/16.
 */

final class ProtectAccountPresenter extends AbstractPresenter<IProtectAccountView> {
    private final int STATUS_OLD_PASS_INVALID = -3;
    private final int STATUS_CONFIRM_PASS_INVALID = -2;
    private final int STATUS_OTP_INVALID = -1;
    private final int STATUS_OLD_PASS_SUCCESS = 1;
    private final int STATUS_NEW_PASS = 2;
    private final int STATUS_OTP = 3;
    private final int STATUS_CONFIRM_NEW_PASS = 4;

    @Inject
    public Context mContext;

    KeyTools mKeyTools;

    @Inject
    UserConfig mUserConfig;

    private EventBus mEventBus;
    private PassportRepository mPassportRepository;
    private AccountStore.Repository mAccountRepository;
    private final FingerprintManagerCompat mFingerprintManagerCompat;
    private PasswordManager mPassword;
    private String mOldPassword;
    private String mNewPassword;

    int getViewStatus() {
        return viewStatus;
    }

    void setViewStatus(int viewStatus) {
        this.viewStatus = viewStatus;
    }

    private int viewStatus;

    @Inject
    ProtectAccountPresenter(PassportRepository passportRepository,
                            AccountStore.Repository accountRepository,
                            EventBus eventBus) {
        mKeyTools = new KeyTools();
        mFingerprintManagerCompat = FingerprintManagerCompat.from(AndroidApplication.instance());
        mPassportRepository = passportRepository;
        mAccountRepository = accountRepository;
        mEventBus = eventBus;
    }

    @Override
    public void attachView(IProtectAccountView iProtectAccountView) {
        super.attachView(iProtectAccountView);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        super.detachView();
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

        if (!mFingerprintManagerCompat.isHardwareDetected()) {
            mView.hideFingerprintLayout();
        }

        boolean useProtect = mUserConfig.isUseProtectAccount();

        mView.setCheckedProtectAccount(useProtect);

        String password = mUserConfig.getEncryptedPassword();
        Timber.d("onViewCreated: password [%s] ", password);

        boolean isFingerprintAuthAvailable = mFingerprintManagerCompat.isFingerprintAvailable();

        mView.setCheckedFingerprint(!TextUtils.isEmpty(password) & isFingerprintAuthAvailable);
    }

    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    private void enableFingerprint() {
        if (!mFingerprintManagerCompat.isFingerprintAvailable()) {
            mView.setCheckedFingerprint(false);
            mView.showError(mContext.getString(R.string.tutorial_fingerprint_unavailable));
            return;
        }

        showFingerAuthentication();
    }

    private void showFingerAuthentication() {
        showAuthenticationDialog(mContext.getString(R.string.confirm_on_fingerprint_message), new AuthenticationCallback() {
            @Override
            public void onAuthenticated(String password) {

                boolean result = mKeyTools.storePassword(password);
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
        AuthenticationPassword authenticationPassword = new AuthenticationPassword((Activity) mView.getContext(), PasswordUtil.detectSuggestFingerprint(mView.getContext(), mUserConfig), callback);
        authenticationPassword.initialize();
        if (authenticationPassword != null && authenticationPassword.getPasswordManager() != null) {
            try {
                authenticationPassword.getPasswordManager().setTitle(mContext.getString(R.string.input_pin_to_confirm));
            } catch (Exception e) {
                Timber.d("ProtectAccountPresenter setTitle password [%s]", e.getMessage());
            }
        }
      /*  AuthenticationDialog fragment = AuthenticationDialog.newInstance();
        fragment.setStage(Stage.PASSWORD);
        fragment.setMessagePassword(message);
        fragment.setAuthenticationCallback(callback);

        fragment.show(((Activity) mView.getContext()).getFragmentManager(), AuthenticationDialog.TAG);*/
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

    void setUseFingerprint(boolean enable) {
        if (enable) {
            return;
        }
        mUserConfig.removeFingerprint();
    }

    private void logout() {
        Subscription subscription = mPassportRepository.logout()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);

        if (mView == null) {
            return;
        }

        ((UserBaseActivity) mView.getContext()).clearUserSession(null);
    }

    void showConfirmSignOut() {
        new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog)
                .setContentText(getActivity().getString(R.string.txt_confirm_sigout))
                .setCancelText(getActivity().getString(R.string.cancel))
                .setTitleText(getActivity().getString(R.string.confirm))
                .setConfirmText(getActivity().getString(R.string.txt_leftmenu_sigout))
                .setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
                    sweetAlertDialog.dismiss();
                    logout();
                })
                .show();
    }

    void changePassword() {
        try {
            if (mPassword != null && mPassword.isShowing()) {
                return;
            }
            mPassword = new PasswordManager(getActivity());
            mPassword.getBuilder()
                    .setTitle(mContext.getString(R.string.protect_account_current_password))
                    .showFPSuggestCheckBox(false)
                    .showSupportInfo(true)
                    .setNeedHashPass(true)
                    .setPasswordCallBack(changePasswordCallBack);
            mPassword.buildDialog();

            mPassword.show();
        } catch (Exception e) {
            Timber.w("AuthenticationPassword show password [%s]", e.getMessage());
        }
    }

    // Callback from keyboard input
    private IPasswordCallBack changePasswordCallBack = new IPasswordCallBack() {
        @Override
        public void onError(String pError) {
            Timber.d(pError);
        }

        @Override
        public void onCheckedFingerPrint(boolean pChecked) {

        }

        @Override
        public void onClose() {
            setViewStatus(0);
        }

        @Override
        public void onComplete(String pHashPin) {
            switch (getViewStatus()) {
                case STATUS_OLD_PASS_INVALID:
                case STATUS_OLD_PASS_SUCCESS:
                    verifyPassword(pHashPin);
                    break;
                case STATUS_NEW_PASS:
                    setChangePasswordViewStatus(STATUS_CONFIRM_NEW_PASS);
                    mNewPassword = pHashPin;
                    break;
                case STATUS_CONFIRM_NEW_PASS:
                case STATUS_CONFIRM_PASS_INVALID:
                    if (pHashPin.equals(mNewPassword)) {
                        changePin(mOldPassword, mNewPassword);
//                        setChangePasswordViewStatus(STATUS_OTP);
                    } else {
                        setChangePasswordViewStatus(STATUS_CONFIRM_PASS_INVALID);
                    }
                    break;
                case STATUS_OTP:
                case STATUS_OTP_INVALID:
                    if(!TextUtils.isEmpty(pHashPin)) {
                        verifyOTP(pHashPin);
                    }
                    break;

                default:
                    verifyPassword(pHashPin);

            }
        }
    };

    // General set error function
    void setError(String pError) {
        try {
            if (mPassword != null) {
                mPassword.setError(pError);
                mPassword.unlock();
            }
        } catch (Exception e) {
            Timber.w("AuthenticationPassword setError() [%s]", e.getMessage());
        }
    }

    /*
    * Functions about handle view status
    * */
    void onSetOldPasswordSuccess() {
        try {
            mPassword.setTitle(mContext.getString(R.string.protect_account_new_password));
            mPassword.getBuilder().resetPasswordInput().clearText();
        } catch (Exception e) {
            Timber.d("View set old password success error [%s]", e.getMessage());
        }
    }

    void onConfirmNewPasswordInvalid() {
        if (mContext == null) {
            return;
        }

        setError(mContext.getString(R.string.protect_account_confirm_new_password_invalid));
    }

    void onDisplayOTP() {
        try {
            mPassword.setTitle(mContext.getString(R.string.protect_account_otp));
            mPassword.getBuilder().showOTPInputView().resetPasswordInput().clearText();
            mPassword.lock();
        } catch (Exception e) {
            Timber.w("View otp error [%s]", e.getMessage());
        }
    }

    void onNewPassword() {
        try {
            mPassword.setTitle(mContext.getString(R.string.protect_account_new_password));
            mPassword.getBuilder().resetPasswordInput();
            mPassword.getBuilder().clearText();
        } catch (Exception e) {
            Timber.w("View set new password error [%s]", e.getMessage());
        }
    }

    void onConfirmNewPassword() {
        try {
            mPassword.setTitle(mContext.getString(R.string.protect_account_confirm_new_password));
            mPassword.getBuilder().resetPasswordInput();
            mPassword.getBuilder().clearText();
        } catch (Exception e) {
            Timber.w("View set confirm new password error [%s]", e.getMessage());
        }
    }

    void onValidateOTPInvalid() {
        if (mContext == null) {
            return;
        }

        setError(mContext.getString(R.string.protect_account_otp_invalid));
    }

    void setChangePasswordViewStatus(int status) {
        setViewStatus(status);
        switch (status) {
            case STATUS_OLD_PASS_INVALID:
                break;
            case STATUS_OLD_PASS_SUCCESS:
                onSetOldPasswordSuccess();
                break;
            case STATUS_NEW_PASS:
                onNewPassword();
                break;
            case STATUS_CONFIRM_NEW_PASS:
                onConfirmNewPassword();
                break;
            case STATUS_CONFIRM_PASS_INVALID:
                onConfirmNewPasswordInvalid();
                break;
            case STATUS_OTP:
                onDisplayOTP();
                break;
            case STATUS_OTP_INVALID:
                onValidateOTPInvalid();
                break;
        }
    }

    /*
    * Verify functions
    * */
    private void verifyPassword(String password) {
        try {
            mPassword.showLoading(true);
        } catch (Exception e) {
            Timber.d("verifyPassword show loading error [%s]", e.getMessage());
        }
        mOldPassword = password;
        Subscription subscription = mAccountRepository.validatePinSha256(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProtectAccountPresenter.ValidatePinSubscriber());
        mSubscription.add(subscription);
    }

    void changePin(String oldPin, String newPin) {
        Subscription subscription = mAccountRepository.changePasswordSha256(oldPin, newPin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ChangePinSubscriber());
        mSubscription.add(subscription);
    }

    void verifyOTP(String otp) {
        Subscription subscription = mAccountRepository.verifyChangePassword(otp)
                .doOnNext(aBoolean -> {
                    if (!TextUtils.isEmpty(mNewPassword) && mKeyTools.isHavePassword()) {
                        boolean encrypt = mKeyTools.storePassword(mNewPassword);
                        Timber.d("encrypt result %s", encrypt);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifySubscriberOTP());
        mSubscription.add(subscription);
    }


    /*
    * Subscriber
    * */
    private final class ValidatePinSubscriber extends DefaultSubscriber<String> {

        @Override
        public void onError(Throwable e) {
            String message = ErrorMessageFactory.create(getActivity(), e);
            setError(message);
            setViewStatus(STATUS_OLD_PASS_INVALID);
        }

        @Override
        public void onNext(String hashPassword) {
            setChangePasswordViewStatus(STATUS_OLD_PASS_SUCCESS);
            setViewStatus(STATUS_NEW_PASS);
        }
    }

    private class ChangePinSubscriber extends DefaultSubscriber<String> {

        @Override
        public void onStart() {
            try {
                mPassword.showLoading(true);
            } catch (Exception e) {
                Timber.w("ChangePinSubscriber onStart exception [%s]", e.getMessage());
            }
        }

        @Override
        public void onNext(String hashPassword) {
            try {
                mPassword.showLoading(false);
                setChangePasswordViewStatus(STATUS_OTP);
            } catch (Exception e) {
                Timber.w("ChangePinSubscriber onNext exception [%s]", e.getMessage());
            }

        }

        @Override
        public void onError(Throwable e) {
            try {
                String message = ErrorMessageFactory.create(getActivity(), e);
                mPassword.showLoading(false);
                setError(message);
            } catch (Exception exception) {
                Timber.w("ChangePinSubscriber onError exception [%s]", exception.getMessage());
            }
        }
    }

    private final class VerifySubscriberOTP extends DefaultSubscriber<Boolean> {

        @Override
        public void onStart() {
            try {
                mPassword.showLoading(true);
            } catch (Exception e) {
                Timber.w("VerifySubscriberOTP onStart exception [%s]", e.getMessage());
            }
        }

        @Override
        public void onNext(Boolean aBoolean) {
            try {
                mPassword.showLoading(false);
                mPassword.close();
                ToastUtil.showToast(getActivity(), mContext.getString(R.string.protect_account_password_changed));
            } catch (Exception e) {
                Timber.w("VerifySubscriberOTP onNext exception [%s]", e.getMessage());
            }
        }

        @Override
        public void onError(Throwable e) {
            try {
                String message = ErrorMessageFactory.create(getActivity(), e);
                mPassword.showLoading(false);
                setError(message);
                setChangePasswordViewStatus(STATUS_OTP_INVALID);
            } catch (Exception exception) {
                Timber.w("VerifySubscriberOTP onError exception [%s]", exception.getMessage());
            }
        }
    }

    /*
    * Event bus
    * */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onReceiveSmsMessages(ReceiveSmsEvent event) {
        String pattern = "(.*)(\\d{6})(.*)";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        for (ReceiveSmsEvent.SmsMessage message : event.messages) {
            Timber.d("Receive SMS: [%s: %s]", message.from, message.body);
            Matcher m = r.matcher(message.body);
            if (m.find()) {
                Timber.d("Found OTP: %s", m.group(2));
                    mPassword.getBuilder().setOTPValue(m.group(2));
            }
        }

        mEventBus.removeStickyEvent(ReceiveSmsEvent.class);
    }
}