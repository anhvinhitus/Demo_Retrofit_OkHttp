package vn.com.vng.zalopay.mdl;

import android.content.Context;
import android.support.annotation.NonNull;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by huuhoa on 7/15/16.
 * Manage cached version of created ReactInstanceManager
 */
public class ReactNativeInstanceManagerLongLife implements ReactNativeInstanceManager {
    private Map<String, ReactInstanceManager> mInstance = new HashMap<>();
    private Map<String, Boolean> mNameMapping = new HashMap<>();

    public ReactNativeInstanceManagerLongLife() {
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Timber.i("finalize");
    }

    @Override
    public ReactInstanceManager acquireReactInstanceManager(final ReactBasedActivity activity) {
        if (activity == null) {
            return null;
        }

        String mapping = createMapping(activity);

        if (mInstance != null && mInstance.containsKey(mapping)) {
            Timber.i("reuse react instance manager");
//            mInstance.onHostResume(activity, activity);
            return mInstance.get(mapping);
        }

        Timber.i("create new react instance manager");
        ReactInstanceManager.Builder builder = ReactInstanceManager.builder()
                .setApplication(activity.getApplication())
                .setJSMainModuleName(activity.getJSMainModuleName())
                .setUseDeveloperSupport(activity.getUseDeveloperSupport())
                .setInitialLifecycleState(activity.mLifecycleState)
                .setNativeModuleCallExceptionHandler(new HandleReactNativeException(this, activity));

        for (ReactPackage reactPackage : activity.getPackages()) {
            builder.addPackage(reactPackage);
        }

        String jsBundleFile = activity.getJSBundleFile();

        if (jsBundleFile != null) {
            builder.setJSBundleFile(jsBundleFile);
        } else {
            builder.setBundleAssetName(activity.getBundleAssetName());
        }

        mInstance.put(mapping, builder.build());
        markMappingInUsed(mapping);
        return mInstance.get(mapping);
    }

    @Override
    public void releaseReactInstanceManager(ReactBasedActivity activity, ReactInstanceManager instance, boolean forceRemove) {
        if (mInstance == null) {
            return;
        }

        Timber.i("release react instance manager");
//        final ReactBasedActivity activity = activityReference.get();
        if (activity == null) {
            return;
        }

        String mapping = getAvailableMapping(activity);

        if (!mInstance.containsKey(mapping)) {
            return;
        }

        if (instance == null) {
            return;
        }

        instance.onHostDestroy();
        markMappingAvailable(mapping);

        if (forceRemove) {
            instance.destroy();
            mInstance.remove(mapping);
        }
    }

    private void removeInstance(ReactBasedActivity activity) {
        if (mInstance == null) {
            return;
        }

        Timber.i("release react instance manager");
//        final ReactBasedActivity activity = activityReference.get();
        if (activity == null) {
            return;
        }

        String mapping = getAvailableMapping(activity);

        if (!mInstance.containsKey(mapping)) {
            return;
        }

        ReactInstanceManager i = mInstance.get(mapping);
        if (i == null) {
            return;
        }

//        if (activity.mReactRootView != null) {
//            i.detachRootView(activity.mReactRootView);
//        }
//
//        i.onHostDestroy();
        mInstance.remove(mapping);
        markMappingAvailable(mapping);
    }

    @NonNull
    private String createMapping(ReactBasedActivity activity) {
        String mapping = activity.getJSBundleFile();
        if (mapping == null) {
            mapping = "NULL";
        }

        String alternateMapping = mapping + activity.toString();
        if (mNameMapping.containsKey(alternateMapping)) {
            Timber.d("Alternate mapping: %s", alternateMapping);
            return alternateMapping;
        }

        if (mNameMapping.containsKey(mapping)) {
            Timber.d("Default mapping is in used: %s", alternateMapping);
            return alternateMapping;
        }

        Timber.d("Default mapping: %s", mapping);
        return mapping;
    }

    private String getAvailableMapping(ReactBasedActivity activity) {
        String mapping = activity.getJSBundleFile();
        if (mapping == null) {
            mapping = "NULL";
        }

        String alternateMapping = mapping + activity.toString();
        if (mNameMapping.containsKey(alternateMapping)) {
            Timber.d("Alternate mapping: %s", alternateMapping);
            return alternateMapping;
        }

        Timber.d("Default mapping: %s", mapping);
        return mapping;
    }

    private void markMappingInUsed(String mapping) {
        mNameMapping.put(mapping, Boolean.TRUE);
    }

    private void markMappingAvailable(String mapping) {
        mNameMapping.remove(mapping);
    }

    @Override
    public void handleJSException(ReactBasedActivity activity, Exception e) {
        Timber.e(e, "Exception! Should not happen with production build");
        if (activity == null) {
            return;
        }

        removeInstance(activity);
        activity.handleException(e);
    }

    private ReactBasedActivity mActivity;
    @Override
    public Context getActivityContext() {
        return mActivity;
    }

    @Override
    public void setActivityContext(ReactBasedActivity activity) {
        mActivity = activity;
    }

    @Override
    public void cleanup() {
        try {
            for (ReactInstanceManager manager : mInstance.values()) {
                manager.destroy();
            }
            mInstance.clear();
            mActivity = null;
        } catch (Exception e) {
            Timber.w(e, "Error on cleanup of ReactNativeInstanceManagerLongLife");
        }
    }
}
