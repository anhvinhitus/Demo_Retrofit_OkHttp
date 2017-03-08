package com.zalopay.ui.widget.dialog;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by lytm on 07/03/2017.
 */

public class DiaLogUtils {
    /**
     * Check device phone or tablet
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
