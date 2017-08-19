package vn.com.zalopay.wallet.helper;

import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.constants.BankAccountError;

/**
 * Created by cpu11843-local on 1/12/17.
 */

public class VcbUtils {
    @BankAccountError
    public static int getVcbType(String pMessage) {
        if (pMessage.toLowerCase().contains(GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_empty_username_mess).toLowerCase())) {
            return BankAccountError.EMPTY_USERNAME;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_empty_password_mess).toLowerCase())) {
            return BankAccountError.EMPTY_PASSWORD;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_empty_captcha_login_mess).toLowerCase())) {
            return BankAccountError.EMPTY_CAPCHA;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_empty_captcha_confirm_mess).toLowerCase())) {
            return BankAccountError.EMPTY_CAPCHA;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_wrong_username_password_mess).toLowerCase())) {
            return BankAccountError.WRONG_USERNAME_PASSWORD;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_account_locked_mess).toLowerCase())) {
            return BankAccountError.ACCOUNT_LOCKED;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getAppContext().getResources().getString(R.string.sdk_vcb_wrong_captcha_mess).toLowerCase())) {
            return BankAccountError.WRONG_CAPTCHA;
        }

        return BankAccountError.OTHERS;
    }
}
