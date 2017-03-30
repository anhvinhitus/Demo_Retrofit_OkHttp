package vn.com.zalopay.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.message.UnlockScreenEventMessage;
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
