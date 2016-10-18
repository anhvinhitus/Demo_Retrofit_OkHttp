package vn.com.vng.zalopay.scanners.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CameraPreview;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.qrcode.BuildConfig;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 9/28/16.
 * *
 */

abstract class AbsQrScanFragment extends BaseFragment implements CameraPreview.StateListener {

    protected abstract void handleResult(String result);

    private CompoundBarcodeView barcodeScannerView;
    private boolean canHandleResult = true;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (!canHandleResult) {
                return;
            }

            if (result.getText() != null) {
                AbsQrScanFragment.this.pause();
                if (BuildConfig.DEBUG) {
                    barcodeScannerView.setStatusText(result.getText());
                }
                handleResult(result.getText());
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public int getResLayoutId() {
        return R.layout.capture_appcompat;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeScannerView = (CompoundBarcodeView) view.findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);
        barcodeScannerView.getBarcodeView().addStateListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void triggerScan(View view) {
        barcodeScannerView.decodeSingle(callback);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event);
    }

    protected void vibrate() {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Activity.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


    public void pause() {
        Timber.d("pause");
        if (barcodeScannerView != null) {
            barcodeScannerView.pause();
        }

        canHandleResult = false;
    }

    public void start() {
        if (barcodeScannerView != null) {
            barcodeScannerView.resume();
        }
        canHandleResult = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pause();
    }

    @Override
    public void previewSized() {
        Timber.d("previewSized");
    }

    @Override
    public void previewStarted() {
        Timber.d("previewStarted");
    }

    @Override
    public void previewStopped() {
        Timber.d("previewStopped");
    }

    @Override
    public void cameraError(Exception error) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                showToast(R.string.exception_open_camera_not_allow);
                return;
            }
        }

        showToast(R.string.exception_open_camera_fail);
    }
}
