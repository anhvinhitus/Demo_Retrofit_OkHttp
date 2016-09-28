package vn.com.vng.zalopay.scanners.qrcode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.scanners.ui.FragmentLifecycle;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 6/7/16.
 * *
 */
public class QRCodeFragment extends AbsQrScanFragment implements IQRScanView, FragmentLifecycle {

    public static QRCodeFragment newInstance() {
        Bundle args = new Bundle();
        QRCodeFragment fragment = new QRCodeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Inject
    QRCodePresenter qrCodePresenter;

    private SweetAlertDialog mProgressDialog;

    @Override
    public int getResLayoutId() {
        return R.layout.fragment_qr_code;
    }

    @Override
    protected void handleResult(String result) {
        try {
            vibrate();
        } catch (Exception ex) {
            Timber.d(ex, "vibrate");
        }

        getAppComponent().monitorTiming().finishEvent(MonitorEvents.QR_SCANNING);
        ZPAnalytics.trackEvent(ZPEvents.SCANQR_GETCODE);
        qrCodePresenter.pay(result);
    }

    @Override
    public void onTokenInvalid() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        qrCodePresenter.setView(this);
    }

    @Override
    public void onDestroyView() {
        qrCodePresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
    }

    @Override
    public void showRetry() {
    }

    @Override
    public void hideRetry() {
    }

    @Override
    public void showError(String message) {
        showToast(message);
        hideLoading();
    }

    @Override
    public void resumeScanner() {
        startAndCheckPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            hideLoading();
            super.start();
        }
    }

    private void startAndCheckPermission() {
        if (checkAndRequestPermission(Manifest.permission.CAMERA, Constants.Permission.REQUEST_CAMERA)) {
            super.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pause();
    }

    private boolean mIsVisibleToUser = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Timber.d("isVisibleToUser %s", isVisibleToUser);
        if (isVisibleToUser) {
            startAndCheckPermission();
        } else {
            pause();
        }
        mIsVisibleToUser = isVisibleToUser;
    }

    public boolean isVisibleToUser() {
        return mIsVisibleToUser;
    }

    public boolean checkAndRequestPermission(String permission, int requestCode) {
        boolean hasPermission = true;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                requestPermissions(new String[]{permission}, requestCode);
            }
        }
        return hasPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.Permission.REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    super.start();
                } else {
                    ZPAnalytics.trackEvent(ZPEvents.SCANQR_ACCESSDENIED);
                }
            }
        }
    }

    @Override
    public void onStartFragment() {
    }

    @Override
    public void onStopFragment() {
    }
}
