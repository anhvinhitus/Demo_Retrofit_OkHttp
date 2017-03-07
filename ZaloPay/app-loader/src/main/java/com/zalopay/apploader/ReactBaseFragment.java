package com.zalopay.apploader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import java.util.List;

import timber.log.Timber;

/**
 * Created by hieuvm on 2/22/17.
 */

public abstract class ReactBaseFragment extends Fragment implements DefaultHardwareBackBtnHandler, PermissionAwareActivity,
        ReactInstanceDelegate {

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

    @Nullable
    private PermissionListener mPermissionListener;

    LifecycleState mLifecycleState = LifecycleState.BEFORE_RESUME;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nativeInstanceManager().setActivityContext(getActivity());
        mReactInstanceManager = nativeInstanceManager().acquireReactInstanceManager(this, mLifecycleState);
        mReactRootView.startReactApplication(mReactInstanceManager, getMainComponentName(), getLaunchOptions());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onActivityResult(requestCode, resultCode, data);
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
    public void onDetach() {
        super.onDetach();
    }

    private boolean mReactInstanceError;

    @Override
    public void onDestroyView() {

        if (mReactRootView != null) {
            mReactRootView.unmountReactApplication();
        }

        if (mReactInstanceManager != null) {
            nativeInstanceManager().releaseReactInstanceManager(this, mReactInstanceManager, mReactInstanceError);
            mReactInstanceManager = null;
        }

        nativeInstanceManager().setActivityContext(null);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy();
        }
        super.onDestroy();
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
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        if (!isAdded()) {
            return;
        }

        mPermissionListener = listener;
        requestPermissions(permissions, requestCode);
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

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        if (!isAdded()) {
            return PackageManager.PERMISSION_DENIED;
        }
        return getActivity().checkPermission(permission, pid, uid);
    }

    @Override
    public int checkSelfPermission(String permission) {
        if (!isAdded()) {
            return PackageManager.PERMISSION_DENIED;
        }

        return ActivityCompat.checkSelfPermission(getActivity(), permission);
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
        getActivity().finish();
    }

    protected void reactInstanceCaughtError() {
        mReactInstanceError = true;
    }
}
