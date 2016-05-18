package vn.com.vng.zalopay.mdl;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.react.LifecycleState;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import timber.log.Timber;
import vn.com.vng.zalopay.mdl.internal.ReactInternalPackage;

/**
 * Created by huuhoa on 4/26/16.
 * Load react native view
 */
public abstract class MiniApplicationBaseActivity extends ReactBasedActivity {
    public MiniApplicationBaseActivity() {
    }

    protected @Nullable String getBundleAssetName() {
        return "index.android.bundle";
    };
    protected @Nullable String getJSBundleFile() {
        return null;
    }
    protected String getJSMainModuleName() {
        return "index.android";
    }
    protected @Nullable Bundle getLaunchOptions() {
        return null;
    }

    protected String getMainComponentName() {
        String componentName = getIntent().getStringExtra("moduleName");

        Timber.e("Starting module: %s", componentName);
        return componentName;
    }

    protected abstract boolean getUseDeveloperSupport();
}
