package vn.com.vng.zalopay.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.listener.ZPWOnEventUpdateListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;

/**
 * Created by longlv on 05/08/2016.
 */
public class AppVersionUtils {

    private static final SharedPreferences mPreferences =
            PreferenceManager.getDefaultSharedPreferences(AndroidApplication.instance().getApplicationContext());

    private static final String LATEST_VERSION_IN_SERVER = "latest_version_in_server";
    private static final String UPDATE_MESSAGE_IN_SERVER = "update_message_in_server";

    private static void setLatestVersionInServer(String latestVersion) {
        mPreferences.edit().putString(LATEST_VERSION_IN_SERVER, latestVersion).apply();
    }

    private static String getLatestVersionInServer() {
        return mPreferences.getString(LATEST_VERSION_IN_SERVER, "");
    }

    private static void setUpdateMessageInServer(String message) {
        mPreferences.edit().putString(UPDATE_MESSAGE_IN_SERVER, message).apply();
    }

    private static String getUpdateMessageInServer() {
        return mPreferences.getString(UPDATE_MESSAGE_IN_SERVER, "");
    }


    public static boolean needUpgradeApp(String newVersion, String message) {
        Timber.d("check version, newVersion [%s]", newVersion);
        setLatestVersionInServer(newVersion);
        setUpdateMessageInServer(message);
        return needUpgradeApp();
    }

    public static boolean needUpgradeApp() {
        try {
            return !isLastVersion();
        } catch (NumberFormatException ex) {
            Timber.w("check app version exception [%s]", ex.getMessage());
            return false;
        }
    }

    private static boolean isLastVersion() throws NumberFormatException {
        String appVersion = BuildConfig.VERSION_NAME;
        String lassVersion = getLatestVersionInServer();
        Timber.d("isLastVersion appVersion [%s]", appVersion);
        Timber.d("isLastVersion lastVersion [%s]", lassVersion);
        if (TextUtils.isEmpty(lassVersion)) {
            setLatestVersionInServer(appVersion);
            return true;
        }

        String[] appVersionArr = appVersion.split("\\.");
        String[] lastVersionArr = lassVersion.split("\\.");
        int lastVersionLength = lastVersionArr.length;

        for (int i = 0; i < appVersionArr.length; i++) {
            int appVersionItem = Integer.valueOf(appVersionArr[i]);
            int lastVersionItem = 0;
            if (i < lastVersionLength) {
                lastVersionItem = Integer.valueOf(lastVersionArr[i]);
            }
            if (appVersionItem - lastVersionItem < 0) {
                return false;
            } else if (appVersionItem - lastVersionItem > 0) {
                return true;
            }
        }
        return true;
    }

    public static void showUpgradeAppDialog(final Activity activity) {
        Timber.d("Show update Dialog, context [%s]", activity);
        if (activity == null) {
            return;
        }
        String contentText = getUpdateMessageInServer();
        String newVersion = getLatestVersionInServer();
        if (TextUtils.isEmpty(contentText)) {
            contentText = activity.getString(R.string.need_update_to_use);
        }
        DialogManager.showSweetDialogUpdate(activity, contentText, newVersion,
                activity.getString(R.string.btn_update),
                null,
                new ZPWOnEventUpdateListener() {
                    @Override
                    public void onUpdateListenner() {
                        checkClearSession();
                        AndroidUtils.openPlayStoreForUpdate(activity);
                    }

                    @Override
                    public void onCancelListenner() {

                    }
                });
    }

    private static void checkClearSession() {
        if (AndroidApplication.instance().getUserComponent() == null) {
            return;
        }
        if (AndroidApplication.instance().getUserComponent().currentUser() == null) {
            return;
        }
        AndroidApplication.instance().getAppComponent().applicationSession().clearUserSession();
    }
}
