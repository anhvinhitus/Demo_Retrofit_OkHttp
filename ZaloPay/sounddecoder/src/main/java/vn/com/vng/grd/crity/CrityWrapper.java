package vn.com.vng.grd.crity;

import android.content.Context;
import android.util.Log;

/**
 * Created by hanv2 on 9/24/15.
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
            if (BuildConfig.DEBUG) {
                ex.printStackTrace();
            }
            try {
                System.load("/data/data/vn.com.vng.vmpay/lib/libcrity.so");
            } catch (Throwable ex2) {
                if (BuildConfig.DEBUG) {
                    ex2.printStackTrace();
                    Log.e("SecurityWrapper", "Cannot load native libs");
                }
            }

        }
    }
}
