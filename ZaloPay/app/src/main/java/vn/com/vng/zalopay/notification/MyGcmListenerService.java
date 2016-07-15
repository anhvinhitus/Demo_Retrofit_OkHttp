/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vn.com.vng.zalopay.notification;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.UserComponent;

public class MyGcmListenerService extends GcmListenerService {

    private Gson mGson = new Gson();

    private UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();

    @Override
    public void onMessageReceived(String from, Bundle data) {


      /*  String message = intent.getStringExtra("msg");
        String badgeNumber = intent.getStringExtra("badgenumber");
        String embeddata = intent.getStringExtra("embeddata");*/

        String message = data.getString("message");
        Timber.d("onMessageReceived: from %s message %s", from, message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        NotificationData event = parseMessage(message);
        if (event != null) {
            sendNotification(event);
        }
    }

    private NotificationData parseMessage(String message) {
        Timber.d("sendNotification: message %s", message);

        NotificationData event = null;
        try {
            if (TextUtils.isEmpty(message)) {
                return null;
            }
            if (userConfig.hasCurrentUser()) {
                return null;
            }

            User user = userConfig.getCurrentUser();
            event = mGson.fromJson(message, NotificationData.class);
            int transType = event.getTransType();
            event.read = !(!user.uid.equals(event.userid) && transType > 0);
        } catch (Exception ex) {
            Timber.e(ex, "exception parse gcm");
        }

        return event;
    }

    private void sendNotification(NotificationData notify) {
        if (notify != null) {
            UserComponent userComponent = AndroidApplication.instance().getUserComponent();
            if (userComponent != null) {
                NotificationHelper notificationHelper = userComponent.notificationHelper();
                notificationHelper.processNotification(notify);
            }
        }
    }


}
