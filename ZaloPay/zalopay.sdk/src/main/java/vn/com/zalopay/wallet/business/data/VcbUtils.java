package vn.com.zalopay.wallet.business.data;

import vn.com.zalopay.wallet.constants.BankAccountError;

/**
 * Created by cpu11843-local on 1/12/17.
 */

public class VcbUtils {
    @BankAccountError
    public static int getVcbType(String pMessage) {
        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_username) == null ?
                "nhập tên truy cập!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_username).toLowerCase())) {
            return BankAccountError.EMPTY_USERNAME;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_password) == null ?
                "nhập mật khẩu!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_password).toLowerCase())) {
            return BankAccountError.EMPTY_PASSWORD;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_login) == null ?
                "nhập mã xác nhận!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_login).toLowerCase())) {
            return BankAccountError.EMPTY_CAPCHA;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_confirm) == null ?
                "nhập mã kiểm tra!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_confirm).toLowerCase())) {
            return BankAccountError.EMPTY_CAPCHA;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_username_password) == null ?
                "Tên truy cập hoặc mật khẩu không chính xác.".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_username_password).toLowerCase())) {
            return BankAccountError.WRONG_USERNAME_PASSWORD;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_account_locked) == null ?
                "Dịch vụ đã bị khóa.".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_account_locked).toLowerCase())) {
            return BankAccountError.ACCOUNT_LOCKED;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_captcha) == null ?
                "Mã kiểm tra không chính xác!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_captcha).toLowerCase())) {
            return BankAccountError.WRONG_CAPTCHA;
        }

        return BankAccountError.OTHERS;
    }
}
