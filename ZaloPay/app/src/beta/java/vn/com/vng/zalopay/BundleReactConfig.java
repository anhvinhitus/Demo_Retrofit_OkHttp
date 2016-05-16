package vn.com.vng.zalopay;

import android.app.Application;

import vn.com.vng.zalopay.mdl.BundleService;

/**
 * Created by AnhHieu on 5/16/16.
 */
public class BundleReactConfig {

    Application context;
    BundleService bundleService;


    public BundleReactConfig(Application context, BundleService bundleService) {
        this.context = context;
        this.bundleService = bundleService;
    }

    public String getJSBundleFile() {
        return bundleService.getInternalBundleFolder();
    }

    public boolean getUseDeveloperSupport() {
        return false;
    }

}
