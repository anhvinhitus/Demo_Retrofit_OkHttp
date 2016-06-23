package vn.com.vng.zalopay.mdl;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import timber.log.Timber;

/**
 * Created by huuhoa on 5/16/16.
 * Based activity for hosting react native components
 */
public abstract class ReactBasedActivity extends Activity implements DefaultHardwareBackBtnHandler {
    private boolean mReactInstanceError;

    public ReactBasedActivity() {
        mReactInstanceError = false;
    }

    protected abstract void doInjection();
    protected void handleException(Throwable e) {
        finish();
    }

    private static final String REDBOX_PERMISSION_MESSAGE =
            "Overlay permissions needs to be granted in order for react native apps to run in dev mode";

    private @Nullable
    ReactInstanceManager mReactInstanceManager;
    ReactRootView mReactRootView;
    LifecycleState mLifecycleState = LifecycleState.BEFORE_RESUME;
    private boolean mDoRefresh = false;
    private final ReactNativeInstanceManager mNativeInstanceManager = new ReactNativeInstanceManagerLongLife(this);

    /**
     * Returns the name of the bundle in assets. If this is null, and no file path is specified for
     * the bundle, the app will only work with {@code getUseDeveloperSupport} enabled and will
     * always try to load the JS bundle from the packager server.
     * e.g. "index.android.bundle"
     */
    protected @Nullable String getBundleAssetName() {
        return "index.android.bundle";
    };

    /**
     * Returns a custom path of the bundle file. This is used in cases the bundle should be loaded
     * from a custom path. By default it is loaded from Android assets, from a path specified
     * by getBundleAssetName.
     * e.g. "file://sdcard/myapp_cache/index.android.bundle"
     */
    protected @Nullable String getJSBundleFile() {
        return null;
    }

    /**
     * Returns the name of the main module. Determines the URL used to fetch the JS bundle
     * from the packager server. It is only used when dev support is enabled.
     * This is the first file to be executed once the {@link ReactInstanceManager} is created.
     * e.g. "index.android"
     */
    protected String getJSMainModuleName() {
        return "index.android";
    }

