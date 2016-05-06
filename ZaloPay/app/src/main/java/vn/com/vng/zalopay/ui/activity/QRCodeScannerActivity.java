package vn.com.vng.zalopay.ui.activity;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.qrcode.activity.QRScanActivity;

/**
 * Created by AnhHieu on 4/21/16.
 */
public class QRCodeScannerActivity extends QRScanActivity {


    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    @Inject
    Navigator navigator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityComponent();
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

    @Override
    public void handleResult(String result) {
        Timber.tag(TAG).i("result:" + result);
        super.handleResult(result);
        navigator.startProductDetailActivity(this);
    }

    protected void setupActivityComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

}
