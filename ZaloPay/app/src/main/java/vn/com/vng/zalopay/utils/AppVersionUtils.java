package vn.com.vng.zalopay.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventUpdateListener;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.event.ForceUpdateAppEvent;
import vn.com.zalopay.utility.PlayStoreUtils;

/**
 * Created by longlv on 05/08/2016.
 * Management app version, show dialog when new version available.
 */
public class AppVersionUtils {

    private static final SharedPreferences mPreferences =
            PreferenceManager.getDefaultSharedPreferences(AndroidApplication.instance().getApplicationContext());

    private static final String LATEST_VERSION_IN_SERVER = "latest_version_in_server";
    private static final String UPDATE_MESSAGE_IN_SERVER = "update_message_in_server";
    private static final String FORCE_UPDATE_APP = "force_update_app";
    private static final String SHOWED_DIALOG_UPDATE_APP = "showed_dialog_update_app";

    private AppVersionUtils() {
        // private constructor for utils class
    }

    private static void showedDialogUpdateApp(String appVersion) {
        mPreferences.edit().putString(SHOWED_DIALOG_UPDATE_APP, appVersion).apply();
    }

    private static boolean isShowedDialogUpdateApp(String version) {
        String currentVersionShowed = mPreferences.getString(SHOWED_DIALOG_UPDATE_APP, "");
        Timber.d("isShowedDialogUpdateApp version[%s] versionShowed[%s]", version, currentVersionShowed);
        return !TextUtils.isEmpty(currentVersionShowed) && currentVersionShowed.equals(version);
    }

    private static boolean isForceUpdateApp() {
        return mPreferences.getBoolean(FORCE_UPDATE_APP, false);
    }

    private static void setForceUpdateApp(boolean forceUpdateApp) {
        mPreferences.edit().putBoolean(FORCE_UPDATE_APP, forceUpdateApp).apply();
    }

    private static String getLatestVersionInServer() {
        return mPreferences.getString(LATEST_VERSION_IN_SERVER, "");
    }

    private static void setLatestVersionInServer(String latestVersion) {
        mPreferences.edit().putString(LATEST_VERSION_IN_SERVER, latestVersion).apply();
    }

    private static String getUpdateMessageInServer() {
        return mPreferences.getString(UPDATE_MESSAGE_IN_SERVER, "");
    }

    private static void setUpdateMessageInServer(String message) {
        mPreferences.edit().putString(UPDATE_MESSAGE_IN_SERVER, message).apply();
    }

    private static boolean needUpgradeApp() {
        try {
            return !isLastVersion();
        } catch (NumberFormatException ex) {
            Timber.w("check app version exception [%s]", ex.getMessage());
            return false;
        }
    }

    /**
     * Convert app version from x.y.z to x.y
     *
     * @return main version.
     */
    public static String getMainVersionName() {
        String appVersion = BuildConfig.VERSION_NAME;
        String[] appVersionArr = appVersion.split("\\.");
        if (appVersionArr.length < 2) {
            return appVersion;
        }
        return appVersionArr[0] + "." + appVersionArr[1];
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

    private static void clearSession() {
        EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
        eventBus.postSticky(new ForceUpdateAppEvent());
    }

    private static void showDialogUpgradeApp(final Activity activity, final boolean forceUpdate) {
        Timber.d("Show upgrade dialog, context [%s] forceUpdate[%s]", activity, forceUpdate);
        String contentText = getUpdateMessageInServer();
        String newVersion = getLatestVersionInServer();
        if (activity == null) {
            return;
        }
        if (TextUtils.isEmpty(contentText)) {
            if (forceUpdate) {
                contentText = activity.getString(R.string.need_update_to_use);
            } else {
                contentText = activity.getString(R.string.recommend_update_to_use);
            }
        }
        showedDialogUpdateApp(newVersion);
        DialogHelper.showSweetDialogUpdate(activity,
                contentText,
                newVersion,
                new ZPWOnEventUpdateListener() {
                    @Override
                    public void onUpdateListenner() {
                        if (forceUpdate) {
                            clearSession();
                        }
                        PlayStoreUtils.openPlayStoreForUpdate(activity, BuildConfig.PACKAGE_IN_PLAY_STORE, AndroidApplication.instance().getResources().getString(R.string.app_name), "force-app-update", "home-page");
                    }

                    @Override
                    public void onCancelListenner() {

                    }
                }, forceUpdate);
    }

    public static boolean showDialogForceUpgradeApp(Activity activity) {
        if (!needUpgradeApp() || !isForceUpdateApp()) {
            return false;
        }
        showDialogUpgradeApp(activity, true);
        return true;
    }

    private static boolean showDialogUpgradeAppIfNeed(Activity activity) {
        boolean upgradeApp = needUpgradeApp();
        if (!upgradeApp) {
            return false;
        }
        if (isForceUpdateApp()) {
            showDialogUpgradeApp(activity, true);
            return true;
        } else if (!isShowedDialogUpdateApp(getLatestVersionInServer())) {
            showDialogUpgradeApp(activity, false);
            return true;
        } else {
            return false;
        }
    }

    public static boolean handleEventUpdateVersion(Activity activity,
                                                   boolean forceUpdate,
                                                   String latestVersion,
                                                   String message) {
        setVersionInfoInServer(forceUpdate, latestVersion, message);
        return showDialogUpgradeAppIfNeed(activity);
    }

    private static void setVersionInfoInServer(boolean forceUpdate,
                                               String latestVersion,
                                               String msg) {
        setForceUpdateApp(forceUpdate);
        setLatestVersionInServer(latestVersion);
        setUpdateMessageInServer(msg);
    }

    public static void clearData() {
        showedDialogUpdateApp("");
        setForceUpdateApp(false);
        setLatestVersionInServer("");
        setUpdateMessageInServer("");
    }
}
