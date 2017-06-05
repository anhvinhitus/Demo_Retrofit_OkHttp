package vn.com.zalopay.wallet.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PlayStoreUtils {
    /**
     * Measure campaigns and traffic sources with the Google Analytics.
     * https://developers.google.com/analytics/devguides/collection/android/v4/campaigns
     *
     * @param campaign        title use to analytic
     * @param trackingContent detail use to analytic
     */
    private static String getGooglePlayCampaign(String appname, String campaign, String trackingContent, String appName) {
        String strCampaign = "&referrer=utm_source%3D" +
                appname +
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

    public static String getUrlPlayStore(String package_store, String appname, String campaign, String trackingContent) {
        return "market://details?id=" +
                package_store +
                getGooglePlayCampaign(appname, campaign, trackingContent, "play-store");
    }

    private static String getUrlWebPlayStore(String package_store, String appname, String campaign, String trackingContent) {
        return "https://play.google.com/store/apps/details?id=" +
                package_store +
                getGooglePlayCampaign(appname, campaign, trackingContent, "web");
    }

    private static void openWebPlayStore(Context context, String package_store, String appname, String campaign, String trackingContent) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(getUrlWebPlayStore(package_store, appname, campaign, trackingContent))));
    }

    private static void openPlayStore(Context context, String package_store, String appname, String campaign, String trackingContent)
            throws Exception {
        if (context == null) {
            return;
        }
        Uri uriUrl = Uri.parse(getUrlPlayStore(package_store, appname, campaign, trackingContent));
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            openWebPlayStore(context, package_store, appname, campaign, trackingContent);
        }
    }

    public static void openPlayStoreForUpdate(Context context, String package_store, String appname, String campaign, String trackingContent) {
        try {
            openPlayStore(context, package_store, appname, campaign, trackingContent);
        } catch (Exception ex) {
            openWebPlayStore(context, package_store, appname, campaign, trackingContent);
        }
    }
}
