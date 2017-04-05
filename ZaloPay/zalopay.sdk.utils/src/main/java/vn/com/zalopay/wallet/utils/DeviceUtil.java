package vn.com.zalopay.wallet.utils;

import android.os.Build;
import android.text.TextUtils;

import java.util.UUID;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Log;

public class DeviceUtil {
    public static String getUniqueDeviceID() {
        String uuid = null;
        try {
            if (SharedPreferencesManager.getInstance().getSharedPreferences() != null)
                uuid = SharedPreferencesManager.getInstance().getUDID();

            if (TextUtils.isEmpty(uuid)) {
                uuid = UUID.randomUUID().toString().replace("-", "");
                SharedPreferencesManager.getInstance().setUDID(uuid);
            }

            return uuid;

        } catch (Exception ex) {
            Log.d("getUniqueDeviceID", ex);
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
