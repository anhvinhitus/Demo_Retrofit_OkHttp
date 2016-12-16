package vn.com.vng.zalopay.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;

/**
 * Created by AnhHieu on 7/29/16.
 */
public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTaskRoot()) {
            ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
            UserConfig userConfig = applicationComponent.userConfig();
            if (userConfig.hasCurrentUser()) {
                applicationComponent.navigator().startHomeActivity(this, true);
            }
        }
        
        finish();
    }
}
