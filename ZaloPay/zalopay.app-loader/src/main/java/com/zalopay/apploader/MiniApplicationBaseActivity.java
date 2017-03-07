package com.zalopay.apploader;

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

    public
    @Nullable
    String getBundleAssetName() {
        return "index.android.bundle";
    }

    public
    @Nullable
    String getJSBundleFile() {
        return null;
    }

    public String getJSMainModuleName() {
        return "index.android";
    }

    protected
    @Nullable
    Bundle getLaunchOptions() {
        return null;
    }

    public String getMainComponentName() {
        String componentName = getIntent().getStringExtra("moduleName");

        Timber.d("Starting module: %s", componentName);
        return componentName;
    }

    public abstract boolean getUseDeveloperSupport();
}
