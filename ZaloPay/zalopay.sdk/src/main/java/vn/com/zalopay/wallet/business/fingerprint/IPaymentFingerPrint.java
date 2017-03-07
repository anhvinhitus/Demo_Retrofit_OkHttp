package vn.com.zalopay.wallet.business.fingerprint;

import android.app.Activity;
import android.app.DialogFragment;

public interface IPaymentFingerPrint {
    DialogFragment getDialogFingerprintAuthentication(Activity pActivity, final IFPCallback pCallback) throws Exception;

    void updatePassword(String pOldPassword, String pNewPassword) throws Exception;

    void showSuggestionDialog(Activity activity, String hashPassword) throws Exception;
}
