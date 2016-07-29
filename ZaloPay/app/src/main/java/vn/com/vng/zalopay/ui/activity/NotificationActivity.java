package vn.com.vng.zalopay.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by AnhHieu on 7/29/16.
 */
public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isTaskRoot()) {
            Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();
            navigator.startHomeActivity(this, true);
        }

        finish();
    }
}
