package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import vn.com.zalopay.wallet.R;
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
            friendlyMessage = GlobalData.getStringResource(RS.string.sdk_website_error_ssl_mess);
        } else if (!TextUtils.isEmpty(description)) {
            friendlyMessage= getDetailMessage();
        }
        return friendlyMessage;
    }

    private String getDetailMessage(){
        if (description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_website_errorcode_not_resolved_domain))) {
            return GlobalData.getStringResource(RS.string.sdk_website_errormess_not_resolved_domain);
        } else if (description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_website_errorcode_timeout_load)) ||
                description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_website_errorcode_timeout_connect))) {
            return GlobalData.getStringResource(RS.string.sdk_website_errormess_timeout_load);
        } else if (description.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_website_errorcode_disconnected))) {
            return GlobalData.getAppContext().getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
        }
        return  null;
    }

    public static boolean isLoadSiteError(String pDescription) {
        return !TextUtils.isEmpty(pDescription) &&
                (pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_website_errorcode_not_resolved_domain))
                        || pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_website_errorcode_timeout_load))
                        || pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.sdk_website_errorcode_timeout_connect)));
    }
}
