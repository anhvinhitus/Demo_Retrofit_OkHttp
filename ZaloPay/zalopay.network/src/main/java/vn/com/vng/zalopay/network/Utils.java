package vn.com.vng.zalopay.network;

import java.util.List;

/**
 * Created by hieuvm on 5/12/17.
 * *
 */

class Utils {
    static String pathSegmentsToString(List pathSegments) {
        return joinWithDelimiter("/", pathSegments.toArray());
    }

    @SafeVarargs
    static <T> String joinWithDelimiter(String delimiter, T... values) {
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
}
