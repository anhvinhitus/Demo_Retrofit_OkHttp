package vn.com.zalopay.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.utils.Log;

/***
 * receiver for networking is changing system event.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this, "===networking is changing===");
        //send local broadcast to listeners
        Intent messageIntent = new Intent();
        messageIntent.setAction(Constants.FILTER_ACTION_NETWORKING_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
    }
}
