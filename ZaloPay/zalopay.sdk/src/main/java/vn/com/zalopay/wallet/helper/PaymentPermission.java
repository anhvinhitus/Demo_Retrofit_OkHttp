package vn.com.zalopay.wallet.helper;

import timber.log.Timber;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;

/***
 * allow some action
 * which config from bundle
 */
public class PaymentPermission {

    public static boolean allowVCBNativeFlow() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_vcb_native_flow));
            return allow > 0;
        } catch (Exception ex) {
            Timber.w(ex, "Exception read allow VCB");
            return true;
        }
    }

    public static boolean allowLuhnCC() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_luhn_cc));
            return allow > 0;
        } catch (Exception ex) {
            Timber.w(ex, "Exception read allow Luhn CC");
            return true;
        }
    }

    public static boolean allowLuhnATM() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_luhn_atm));
            return allow > 0;
        } catch (Exception ex) {
            Timber.w(ex, "Exception read allow Luhn ATM");
            return true;
        }
    }

    public static boolean allowUseFingerPrint() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_use_fingerprint_feature));
            return allow > 0;
        } catch (Exception ex) {
            Timber.w(ex, "Exception read allow user ff");
            return true;
        }
    }

    public static boolean allowSendLogOnTransactionFail() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_use_send_log_on_transactionfail));
            return allow > 0;
        } catch (Exception ex) {
            Timber.w(ex, "Exception read allow send logs on trans fail");
            return true;
        }
    }

    public static boolean allowLinkAtm() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_link_atm));
            return allow > 0;
        } catch (Exception ex) {
            Timber.w(ex, "Exception read allow link atm");
            return true;
        }
    }

    public static boolean allowLinkCC() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_link_cc));
            return allow > 0;
        } catch (Exception ex) {
            Timber.w(ex, "Exception read allow link cc");
            return true;
        }
    }

    public static boolean allowLinkChannel() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_link_channel));
            return allow > 0;
        } catch (Exception ex) {
            Timber.d(ex, "Exception read allow link channel");
            return true;
        }
    }
}
