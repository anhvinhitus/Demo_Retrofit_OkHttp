package com.zalopay.apploader.impl;

import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.BundleService;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 5/16/16.
 * Internal RN development configuration
 */
public class BundleReactConfigInternalDev implements BundleReactConfig {
    private BundleService bundleService;

    public BundleReactConfigInternalDev(BundleService bundleService) {
        this.bundleService = bundleService;
    }

    @Override
    public String getInternalJsBundle() {
        return null;
    }

    @Override
    public boolean isInternalDevSupport() {
        return true;
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
