package vn.com.zalopay.wallet.utils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.enumeration.EVCBType;

/**
 * Created by cpu11843-local on 1/12/17.
 */

public class VcbUtils {
    public static EVCBType getVcbType(String pMessage) {
        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_username) == null ?
                "nhập tên truy cập!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_username).toLowerCase())) {
            return EVCBType.EMPTY_USERNAME;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_password) == null ?
                "nhập mật khẩu!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_password).toLowerCase())) {
            return EVCBType.EMPTY_PASSWORD;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_login) == null ?
                "nhập mã xác nhận!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_login).toLowerCase())) {
            return EVCBType.EMPTY_CAPCHA;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_confirm) == null ?
                "nhập mã kiểm tra!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_empty_captcha_confirm).toLowerCase())) {
            return EVCBType.EMPTY_CAPCHA;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_username_password) == null ?
                "Tên truy cập hoặc mật khẩu không chính xác.".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_username_password).toLowerCase())) {
            return EVCBType.WRONG_USERNAME_PASSWORD;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_account_locked) == null ?
                "Dịch vụ đã bị khóa.".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_account_locked).toLowerCase())) {
            return EVCBType.ACCOUNT_LOCKED;
        }

        if (pMessage.toLowerCase().contains(GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_captcha) == null ?
                "Mã kiểm tra không chính xác!".toLowerCase() : GlobalData.getStringResource(RS.string.zpw_string_vcb_wrong_captcha).toLowerCase())) {
            return EVCBType.WRONG_CAPTCHA;
        }

        return EVCBType.OTHERS;
    }
}
