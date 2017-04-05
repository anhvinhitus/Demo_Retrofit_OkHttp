package vn.com.zalopay.wallet.utils;

import android.content.Context;

public class DimensionUtil {
    public static String getScreenType(Context pContext) {
        float density = pContext.getResources().getDisplayMetrics().density;
        if (density <= 1.5) {
            return "hdpi";
        } else if (density <= 2) {
            return "xhdpi";
        } else if (density <= 3) {
            return "xxhdpi";
        } else if (density <= 4) {
            return "xxhdpi";
        } else {
            return "xhdpi";
        }
    }
}
