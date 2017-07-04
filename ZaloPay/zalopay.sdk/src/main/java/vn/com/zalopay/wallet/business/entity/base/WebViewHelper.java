package vn.com.zalopay.wallet.business.entity.base;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;

public class WebViewHelper {
    public static final int SSL_ERROR = -998;
    public int code;
    public String description;

    public WebViewHelper(int pCode, String pDescription) {
        this.code = pCode;
        this.description = pDescription;
    }

    public String getFriendlyMessage() {
        String friendlyMessage = null;
        if (code == SSL_ERROR) {
            friendlyMessage = GlobalData.getStringResource(RS.string.zpw_fail_transanction_by_ssl);
        } else if (!TextUtils.isEmpty(description)) {
            friendlyMessage= getDetailMessage();
        }
        return friendlyMessage;
    }

    private String getDetailMessage(){
        if (description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_name_not_resolved))) {
            return GlobalData.getStringResource(RS.string.zpw_string_error_friendlymessage_name_not_resolved);
        } else if (description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_timeout)) ||
                description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_timeout_connection))) {
            return GlobalData.getStringResource(RS.string.zpw_string_error_friendlymessage_timeout);
        } else if (description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_disconnected))) {
            return GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }
        return  null;
    }

    public static boolean isLoadSiteError(String pDescription) {
        return !TextUtils.isEmpty(pDescription) &&
                (pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_name_not_resolved))
                        || pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_timeout))
                        || pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_timeout_connection)));
    }
}
