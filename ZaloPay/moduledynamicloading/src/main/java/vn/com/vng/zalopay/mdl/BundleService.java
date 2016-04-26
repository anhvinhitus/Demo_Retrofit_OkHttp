package vn.com.vng.zalopay.mdl;

import com.facebook.react.ReactInstanceManager;

/**
 * Created by huuhoa on 4/26/16.
 * Service interface for managing RN bundles
 */
public interface BundleService {
    ReactInstanceManager getInternalBundleInstanceManager();

    boolean checkForInternalBundleUpdate();

    void downloadInternalBundle();

    void prepareInternalBundle();
}
