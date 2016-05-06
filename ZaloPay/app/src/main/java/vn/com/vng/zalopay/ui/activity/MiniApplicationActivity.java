package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import javax.inject.Inject;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.MiniApplicationBaseActivity;

/**
 * Created by huuhoa on 4/26/16.
 *
 */
public class MiniApplicationActivity extends MiniApplicationBaseActivity {
    @Inject
    BundleService mBundleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected BundleService bundleService() {
        return mBundleService;
    }

    @Override
    protected void doInjection() {
        AndroidApplication.instance().getAppComponent().inject(this);
    }
}
