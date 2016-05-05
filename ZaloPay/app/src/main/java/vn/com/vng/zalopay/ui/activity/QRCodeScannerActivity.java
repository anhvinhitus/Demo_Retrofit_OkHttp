package vn.com.vng.zalopay.ui.activity;

import android.content.Intent;

import timber.log.Timber;
import vn.com.vng.zalopay.qrcode.activity.QRScanActivity;

/**
 * Created by AnhHieu on 4/21/16.
 */
public class QRCodeScannerActivity extends QRScanActivity {

    @Override
    public void handleResult(String result) {
        Timber.tag(TAG).i("result:" + result);
        super.handleResult(result);
        gotoProductDetailActivity();
    }

    private void gotoProductDetailActivity() {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        startActivity(intent);
    }
}
