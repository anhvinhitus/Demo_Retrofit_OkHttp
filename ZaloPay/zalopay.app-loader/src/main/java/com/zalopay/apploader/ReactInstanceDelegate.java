package com.zalopay.apploader;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.ReactPackage;

import java.util.List;

/**
 * Created by hieuvm on 2/22/17.
 */

interface ReactInstanceDelegate {

    @NonNull
    Application getApplication();

    @Nullable
    String getBundleAssetName();

    @Nullable
    String getJSMainModuleName();

    @Nullable
    String getJSBundleFile();

    boolean getUseDeveloperSupport();

    void handleException(@NonNull Throwable e);

    List<ReactPackage> getPackages();
}
