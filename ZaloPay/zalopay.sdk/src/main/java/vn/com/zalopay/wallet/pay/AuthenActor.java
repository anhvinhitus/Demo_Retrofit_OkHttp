package vn.com.zalopay.wallet.pay;

import android.app.Activity;
import android.app.DialogFragment;
import android.text.TextUtils;

import com.zalopay.ui.widget.password.interfaces.IPinCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PaymentChannel;
import vn.com.zalopay.wallet.business.fingerprint.FPError;
import vn.com.zalopay.wallet.business.fingerprint.IFPCallback;
import vn.com.zalopay.wallet.business.fingerprint.PaymentFingerPrint;

/**
 * Created by chucvv on 6/22/17.
 */

public class AuthenActor {
    private WeakReference<PayProxy> mPayProxy;
    private DialogFragment mFingerPrintDialog = null;
    private String fpPassword;//password from fingerprint
    private String popupPassword;//password input on popup
    private boolean useFPPassword = true;//user check checkbox
    private PasswordManager mPassword;
    private IPinCallBack mPasswordCallback = new IPinCallBack() {
        @Override
        public void onError(String pError) {
            try {
                getProxy().onErrorPasswordPopup();
            } catch (Exception e) {
                Log.e(this, e);
            }
            Log.e(this, pError);
        }

        @Override
        public void onCheckedFingerPrint(boolean pChecked) {
            Log.d(this, "on changed check", pChecked);
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(String pHashPassword) {
            if (!TextUtils.isEmpty(pHashPassword)) {
                popupPassword = pHashPassword;
            }
            try {
                getProxy().onCompletePasswordPopup(pHashPassword);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    };
    private final IFPCallback mFingerPrintCallback = new IFPCallback() {
        @Override
        public void onError(FPError pError) {
            closeAuthen();
            try {
                getProxy().onErrorFingerPrint();
            } catch (Exception e) {
                Log.e(this, e);
            }
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(String pHashPassword) {
            closeAuthen();
            try {
                getProxy().onComleteFingerPrint(pHashPassword);
            } catch (Exception e) {
                Log.e(this, e);
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
        /***
         * user use wrong fingerprint
         * update again password after payment success
         */
        if (!TextUtils.isEmpty(fpPassword) && !TextUtils.isEmpty(popupPassword) && !fpPassword.equals(popupPassword)) {
            try {
                PaymentFingerPrint.shared().updatePassword(fpPassword, popupPassword);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (useFPPassword && !TextUtils.isEmpty(popupPassword) && shouldUseFPPassword()) {
            return PaymentFingerPrint.shared().putPassword(popupPassword);
        }
        return false;
    }

    public AuthenActor plant(PayProxy payProxy) {
        mPayProxy = new WeakReference<>(payProxy);
        return this;
    }

    private PayProxy getProxy() throws Exception {
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
        String logo_path = ResourceManager.getAbsoluteImagePath(pPaymentChannel.channel_icon);
        if (mPassword == null) {
            //just show checkbox when device have fingerprint feature available but user hasn't config password payment yet
            boolean visualCheckbox = shouldUseFPPassword();
            mPassword = new PasswordManager(pActivity, null, pPaymentChannel.pmcname, logo_path, visualCheckbox, mPasswordCallback);
        } else {
            mPassword.setContent(pPaymentChannel.pmcname, logo_path);
        }
        if (mPassword == null) {
            throw new Exception("password popup is not ready");
        }
        mPassword.show();
    }

    public void showFingerPrint(Activity pActivity) throws Exception {
        mFingerPrintDialog = PaymentFingerPrint.shared().getDialogFingerprintAuthentication(pActivity, mFingerPrintCallback);
        if (mFingerPrintDialog != null) {
            mFingerPrintDialog.show(pActivity.getFragmentManager(), null);
        } else {
            throw new Exception("fingerprint is not ready");
        }
    }

    private void closeFingerPrint() {
        if (mFingerPrintDialog != null && !mFingerPrintDialog.isDetached()) {
            mFingerPrintDialog.dismiss();
            mFingerPrintDialog = null;
            Log.d(this, "dismiss dialog fingerprint");
        }
    }

    public boolean showLoading() {
        if (mPassword != null) {
            mPassword.showLoading(true);
            mPassword.lock();
        }
        return mPassword != null;
    }

    public boolean hideLoading(String pError) {
        if (mPassword != null) {
            mPassword.setErrorMessage(pError);
            mPassword.unlock();
        }
        return mPassword != null;
    }

    public void closeAuthen() {
        closePassword();
        closeFingerPrint();
    }

    private void closePassword() {
        if (mPassword != null) {
            mPassword.closePinView();
            mPassword = null;
        }
    }

    public void release() {
        mFingerPrintDialog = null;
        mPayProxy = null;
    }
}
