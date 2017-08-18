package vn.com.zalopay.wallet.helper;

import vn.com.zalopay.wallet.constants.Constants;

/**
 * Created by chucvv on 6/29/17.
 */

public class ListUtils {
    public static String filterMapKey(String keyList, String cardKey) {
        String[] keys = keyList.split(Constants.COMMA);
        int length = keys.length;
        if (length <= 0) {
            return null;
        }
        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String key = keys[i];
            if (key.equals(cardKey)) {
                continue;
            }
            keyBuilder.append(keys[i]);
            if (i < length) {
                keyBuilder.append(Constants.COMMA);
            }
        }
        return keyBuilder.toString();
    }
}
