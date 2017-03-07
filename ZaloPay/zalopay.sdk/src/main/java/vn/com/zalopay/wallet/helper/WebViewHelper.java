package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;

public class WebViewHelper {
    public static boolean isLoadSiteError(String pDescription) {
        return !TextUtils.isEmpty(pDescription) && (pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_name_not_resolved))
                || pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_timeout)));
        //|| pDescription.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_error_code_disconnected)));
    }
}
