package com.zalopay.apploader;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 5/16/16.
 */
public interface BundleReactConfig {

    String getInternalJsBundle();

    boolean isInternalDevSupport();

    String getExternalJsBundle(AppResource appResource);

    boolean isExternalDevSupport();
}
