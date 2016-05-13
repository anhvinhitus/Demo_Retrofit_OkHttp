package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import com.facebook.react.ReactInstanceManager;

import javax.inject.Inject;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.MiniApplicationBaseActivity;
import vn.com.vng.zalopay.mdl.internal.ReactInternalPackage;

/**
 * Created by huuhoa on 4/26/16.
 */
public class MiniApplicationActivity extends MiniApplicationBaseActivity {
 /*   @Inject
    BundleService mBundleService;
*/
//    @Inject
//    ReactInstanceManager reactInstanceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected BundleService bundleService() {
        return null;
    }

    @Override
    protected void doInjection() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

//    @Override
//    protected ReactInstanceManager reactInstanceManager() {
//        return reactInstanceManager;
//    }

    @Override
    protected boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected ReactInternalPackage reactInternalPackage() {
        return new ReactInternalPackage(AndroidApplication.instance().getUserComponent().zaloPayRepository());
    }
}
