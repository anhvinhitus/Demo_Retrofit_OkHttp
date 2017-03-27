package vn.com.zalopay.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.eventmessage.PaymentEventBus;
import vn.com.zalopay.wallet.eventmessage.UnlockScreenEventMessage;
import vn.com.zalopay.wallet.utils.Log;

/***
 * receiver to capture unlock screen event
 * sdk need to capture this event to focus view again
 * after user turn off screen and unlock screen to open app again
 */
public class UnLockScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.d(this, "==== UnLock ====");
            PaymentEventBus.shared().postSticky(new UnlockScreenEventMessage());
        }

    }
}
