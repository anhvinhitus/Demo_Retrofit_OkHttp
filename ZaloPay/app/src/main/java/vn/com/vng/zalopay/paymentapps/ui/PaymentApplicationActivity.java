package vn.com.vng.zalopay.paymentapps.ui;

import android.os.Bundle;

import com.burnweb.rnsendintent.RNSendIntentPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.IPaymentService;
import vn.com.vng.zalopay.mdl.ReactBasedActivity;
import vn.com.vng.zalopay.mdl.internal.ReactIAPPackage;
import vn.com.vng.zalopay.utils.ToastUtil;

/**
 * Created by huuhoa on 5/16/16.
 */
public class PaymentApplicationActivity extends ReactBasedActivity {

    private String mComponentName;

    @Inject
    ZaloPayIAPRepository zaloPayIAPRepository;

    @Inject
    IPaymentService paymentService;

    @Inject
    User mUser;

    @Inject
    BundleReactConfig bundleReactConfig;

    @Inject
    EventBus eventBus;

    private AppResource appResource;

    public PaymentApplicationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            appResource = getIntent().getParcelableExtra("appResource");
            mComponentName = getIntent().getStringExtra("moduleName");
        } else {
            appResource = savedInstanceState.getParcelable("appResource");
            mComponentName = savedInstanceState.getString("moduleName");
        }

        Timber.d("Starting module: %s", mComponentName);
        Timber.d("appResource appname %s", appResource == null ? "" : appResource.appname);
        Timber.d("appResource appid %d", appResource == null ? 0 : appResource.appid);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (appResource != null) {
            outState.putParcelable("appResource", appResource);
        }

        if (mComponentName != null) {
            outState.putString("moduleName", mComponentName);
        }
    }

    protected void doInjection() {
        createUserComponent();
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    /**
     * Returns the name of the bundle in assets. If this is null, and no file path is specified for
     * the bundle, the app will only work with {@code getUseDeveloperSupport} enabled and will
     * always try to load the JS bundle from the packager server.
     * e.g. "index.android.bundle"
     */
    @Nullable
    protected String getBundleAssetName() {
        return "index.android.bundle";
    }


    /**
     * Returns a custom path of the bundle file. This is used in cases the bundle should be loaded
     * from a custom path. By default it is loaded from Android assets, from a path specified
     * by getBundleAssetName.
     * e.g. "file://sdcard/myapp_cache/index.android.bundle"
     */
    @Nullable
    protected String getJSBundleFile() {
        String jsBundleFile = bundleReactConfig.getExternalJsBundle(appResource);
        Timber.d("jsBundleFile %s", jsBundleFile);
        return jsBundleFile;
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
        Timber.d("Developer support: %s", bundleReactConfig.isExternalDevSupport());
        return bundleReactConfig.isExternalDevSupport();
    }

    @Override
    protected List<ReactPackage> getPackages() {

        long appId = appResource == null ? 0 : appResource.appid;

        return Arrays.asList(
                new MainReactPackage(),
                new RNSendIntentPackage(),
                new ReactIAPPackage(zaloPayIAPRepository, paymentService, mUser, appId)
        );
    }

    private void createUserComponent() {
        Timber.d(" user component %s", getUserComponent());
        if (getUserComponent() != null)
            return;

        UserConfig userConfig = getAppComponent().userConfig();
        Timber.d(" userConfig %s", userConfig.isSignIn());
        if (userConfig.isSignIn()) {
            userConfig.loadConfig();
            AndroidApplication.instance().createUserComponent(userConfig.getCurrentUser());
        }
    }

    public ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onTokenExpired(TokenExpiredEvent event) {
        showToast(R.string.exception_token_expired_message);
        getAppComponent().applicationSession().clearUserSession();
    }

    public void showToast(String message) {
        ToastUtil.showToast(this, message);
    }

    public void showToast(int message) {
        ToastUtil.showToast(this, message);
    }
}
