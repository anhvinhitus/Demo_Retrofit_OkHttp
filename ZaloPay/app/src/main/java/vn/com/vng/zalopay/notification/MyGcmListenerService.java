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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.NotificationActivity;

public class MyGcmListenerService extends GcmListenerService {
    

    @Override
    public void onMessageReceived(String from, Bundle data) {

        if (data == null) {
            return;
        }

        String message = data.getString("msg");
        String badgeNumber = data.getString("badgenumber");
        String embeddata = data.getString("embeddata");

        Timber.d("onMessageReceived: from %s message %s embeddata %s badgeNumber %s ", from, message, embeddata, badgeNumber);

        createUserComponent();

        sendNotification(message);
    }

    private void sendNotification(String message) {
        if (!TextUtils.isEmpty(message)) {
            UserComponent userComponent = getUserComponent();
            Timber.d("Create notification with userComponent %s", userComponent);
            if (userComponent != null) {

                Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);

                NotificationHelper notificationHelper = userComponent.notificationHelper();
                notificationHelper.create(getApplicationContext(), 1, intent,
                        R.mipmap.ic_launcher, getString(R.string.app_name), message);
            }
        }

    }

    private void createUserComponent() {

        Timber.d(" user component %s", getUserComponent());

        if (getUserComponent() != null)
            return;

        UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();
        Timber.d(" mUserConfig %s", userConfig.isSignIn());
        if (userConfig.isSignIn()) {
            userConfig.loadConfig();
            AndroidApplication.instance().createUserComponent(userConfig.getCurrentUser());
        }
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }
}
