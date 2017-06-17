package vn.com.zalopay.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkNetworkEventMessage;
import vn.com.zalopay.wallet.business.data.Log;

/***
 * receiver for networking is changing system event.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this, "networking is changing");
        SdkNetworkEventMessage networkEventMessage = new SdkNetworkEventMessage();
        networkEventMessage.origin = Constants.RECEIVER_ORIGIN;
        SDKApplication.getApplicationComponent().eventBus().post(networkEventMessage);
    }
}
