package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

/**
 * Created by longlv on 16/06/2016.
 */
public class PhoneUtil {

    public static String toString(long phoneNumber) {
        return formatPhoneNumber(String.valueOf(phoneNumber));
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber) || !isPhoneNumber(phoneNumber)) {
            return "";
        }
        if (phoneNumber.startsWith("84")) {
            return phoneNumber.replace("84", "0");
        }
        if (phoneNumber.startsWith("9")) {
            return phoneNumber.replace("9", "09");
        }
        if (phoneNumber.startsWith("1")) {
            return phoneNumber.replace("1", "01");
        }
        return phoneNumber;
    }

    public static boolean isPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        return !(phoneNumber.length() < 9 || phoneNumber.length() > 13);
    }
}
