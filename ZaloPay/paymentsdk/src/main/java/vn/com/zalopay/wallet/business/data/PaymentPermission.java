package vn.com.zalopay.wallet.business.data;

import vn.com.zalopay.wallet.utils.Log;

/***
 * allow some action
 * which config from bundle
 */
public class PaymentPermission {
    public static boolean allowUseFingerPrint() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_use_fingerprint_feature));
            return allow > 0;
        } catch (Exception ex) {
            Log.e("allowUserFingerPrint", ex);
            return true;
        }
    }

    public static boolean allowUseTrackingTiming() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_use_tracking_timing));
            return allow > 0;
        } catch (Exception ex) {
            Log.e("allowUseTrackingTiming", ex);
            return true;
        }
    }

    public static boolean allowSendLogOnTransactionFail() {
        try {
            int allow = Integer.parseInt(GlobalData.getStringResource(RS.string.allow_use_send_log_on_transactionfail));
            return allow > 0;
        } catch (Exception ex) {
            Log.e("allowSendLogOnTransactionFail", ex);
            return true;
        }
    }
}
