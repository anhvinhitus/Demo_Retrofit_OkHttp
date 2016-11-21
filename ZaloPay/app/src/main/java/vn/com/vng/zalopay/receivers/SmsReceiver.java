package vn.com.vng.zalopay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.event.ReceiveSmsEvent;

/**
 * Created by huuhoa on 7/21/16.
 * Process received sms
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null) {

            ReceiveSmsEvent event = new ReceiveSmsEvent();
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                msgs = new SmsMessage[pdus.length];
            }
            if (msgs != null) {
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    if (msgs[i] == null) {
                        continue;
                    }
                    str += "SMS from " + msgs[i].getOriginatingAddress();
                    str += " :";
                    str += msgs[i].getMessageBody();
                    event.addMessage(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                    str += "\n";
                }
            }
            EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
            eventBus.removeStickyEvent(ReceiveSmsEvent.class);
            eventBus.postSticky(event);

            //---display the new SMS message---
            Timber.d(str);
        }
    }
}
