package vn.com.vng.zalopay.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import vn.com.vng.zalopay.AndroidApplication;

/**
 * Created by longlv on 11/09/2016.
 *
 */
public class IntroAppUtils {

    private static final SharedPreferences mPreferences =
            PreferenceManager.getDefaultSharedPreferences(AndroidApplication.instance().getApplicationContext());

    private static final String LATEST_TIME_SHOW_INTRO = "latest_time_show_intro";

    public static boolean isShowedIntro() {
        return mPreferences.getBoolean(LATEST_TIME_SHOW_INTRO, false);
    }

    public static void setShowedIntro(boolean showedIntro) {
        mPreferences.edit().putBoolean(LATEST_TIME_SHOW_INTRO, showedIntro).apply();
    }

}
