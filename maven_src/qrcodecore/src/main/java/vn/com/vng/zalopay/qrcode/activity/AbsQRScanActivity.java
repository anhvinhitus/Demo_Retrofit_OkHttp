package vn.com.vng.zalopay.qrcode.activity;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import vn.com.vng.zalopay.qrcode.BuildConfig;
import vn.com.vng.zalopay.qrcode.R;

/**
 * Created by AnhHieu on 5/14/16.
 */
public abstract class AbsQRScanActivity extends AppCompatActivity {

    protected abstract void handleResult(String result);

    protected final String TAG = this.getClass().getSimpleName();

    private CompoundBarcodeView barcodeScannerView;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                if (BuildConfig.DEBUG) {
                    barcodeScannerView.setStatusText(result.getText());
                }
                barcodeScannerView.pause();
                handleResult(result.getText());
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    public void resumeScanner() {
        if (barcodeScannerView != null) {
            barcodeScannerView.resume();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResLayoutId());
        barcodeScannerView = (CompoundBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        resumeScanner();
    }

    public int getResLayoutId() {
        return R.layout.capture_appcompat;
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    public void triggerScan(View view) {
        barcodeScannerView.decodeSingle(callback);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    protected void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


}
