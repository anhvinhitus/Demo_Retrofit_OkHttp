package vn.com.vng.zalopay.paymentapps.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.BV.LinearGradient.LinearGradientPackage;
import com.airbnb.android.react.maps.MapsPackage;
import com.beefe.picker.PickerViewPackage;
import com.burnweb.rnsendintent.RNSendIntentPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.joshblour.reactnativepermissions.ReactNativePermissionsPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.oblador.vectoricons.VectorIconsPackage;
import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.ReactNativeHostable;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.apploader.network.NetworkService;
import com.zalopay.zcontacts.ZContactsPackage;

import org.greenrobot.eventbus.EventBus;
import org.pgsqlite.SQLitePluginPackage;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import cl.json.RNSharePackage;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.PaymentAppExceptionEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.UserReactBasedActivity;
import vn.com.vng.zalopay.react.analytics.GoogleAnalyticsBridgePackage;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.react.iap.ReactIAPPackage;

/**
 * Created by huuhoa on 5/16/16.
 * Activity for hosting payment app
 */
public class PaymentApplicationActivity extends UserReactBasedActivity {

    private static final int RECHARGE_MONEY_PHONE_APP_ID = 11;
    private static final int RECHARGE_MONEY_PHONE_V2_APP_ID = 61;

    private String mComponentName;

    @Inject
    IPaymentService paymentService;

    @Inject
    User mUser;

    @Inject
    BundleReactConfig bundleReactConfig;

    @Inject
    EventBus mEventBus;

    @Inject
    ReactNativeHostable mReactNativeHostable;

    @Inject
    @Named("NetworkServiceWithRetry")
    NetworkService mNetworkServiceWithRetry;

    private AppResource appResource;

    @Inject
    Navigator mNavigator;

    Bundle mLaunchOptions = new Bundle();

    public PaymentApplicationActivity() {
    }

    @Override
    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {
        userComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            getWindow().setBackgroundDrawable(null);
        } catch (Exception e) {
            Timber.e(e, "Caught exception while initializing Payment App");
        }
        super.onCreate(savedInstanceState);
    }

    protected void initArgs(Bundle savedInstanceState) {
        mComponentName = ModuleName.PAYMENT_MAIN;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                appResource = extras.getParcelable("appResource");
                mLaunchOptions = extras.getBundle("launchOptions");
            }
        } else {
            appResource = savedInstanceState.getParcelable("appResource");
            mLaunchOptions = savedInstanceState.getBundle("launchOptions");
        }

        if (mLaunchOptions == null) {
            mLaunchOptions = new Bundle();
        }

        buildLaunchOptions(mLaunchOptions);

        Timber.d("Starting module: %s", mComponentName);
        Timber.d("appResource [appid: %d - appname: %s]", appResource == null ? 0 : appResource.appid,
                appResource == null ? "" : appResource.appname);
    }

    private void buildLaunchOptions(Bundle launchOption) {
        if (appResource != null) {
            if (appResource.appid == RECHARGE_MONEY_PHONE_APP_ID|| appResource.appid == RECHARGE_MONEY_PHONE_V2_APP_ID) {
                launchOption.putString("user_phonenumber", String.valueOf(mUser.phonenumber));
                launchOption.putString("zalopay_userid", String.valueOf(mUser.zaloPayId));
            } else if (appResource.appid == PaymentAppConfig.Constants.SHOW_SHOW) {
                launchOption.putString("url", BuildConfig.APP22_URL);
            }
        }

        launchOption.putInt("environment", BuildConfig.ENVIRONMENT);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (appResource != null) {
            outState.putParcelable("appResource", appResource);
        }

        outState.putBundle("launchOptions", mLaunchOptions);
    }

    @Override
    protected ReactNativeHostable nativeInstanceManager() {
        return mReactNativeHostable;
    }

    /**
     * Returns the name of the bundle in assets. If this is null, and no file path is specified for
     * the bundle, the app will only work with {@code getUseDeveloperSupport} enabled and will
     * always try to load the JS bundle from the packager server.
     * e.g. "index.android.bundle"
     */
    @Nullable
    public String getBundleAssetName() {
        return "index.android.bundle";
    }


    /**
     * Returns a custom path of the bundle file. This is used in cases the bundle should be loaded
     * from a custom path. By default it is loaded from Android assets, from a path specified
     * by getBundleAssetName.
     * e.g. "file://sdcard/myapp_cache/index.android.bundle"
     */
    @Nullable
    public String getJSBundleFile() {
        String jsBundleFile = bundleReactConfig.getExternalJsBundle(appResource);
        Timber.d("jsBundleFile %s", jsBundleFile);
        return jsBundleFile;
    }

    /**
     * Returns the name of the main module. Determines the URL used to fetch the JS bundle
     * from the packager server. It is only used when dev support is enabled.
     * This is the first file to be executed once the {@link com.facebook.react.ReactInstanceManager} is created.
     * e.g. "index.android"
     */
    public String getJSMainModuleName() {
        return "index.android";
    }

    /**
     * Returns the launchOptions which will be passed to the {@link com.facebook.react.ReactInstanceManager}
     * when the application is started. By default, this will return null and an empty
     * object will be passed to your top level component as its initial props.
     * If your React Native application requires props set outside of JS, override
     * this method to return the Android.os.Bundle of your desired initial props.
     */
    protected
    @Nullable
    Bundle getLaunchOptions() {
        return mLaunchOptions;
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
    public boolean getUseDeveloperSupport() {
        Timber.d("Developer support: %s", bundleReactConfig.isExternalDevSupport());
        return bundleReactConfig.isExternalDevSupport();
    }

    @Override
    public List<ReactPackage> getPackages() {

        long appId = appResource == null ? 0 : appResource.appid;
        Timber.d("getPackages: appId %s", appId);
        return Arrays.asList(
                new MainReactPackage(),
                new RNSendIntentPackage(),
                new ZContactsPackage(),
                new RNDeviceInfo(),
                new MapsPackage(),
                new VectorIconsPackage(),
                new SQLitePluginPackage(),
                new RNSharePackage(),
                new GoogleAnalyticsBridgePackage(),
                new LinearGradientPackage(),
                new ReactNativePermissionsPackage(),
                new PickerViewPackage(),
                new ReactIAPPackage(paymentService,
                        mUser, appId,
                        mNetworkServiceWithRetry,
                        mNavigator,
                        mReactNativeHostable)
        );
    }

    @Override
    public void handleException(Throwable e) {
        mEventBus.post(new PaymentAppExceptionEvent(e, appResource.appid));
        super.handleException(e);
    }

}
