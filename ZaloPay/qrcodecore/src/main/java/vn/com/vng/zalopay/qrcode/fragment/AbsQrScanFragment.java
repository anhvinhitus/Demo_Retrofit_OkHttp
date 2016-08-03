package vn.com.vng.zalopay.qrcode.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

import vn.com.vng.zalopay.qrcode.BuildConfig;
import vn.com.vng.zalopay.qrcode.R;

/**
 * Created by AnhHieu on 6/7/16.
 */
public abstract class AbsQrScanFragment extends Fragment {

    protected abstract void handleResult(String result);

    protected final String TAG = this.getClass().getSimpleName();

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


    public int getResLayoutId() {
        return R.layout.capture_appcompat;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getResLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeScannerView = (CompoundBarcodeView) view.findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);
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
}
