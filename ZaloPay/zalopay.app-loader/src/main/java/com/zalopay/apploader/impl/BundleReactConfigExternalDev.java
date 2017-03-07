package com.zalopay.apploader.impl;

import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.BundleService;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 5/16/16.
 * External RN development configuration
 */
public class BundleReactConfigExternalDev implements BundleReactConfig {

    private BundleService bundleService;

    public BundleReactConfigExternalDev(BundleService service) {
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
        return null;
    }

    @Override
    public boolean isExternalDevSupport() {
        return true;
    }
}
