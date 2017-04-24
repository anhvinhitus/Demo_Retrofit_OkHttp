package vn.com.vng.zalopay.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 9/14/15.
 * *
 */
public class AndroidUtils {
    public static final String TAG = "AndroidUtils";

    private static boolean waitingForSms = false;
    private static final Object smsLock = new Object();

    public static int statusBarHeight = 0;
    public static float density = 1;
    public static Point displaySize = new Point();
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static int leftBaseline;
    public static boolean usingHardwareInput;
    private static Boolean isTablet = null;
    private static int adjustOwnerClassGuid = 0;

    public static volatile Handler applicationHandler;

    static {
        density = AndroidApplication.instance().getResources().getDisplayMetrics().density;
        leftBaseline = isTablet(AndroidApplication.instance()) ? 80 : 72;
        checkDisplaySize();
        applicationHandler = new Handler(AndroidApplication.instance().getMainLooper());

        statusBarHeight = AndroidUtils.getStatusBarHeight(AndroidApplication.instance());
    }

    public static String getScreenType() {
        if (density <= 1.5) {
            return "hdpi";
        } else if (density <= 2) {
            return "xhdpi";
        } else if (density <= 3) {
            return "xxhdpi";
        } else if (density <= 4) {
            return "xxhdpi";
        } else {
            return "xhdpi";
        }
    }

