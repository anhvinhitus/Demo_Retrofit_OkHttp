package vn.com.zalopay.wallet.fingerprint;

import android.app.Activity;
import android.app.DialogFragment;

public interface IPaymentFingerPrint {
    DialogFragment getDialogFingerprintAuthentication(Activity pActivity, final IFPCallback pCallback) throws Exception;

    /***
     * use fingerprint password - when submit server get wrong password fail - this happen when user switch device
     * then sdk move to password popup - payment success - need to update the right password again
     * for fingerprint
     * @param pOldPassword
     * @param pNewPassword
     * @throws Exception
     */
    void updatePassword(String pOldPassword, String pNewPassword) throws Exception;

    /***
     * Device has fingerprint support but user hasn't config payment fingerprint yet
     * UI show checkbox for using fp as default then call this to update
     * @param pNewPassword
     */
    boolean putPassword(String pNewPassword);

    boolean isFingerPrintAvailable();

    /***
     * user set password for fingerprint
     * @return
     */
    boolean hasPassword();
}
