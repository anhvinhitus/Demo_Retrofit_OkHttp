package vn.com.vng.zalopay.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import timber.log.Timber;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        boolean isOnline = networkInfo != null && networkInfo.isConnected();
        Timber.d("Network State Change %s", isOnline);
    }
}
