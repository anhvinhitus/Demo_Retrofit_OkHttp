package vn.com.vng.zalopay.data.appresources;

import android.content.Context;

import java.util.Locale;

/**
 * Created by longlv on 10/7/16.
 * *
 */
public class ResourceHelper {

    private static String mBundleRootFolder = "";
    public static String getBundleRootFolder() {
        return mBundleRootFolder;
    }

    public static void setBundleRootFolder(String mBundleRootFolder) {
        ResourceHelper.mBundleRootFolder = mBundleRootFolder;
    }

    /**
     * Returns the path of this module.
     */
    public static String getPath(int appId) {
        return String.format(Locale.getDefault(), "%s/modules/%d/app", getBundleRootFolder(), appId);
    }

    public static String getResource(Context context, int appId, String resourceName) {
        String screenType = getScreenType(context);
        return String.format(Locale.getDefault(), "%s/modules/%d/app/%s/%s",
                getBundleRootFolder(), appId, screenType, resourceName);
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
            return "drawable-xxxhdpi";
        } else {
            return "drawable-xhdpi";
        }
    }
}
