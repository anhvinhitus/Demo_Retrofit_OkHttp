package com.zalopay.apploader.impl;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.zalopay.apploader.BundleService;
import com.zalopay.apploader.internal.FileUtils;
import com.zalopay.apploader.model.ReactBundleAssetData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.repository.LocalResourceRepository;

/**
 * Created by huuhoa on 4/25/16.
 * Manage mini application bundles.
 * There are two types of bundle to manage:
 * + Internal mini application bundle: some core app's functionality are implemented using react-native
 * + External mini application bundle: external payment scenarios implemented using react-native
 * `BundleService` provide following functions:
 * + Check for update of internalBundle, some externalBundles (externalBundles that are parts of app)
 * + Download bundle updates
 */
public class BundleServiceImpl implements BundleService {


    public static final long ZALOPAY_INTERNAL_APPLICATION_ID = 1;

    private Application mApplication;
    //    private String mCurrentInternalBundleFolder;
    private final LocalResourceRepository mLocalResourceRepository;
    private final AppResourceStore.Repository mAppResourceRepository;
    private Gson mGson;

    public BundleServiceImpl(Application application,
                             LocalResourceRepository localResourceRepository,
                             AppResourceStore.Repository appResourceRepository,
                             Gson gson) {
        this.mApplication = application;
        this.mLocalResourceRepository = localResourceRepository;
        this.mAppResourceRepository = appResourceRepository;
        this.mGson = gson;
    }

    @Override
    public String getInternalBundleFolder() {
        return getExternalBundleFolder(ZALOPAY_INTERNAL_APPLICATION_ID);
    }

    @Override
    public String getExternalBundleFolder(long appId) {
        //return String.format(Locale.getDefault(), "%s/modules/%d/app", mBundleRootFolder, appId);
        return ResourceHelper.getPath(appId);
    }

    @Override
    public void ensureLocalResources() {
        try {
            PackageInfo packageInfo = mApplication.getPackageManager().getPackageInfo(mApplication.getPackageName(), 0);
            Timber.i("Version name [%s] versionCode [%s] ", packageInfo.versionName, packageInfo.versionCode);
            ensurePaymentAppLocalResources(packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.w(e, "Error!!!");
        }
    }

    private void ensurePaymentAppLocalResources(PackageInfo packageInfo) {

        String bundle;

        try {
            AssetManager assetManager = mApplication.getAssets();
            bundle = FileUtils.loadStringFromStream(assetManager.open("bundle.json"));
        } catch (IOException ex) {
            Timber.w(ex, "IOException loadStringFromStream");
            return;
        }

        ReactBundleAssetData reactBundleAssetData = mGson.fromJson(bundle, ReactBundleAssetData.class);

        for (ReactBundleAssetData.ExternalBundle eBundle : reactBundleAssetData.external_bundle) {
            String destination = getExternalBundleFolder(eBundle.appid);
            ensurePaymentAppFolder(destination);

            String appVersion = mLocalResourceRepository.getExternalResourceVersion(eBundle.appid);
            String keyVersion = packageInfo.versionName + packageInfo.versionCode;

            if (appVersion != null && appVersion.equalsIgnoreCase(keyVersion)) {
                continue;
            }

            Timber.i("Application %s need to be updated", eBundle.appid);
            if (!updatePaymentAppLocalResource(eBundle)) {
                continue;
            }

            mLocalResourceRepository.setExternalResourceVersion(eBundle.appid, keyVersion);
            mAppResourceRepository.resetStateResource(eBundle.appid)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<>());
        }

        Timber.i("Update PaymentApp done");
    }

    private boolean updatePaymentAppLocalResource(ReactBundleAssetData.ExternalBundle bundle) {
        String destination = getExternalBundleFolder(bundle.appid);

        Timber.d("destination %s %s ", destination, bundle.appname);

        return unzipAssetToFolder(bundle.asset, destination);
    }

    private boolean unzipAssetToFolder(String assetName, String dstPath) {
        try {
            InputStream stream = mApplication.getAssets().open(assetName);
            FileUtils.unzipFile(stream, dstPath, true);
            return true;
        } catch (Exception e) {
            Timber.w(e, "exception %s", e.getMessage());
            return false;
        }
    }

    private void ensurePaymentAppFolder(String destination) {
        try {
            File destinationFolder = new File(destination);
            boolean b = destinationFolder.mkdirs();
        } catch (Exception e) {
            Timber.w(e, "exception while ensuring payment app folder %s", destination);
        }
    }
}
