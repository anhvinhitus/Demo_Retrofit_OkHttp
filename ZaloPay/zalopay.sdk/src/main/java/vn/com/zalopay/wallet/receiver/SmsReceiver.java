package vn.com.zalopay.wallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.event.SdkSmsMessage;

/***
 * sms otp is comming
 */
public class SmsReceiver extends BroadcastReceiver {
    //create message from pdus
    private void prepareMessageAndSendBroadCast(Bundle pBundle) throws Exception {
        try {
            String sender;
            String body;//content sms
            //concat  multiple sms
            Object[] pdus = (Object[]) pBundle.get("pdus");

            if (pdus == null) {
                return;
            }
            int length = pdus.length;
            SmsMessage[] messages = new SmsMessage[length];
            for (int i = 0; i < length; i++) {
                messages[i] = getIncomingMessage(pdus[i], pBundle);
            }

            SmsMessage sms = messages[0];
            if (sms == null) {
                throw new Exception("Nội dung tin nhắn trống");
            }
            sender = sms.getOriginatingAddress();//shared numberphone
            Timber.d("sender %s", sender);

            if (messages.length == 1 || sms.isReplace()) {
                body = sms.getDisplayMessageBody();
                Timber.d("content sms %s", body);
            } else {
                //if sms has length over 160,it's devided by multipart to send
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                body = bodyText.toString();
                Timber.d("content sms %s", body);
            }

            if (!TextUtils.isEmpty(body)) {
                //send otp to channel activity
                SdkSmsMessage smsEventMessage = new SdkSmsMessage();
                smsEventMessage.sender = sender;
                smsEventMessage.message = body;
                SDKApplication.getApplicationComponent().eventBus().post(smsEventMessage);
            }
        } catch (Exception e) {
            Timber.d(e, "Exception parse sms");
        }
    }

    // using new createFromPdu with >= API 23
    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
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
            Timber.w(e, "Exception onReceive");
        }
    }
}
