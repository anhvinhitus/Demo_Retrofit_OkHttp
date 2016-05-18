package vn.com.vng.zalopay.mdl.impl;

import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.BundleService;

/**
 * Created by AnhHieu on 5/16/16.
 */
public class BundleReactConfigImpl implements BundleReactConfig {

    private BundleService bundleService;

    public BundleReactConfigImpl(BundleService service) {
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
    public String getExternalJsBundle() {
        return null;
    }

    @Override
    public boolean isExternalDevSupport() {
        return true;
    }
}
