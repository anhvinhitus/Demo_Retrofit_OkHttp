package vn.com.zalopay.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ConnectionUtil {
    public static String getSimOperator(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tel.getSimOperator();
    }

    public static boolean isOnline(Context ctx) {
        boolean online;
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            online = netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            Log.d("isOnline", e.getMessage());
            online = true;
        }
        return online;
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
