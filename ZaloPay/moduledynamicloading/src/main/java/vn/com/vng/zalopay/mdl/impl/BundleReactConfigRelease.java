package vn.com.vng.zalopay.mdl.impl;

import android.os.Environment;

import java.io.File;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.model.ReactBundleAssetData;

/**
 * Created by AnhHieu on 5/16/16.
 * Production configuration
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
        if (appResource == null) {
            return "";
        }

        return bundleService.getExternalBundleFolder(appResource.appid) + "/main.jsbundle";
    }

    @Override
    public boolean isExternalDevSupport() {
        return false;
    }

}
