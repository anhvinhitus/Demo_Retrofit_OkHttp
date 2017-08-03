package vn.com.vng.zalopay.scanners.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.monitors.MonitorEvents;
import vn.com.vng.zalopay.widget.FragmentLifecycle;
import vn.com.vng.zalopay.ui.view.IQRScanView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 6/7/16.
 * *
 */
public class QRCodeFragment extends AbsQrScanFragment implements IQRScanView, FragmentLifecycle {
    private static final int FOREGROUND_IMAGE_REQUEST_CODE = 101;
    private boolean mSelectedImageInGallery = false;

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

    @BindView(R.id.tvErrorMessage)
    TextView mErrorMessageCamera;

    protected void startPickImage(int requestCode) {
        try {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, requestCode);
        } catch (Exception ex) {
            Timber.w(ex, "startPickImage");
        }
    }

    private boolean isExecutedOnce = false;

    @Override
    public int getResLayoutId() {
        return R.layout.fragment_qr_code;
    }

    @Override
    protected void handleResult(String result) {
        Timber.d("Scan QR: %s", result);
        try {
            vibrate();
        } catch (Exception ex) {
            Timber.d(ex, "vibrate");
        }

        getAppComponent().monitorTiming().finishEvent(MonitorEvents.QR_SCANNING);
        ZPAnalytics.trackEvent(ZPEvents.SCANQR_RESULT);
        qrCodePresenter.handleResult(result, QRCodeResource.SCANNER);
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        qrCodePresenter.attachView(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        qrCodePresenter.detachView();
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
    public void showWarningDialogAndResumeScan(@StringRes int strResource) {
        showWarningDialogAndResumeScan(getString(strResource));
    }

    public void showWarningDialogAndResumeScan(String error) {
        showWarningDialog(error, () -> resumeScanner());
    }

    @Override
    public void showError(String message) {
        showWarningDialogAndResumeScan(message);
        hideLoading();
    }

    @Override
    public void resumeScanner() {
        startAndCheckPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtils.runOnUIThread(mRunnableScan, 200);
    }

    Runnable mRunnableScan = () -> {
        if (getUserVisibleHint() && !mSelectedImageInGallery) {
            hideLoading();
            startAndCheckPermissionOnce();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        pause();
        mSelectedImageInGallery = false;
        AndroidUtils.cancelRunOnUIThread(mRunnableScan);
    }

    private void startAndCheckPermissionOnce() {
        Timber.d("startAndCheckPermissionOnce: %s", isExecutedOnce);
        if (!isExecutedOnce) {
            isExecutedOnce = true;
            startAndCheckPermission();
        } else {
            if (isPermissionGranted(Manifest.permission.CAMERA)) {
                Timber.d("start without check permission");
                start();
            } else {
                showCameraError(R.string.exception_open_camera_not_allow);
            }
        }
    }

    private void startAndCheckPermission() {
        Timber.d("start with check permission");
        if (isPermissionGrantedAndRequest(Manifest.permission.CAMERA, PERMISSION_CODE.CAMERA)) {
            super.start();
        }
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
        switch (permissionRequestCode) {
            case PERMISSION_CODE.CAMERA:
                handleGrantedCamera(isGranted);
                break;
            case PERMISSION_CODE.READ_EXTERNAL_STORAGE:
                handleGrantedReadExternalStorage(isGranted);
                break;
        }
    }

    private void handleGrantedCamera(boolean isGranted) {
        if (isGranted) {
            super.start();
        } else {
            showCameraError(R.string.exception_open_camera_not_allow);
        }
    }

    private void handleGrantedReadExternalStorage(boolean isGranted) {
        if (isGranted) {
            startPickImage(FOREGROUND_IMAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onStartFragment() {
        resumeScanner();
    }

    @Override
    public void onStopFragment() {
        Timber.d("onStopFragment: ");
        pause();
    }

    @Override
    protected void showCameraError(int message) {
        if (mErrorMessageCamera != null) {
            mErrorMessageCamera.setText(message);
        }
    }

    @Override
    public void previewStarted() {
        super.previewStarted();
        if (mErrorMessageCamera != null) {
            mErrorMessageCamera.setText(null);
        }
    }

    @Override
    public void previewSized() {
        super.previewSized();
        if (mErrorMessageCamera != null) {
            mErrorMessageCamera.setText(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult: requestCode %s resultCode %s", requestCode, resultCode);
        if (requestCode == FOREGROUND_IMAGE_REQUEST_CODE) {
            Timber.d("onActivityResult of request select image, data[%s]", data);
            if (resultCode == Activity.RESULT_OK) {
                mSelectedImageInGallery = true;
                if (qrCodePresenter != null) {
                    qrCodePresenter.pay(data);
                }
            }
        }
        if(qrCodePresenter == null){
            return;
        }
        qrCodePresenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_qrcode, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_scan_qr_from_image) {
            ZPAnalytics.trackEvent(ZPEvents.SCANQR_TOUCH_PHOTO);
            if (isPermissionGrantedAndRequest(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_CODE.READ_EXTERNAL_STORAGE)) {
                startPickImage(FOREGROUND_IMAGE_REQUEST_CODE);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
