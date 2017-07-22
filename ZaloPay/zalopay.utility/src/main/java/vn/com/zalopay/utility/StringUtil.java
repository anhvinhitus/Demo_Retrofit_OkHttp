package vn.com.zalopay.utility;

import android.text.TextUtils;

public class StringUtil {
    public static String getFirstStringWithSize(String pString, int pSize) {
        if (TextUtils.isEmpty(pString) || pString.length() <= pSize) {
            return "";
        }
        return pString.substring(0, pSize);
    }

    public static String getLastStringWithSize(String pString, int pSize) {
        if (TextUtils.isEmpty(pString)) {
            return "";
        }
        return pString.substring(pString.length() - pSize);
    }
}
