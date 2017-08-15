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
    private static final int INDEX_PHONE_FORMAT = 7;
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

    public static int getMaxLengthPhoneNumber(int lengthDefault) {
        if (mPhoneFormat != null && mPhoneFormat.mMaxLength > 0) {
            return mPhoneFormat.mMaxLength;
        }
        return lengthDefault;
    }

    public static int getMinLengthPhoneNumber(int lengthDefault) {
        if (mPhoneFormat != null && mPhoneFormat.mMinLength > 0) {
            return mPhoneFormat.mMinLength;
        }
        return lengthDefault;
    }

    public static boolean isMobileNumber(String input) {
        return validPhoneFormat(input);
    }

    private static boolean validPhoneFormat(String input) {
        return (!TextUtils.isEmpty(input)
                && validMinLength(input)
                && validMaxLength(input)
                && validateMobileNumberPattern(input));
    }

    public static boolean validLength(@NonNull String input) {
        return (validMinLength(input) && validMaxLength(input));
    }

    private static boolean validMinLength(@NonNull String input) {
        return mPhoneFormat != null && input.length() >= mPhoneFormat.mMinLength;
    }

    private static boolean validMaxLength(@NonNull String input) {
        return mPhoneFormat != null && input.length() <= mPhoneFormat.mMaxLength;
    }

    public static boolean validateMobileNumberPattern(@NonNull String input) {
        if (mPhoneFormat == null || TextUtils.isEmpty(mPhoneFormat.normalizedRegex)) {
            return false;
        }

        try {
            return input.matches(mPhoneFormat.normalizedRegex);
        } catch (PatternSyntaxException e) {
            Timber.w(e, "Valid phone format throw exception.");
        }
        return false;
    }

//    public static boolean validateMobileNumberPattern(String  input) {
//        ArrayList<String> patterns = new ArrayList<>();
//        patterns.add("012\\d{8}");
//        patterns.add("016[2-9]\\d{7}");
//        patterns.add("018(6|8)\\d{7}");
//        patterns.add("0199\\d{7}");
//        patterns.add("086(8|9)\\d{6}");
//        patterns.add("08(8|9)\\d{7}");
//        patterns.add("09\\d{8}");
//
//        ArrayList<String> newPattern = new ArrayList<>();
//        for (String regex : patterns) {
//            newPattern.add(String.format("(^%s$)", regex));
//        }
//
//        String finalRegex = Strings.joinWithDelimiter("|", newPattern);
//        if (input.matches(finalRegex)) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    public static String normalizeMobileNumber(String input) {
        String refinedInput = Strings.stripWhitespace(input);
        refinedInput = refinedInput.replaceAll("^\\+(840|84)", "0");
        refinedInput = refinedInput.replaceAll("^(840|84)", "0");
        return refinedInput;
    }

    public static String formatPhoneNumberWithDot(long number) {
        String formattedNumber = formatPhoneNumber(number);
        return formatPhoneNumberWithSymbol(formattedNumber, ".");
    }

    public static String formatPhoneNumberWithSymbol(String formattedNumber, String symbol) {
        if (TextUtils.isEmpty(formattedNumber)) {
            return formattedNumber;
        }

        try {
            formattedNumber = formattedNumber.substring(0, formattedNumber.length() - 3) + symbol + formattedNumber.substring(formattedNumber.length() - 3, formattedNumber.length());
            formattedNumber = formattedNumber.substring(0, formattedNumber.length() - 7) + symbol + formattedNumber.substring(formattedNumber.length() - 7, formattedNumber.length());
        } catch (Exception e) {
            Timber.w(" Format Phone Number Error [%s]", formattedNumber);
        }
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


    public static String formatPhoneNumberZPC(@NonNull String phone) {
        int lengthPhone = phone.length();
        String formattedNumber;
        if (lengthPhone > INDEX_PHONE_FORMAT) {
            int endIndex = lengthPhone - INDEX_PHONE_FORMAT;
            formattedNumber = phone.substring(0, endIndex) + " " + phone.substring(endIndex, lengthPhone);
        } else {
            formattedNumber = phone;
        }

        return formattedNumber;
    }
}