    /**
     * Returns the launchOptions which will be passed to the {@link ReactInstanceManager}
     * when the application is started. By default, this will return null and an empty
     * object will be passed to your top level component as its initial props.
     * If your React Native application requires props set outside of JS, override
     * this method to return the Android.os.Bundle of your desired initial props.
     */
    protected @Nullable
    Bundle getLaunchOptions() {
        return null;
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     * e.g. "MoviesApp"
     */
    protected abstract String getMainComponentName();

    /**
     * Returns whether dev mode should be enabled. This enables e.g. the dev menu.
     */
    protected abstract boolean getUseDeveloperSupport();

    /**
     * Returns a list of {@link ReactPackage} used by the app.
     * You'll most likely want to return at least the {@code MainReactPackage}.
     * If your app uses additional views or modules besides the default ones,
     * you'll want to include more packages here.
     */
    protected abstract List<ReactPackage> getPackages();

    /**
     * A subclass may override this method if it needs to use a custom {@link ReactRootView}.
     */
    protected ReactRootView createRootView() {
        return new ReactRootView(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doInjection();

        if (getUseDeveloperSupport() && Build.VERSION.SDK_INT >= 23) {
            // Get permission to show redbox in dev builds.
            if (!Settings.canDrawOverlays(this)) {
                Intent serviceIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(serviceIntent);
                FLog.w(ReactConstants.TAG, REDBOX_PERMISSION_MESSAGE);
                Toast.makeText(this, REDBOX_PERMISSION_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }

        mReactInstanceManager = mNativeInstanceManager.acquireReactInstanceManager();
        Timber.i("ReactInstanceManager currently has context: %s", mReactInstanceManager.hasStartedCreatingInitialContext());
//        mReactInstanceManager.createReactContextInBackground();
        mReactRootView = createRootView();
        mReactRootView.startReactApplication(mReactInstanceManager, getMainComponentName(), getLaunchOptions());
        setContentView(mReactRootView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mLifecycleState = LifecycleState.BEFORE_RESUME;

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLifecycleState = LifecycleState.RESUMED;

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }
    }

    @Override
    protected void onDestroy() {
        if (mReactRootView != null) {
            mReactRootView.unmountReactApplication();
            ViewParent view = mReactRootView.getParent();
            if (view != null) {
                if (view instanceof ViewGroup) {
                    ViewGroup l = (ViewGroup) view;
                    l.removeAllViews();
                }
            }
            mReactRootView = null;
        }

        if (mReactInstanceManager != null) {
            mNativeInstanceManager.releaseReactInstanceManager(mReactInstanceManager, mReactInstanceError);
            mReactInstanceManager = null;
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mReactInstanceManager != null &&
                mReactInstanceManager.getDevSupportManager().getDevSupportEnabled()) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                mReactInstanceManager.showDevOptionsDialog();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_R && !(getCurrentFocus() instanceof EditText)) {
                // Enable double-tap-R-to-reload
                if (mDoRefresh) {
                    mReactInstanceManager.getDevSupportManager().handleReloadJS();
                    mDoRefresh = false;
                } else {
                    mDoRefresh = true;
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    mDoRefresh = false;
                                }
                            },
                            200);
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Timber.i("finalize");
    }

    protected void reactInstanceCaughtError() {
        mReactInstanceError = true;
    }

    interface ReactNativeInstanceManager {
        ReactInstanceManager acquireReactInstanceManager();
        void releaseReactInstanceManager(ReactInstanceManager instance, boolean forceRemove);
    }

    class ReactNativeInstanceManagerShortLife implements ReactNativeInstanceManager {
        @Override
        public ReactInstanceManager acquireReactInstanceManager() {
            ReactInstanceManager.Builder builder = ReactInstanceManager.builder()
                    .setApplication(getApplication())
                    .setJSMainModuleName(getJSMainModuleName())
                    .setUseDeveloperSupport(getUseDeveloperSupport())
                    .setInitialLifecycleState(mLifecycleState);

            for (ReactPackage reactPackage : getPackages()) {
                builder.addPackage(reactPackage);
            }

            String jsBundleFile = getJSBundleFile();

            if (jsBundleFile != null) {
                builder.setJSBundleFile(jsBundleFile);
            } else {
                builder.setBundleAssetName(getBundleAssetName());
            }

            return builder.build();
        }

        @Override
        public void releaseReactInstanceManager(ReactInstanceManager instance, boolean forceRemove) {
            if (instance != null) {
                instance.onHostDestroy();
                instance.destroy();
            }
        }
    }
}

class ReactNativeInstanceManagerLongLife implements ReactBasedActivity.ReactNativeInstanceManager {
    private static HashMap<String, ReactInstanceManager> mInstance = new HashMap<>();
    private final WeakReference<ReactBasedActivity> activityReference;

    public ReactNativeInstanceManagerLongLife(ReactBasedActivity activity) {
        activityReference = new WeakReference<>(activity);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Timber.i("finalize");
    }

    @Override
    public ReactInstanceManager acquireReactInstanceManager() {
        final ReactBasedActivity activity = activityReference.get();
        if (activity == null) {
            return null;
        }

        String mapping = getMappingString(activity);

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
                .setNativeModuleCallExceptionHandler(new NativeModuleCallExceptionHandler() {
                    @Override
                    public void handleException(Exception e) {
                        handleJSException(e);
                    }
                });

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
        return mInstance.get(mapping);
    }

    @Override
    public void releaseReactInstanceManager(ReactInstanceManager instance, boolean forceRemove) {
        if (mInstance == null) {
            return;
        }

        Timber.i("release react instance manager");
        final ReactBasedActivity activity = activityReference.get();
        if (activity == null) {
            return;
        }

        String mapping = getMappingString(activity);

        if (!mInstance.containsKey(mapping)) {
            return;
        }

        if (instance == null) {
            return;
        }

        instance.onHostDestroy();

        if (forceRemove) {
            instance.destroy();
            mInstance.remove(mapping);
        }
    }

    private void removeInstance() {
        if (mInstance == null) {
            return;
        }

        Timber.i("release react instance manager");
        final ReactBasedActivity activity = activityReference.get();
        if (activity == null) {
            return;
        }

        String mapping = getMappingString(activity);

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
    }

    @NonNull
    private String getMappingString(ReactBasedActivity activity) {
        String mapping = activity.getJSBundleFile();
        if (mapping == null) {
            mapping = "NULL";
        }
        return mapping;
    }

    private void handleJSException(Exception e) {
        Timber.e(e, "Exception! Should not happen with production build");
        final ReactBasedActivity activity = activityReference.get();
        if (activity == null) {
            return;
        }

        removeInstance();
        activity.handleException(e);
    }
}

