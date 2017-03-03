package vn.com.zalopay.wallet.business.fingerprint;

import android.app.Activity;
import android.app.DialogFragment;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.PaymentPermission;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.utils.FingerprintUtils;
import vn.com.zalopay.wallet.utils.Log;

public abstract class BaseFingerPrint extends SingletonBase implements IPaymentFingerPrint {
    protected IPaymentFingerPrint mPaymentFingerPrint;

    public static boolean isDeviceSupportFingerPrint() {
        try {
            return FingerprintUtils.isFingerPrintValid(GlobalData.getAppContext());
        } catch (Exception ex) {
            Log.e("isDeviceSupportFingerPrint", ex);
        }
        return false;
    }

    public static boolean isAllowFingerPrintFeature() {
        return PaymentPermission.allowUseFingerPrint();
    }

    public void setPaymentFingerPrint(IPaymentFingerPrint pPaymentFingerPrint) {
        this.mPaymentFingerPrint = pPaymentFingerPrint;
    }

    protected IPaymentFingerPrint getPaymentFingerPrintCallback() {
        return mPaymentFingerPrint;
    }

    @Override
    public DialogFragment getDialogFingerprintAuthentication(Activity pActivity, IFPCallback pCallback) throws Exception {
        if (this.getPaymentFingerPrintCallback() == null) {
            throw new Exception("mPaymentFingerPrint=NULL");
        }

        return this.getPaymentFingerPrintCallback().getDialogFingerprintAuthentication(pActivity, pCallback);
    }

    @Override
    public void updatePassword(String pOldPassword, String pNewPassword) throws Exception {
        if (this.getPaymentFingerPrintCallback() == null) {
            throw new Exception("mPaymentFingerPrint=NULL");
        }
        this.getPaymentFingerPrintCallback().updatePassword(pOldPassword, pNewPassword);
    }

    @Override
    public void showSuggestionDialog(Activity activity, String hashPassword) throws Exception {
        if (this.getPaymentFingerPrintCallback() == null) {
            throw new Exception("mPaymentFingerPrint=NULL");
        }
        this.getPaymentFingerPrintCallback().showSuggestionDialog(activity, hashPassword);
    }
}
