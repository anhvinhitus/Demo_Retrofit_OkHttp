package vn.com.vng.zalopay.mdl.impl;

import vn.com.vng.zalopay.mdl.BundleReactConfig;

/**
 * Created by AnhHieu on 5/16/16.
 */
public class BundleReactConfigDevel implements BundleReactConfig {


    public BundleReactConfigDevel() {
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
    public String getExternalJsBundle(String paymentAppName) {
        return null;
    }

    @Override
    public boolean isExternalDevSupport() {
        return false;
    }
}
