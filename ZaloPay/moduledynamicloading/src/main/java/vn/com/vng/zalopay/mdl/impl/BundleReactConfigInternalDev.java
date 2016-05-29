package vn.com.vng.zalopay.mdl.impl;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.mdl.BundleReactConfig;

/**
 * Created by AnhHieu on 5/16/16.
 * Internal RN development configuration
 */
public class BundleReactConfigInternalDev implements BundleReactConfig {


    public BundleReactConfigInternalDev() {
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
        return null;
    }

    @Override
    public boolean isExternalDevSupport() {
        return false;
    }
}
