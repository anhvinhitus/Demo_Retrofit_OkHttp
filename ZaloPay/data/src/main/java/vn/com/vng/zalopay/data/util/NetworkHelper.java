package vn.com.vng.zalopay.data.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import timber.log.Timber;

/**
 * Created by huuhoa on 7/4/16.
 * Provide static helper methods which are network related
 */
public class NetworkHelper {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return false;
        }

        Timber.d("Network type [%s] state [%s]", networkInfo.getTypeName(), networkInfo.getState());
        return networkInfo.isConnected();
    }
}
