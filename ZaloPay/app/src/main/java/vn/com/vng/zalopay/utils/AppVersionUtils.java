package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 05/08/2016.
 *
 */
public class AppVersionUtils {

    private static final SharedPreferences mPreferences =
            PreferenceManager.getDefaultSharedPreferences(AndroidApplication.instance().getApplicationContext());

    private static final String LATEST_VERSION_IN_SERVER = "latest_version_in_server";

    private static void setLatestVersionInServer(String latestVersion) {
        mPreferences.edit().putString(LATEST_VERSION_IN_SERVER, latestVersion).apply();
    }

    private static String getLatestVersionInServer() {
        return mPreferences.getString(LATEST_VERSION_IN_SERVER, "");
    }

    public static boolean needUpgradeApp(String newVersion) {
        Timber.d("check version, newVersion [%s]", newVersion);
        setLatestVersionInServer(newVersion);
        return  !needUpgradeApp();
    }

    public static boolean needUpgradeApp() {
        return  !isLastVersion();
    }

    private static boolean isLastVersion() {
        String appVersion = BuildConfig.VERSION_NAME;
        String lassVersion = getLatestVersionInServer();
        Timber.d("isLastVersion appVersion [%s]", appVersion);
        Timber.d("isLastVersion lassVersion [%s]", lassVersion);
        if (TextUtils.isEmpty(lassVersion)) {
            setLatestVersionInServer(appVersion);
            return true;
        }
        return appVersion.equals(lassVersion);
    }

    public static void showUpgradeAppDialog(final Context context) {
        Timber.d("Show update Dialog, context [%s]", context);
        if (context == null) {
            return;
        }
        new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog)
                .setContentText(context.getString(R.string.need_update_to_use))
                .setConfirmText(context.getString(R.string.btn_update))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        checkClearSession();
                        AndroidUtils.openPlayStoreForUpdate(context);
                    }
                })
                .setCancelText(context.getString(R.string.btn_cancel))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        checkClearSession();
                    }
                })
                .show();
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
