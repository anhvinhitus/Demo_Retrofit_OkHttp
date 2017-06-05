package vn.com.zalopay.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import java.util.UUID;

public class DeviceUtil {
    protected static final String UDID_KEY = "device_id";
    private static final String SHARE_PREFERENCES_NAME = "ZALO_PAY_CACHED";
    public static String getUniqueDeviceID(Context pContext) {
        String uuid = null;
        try {
            SharedPreferences sharedPreferences = pContext.getSharedPreferences(SHARE_PREFERENCES_NAME, 0);
            if (sharedPreferences != null) {
                uuid = sharedPreferences.getString(UDID_KEY, null);
            }
            if (TextUtils.isEmpty(uuid)) {
                uuid = UUID.randomUUID().toString().replace("-", "");
                sharedPreferences.edit().putString(UDID_KEY, uuid).commit();
            }
            return uuid;
        } catch (Exception ex) {
        }
        return null;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
}
