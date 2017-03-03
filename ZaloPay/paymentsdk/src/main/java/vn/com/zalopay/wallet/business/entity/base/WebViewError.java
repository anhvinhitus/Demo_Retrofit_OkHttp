package vn.com.zalopay.wallet.business.entity.base;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;

public class WebViewError {
    public static final int SSL_ERROR = -998;

    public int code;
    public String description;

    public WebViewError(int pCode, String pDescription) {
        this.code = pCode;
        this.description = pDescription;
    }

    public String getFriendlyMessage() {
        String friendlyMessage = null;

        if (code == SSL_ERROR) {
            friendlyMessage = GlobalData.getStringResource(RS.string.zpw_fail_transanction_by_ssl);
        } else if (!TextUtils.isEmpty(this.description) && this.description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_name_not_resolved))) {
            friendlyMessage = GlobalData.getStringResource(RS.string.zpw_string_error_friendlymessage_name_not_resolved);
        } else if (!TextUtils.isEmpty(this.description) && this.description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_timeout))) {
            friendlyMessage = GlobalData.getStringResource(RS.string.zpw_string_error_friendlymessage_timeout);
        } else if (!TextUtils.isEmpty(this.description) && this.description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_disconnected))) {
            friendlyMessage = GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);
        }

        return friendlyMessage;
    }
}
