package vn.com.vng.zalopay.mdl.impl;

import android.os.Environment;

import java.io.File;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.BundleService;

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
        return getUnZipPath(appResource) + "/main.jsbundle";
    }

    @Override
    public boolean isExternalDevSupport() {
        return false;
    }


    private String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private String getResourcePath() {
        return getRootPath() + File.separator + "zmres";
    }

    private String getTempFilePath() {
        return getResourcePath() + File.separator + "temp.zip";
    }

    private String getUnZipPath(AppResource resource) {
        return getResourcePath() + File.separator + resource.appname + File.separator + resource.checksum;
    }
}