    public static String getCarrierName() {
        TelephonyManager manager = (TelephonyManager) AndroidApplication.instance().
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    public static String getNetworkClass() {
        ConnectivityManager cm = (ConnectivityManager) AndroidApplication.instance()
                .getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected())
            return "Unknown"; //not connected
        if (info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "GPRS";
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return "EDGE";
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return "CDMA";
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return "1xRTT";
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "UMTS";
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return "EVDO_0";
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return "EVDO_A";
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return "HSDPA";
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return "HSUPA";
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return "HSPA";
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    return "EVDO_B";
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    return "EHRPD";
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return "4G";
                default:
                    return "Unknown";
            }
        }
        return "Unknown";
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android " + sdkVersion + " (" + release + ")";
    }

    public static String getDeviceManufacturer() {
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;
        return (manufacturer + "/" + model);
    }

    private static final String PREF_NAME = "PREF_UTILS";
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public static String getUUID() {
        SharedPreferences sharedPrefs = AndroidApplication.instance().getSharedPreferences(
                PREF_NAME, Context.MODE_PRIVATE);
        String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (TextUtils.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            sharedPrefs.edit().putString(PREF_UNIQUE_ID, uniqueID).apply();
        }
        return uniqueID;
    }

  /*  public static String getDeviceId() {
        final TelephonyManager tm = (TelephonyManager) AndroidApplication.instance()
                .getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(AndroidApplication.instance()
                .getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }*/

    public static void hideKeyboarInputMethod(Activity activity) {
        if (activity == null)
            return;
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getScreenWidth(Activity context) {
        int measuredWidth;
        WindowManager w = context.getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            w.getDefaultDisplay().getSize(size);
            measuredWidth = size.x;
        } else {
            Display d = w.getDefaultDisplay();
            measuredWidth = d.getWidth();
        }
        return measuredWidth;
    }

    /**
     * Get color string from color resource.
     *
     * @return color string (format #f0f0f0)
     */
    public static String getColorFromResource(@ColorRes int colorResource) {
        if (colorResource == 0) {
            return null;
        }
        try {
            return "#" + Integer.toHexString(ContextCompat.getColor(AndroidApplication.instance(),
                    colorResource));
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    public static float dpToPixels(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int pixelsToDp(Context context, float pixels) {
        float density = context.getResources().getDisplayMetrics().densityDpi;
        return Math.round(pixels / (density / 160f));
    }

    public static void sendMailTo(Context context, String extraSubject, String[] to, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        Intent mailer = Intent.createChooser(intent, title);
        StringBuilder strContent = new StringBuilder();
        strContent.append("To:");
        strContent.append(Arrays.toString(to));
        strContent.append(", title:");
        strContent.append(title);
        strContent.append(", extraSubject:");
        strContent.append(extraSubject);
//        ModuleCommon.instance().getGoogleAnalytics().sendGoogleAnalyticsHitEvents(AndroidUtils.class.getSimpleName(), "Email", "Send", strContent.toString());
        try {
            context.startActivity(mailer);
        } catch (Exception ex) {
        }
    }

    public static String getDensityType(Context context) {
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (densityDpi == DisplayMetrics.DENSITY_XHIGH) {
                return "xhdpi";
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (densityDpi == DisplayMetrics.DENSITY_XXHIGH) {
                return "xxhdpi";
            }
        }
        if (densityDpi == DisplayMetrics.DENSITY_HIGH) {
            return "hdpi";
        } else if (densityDpi == DisplayMetrics.DENSITY_MEDIUM) {
            return "mdpi";
        } else if (densityDpi == DisplayMetrics.DENSITY_LOW) {
            return "ldpi";
        } else {
            return "xhdpi";
        }
    }

    public static boolean checkInstalledOrNot(Activity activity,
                                              String packageName) {
        // DebugUtils.d("CommonUtils:", "checkInstalledOrNot, mactivity: "+ activity);
        if (activity == null)
            return false;
        PackageManager pm = activity.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            // DebugUtils.d("CommonUtils:", "checkInstalledOrNot, chua cai roai ");
            app_installed = false;
        }
        return app_installed;
    }

    public static void openAppInfo(Context context, String packageName) {
        String SCHEME = "package";
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(SCHEME, packageName, null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void openBrowser(Context context, String url) {
        Timber.tag("AndroidUtils").d("openBrowser============url:" + url);
        if (context == null)
            return;
        if (TextUtils.isEmpty(url))
            return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Timber.tag("AndroidUtils").d("Show toast khong the tim thay browser");
                ToastUtil.showToast(context, context.getResources().getString(R.string.miss_browser));
            }
        } catch (Exception ex) {
            Timber.e(ex, "openBrowser exception [%s]", ex.getMessage());
            ToastUtil.showToast(context, context.getResources().getString(R.string.miss_browser));
        }
    }

    public static void openPlayStore(Context context, String packageName) {
        Timber.tag("AndroidUtils").d("openPlayStore============packageName:" + packageName);
        if (context == null)
            return;
        try {
            String appName = "context.getResources().getString(R.string.miss_browser)";

            Uri uriUrl = Uri
                    .parse("market://details?id="
                            + packageName
                            + "&referrer=utm_source%3D"
                            + appName
                            + "%26utm_medium%3Dandroid-app%26utm_campaign%3Dcross-promote");
            Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                ToastUtil.showToast(context, context.getResources().getString(R.string.miss_playstore));
            }
        } catch (Exception ex) {
            ToastUtil.showToast(context, context.getResources().getString(R.string.miss_playstore));
        }
    }

    public static boolean isMainThread() {
        boolean ret = false;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ret = true;
        }
        Timber.d(ret ? " Current Thread is Main Thread " : " Current Thread is Background Thread ");
        return ret;
    }


    public static void requestAdjustResize(Activity activity, int classGuid) {
        if (activity == null || isTablet(activity)) {
            return;
        }
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        adjustOwnerClassGuid = classGuid;
    }

    public static void removeAdjustResize(Activity activity, int classGuid) {
        if (activity == null || isTablet(activity)) {
            return;
        }
        if (adjustOwnerClassGuid == classGuid) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    public static boolean isWaitingForSms() {
        boolean value;
        synchronized (smsLock) {
            value = waitingForSms;
        }
        return value;
    }

    public static void setWaitingForSms(boolean value) {
        synchronized (smsLock) {
            waitingForSms = value;
        }
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
/*

    public static boolean isKeyboardShowed(View view) {
        if (view == null) {
            return false;
        }
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputManager.isActive(view);
    }
*/

    public static boolean isKeyboardShowed(View rootView) {
            /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        //DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
            /* heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
        int heightDiff = rootView.getBottom() - r.bottom;
            /* Threshold size: dp to pixels, multiply with display density */
        return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * density;
    }


    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) return;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static File getCacheDir() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {

        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file = AndroidApplication.instance().getExternalCacheDir();
                if (file != null) {
                    return file;
                }
            } catch (Exception e) {

            }
        }
        try {
            File file = AndroidApplication.instance().getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Exception e) {

        }
        return new File("");
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static float dpf2(float value) {
        if (value == 0) {
            return 0;
        }
        return density * value;
    }

    public static void checkDisplaySize() {
        try {
            Configuration configuration = AndroidApplication.instance().getResources().getConfiguration();
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) AndroidApplication.instance().getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    if (Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    private final static Object _lock = new Object();

    public static void runOnUIThread(Runnable runnable, long delay) {
        // synchronized (_lock) {
        if (delay == 0) {
            applicationHandler.post(runnable);
        } else {
            applicationHandler.postDelayed(runnable, delay);
        }
        // }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        applicationHandler.removeCallbacks(runnable);
    }

    /*  public static boolean isTablet() {
          if (isTablet == null) {

              //Fixme
              //     isTablet = AndroidApplication.instance().getResources().getBoolean(R.bool.isTablet);
          }
          return isTablet;
      }
  */


    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }


    public static boolean isSmallTablet() {
        float minSide = Math.min(displaySize.x, displaySize.y) / density;
        return minSide <= 700;
    }

    public static int getMinTabletSide() {
        if (!isSmallTablet()) {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int leftSide = smallSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return smallSide - leftSide;
        } else {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int maxSide = Math.max(displaySize.x, displaySize.y);
            int leftSide = maxSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return Math.min(smallSide, maxSide - leftSide);
        }
    }

    public static void clearCursorDrawable(EditText editText) {
        if (editText == null || Build.VERSION.SDK_INT < 12) {
            return;
        }
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.setInt(editText, 0);
        } catch (Exception e) {

        }
    }


   /* public static Point getRealScreenSize() {
        Point size = new Point();
        try {
            WindowManager windowManager = (WindowManager) AndroidApplication.instance().getSystemService(Context.WINDOW_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                windowManager.getDefaultDisplay().getRealSize(size);
            } else {
                try {
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    size.set((Integer) mGetRawW.invoke(windowManager.getDefaultDisplay()), (Integer) mGetRawH.invoke(windowManager.getDefaultDisplay()));
                } catch (Exception e) {
                    size.set(windowManager.getDefaultDisplay().getWidth(), windowManager.getDefaultDisplay().getHeight());

                }
            }
        } catch (Exception e) {

        }
        return size;
    }*/

    @SuppressLint("NewApi")
    public static void clearDrawableAnimation(View view) {
        if (Build.VERSION.SDK_INT < 21 || view == null) {
            return;
        }
        Drawable drawable;
        if (view instanceof ListView) {
            drawable = ((ListView) view).getSelector();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
            }
        } else {
            drawable = view.getBackground();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
                drawable.jumpToCurrentState();
            }
        }
    }

    public static final int FLAG_TAG_BR = 1;
    public static final int FLAG_TAG_BOLD = 2;
    public static final int FLAG_TAG_COLOR = 4;
    public static final int FLAG_TAG_ALL = FLAG_TAG_BR | FLAG_TAG_BOLD | FLAG_TAG_COLOR;


    public static void addMediaToGallery(String fromPath) {
        if (fromPath == null) {
            return;
        }
        File f = new File(fromPath);
        Uri contentUri = Uri.fromFile(f);
        addMediaToGallery(contentUri);
    }

    public static void addMediaToGallery(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            AndroidApplication.instance().sendBroadcast(mediaScanIntent);
        } catch (Exception e) {

        }
    }

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static byte[] decodeQuotedPrintable(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i];
            if (b == '=') {
                try {
                    final int u = Character.digit((char) bytes[++i], 16);
                    final int l = Character.digit((char) bytes[++i], 16);
                    buffer.write((char) ((u << 4) + l));
                } catch (Exception e) {

                    return null;
                }
            } else {
                buffer.write(b);
            }
        }
        byte[] array = buffer.toByteArray();
        try {
            buffer.close();
        } catch (Exception e) {

        }
        return array;
    }

    public static boolean copyFile(InputStream sourceFile, File destFile) throws IOException {
        OutputStream out = new FileOutputStream(destFile);
        byte[] buf = new byte[4096];
        int len;
        while ((len = sourceFile.read(buf)) > 0) {
            Thread.yield();
            out.write(buf, 0, len);
        }
        out.close();
        return true;
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileInputStream source = null;
        FileOutputStream destination = null;
        try {
            source = new FileInputStream(sourceFile);
            destination = new FileOutputStream(destFile);
            destination.getChannel().transferFrom(source.getChannel(), 0, source.getChannel().size());
        } catch (Exception e) {

            return false;
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return true;
    }


    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight >= 0) return statusBarHeight;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static void animateLike(View v) {
        ScaleAnimation scal = new ScaleAnimation(1f, 1.8f, 1, 1.8f, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
        scal.setDuration(250);
        scal.setFillAfter(true);
        scal.setRepeatCount(1);
        scal.setRepeatMode(Animation.REVERSE);
        v.startAnimation(scal);
    }

  /*  public static void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);

        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, regular);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }*/

    public static void deleteFile(String path) {
        try {
            File oldDir = new File(path);
            deleteRecursive(oldDir);
        } catch (Exception ex) {
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static void writeToFile(final String fileContents, String path) throws IOException {
        FileWriter out = null;
        try {
            out = new FileWriter(new File(path));
            out.write(fileContents);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static String readFromFile(String filePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;
        FileReader fr = null;
        try {

            fr = new FileReader(new File(filePath));
            in = new BufferedReader(fr);
            while ((line = in.readLine()) != null) stringBuilder.append(line);

        } finally {
            if (in != null) in.close();
            if (fr != null) fr.close();
        }
        return stringBuilder.toString();
    }

    public static boolean unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {

            ZipEntry ze;
            int count;
            byte[] buffer = new byte[1024];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    return false;
                }
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            if (zis != null) zis.close();
        }

        return true;
    }

    public static boolean unzip(String source, String destinationDirectory) throws IOException {
        return unzip(new File(source), new File(destinationDirectory));
    }


    public static String unzip2(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));

        String dirPath = null;
        try {

            ZipEntry ze;
            int count;
            byte[] buffer = new byte[1024 * 2];

            boolean isSetPath = false;

            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir;
                if (ze.isDirectory()) {
                    if (!isSetPath) {
                        isSetPath = true;
                        dirPath = targetDirectory.getAbsolutePath() + "/" + ze.getName();
                    }
                    dir = file;
                } else {
                    dir = file.getParentFile();
                }

                if (!dir.isDirectory() && !dir.mkdirs()) {
                    return null;
                }
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            if (zis != null) zis.close();
        }

        return dirPath;
    }

    public static String unzip2(String source, String destinationDirectory) throws IOException {
        return unzip2(new File(source), new File(destinationDirectory));
    }

    public static String readFileFromAsset(Context context, String fileName) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets().open(fileName);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line;
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
            }
        }
        return returnString.toString();
    }

    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    private static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
        if (Build.VERSION.SDK_INT >= 21) {
            Map<String, Typeface> newMap = new HashMap<>();
            newMap.put("sans-serif", newTypeface);
            try {
                final Field staticField = Typeface.class.getDeclaredField("sSystemFontMap");
                staticField.setAccessible(true);
                staticField.set(null, newMap);
            } catch (NoSuchFieldException e) {
                Timber.e(e, "replaceFont exception [%s]", e.getMessage());
            } catch (IllegalAccessException e) {
                Timber.e(e, "replaceFont exception [%s]", e.getMessage());
            }
        } else {
            try {
                final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
                staticField.setAccessible(true);
                staticField.set(null, newTypeface);
            } catch (NoSuchFieldException e) {
                Timber.e(e, "replaceFont exception [%s]", e.getMessage());
            } catch (IllegalAccessException e) {
                Timber.e(e, "replaceFont exception [%s]", e.getMessage());
            }
        }
    }

    public static void setSpannedMessageToView(TextView tv,
                                               String message,
                                               String spannedMessage,
                                               boolean isUnderline,
                                               boolean isMessageBold,
                                               int linkColor,
                                               ClickableSpan clickableSpan) {
        if (tv != null) {
            // set spannable for text view
            int startIndex = message.indexOf("%s");
            int endIndex = startIndex + spannedMessage.length();
            message = String.format(message, spannedMessage);
            Spannable span = Spannable.Factory.getInstance().newSpannable(message);
            // set span color
            span.setSpan(new ForegroundColorSpan(linkColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // set span underline
            if (isUnderline) {
                span.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // set bold message
            if (isMessageBold) {
                span.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            span.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv.setText(span);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static void setSpannedMessageToView(TextView tv,
                                               int message,
                                               int spannedMessage,
                                               boolean isUnderline,
                                               boolean isMessageBold,
                                               @ColorRes int linkColorResId,
                                               ClickableSpan clickableSpan) {
        Context context = AndroidApplication.instance();

        setSpannedMessageToView(tv, context.getString(message),
                context.getString(spannedMessage),
                isUnderline, isMessageBold, ContextCompat.getColor(context, linkColorResId), clickableSpan);
    }

    /**
     * Measure campaigns and traffic sources with the Google Analytics.
     * https://developers.google.com/analytics/devguides/collection/android/v4/campaigns
     *
     * @param campaign        title use to analytic
     * @param trackingContent detail use to analytic
     */
    private static String getGooglePlayCampaign(String campaign, String trackingContent, String appName) {
        String strCampaign = "&referrer=utm_source%3D" +
                AndroidApplication.instance().getResources().getString(R.string.app_name) +
                "%26utm_medium%3D" +
                "android-app" +
                "%26utm_content%3D" +
                trackingContent +
                "%26utm_campaign%3D" +
                campaign +
                "%26utm_term%3D" +
                appName;
        return strCampaign;
    }

    public static String getUrlPlayStore(String campaign, String trackingContent) {
        return "market://details?id=" +
                BuildConfig.PACKAGE_IN_PLAY_STORE +
                getGooglePlayCampaign(campaign, trackingContent, "play-store");
    }

    private static String getUrlWebPlayStore(String campaign, String trackingContent) {
        return "https://play.google.com/store/apps/details?id=" +
                BuildConfig.PACKAGE_IN_PLAY_STORE +
                getGooglePlayCampaign(campaign, trackingContent, "web");
    }

    private static void openWebPlayStore(Context context, String campaign, String trackingContent) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(getUrlWebPlayStore(campaign, trackingContent))));
    }

    private static void openPlayStore(Context context, String campaign, String trackingContent)
            throws Exception {
        if (context == null) {
            return;
        }
        Uri uriUrl = Uri.parse(getUrlPlayStore(campaign, trackingContent));
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Timber.w(context.getResources().getString(R.string.miss_playstore));
            openWebPlayStore(context, campaign, trackingContent);
        }
    }

    public static void openPlayStoreForUpdate(Context context, String campaign, String trackingContent) {
        try {
            openPlayStore(context, campaign, trackingContent);
        } catch (Exception ex) {
            Timber.w(ex, "open PlayStore for update exception [%s]", ex.getMessage());
            openWebPlayStore(context, campaign, trackingContent);
        }
    }

    public static int getFrontCameraId(CameraManager cManager) {
        if (Build.VERSION.SDK_INT < 22) {
            Camera.CameraInfo ci = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, ci);
                if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) return i;
            }
        } else {
            try {
                for (int j = 0; j < cManager.getCameraIdList().length; j++) {
                    String[] cameraId = cManager.getCameraIdList();
                    CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId[j]);
                    int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT)
                        return j;
                }
            } catch (Exception e) {
                Timber.d(e, "get front camera Id");
            }
        }

        return -1; // No front-facing camera found
    }

    public static void validateMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Must be called from the main thread.");
        }
    }

    public static boolean isHttpRequest(String input) {
        String URL_REGEX = "^((https?|http)://|www\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
}