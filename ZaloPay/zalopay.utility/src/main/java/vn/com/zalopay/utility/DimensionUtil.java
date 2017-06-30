package vn.com.zalopay.utility;

public class DimensionUtil {
    public static String getScreenType(float density) {
        if (density <= 1.5) {
            return "hdpi";
        } else if (density <= 2) {
            return "xhdpi";
        } else {
            return "xxhdpi";
        }
    }
}
