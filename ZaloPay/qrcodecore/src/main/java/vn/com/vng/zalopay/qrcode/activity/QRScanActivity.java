package vn.com.vng.zalopay.qrcode.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import timber.log.Timber;
import vn.com.vng.zalopay.qrcode.QRCodeView;
import vn.com.vng.zalopay.qrcode.R;
import vn.com.vng.zalopay.qrcode.zxing.ZXingView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;

/**
 * Created by AnhHieu on 4/8/16.
 */
public class QRScanActivity extends AppCompatActivity implements QRCodeView.ResultHandler {

    protected final String TAG = QRScanActivity.class.getSimpleName();
    protected QRCodeView mQRCodeView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(TAG);
        setContentView(getResLayoutId());
        mQRCodeView = (ZXingView) findViewById(R.id.zxingview);
        mQRCodeView.setResultHandler(this);
    }

    public int getResLayoutId() {
        return R.layout.activity_scan;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkAndShowRequestPermissionCamera()) {
            startCamera();
        }

        mQRCodeView.showScanRect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    private void startCamera() {
        mQRCodeView.startCamera();
    }

    private boolean isCameraAvailable() {
        return ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkAndShowRequestPermissionCamera() {
        if (isCameraAvailable()) return true;
        return ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA);
    }

    private boolean isVibratorAvailable() {
        return ActivityCompat.checkSelfPermission(this, VIBRATE) == PackageManager.PERMISSION_GRANTED;
    }


    protected void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void handleResult(String result) {
        Timber.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        vibrate();
//        mQRCodeView.startSpot();
    }

    @Override
    public void handleCameraError() {
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.start_spot) {
            mQRCodeView.startSpot();
        } else if (id == R.id.stop_spot) {
            mQRCodeView.stopSpot();
        } else if (id == R.id.start_spot_showrect) {
            mQRCodeView.startSpotAndShowRect();
        } else if (id == R.id.stop_spot_hiddenrect) {
            mQRCodeView.stopSpotAndHiddenRect();
        } else if (id == R.id.show_rect) {
            mQRCodeView.showScanRect();
        } else if (id == R.id.hidden_rect) {
            mQRCodeView.hiddenScanRect();
        } else if (id == R.id.start_preview) {
            mQRCodeView.startCamera();
        } else if (id == R.id.stop_preview) {
            mQRCodeView.stopCamera();
        } else if (id == R.id.open_flashlight) {
            mQRCodeView.openFlashlight();
        } else if (id == R.id.close_flashlight) {
            mQRCodeView.closeFlashlight();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            for (String _permission : permissions) {
                if (_permission == CAMERA) {
                    startCamera();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
