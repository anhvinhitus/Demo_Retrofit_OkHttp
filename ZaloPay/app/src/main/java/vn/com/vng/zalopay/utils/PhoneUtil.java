package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

/**
 * Created by longlv on 16/06/2016.
 * Reference https://vi.wikipedia.org/wiki/M%C3%A3_%C4%91i%E1%BB%87n_tho%E1%BA%A1i_Vi%E1%BB%87t_Nam#.C4.90.E1.BA.A7u_s.E1.BB.91_.C4.91i.E1.BB.87n_tho.E1.BA.A1i_di_.C4.91.E1.BB.99ng
 */
public class PhoneUtil {

    public static String toString(long phoneNumber) {
        return formatPhoneNumber(String.valueOf(phoneNumber));
    }

    public static String formatPhoneNumber(long phoneNumber) {
        if (phoneNumber <= 0) {
            return "";
        }
        return formatPhoneNumber(String.valueOf(phoneNumber));
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber) || !isPhoneNumber(phoneNumber)) {
            return "";
        }
        //Ma vung VN
        if (phoneNumber.startsWith("84")) {
            return phoneNumber.replaceFirst("84", "0");
        }
        //10 số đối với đầu 09x
        if (phoneNumber.startsWith("9")) {
            return phoneNumber.replaceFirst("9", "09");
        }
        //11 số đối với đầu 01xx
        if (phoneNumber.startsWith("1")) {
            return phoneNumber.replaceFirst("1", "01");
        }
        //Di động (10 số, đầu số 086, 088, 089)
        if (phoneNumber.startsWith("86")) {
            return phoneNumber.replaceFirst("86", "086");
        }
        if (phoneNumber.startsWith("88")) {
            return phoneNumber.replaceFirst("88", "088");
        }
        if (phoneNumber.startsWith("89")) {
            return phoneNumber.replaceFirst("89", "089");
        }
        //Quân đội - Công an
        if (phoneNumber.startsWith("69")) {
            return phoneNumber.replaceFirst("69", "069");
        }
        //VSAT
        if (phoneNumber.startsWith("992")) {
            return phoneNumber.replaceFirst("992", "0992");
        }
        return phoneNumber;
    }

    public static boolean isPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        return !(phoneNumber.length() < 7 || phoneNumber.length() > 13);
    }
}
