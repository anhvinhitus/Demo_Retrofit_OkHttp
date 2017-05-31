package vn.com.vng.zalopay.data.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.model.PhoneFormat;

/**
 * Created by longlv on 16/06/2016.
 * Reference https://vi.wikipedia.org/wiki/M%C3%A3_%C4%91i%E1%BB%87n_tho%E1%BA%A1i_Vi%E1%BB%87t_Nam#.C4.90.E1.BA.A7u_s.E1.BB.91_.C4.91i.E1.BB.87n_tho.E1.BA.A1i_di_.C4.91.E1.BB.99ng
 * Update 11->10 number: http://news.zing.vn/sim-11-so-duoc-chuyen-thanh-10-so-nhu-the-nao-post718826.html
 */
public class PhoneUtil {
    private static final List<String> PHONE_VN_CODE = new ArrayList<>();
    private static final List<String> INTERNATIONAL_CODE = new ArrayList<>();
    //    private final static String PHONE_REGEX = "(\\+84|0)(1\\d{9}|[3-9]\\d{8})";
    private static PhoneFormat mPhoneFormat;

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
        if (TextUtils.isEmpty(rawNumber)) {
            return "";
        }

        String phoneNumber = rawNumber.replaceAll("\\D+", "");

        if (TextUtils.isEmpty(phoneNumber)
                || phoneNumber.length() < 7
                || phoneNumber.length() > 13) {
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

    public static boolean setPhoneFormat(PhoneFormat phoneFormat) {
        if (phoneFormat == null) {
            return false;
        }
        mPhoneFormat = phoneFormat;
        return true;
    }

    public static PhoneFormat getPhoneFormat() {
        return mPhoneFormat;
    }

    public static boolean isMobileNumber(String input) {
        return isMobileNumber(input, false);
    }

    public static boolean isMobileNumber(String input, boolean acceptSpacingBetweenNumbers) {
        if (TextUtils.isEmpty(input)) return false;
        else {
            String refinedInput = input;
            if (acceptSpacingBetweenNumbers) {
                refinedInput = removeSpacingMobileNumber(input);
            }
            return validPhoneFormat(refinedInput);
        }
    }

    private static boolean validPhoneFormat(String input) {
        return (TextUtils.isEmpty(input)
                && validMinLength(input)
                && validMaxLength(input)
                && validPatterns(input));
    }

    public static boolean validLength(@NonNull String input) {
        return (validMinLength(input) && validMaxLength(input));
    }

    private static boolean validMinLength(@NonNull String input) {
        return input.length() >= mPhoneFormat.mMinLength;
    }

    private static boolean validMaxLength(@NonNull String input) {
        return input.length() <= mPhoneFormat.mMaxLength;
    }

    public static boolean validPatterns(@NonNull String input) {
        try {
            if (mPhoneFormat != null && mPhoneFormat.mPatterns != null) {
                for (String pattern : mPhoneFormat.mPatterns) {
                    if (input.matches(pattern)) {
                        return true;
                    }
                }
            }
        } catch (PatternSyntaxException e) {
            Timber.e(e, "Valid phone format throw exception.");
        }
        return false;
    }

    public static String normalizeMobileNumber(String input) {
        return normalizeMobileNumber(input, false);
    }

    public static String normalizeMobileNumber(String input, boolean removePlus84) {
        String refinedInput = removeSpacingMobileNumber(input);
        if (isMobileNumber(input, true)) {
            return refinedInput.replaceAll("\\+84", "0");
        } else {
            return null;
        }
    }

    public static String removeSpacingMobileNumber(String input) {
        if (TextUtils.isEmpty(input)) return input;
        else {
            input = input.trim();
            if (input.startsWith("+")) {
                String inputWithOutPrefix = input.substring(1, input.length());
                return "+" + inputWithOutPrefix.replaceAll("[^\\d]", "");
            } else {
                return input.replaceAll("[^\\d]", "");
            }

        }
    }

    public static String formatPhoneNumberWithSpace(long number) {
        String formattedNumber = formatPhoneNumber(number);
        formattedNumber = formattedNumber.substring(0, formattedNumber.length() - 3) + " " + formattedNumber.substring(formattedNumber.length() - 3, formattedNumber.length());
        formattedNumber = formattedNumber.substring(0, formattedNumber.length() - 7) + " " + formattedNumber.substring(formattedNumber.length() - 7, formattedNumber.length());
        return formattedNumber;
    }

    public static String getPhoneNumberScreened(long phoneNumber) {
        return getPhoneNumberScreened(formatPhoneNumber(phoneNumber));
    }

    public static String getPhoneNumberScreened(String phoneNumber) {
        final int FIRST_NUMBER_SHOW = 3;
        final int LAST_NUMBER_SHOW = 3;
        try {
            if (TextUtils.isEmpty(phoneNumber)) {
                return "";
            } else if (phoneNumber.length() <= (FIRST_NUMBER_SHOW + LAST_NUMBER_SHOW)) {
                return phoneNumber;
            } else {
                String first3Number = phoneNumber.substring(0, FIRST_NUMBER_SHOW);
                String last3Number = phoneNumber.substring(phoneNumber.length() - LAST_NUMBER_SHOW,
                        phoneNumber.length());
                String betweenNumber = phoneNumber.substring(FIRST_NUMBER_SHOW,
                        phoneNumber.length() - LAST_NUMBER_SHOW);
                return String.format("%s %s %s", first3Number, betweenNumber.replaceAll("\\d", "*"), last3Number);
            }
        } catch (Exception e) {
            Timber.e(e, "Function getPhoneNumber throw exception [%s]", e.getMessage());
        }
        return phoneNumber;
    }
}
