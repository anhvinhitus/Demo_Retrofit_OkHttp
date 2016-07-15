package vn.com.vng.zalopay.data.util;

import java.text.Normalizer;
import java.util.List;

public final class Strings {

    private Strings() {
    }

    public static String joinWithDelimiter(String delimiter, List longList) {
        return joinWithDelimiter(delimiter, longList.toArray());
    }

    @SafeVarargs
    public static <T> String joinWithDelimiter(String delimiter, T... values) {
        StringBuilder sb = new StringBuilder();
        String loopDelimiter = "";
        for (T value : values) {
            if (value != null) {
                sb.append(loopDelimiter);
                sb.append(String.valueOf(value));

                loopDelimiter = delimiter;
            }
        }

        return sb.toString();
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static String nullToEmpty(String string) {
        return nullOrEmptyToDefault(string, "");
    }

    public static String emptyToNull(String string) {
        if (isNullOrEmpty(string)) {
            return null;
        } else {
            return string;
        }
    }

    public static String nullOrEmptyToDefault(String string, String defaultString) {
        return isNullOrEmpty(string) ? defaultString : string;
    }

    public static boolean hasAnyPrefix(String number, String... prefixes) {
        if (number == null) {
            return false;
        }
        for (String prefix : prefixes) {
            if (number.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean equalsIgnoreCase(String first, String second) {
        if (first == null) {
            return second == null;
        } else {
            return first.equalsIgnoreCase(second);
        }
    }

    public static String truncateAt(String string, int length) {
        return string.length() > length ? string.substring(0, length) : string;
    }

    public static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }
}
