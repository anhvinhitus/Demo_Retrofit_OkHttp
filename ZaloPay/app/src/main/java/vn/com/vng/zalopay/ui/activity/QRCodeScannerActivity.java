package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.qrcode.activity.QRScanActivity;

/**
 * Created by AnhHieu on 4/21/16.
 */
public class QRCodeScannerActivity extends QRScanActivity {

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public int getResLayoutId() {
        return R.layout.activity_qr_scaner;
    }

    @Override
    protected void onResume() {
        super.onResume();
      //  mQRCodeView.startSpot();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
