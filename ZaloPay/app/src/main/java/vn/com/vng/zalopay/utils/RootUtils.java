package vn.com.vng.zalopay.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import vn.com.vng.zalopay.AndroidApplication;

/**
 * @author Kevin Kowalewski
 */
public class RootUtils {

    private static final SharedPreferences mPreferences =
            PreferenceManager.getDefaultSharedPreferences(AndroidApplication.instance().getApplicationContext());

    private static Boolean mIsRoot = null;

    //should show or not WarningRooted page.
    private static final String PREF_WARNING_ROOTED_REMIND = "pref_warning_rooted_remind";
    //last checkbox state. Default is true, if uncheck is false.
    private static final String PREF_LASTEST_CHECKBOX_STATE = "pref_lastest_checkbox_state";

    public static boolean isHideWarningRooted() {
        return mPreferences.getBoolean(PREF_WARNING_ROOTED_REMIND, false);
    }

    public static void setHideWarningRooted(boolean showedIntro) {
        mPreferences.edit().putBoolean(PREF_WARNING_ROOTED_REMIND, showedIntro).apply();
    }

    public static boolean isDeviceRooted() {
        if (mIsRoot == null) {
            mIsRoot = (checkRootMethod1() || checkRootMethod2() || checkRootMethod3());
        }
        return mIsRoot;
    }

    public static void setLastestCheckboxState(boolean checked) {
        mPreferences.edit().putBoolean(PREF_LASTEST_CHECKBOX_STATE, checked).apply();
    }

    public static boolean isCheckboxCheckedate() {
        return mPreferences.getBoolean(PREF_LASTEST_CHECKBOX_STATE, true);
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean findBinary(String binaryName) {
        boolean found = false;
        String[] paths = {"/system/app/Superuser.apk",
                "/sbin/",
                "/system/bin/",
                "/system/xbin/",
                "/data/local/xbin/",
                "/data/local/bin/",
                "/system/sd/xbin/",
                "/system/bin/failsafe/",
                "/data/local/",
                "/su/bin/"};
        for (String path : paths) {
            if (new File(path + binaryName).exists()) {
                found = true;
                break;
            }
        }
        return found;
    }

    private static boolean checkRootMethod2() {
        return findBinary("su");
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        BufferedReader in = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // empty catch block
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

}
