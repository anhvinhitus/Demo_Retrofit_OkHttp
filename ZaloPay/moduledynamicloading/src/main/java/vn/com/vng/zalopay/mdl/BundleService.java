package vn.com.vng.zalopay.mdl;

import com.facebook.react.ReactInstanceManager;

/**
 * Created by huuhoa on 4/26/16.
 * Service interface for managing RN bundles
 */
public interface BundleService {
    String getInternalBundleFolder();
    String getExternalBundleFolder(int appId);

    /**
     * Make sure the local resources is up-to-date with resources shipped with apk
     */
    void ensureLocalResources();
}
