/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed,
 * or transmitted in any form or by any means, including photocopying, recording,
 * or other electronic or mechanical methods, without the prior written permission
 * of the publisher, except in the case of brief quotations embodied in critical reviews
 * and certain other noncommercial uses permitted by copyright law.
 */
package vn.com.zalopay.wallet.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.List;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;

public class ConnectionUtil {

    /**
     * Get an abstract description of an operation to open a browser with
     * specified URL.
     *
     * @param ctx
     *            Global information about an owner application environment.
     *
     * @param url
     *            Location of website
     *
     * @return An abstract description of an operation to open a browser with
     *         specified URL
     */
    public static Intent getBrowserIntent(Context ctx, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);

        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_FROM_BACKGROUND);

        ComponentName componentName = null;
        PackageManager packageManager = ctx.getPackageManager();
        ResolveInfo browser_ri = null;
        List<ResolveInfo> rList = packageManager.queryIntentActivities(i, 0);

        for (ResolveInfo ri : rList) {
            if ("com.android.chrome".equals(ri.activityInfo.packageName)
                    || "com.chrome.beta".equals(ri.activityInfo.packageName)) {
                browser_ri = ri;
                break;
            } else if ("com.android.browser".equals(ri.activityInfo.packageName)) {
                browser_ri = ri;
                break;
            }
        }

        if (browser_ri != null) {
            componentName = new ComponentName(browser_ri.activityInfo.packageName, browser_ri.activityInfo.name);
        }

        if (componentName != null) {
            i.setComponent(componentName);
        }
        return i;
    }

    /**
     * Check if we can request short message service to send a message
     *
     * @param context
     *            Global information about an owner application environment.
     *
     * @return {@code TRUE} if possible, {@code FALSE} otherwise
     */
    public static boolean isAbleTofSendSMS(Context context) {
        String defApp = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");

        Uri smsUri = Uri.parse("smsto:1111");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(defApp);
        intent.putExtra("sms_body", "");
        intent.putExtra("address", "");
        intent.putExtra(Intent.EXTRA_TEXT, "");

        intent.setData(smsUri);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        return isIntentSafe;
    }

    /**
     * Get Mobile Network Operator name of current device.
     *
     * @param context
     *            Global information about an owner application environment.
     *
     * @return MNO string name - the MCC+MNC (mobile country code + mobile
     *         network code) of the provider of the SIM. 5 or 6 decimal digits.
     */
    public static String getSimOperator(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tel.getSimOperator();
    }

    /**
     * Check if current device support dual sim feature.
     *
     * @param context
     *            Global information about an owner application environment.
     *
     * @return {@code TRUE} if support, {@code FALSE} otherwise
     */
    public static boolean isDualSim(Context context) {
        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);
        return telephonyInfo.isDualSIM();
    }

    //
    // /**
    // * Check if Google Play services is available in this device.
    // *
    // * @param ctx
    // * Global information about an owner application environment.
    // *
    // * @return {@code TRUE} if available, {@code FALSE} otherwise
    // */
    // public static boolean checkPlayServices(Context ctx) {
    // int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
    // return status == ConnectionResult.SUCCESS;
    // }

    public static boolean isOnline(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
        } catch (Exception e) {
            Log.e("isOnline", e);
        }

        return false;
    }

    public static boolean isConnectedWifi(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedMobile(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static String getConnectionType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return "wifi";
            else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return "mobile";

        }
        return "";
    }
}
