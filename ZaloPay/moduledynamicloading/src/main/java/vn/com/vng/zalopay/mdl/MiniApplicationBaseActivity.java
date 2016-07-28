package vn.com.vng.zalopay.mdl;

import android.os.Bundle;

import javax.annotation.Nullable;

import timber.log.Timber;

/**
 * Created by huuhoa on 4/26/16.
 * Load react native view
 */
public abstract class MiniApplicationBaseActivity extends ReactBasedActivity {
    public MiniApplicationBaseActivity() {
    }

    protected @Nullable String getBundleAssetName() {
        return "index.android.bundle";
    }

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

        Timber.d("Starting module: %s", componentName);
        return componentName;
    }

    protected abstract boolean getUseDeveloperSupport();
}
