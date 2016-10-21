package vn.com.vng.zalopay.account.network.listener;

/**
 * Created by longlv on 10/21/16.
 * Define errorCode & msg that zalo sdk return after login
 * Reference: http://developer.zaloapp.com/docs/android/reference/error-code
 */

public interface ZaloErrorCode {

    int RESULTCODE_PERMISSION_DENIED = -201;
    int RESULTCODE_USER_CANCEL = -1011;
    int RESULTCODE_USER_BACK = -1111;
    int RESULTCODE_USER_REJECT = -1114;
    int RESULTCODE_USER_BACK_BUTTON = 2;
}
