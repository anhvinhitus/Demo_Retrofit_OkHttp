package vn.com.vng.zalopay.webapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by huuhoa on 5/16/17.
 * Helper for system navigator
 */

class NavigatorHelper {
    static void shareWebOnZalo(Context context, String currentUrl) {
        try {
            List<Intent> targetShareIntents = new ArrayList<>();
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(shareIntent, 0);

            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo resolveInfo : resolveInfos) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    if (packageName.contains("zalo")) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, currentUrl);
                        intent.setPackage(packageName);
                        targetShareIntents.add(intent);
                    }
                }

                if (!targetShareIntents.isEmpty()) {
                    Intent chooserIntent = Intent.createChooser(targetShareIntents.get(0), "Choose app to share");
                    context.startActivity(chooserIntent);
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Cannot share web on Zalo");
        }
    }

    static void openWebInBrowser(Context context, String currentUrl) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
            context.startActivity(browserIntent);
        } catch (Exception e) {
            Timber.e(e, "Cannot open web in default browser");
        }
    }
}
