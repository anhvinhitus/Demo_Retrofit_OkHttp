package vn.com.vng.zalopay.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.UUID;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;

/**
 * Created by AnhHieu on 9/14/15.
 */
public class AndroidUtils {
    public static final String TAG = "AndroidUtils";

    public static String getDeviceId() {
        final TelephonyManager tm = (TelephonyManager) AndroidApplication.instance().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(AndroidApplication.instance().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

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
        int measuredWidth = 0;
        int measuredHeight = 0;
        WindowManager w = context.getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            w.getDefaultDisplay().getSize(size);
            measuredWidth = size.x;
            measuredHeight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            measuredWidth = d.getWidth();
            measuredHeight = d.getHeight();
        }
        return measuredWidth;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getSmallerDimen(Activity context) {
        int measuredWidth = 0;
        int measuredHeight = 0;
        WindowManager w = context.getWindowManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            w.getDefaultDisplay().getSize(size);
            measuredWidth = size.x;
            measuredHeight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            measuredWidth = d.getWidth();
            measuredHeight = d.getHeight();
        }
        return measuredWidth < measuredHeight ? measuredWidth : measuredHeight;
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
        strContent.append(to);
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
        boolean app_installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            // DebugUtils.d("CommonUtils:", "checkInstalledOrNot, chua cai roai ");
            app_installed = false;
        }
        return app_installed;
    }

//    public static void openBrowser(Activity activity, String url) {
//        Timber.tag("AndroidUtils").d("openBrowser============");
//        if (activity == null)
//            return;
//        if (TextUtils.isEmpty(url))
//            return;
//        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse(url));
//            if (intent.resolveActivity(activity.getPackageManager()) != null) {
//                activity.startActivity(intent);
//            } else {
//                Timber.tag("AndroidUtils").d("Show toast khong the tim thay browser");
//                CommonUtils.throwToast(activity, "R.string.miss_browser");
//            }
//        } catch (Exception ex) {
//            CommonUtils.throwToast(activity, "R.string.miss_browser");
//        }
//    }
//
//    public static void openPlayStoreFromPackageName(Activity activity,
//                                                    String packageName) {
//        if (activity == null)
//            return;
//        try {
//            String appName = activity.getResources().getString(R.string.app_name);
//
//            Uri uriUrl = Uri
//                    .parse("market://details?id="
//                            + packageName
//                            + "&referrer=utm_source%3D"
//                            + appName
//                            + "%26utm_medium%3Dandroid-app%26utm_campaign%3Dcross-promote");
//            Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
//            if (intent.resolveActivity(activity.getPackageManager()) != null) {
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                activity.startActivity(intent);
//            } else {
//                CommonUtils.throwToast(
//                        activity,
//                        "R.string.miss_playstore");
//            }
//        } catch (Exception ex) {
//            CommonUtils.throwToast(activity,
//                    "R.string.miss_playstore");
//        }
//    }

    public static void startActionDial(Context context, String numberPhone) {
        if (context == null) {
//			context = AndroidApplication.instance();
        }
        if (context == null)
            return;
        if (TextUtils.isEmpty(numberPhone))
            return;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numberPhone));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        ModuleCommon.instance().getGoogleAnalytics().sendGoogleAnalyticsHitEvents(AndroidUtils.class.getSimpleName(), "ContactSupport", "Dial", numberPhone);
    }


    public static void showNotification(Context context, int notificationId, Notification notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    public static Notification getBigTextNotification(Context context, int reqCode, Intent target, String title, String content, String bigTitle, String bigText, int iconResource) {

//        Intent intent = new Intent(context, WelcomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode , target,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle notiStyle = new
                NotificationCompat.BigTextStyle();
        notiStyle.setBigContentTitle(bigTitle);
        notiStyle.bigText(bigText);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(iconResource)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_ticket_leftmenu))
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(notiStyle);
        return notificationBuilder.build();
    }

    public static Notification getDefaultNotification(Context context, String title, String content, int iconResource, Activity activity) {

        Intent intent = new Intent(context, activity.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(iconResource)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_ticket))
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        return notificationBuilder.build();
    }

    @Deprecated
    public static boolean checkNetwork(Context context) {
        if (context == null) {
            context = AndroidApplication.instance();
        }
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
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
                ToastUtil.showToast(context, "context.getResources().getString(R.string.miss_browser)");
            }
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) {
                ex.printStackTrace();
            }
            ToastUtil.showToast(context, "context.getResources().getString(R.string.miss_browser)");
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
                ToastUtil.showToast(context, "context.getResources().getString(R.string.miss_playstore)");
            }
        } catch (Exception ex) {
            ToastUtil.showToast(context, "context.getResources().getString(R.string.miss_playstore)");
        }
    }

}