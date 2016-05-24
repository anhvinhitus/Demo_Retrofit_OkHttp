package vn.com.vng.zalopay.paymentapps.ui;

import android.os.Bundle;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.ReactBasedActivity;
import vn.com.vng.zalopay.mdl.internal.ReactIAPPackage;

/**
 * Created by huuhoa on 5/16/16.
 */
public class PaymentApplicationActivity extends ReactBasedActivity {
    private String mComponentName;

    @Inject
    ZaloPayRepository mRepository;

    @Inject
    User mUser;

    @Inject
    BundleReactConfig bundleReactConfig;


    private AppResource appResource;

    public PaymentApplicationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void doInjection() {
        mComponentName = getIntent().getStringExtra("moduleName");
        Timber.e("Starting module: %s", mComponentName);

        AndroidApplication.instance().getUserComponent().inject(this);
    }

    /**
     * Returns the name of the bundle in assets. If this is null, and no file path is specified for
     * the bundle, the app will only work with {@code getUseDeveloperSupport} enabled and will
     * always try to load the JS bundle from the packager server.
     * e.g. "index.android.bundle"
     */
    protected
    @Nullable
    String getBundleAssetName() {
        return "index.android.bundle";
    }

    ;

    /**
     * Returns a custom path of the bundle file. This is used in cases the bundle should be loaded
     * from a custom path. By default it is loaded from Android assets, from a path specified
     * by getBundleAssetName.
     * e.g. "file://sdcard/myapp_cache/index.android.bundle"
     */
    protected
    @Nullable
    String getJSBundleFile() {
        return bundleReactConfig.getExternalJsBundle(appResource);
    }

    /**
     * Returns the name of the main module. Determines the URL used to fetch the JS bundle
     * from the packager server. It is only used when dev support is enabled.
     * This is the first file to be executed once the {@link ReactInstanceManager} is created.
     * e.g. "index.android"
     */
    protected String getJSMainModuleName() {
        return "index.android";
    }

    /**
     * Returns the launchOptions which will be passed to the {@link ReactInstanceManager}
     * when the application is started. By default, this will return null and an empty
     * object will be passed to your top level component as its initial props.
     * If your React Native application requires props set outside of JS, override
     * this method to return the Android.os.Bundle of your desired initial props.
     */
    protected
    @Nullable
    Bundle getLaunchOptions() {
        return null;
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     * e.g. "MoviesApp"
     */
    protected String getMainComponentName() {
        return mComponentName;
    }

    /**
     * Returns whether dev mode should be enabled. This enables e.g. the dev menu.
     */
    protected boolean getUseDeveloperSupport() {
        return bundleReactConfig.isExternalDevSupport();
    }

    @Override
    protected List<ReactPackage> getPackages() {

        long appId = 0;

        if (appResource != null) {
            appId = appResource.appid;
        }

        return Arrays.<ReactPackage>asList(
                new MainReactPackage(),
                new ReactIAPPackage(mRepository, mUser, appId)
        );
    }
}
