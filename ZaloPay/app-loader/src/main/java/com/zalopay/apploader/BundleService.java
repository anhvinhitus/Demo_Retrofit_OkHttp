package com.zalopay.apploader;

/**
 * Created by huuhoa on 4/26/16.
 * Service interface for managing RN bundles
 */
public interface BundleService {
    String getInternalBundleFolder();
    String getExternalBundleFolder(long appId);

    /**
     * Make sure the local resources is up-to-date with resources shipped with apk
     */
    void ensureLocalResources();
}
