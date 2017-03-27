package vn.com.zalopay.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.eventmessage.NetworkEventMessage;
import vn.com.zalopay.wallet.eventmessage.PaymentEventBus;
import vn.com.zalopay.wallet.utils.Log;

/***
 * receiver for networking is changing system event.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(this, "===networking is changing===");
        NetworkEventMessage networkEventMessage = new NetworkEventMessage();
        networkEventMessage.origin = Constants.RECEIVER_ORIGIN;
        PaymentEventBus.shared().post(networkEventMessage);
    }
}
