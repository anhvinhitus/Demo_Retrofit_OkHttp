package vn.com.vng.zalopay.data.util;

import java.security.MessageDigest;

import timber.log.Timber;

/**
 * Created by AnhHieu on 8/26/16.
 */
public class Utils {
    public static String sha256Base(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            Timber.e(ex, "sha256Base");
            return "";
        }
    }


    /*
    * join with char |
    * */
    public static String sha256(String... params) {
        StringBuilder builder = new StringBuilder();
        if (params != null) {
            int length = params.length;
            for (int i = 0; i < length; i++) {
                String param = params[i];
                if (i != length - 1) {
                    param += "|";
                }
                builder.append(param);
            }
        }
        Timber.d("pre-sha256 : %s", builder.toString());
        return sha256Base(builder.toString());
    }
}
