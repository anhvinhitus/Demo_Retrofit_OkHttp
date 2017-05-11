package vn.com.vng.zalopay.data.util;

/**
 * Created by huuhoa on 12/6/16.
 * Data converter helper
 */

public class ConvertHelper {
//
//    @SuppressWarnings (value="unchecked")
//    public static <T, U> U unboxValue(T value, U defaultValue) {
//        if (value == null) {
//            return defaultValue;
//        } else {
//            return (U)value;
//        }
//    }

    public static long unboxValue(Long value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static int unboxValue(Integer value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignore) {
            return defaultValue;
        }
    }

      public static byte unboxValue(Byte value, byte defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
