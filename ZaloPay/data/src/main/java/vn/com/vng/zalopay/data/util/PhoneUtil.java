package vn.com.vng.zalopay.data.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longlv on 16/06/2016.
 * Reference https://vi.wikipedia.org/wiki/M%C3%A3_%C4%91i%E1%BB%87n_tho%E1%BA%A1i_Vi%E1%BB%87t_Nam#.C4.90.E1.BA.A7u_s.E1.BB.91_.C4.91i.E1.BB.87n_tho.E1.BA.A1i_di_.C4.91.E1.BB.99ng
 */
public class PhoneUtil {
    private static final List<String> PHONE_VN_CODE = new ArrayList<>();
    private static final List<String> INTERNATIONAL_CODE = new ArrayList<>();

    static {
        PHONE_VN_CODE.add("9");
        PHONE_VN_CODE.add("1");
        PHONE_VN_CODE.add("86");
        PHONE_VN_CODE.add("88");
        PHONE_VN_CODE.add("89");
        PHONE_VN_CODE.add("69");
        PHONE_VN_CODE.add("992");

        INTERNATIONAL_CODE.add("84");
        INTERNATIONAL_CODE.add("+84");
    }

    public static String formatPhoneNumber(long phoneNumber) {
        if (phoneNumber <= 0) {
            return "";
        }
        return formatPhoneNumber(String.valueOf(phoneNumber));
    }

    public static String formatPhoneNumber(String rawNumber) {

        String phoneNumber = rawNumber.replaceAll("\\D+", "");

        if (!isPhoneNumber(phoneNumber)) {
            return phoneNumber;
        }

        for (String s : INTERNATIONAL_CODE) {
            if (phoneNumber.startsWith(s)) {
                return phoneNumber.replaceFirst(s, "0");
            }
        }

        for (String code : PHONE_VN_CODE) {
            if (phoneNumber.startsWith(code)) {
                return phoneNumber.replaceFirst(code, 0 + code);
            }
        }


        return phoneNumber;
    }

    public static boolean isPhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) &&
                !(phoneNumber.length() < 7 || phoneNumber.length() > 13);
    }
}
