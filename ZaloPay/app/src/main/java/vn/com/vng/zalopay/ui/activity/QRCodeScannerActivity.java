package vn.com.vng.zalopay.ui.activity;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import vn.com.vng.zalopay.analytics.ZPEvents;
import vn.com.vng.zalopay.scanners.qrcode.QRCodeFragment;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 4/21/16.
 */
public class QRCodeScannerActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zpAnalytics.trackEvent(ZPEvents.SCANQR_LAUNCH);

    }

    @Override
    public BaseFragment getFragmentToHost() {
        return null;
    }

    @Override
    protected void hostFragment(BaseFragment fragment, int id) {
        if (getFragmentManager().findFragmentByTag("QRCodeFragment") == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(id, QRCodeFragment.newInstance(), "QRCodeFragment");
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
        zpAnalytics.trackEvent(ZPEvents.SCANQR_NAVIGATEBACK);
    }
}

