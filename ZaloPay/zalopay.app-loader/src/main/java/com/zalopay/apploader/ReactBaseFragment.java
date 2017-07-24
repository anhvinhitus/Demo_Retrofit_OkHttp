package com.zalopay.apploader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import java.util.List;

import timber.log.Timber;

/**
 * Created by hieuvm on 2/22/17.
 * *
 */

public abstract class ReactBaseFragment extends Fragment implements DefaultHardwareBackBtnHandler,
        ReactInstanceDelegate, ReactInstanceManager.ReactInstanceEventListener {

    protected abstract void setupFragmentComponent();

    public abstract boolean getUseDeveloperSupport();

    public abstract List<ReactPackage> getPackages();

    protected abstract String getMainComponentName();

    protected abstract ReactNativeHostable nativeInstanceManager();

    public String getBundleAssetName() {
        return "index.android.bundle";
    }

    public String getJSBundleFile() {
        return null;
    }

    public String getJSMainModuleName() {
        return "index.android";
    }

    private ReactRootView mReactRootView;

    private ReactInstanceManager mReactInstanceManager;

    private boolean mDoRefresh = false;

    private static final String REDBOX_PERMISSION_MESSAGE =
            "Overlay permissions needs to be granted in order for react native apps to run in dev mode";

    @Nullable
    private PermissionListener mPermissionListener;

    LifecycleState mLifecycleState = LifecycleState.BEFORE_RESUME;

    private boolean mReactInstanceError;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mReactRootView = new ReactRootView(getContext());
        setupFragmentComponent();
        return mReactRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getUseDeveloperSupport() && Build.VERSION.SDK_INT >= 23) {
            // Get permission to show redbox in dev builds.
            if (!Settings.canDrawOverlays(getActivity())) {
                Intent serviceIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(serviceIntent);
                FLog.w(ReactConstants.TAG, REDBOX_PERMISSION_MESSAGE);
                Toast.makeText(getActivity(), REDBOX_PERMISSION_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nativeInstanceManager().setActivityContext(getActivity());
        mReactInstanceManager = nativeInstanceManager().acquireReactInstanceManager(this, mLifecycleState);
        mReactInstanceManager.addReactInstanceEventListener(this);
    }

    protected void startReactApplication() {
        mReactRootView.startReactApplication(mReactInstanceManager, getMainComponentName(), getLaunchOptions());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onActivityResult(getActivity(), requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLifecycleState = LifecycleState.BEFORE_RESUME;
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(getActivity(), this);
        }
    }

    @Override
    public void onPause() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {

        if (mReactRootView != null) {
            mReactRootView.unmountReactApplication();
        }

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy(getActivity());
        }

        if (mReactInstanceManager != null) {
            mReactInstanceManager.removeReactInstanceEventListener(this);
            nativeInstanceManager().releaseReactInstanceManager(this, mReactInstanceManager, mReactInstanceError);
            mReactInstanceManager = null;
        }

        nativeInstanceManager().destroyActivityContext(getActivity());
        super.onDestroyView();
    }

    @Nullable
    protected Bundle getLaunchOptions() {
        return null;
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        if (!isAdded()) {
            return;
        }

        getActivity().onBackPressed();
    }

    @Nullable
    public ReactInstanceManager getReactInstanceManager() {
        return mReactInstanceManager;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.d("onRequestPermissionsResult: requestCode [%s] grantResults [%s]", requestCode, grantResults);
        if (mPermissionListener != null &&
                mPermissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            mPermissionListener = null;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Nullable
    public Intent getIntent() {
        if (getActivity() != null) {
            return getActivity().getIntent();
        }
        return null;
    }

    @NonNull
    @Override
    public Application getApplication() {
        return getActivity().getApplication();
    }

    @Override
    public void handleException(@NonNull Throwable e) {
        // getActivity().finish();
    }

    protected void reactInstanceCaughtError() {
        mReactInstanceError = true;
    }

    @Nullable
    public ReactContext getReactContext() {
        if (mReactInstanceManager != null) {
            return mReactInstanceManager.getCurrentReactContext();
        }
        return null;
    }

    @Override
    public void onReactContextInitialized(ReactContext context) {

    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mReactInstanceManager != null) {
            if (mReactInstanceManager.getDevSupportManager().getDevSupportEnabled()) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    mReactInstanceManager.showDevOptionsDialog();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_R && !(getActivity().getCurrentFocus() instanceof EditText)) {
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
                    return true;
                }
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mReactInstanceManager.onBackPressed();
                return true;
            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                invokeDefaultOnBackPressed();
                return true;
            }
        }
        return false;
    }
}
