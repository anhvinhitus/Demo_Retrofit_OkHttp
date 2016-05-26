package vn.com.vng.zalopay.mdl.impl;

import android.os.Environment;

import java.io.File;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.model.ReactBundleAssetData;

/**
 * Created by AnhHieu on 5/16/16.
 */
public class BundleReactConfigRelease implements BundleReactConfig {

    private BundleService bundleService;

    public BundleReactConfigRelease(BundleService service) {
        this.bundleService = service;
    }

    @Override
    public String getInternalJsBundle() {
        return bundleService.getInternalBundleFolder() + "/main.jsbundle";
    }

    @Override
    public boolean isInternalDevSupport() {
        return false;
    }

    @Override
    public String getExternalJsBundle(AppResource appResource) {
        if (appResource == null) return "";
        return getUnZipPath(appResource.appname) + "/main.jsbundle";
    }

    @Override
    public boolean isExternalDevSupport() {
        return false;
    }

    public String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getResourcePath() {
        return getRootPath() + File.separator + "zmres";
    }

    public String getTempFilePath() {
        return getResourcePath() + File.separator + "temp.zip";
    }


    public String getRootApplicationPath(String appName) {
        return getResourcePath() + File.separator + appName;
    }

    public String getUnZipPath(String appName) {
        return getRootApplicationPath(appName) + File.separator + "app";
    }

}
