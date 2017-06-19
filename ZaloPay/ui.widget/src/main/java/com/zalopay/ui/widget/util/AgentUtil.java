package com.zalopay.ui.widget.util;

import android.os.Build;

/**
 * Created by lytm on 19/06/2017.
 */

public class AgentUtil {

    public static String getUserAgent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return "Mozilla/5.0 (Linux; Android 5.1.1; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
        } else {
            return "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
        }

    }

    public static String getUserAgent(String userAgent) {
        return getUserAgent() + userAgent;
    }
}
