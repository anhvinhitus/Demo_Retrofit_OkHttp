package vn.com.vng.zalopay.mdl.impl;

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
    public String getExternalJsBundle(String paymentAppName) {
        return bundleService.getExternalBundleFolder(paymentAppName) + "/main.jsbundle";
    }

    @Override
    public boolean isExternalDevSupport() {
        return false;
    }
}
