package vn.com.zalopay.wallet.pay;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.text.TextUtils;

import com.zalopay.ui.widget.password.interfaces.IPasswordCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;
import vn.com.zalopay.wallet.repository.ResourceManager;

/*
 * Created by chucvv on 6/22/17.
 */

public class AuthenActor {
    String fpPassword;//password from fingerprint
    String popupPassword;//password input on popup
    boolean useFPPassword = true;//user check checkbox
    private WeakReference<PayProxy> mPayProxy;
    private DialogFragment mFingerPrintDialog = null;
    private PasswordManager mPassword;
    private IPasswordCallBack mPasswordCallback = new IPasswordCallBack() {
        @Override
        public void onError(String pError) {
            try {
                getProxy().onErrorPasswordPopup();
            } catch (Exception e) {
                Timber.d(e, "password on error");
            }
            Timber.w("password on error %s", pError);
        }

        @Override
        public void onCheckedFingerPrint(boolean pChecked) {
            Timber.d("activate ff for payment later %s", pChecked);
            useFPPassword = pChecked;
        }

        @Override
        public void onClose() {
            Timber.d("user close password - reset some payment data");
            try {
                getProxy().resetResponse();
            } catch (Exception e) {
                Timber.w(e);
            }
        }

        @Override
        public void onComplete(String pHashPassword) {
            if (!TextUtils.isEmpty(pHashPassword)) {
                popupPassword = pHashPassword;
            }
            try {
                getProxy().onCompletePasswordPopup(pHashPassword);
            } catch (Exception e) {
                Timber.d(e, "Exception onComplete");
            }
        }
    };
    private final IFPCallback mFingerPrintCallback = new IFPCallback() {
        @Override
        public void onError(FPError pError) {
            try {
                closeAuthen();
                getProxy().onErrorFingerPrint();
            } catch (Exception e) {
                Timber.d(e, "Exception FF onError");
            }
        }

        @Override
        public void showPassword() {
            Timber.d("showPassword()");
            try {
                getProxy().showPassword();
            } catch (Exception e) {
                Timber.w(e, "Exception show password");
            }
        }

        @Override
        public void onComplete(String pHashPassword) {
            closeAuthen();
            try {
                getProxy().onCompleteFingerPrint(pHashPassword);
            } catch (Exception e) {
                Timber.d(e, "Exception onComplete FF");
            }
            if (!TextUtils.isEmpty(pHashPassword)) {
                fpPassword = pHashPassword;
            }
        }
    };

    public static AuthenActor get() {
        return new AuthenActor();
    }

    public boolean updatePassword() {
        /*
         * user use wrong fingerprint
         * update again password after payment success
         */
        if (!TextUtils.isEmpty(fpPassword) && !TextUtils.isEmpty(popupPassword) && !fpPassword.equals(popupPassword)) {
            try {
                PaymentFingerPrint.shared().updatePassword(fpPassword, popupPassword);
                return useFPPassword && !TextUtils.isEmpty(popupPassword) && shouldUseFPPassword() && PaymentFingerPrint.shared().putPassword(popupPassword);
            } catch (Exception e) {
                Timber.d(e, "Exception update Password");
            }
        }
        return false;
    }

    public AuthenActor plant(PayProxy payProxy) {
        mPayProxy = new WeakReference<>(payProxy);
        return this;
    }

    PayProxy getProxy() throws Exception {
        if (mPayProxy == null || mPayProxy.get() == null) {
            throw new IllegalStateException("invalid pay proxy");
        }
        return mPayProxy.get();
    }

    private boolean shouldUseFPPassword() {
        boolean fingerPrintAvailable = PaymentFingerPrint.shared().isFingerPrintAvailable();
        boolean hasPassword = PaymentFingerPrint.shared().hasPassword();
        return fingerPrintAvailable && !hasPassword;
    }

    public void showPasswordPopup(Activity pActivity, PaymentChannel pPaymentChannel) throws Exception {
        if (mPassword != null && mPassword.isShowing()) {
            return;
        }
        if (pActivity == null || pActivity.isFinishing()) {
            return;
        }
        String logo_path = ResourceManager.getAbsoluteImagePath(pPaymentChannel.channel_icon);
        String title = getProxy().getPaymentInfoHelper().getTitlePassword(pActivity);
        boolean visualCheckbox = shouldUseFPPassword();
        mPassword = new PasswordManager(pActivity);
        mPassword.getBuilder()
                .setTitle(title)
                .setContent(pPaymentChannel.pmcname)
                .setLogoPath(logo_path)
                .showFPSuggestCheckBox(visualCheckbox)
                .setPasswordCallBack(mPasswordCallback);
        mPassword.buildDialog();
        mPassword.show();
    }

    public boolean showFingerPrint(Activity pActivity) throws Exception {
        mFingerPrintDialog = PaymentFingerPrint.shared().getDialogFingerprintAuthentication(pActivity, mFingerPrintCallback);
        if (mFingerPrintDialog != null && !pActivity.isFinishing()) {
            FragmentManager fragmentManager = pActivity.getFragmentManager();
            if (fragmentManager == null) {
                return false;
            }
            mFingerPrintDialog.show(pActivity.getFragmentManager(), null);
            return true;
        } else {
            return false;
        }
    }

    private void closeFingerPrint() throws Exception {
        if (mFingerPrintDialog != null && !mFingerPrintDialog.isDetached()) {
            mFingerPrintDialog.dismissAllowingStateLoss();
            mFingerPrintDialog = null;
            Timber.d("dismiss dialog fingerprint");
        }
    }

    public boolean showLoading() {
        try {
            if (mPassword != null) {
                mPassword.showLoading(true);
                mPassword.lock();
            }
        } catch (Exception e) {
            Timber.d(e, "Exception showLoading");
        }
        return mPassword != null;
    }

    public boolean hideLoading(String pError) {
        try {
            if (mPassword != null) {
                mPassword.setError(pError);
                mPassword.unlock();
            }
        } catch (Exception e) {
            Timber.d(e, "AuthenActor hideLoading");
        }
        return mPassword != null;
    }

    public void closeAuthen() {
        try {
            closeFingerPrint();
            closePassword();
        } catch (Exception e) {
            Timber.d(e, "Exception close authen");
        }
    }

    private void closePassword() {
        try {
            if (mPassword != null) {
                mPassword.close();
                mPassword = null;
            }
        } catch (Exception e) {
            Timber.d(e, "AuthenActor closePassword");
        }
    }

    public void release() {
        mFingerPrintDialog = null;
        mPassword = null;
        mPayProxy = null;
    }
}
