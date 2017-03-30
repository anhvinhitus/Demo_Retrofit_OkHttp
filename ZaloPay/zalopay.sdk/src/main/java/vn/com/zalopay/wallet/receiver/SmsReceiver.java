package vn.com.zalopay.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import vn.com.zalopay.wallet.message.PaymentEventBus;
import vn.com.zalopay.wallet.message.SmsEventMessage;
import vn.com.zalopay.wallet.utils.Log;

/***
 * sms otp is comming
 */
public class SmsReceiver extends BroadcastReceiver {
    //create message from pdus
    private void prepareMessageAndSendBroadCast(Bundle pBundle) throws Exception {
        String sender;
        String body;//content sms
        //concat  multiple sms
        Object[] pdus = (Object[]) pBundle.get("pdus");

        SmsMessage[] messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }

        SmsMessage sms = messages[0];
        if (sms == null) {
            throw new Exception("===sms=NULL");
        }
        sender = sms.getOriginatingAddress();//shared numberphone
        Log.d("SmsReceiver", "sender:" + sender);

        try {
            if (messages.length == 1 || sms.isReplace()) {
                body = sms.getDisplayMessageBody();
                Log.d("SmsReceiver", "content sms: " + body);
            } else {
                //if sms has length over 160,it's devided by multipart to send
                StringBuilder bodyText = new StringBuilder();
                for (int i = 0; i < messages.length; i++) {
                    bodyText.append(messages[i].getMessageBody());
                }
                body = bodyText.toString();
                Log.d("SmsReceiver", "content sms: " + body);
            }

            if (!TextUtils.isEmpty(body)) {
                //send otp to channel activity
                SmsEventMessage smsEventMessage = new SmsEventMessage();
                smsEventMessage.sender = sender;
                smsEventMessage.message = body;
                PaymentEventBus.shared().postSticky(smsEventMessage);
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", e != null ? e.getMessage() : "error");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        try {
            prepareMessageAndSendBroadCast(extras);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }
}
