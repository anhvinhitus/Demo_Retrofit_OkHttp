package vn.com.zalopay.wallet.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import timber.log.Timber;

public class PlayStoreUtils {
    /**
     * Measure campaigns and traffic sources with the Google Analytics.
     * https://developers.google.com/analytics/devguides/collection/android/v4/campaigns
     *
     * @param campaign        title use to analytic
     * @param trackingContent detail use to analytic
     */
    private static String getGooglePlayCampaign(String pAppName, String campaign, String trackingContent, String appName) {
        String strCampaign = "&referrer=utm_source%3D" + pAppName +
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

    public static String getUrlPlayStore(String pPackage, String pAppName, String campaign, String trackingContent) {
        return "market://details?id=" + pPackage +
                getGooglePlayCampaign(pAppName, campaign, trackingContent, "play-store");
    }

    private static String getUrlWebPlayStore(String pPackage, String pAppName, String campaign, String trackingContent) {
        return "https://play.google.com/store/apps/details?id=" + pPackage +
                getGooglePlayCampaign(pAppName, campaign, trackingContent, "web");
    }

    private static void openWebPlayStore(Context context, String pAppName, String campaign, String trackingContent) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(getUrlWebPlayStore(context.getPackageName(), pAppName, campaign, trackingContent))));
    }

    private static void openPlayStore(Context context, String pAppName, String campaign, String trackingContent)
            throws Exception {
        if (context == null) {
            return;
        }
        String pLinkApp = getUrlPlayStore(context.getPackageName(), pAppName, campaign, trackingContent);
        Log.d("openPlayStore", "start link to app on store for updating", pLinkApp);
        Uri uriUrl = Uri.parse(pLinkApp);
        Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            openWebPlayStore(context, pAppName, campaign, trackingContent);
        }
    }

    public static void openPlayStoreForUpdate(Context context, String campaign, String trackingContent) {
        try {
            openPlayStore(context, "Zalo Pay", campaign, trackingContent);
        } catch (Exception ex) {
            Timber.w(ex, "open PlayStore for update exception [%s]", ex.getMessage());
            openWebPlayStore(context, "Zalo Pay", campaign, trackingContent);
        }
    }
}
