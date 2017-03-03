package vn.com.vng.zalopay.data.appresources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by longlv on 10/7/16.
 * *
 */
public class ResourceHelper {

    private static String mBundleRootFolder = "";

    public static String getBundleRootFolder() {
        return mBundleRootFolder;
    }

    public static void setBundleRootFolder(String bundleRootFolder) {
        ResourceHelper.mBundleRootFolder = bundleRootFolder;
    }

    /**
     * Returns the path of this module.
     */
    public static String getPath(long appId) {
        if (appId <= 0) {
            return null;
        }
        return String.format(Locale.getDefault(), "%s/modules/%d/app", getBundleRootFolder(), appId);
    }

    /**
     * Returns the path of this module.
     */
    public static String getFontPath(long appId) {
        if (appId <= 0) {
            return null;
        }
        return String.format(Locale.getDefault(), "%s/fonts/", ResourceHelper.getPath(appId));
    }

    public static String getResource(Context context, int appId, String resourceName) {
        if (context == null || TextUtils.isEmpty(resourceName)) {
            return null;
        }
        String screenType = getScreenType(context);
        return String.format(Locale.getDefault(), "%s/modules/%d/app/%s/%s",
                getBundleRootFolder(), appId, screenType, resourceName);
    }

    public static Bitmap getBitmap(Context context, int appId, String resourceName) {
        if (context == null || TextUtils.isEmpty(resourceName)) {
            return null;
        }
        String pathName = getResource(context, appId, resourceName);
        if (TextUtils.isEmpty(pathName)) {
            Timber.w("Not found path of image in resource app 1, image name [%s]", resourceName);
            return null;
        }
        try {
            File file = new File(pathName);
            if (file.exists()) {
                Timber.d("Found image in path success, start set image from bitmap");
                return BitmapFactory.decodeFile(pathName);
            } else {
                Timber.w("Not found image in path [%s]", pathName);
            }
        } catch (NullPointerException e) {
            Timber.e(e, "Get bitmap from file throw exception, path [%s]", pathName);
        } catch (SecurityException e) {
            Timber.e(e, "Check file exits throw exception.");
        }

        return null;
    }

    private static String getScreenType(Context context) {
        if (context == null) {
            return "drawable-xhdpi";
        }

        float density = context.getResources().getDisplayMetrics().density;
        if (density == 0.75) {
            return "drawable-ldpi";
        } else if (density == 1) {
            return "drawable-mdpi";
        } else if (density == 1.5) {
            return "drawable-hdpi";
        } else if (density == 2) {
            return "drawable-xhdpi";
        } else if (density == 3) {
            return "drawable-xxhdpi";
        } else if (density == 4) {
            return "drawable-xxhdpi";
        } else {
            return "drawable-xhdpi";
        }
    }
}
