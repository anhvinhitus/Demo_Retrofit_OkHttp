package vn.com.zalopay.wallet.business.fingerprint;

import android.app.Activity;
import android.app.DialogFragment;

import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;

public abstract class BaseFingerPrint extends SingletonBase implements IPaymentFingerPrint {
    protected IPaymentFingerPrint mPaymentFingerPrint;

    public static boolean isAllowFingerPrintFeature() {
        return PaymentPermission.allowUseFingerPrint();
    }

    public void setPaymentFingerPrint(IPaymentFingerPrint pPaymentFingerPrint) {
        this.mPaymentFingerPrint = pPaymentFingerPrint;
    }

    @Override
    public DialogFragment getDialogFingerprintAuthentication(Activity pActivity, IFPCallback pCallback) throws Exception {
        if (mPaymentFingerPrint == null) {
            throw new Exception("mPaymentFingerPrint is null");
        }
        return mPaymentFingerPrint.getDialogFingerprintAuthentication(pActivity, pCallback);
    }

    @Override
    public void updatePassword(String pOldPassword, String pNewPassword) throws Exception {
        if (mPaymentFingerPrint == null) {
            throw new Exception("mPaymentFingerPrint is null");
        }
        mPaymentFingerPrint.updatePassword(pOldPassword, pNewPassword);
    }

    @Override
    public boolean isFingerPrintAvailable() {
        return mPaymentFingerPrint != null && mPaymentFingerPrint.isFingerPrintAvailable();
    }

    @Override
    public boolean hasPassword() {
        return mPaymentFingerPrint != null && mPaymentFingerPrint.hasPassword();
    }

    @Override
    public boolean putPassword(String pNewPassword) {
        return mPaymentFingerPrint != null && mPaymentFingerPrint.putPassword(pNewPassword);
    }
}
