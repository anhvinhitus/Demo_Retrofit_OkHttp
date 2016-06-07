package vn.com.vng.zalopay.crity;

import android.content.Context;
import android.util.Log;

import vn.com.vng.grd.crity.BuildConfig;

/**
 * Created by hanv2 on 9/24/15.
 *
 */
public class CrityWrapper {
    // Declare native method (and make it public to expose it directly)
    //encrypt session key
    private static native String createSecureKeyPart(Object partName, Object context);

    public static String doCreateSecureKeyPart(String partName, Context context) {
        return createSecureKeyPart(partName, context);
    }

    // Load library
    static {
        try {
            System.loadLibrary("crity");
        } catch (Throwable ex) {
            Log.e("Wrapper", "Error loading crity", ex);
        }
    }
}
