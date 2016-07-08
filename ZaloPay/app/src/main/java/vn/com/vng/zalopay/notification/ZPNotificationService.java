package vn.com.vng.zalopay.notification;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.NotificationHelper;

public class ZPNotificationService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    /*Server API Key: AIzaSyCweupE81mBm3_m8VOoFTUbuhBF82r_GwI
    Sender ID: 386726389536*/

    public ZPNotificationService() {
        super(TAG);
    }


    final EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
    final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();
    final NotificationHelper notificationHelper = AndroidApplication.instance().getAppComponent().notificationHelper();
    final SharedPreferences sharedPreferences = AndroidApplication.instance().getAppComponent().sharedPreferences();

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);

            Timber.d("onHandleIntent: senderId %s", getString(R.string.gcm_defaultSenderId));

            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToServer(token);
            subscribeTopics(token);
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendRegistrationToServer(String token) {
        Timber.d("sendRegistrationToServer: token %s", token);
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}
