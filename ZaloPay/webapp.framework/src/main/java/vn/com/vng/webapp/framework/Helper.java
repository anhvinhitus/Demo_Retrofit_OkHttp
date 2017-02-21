package vn.com.vng.webapp.framework;


import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

/**
 * Created by huuhoa on 2/18/17.
 * Helper class
 */

final class Helper {
    private static Handler mApplicationHandler;

    static {
        mApplicationHandler = new Handler(Looper.getMainLooper());
    }

    static void runOnUIThread(Runnable runnable) {
        mApplicationHandler.post(runnable);
    }

    static String toJSONString(JSONObject object) {
        String json = object.toString();
        json = json.replaceAll("'", "&quot;");
        return String.format("'%s'", json);
    }
}
