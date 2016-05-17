package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.mdl.BundleService;
import vn.com.vng.zalopay.mdl.MiniApplicationBaseActivity;
import vn.com.vng.zalopay.mdl.internal.ReactInternalPackage;

/**
 * Created by huuhoa on 4/26/16.
 * Mini (Internal) application
 */
public class MiniApplicationActivity extends MiniApplicationBaseActivity {
//    @Inject
//    ReactInstanceManager reactInstanceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void doInjection() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    @Override
    protected boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.asList(
                new MainReactPackage(),
                reactInternalPackage());
    }

    protected ReactInternalPackage reactInternalPackage() {
        return new ReactInternalPackage(AndroidApplication.instance().getUserComponent().zaloPayRepository());
    }
}
