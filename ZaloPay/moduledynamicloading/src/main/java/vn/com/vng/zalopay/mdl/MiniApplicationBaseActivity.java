package vn.com.vng.zalopay.mdl;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import timber.log.Timber;

/**
 * Created by huuhoa on 4/26/16.
 * Load react native view
 */
public abstract class MiniApplicationBaseActivity extends Activity implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;

    public MiniApplicationBaseActivity() {
    }

    protected abstract BundleService bundleService();

    protected abstract void doInjection();

    protected ReactInstanceManager reactInstanceManager() {
        return bundleService().getInternalBundleInstanceManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String moduleName = getIntent().getStringExtra("moduleName");

        Timber.e("Starting module: %s", moduleName);

        doInjection();

        mReactRootView = new ReactRootView(this);
        mReactRootView.startReactApplication(reactInstanceManager(), moduleName, null);

        setContentView(mReactRootView);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (reactInstanceManager() != null) {
            reactInstanceManager().onHostPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (reactInstanceManager() != null) {
            reactInstanceManager().onHostResume(this, this);
        }
    }

    @Override
    public void onBackPressed() {
        if (reactInstanceManager() != null) {
            reactInstanceManager().onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && reactInstanceManager() != null) {
            reactInstanceManager().showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
