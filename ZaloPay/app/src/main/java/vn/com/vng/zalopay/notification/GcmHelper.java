package vn.com.vng.zalopay.notification;

import android.content.Context;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import timber.log.Timber;
import vn.com.vng.zalopay.R;

/**
 * Created by longlv on 11/25/16.
 * Support for subscribe topics gcm
 */

public class GcmHelper {

    private static final String[] TOPICS = {"global"};

    public static String getTokenGcm(Context context) {
        try {
            InstanceID instanceID = InstanceID.getInstance(context);
//            Timber.d("onHandleIntent: senderId %s", getString(R.string.gcm_defaultSenderId));
            return instanceID.getToken(context.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception ex) {
            Timber.d(ex, "exception in working with GCM");
            //  sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }
        return null;
    }

    public static void subscribeTopics(Context context, String token) throws IOException {
        Timber.d("subscribeTopics mIsSubscribeGcm[%s]", token);
        GcmPubSub pubSub = GcmPubSub.getInstance(context);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
