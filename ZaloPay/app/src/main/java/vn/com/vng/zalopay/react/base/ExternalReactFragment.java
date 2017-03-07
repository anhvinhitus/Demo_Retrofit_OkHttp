package vn.com.vng.zalopay.react.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.BV.LinearGradient.LinearGradientPackage;
import com.airbnb.android.react.maps.MapsPackage;
import com.burnweb.rnsendintent.RNSendIntentPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.idehub.GoogleAnalyticsBridge.GoogleAnalyticsBridgePackage;
import com.joshblour.reactnativepermissions.ReactNativePermissionsPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.oblador.vectoricons.VectorIconsPackage;
import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.ReactBaseFragment;
import com.zalopay.apploader.ReactNativeHostable;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.apploader.network.NetworkService;
import com.zalopay.zcontacts.ZContactsPackage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pgsqlite.SQLitePluginPackage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import cl.json.RNSharePackage;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.react.iap.ReactIAPPackage;

/**
 * Created by hieuvm on 2/23/17.
 */

public class ExternalReactFragment extends ReactBaseFragment {
    public static final String TAG = "ExternalReactFragment";

    public static ExternalReactFragment newInstance(AppResource app, HashMap<String, String> launchOptions) {

        Bundle options = new Bundle();
        for (Map.Entry<String, String> e : launchOptions.entrySet()) {
            options.putString(e.getKey(), e.getValue());
        }

        Bundle args = new Bundle();
        args.putParcelable("appResource", app);
        args.putBundle("launchOptions", options);

        ExternalReactFragment fragment = new ExternalReactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final int RECHARGE_MONEY_PHONE_APP_ID = 11;

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

    @Inject
    @Named("NetworkServiceWithoutRetry")
    NetworkService mNetworkServiceWithoutRetry;

    private AppResource appResource;

    @Inject
    Navigator mNavigator;

    Bundle mLaunchOptions = new Bundle();

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    public boolean getUseDeveloperSupport() {
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
                new ReactIAPPackage(paymentService,
                        mUser, appId,
                        mNetworkServiceWithRetry,
                        mNetworkServiceWithoutRetry,
                        mNavigator,
                        mReactNativeHostable)
        );
    }

    @Override
    public String getMainComponentName() {
        return mComponentName;
    }

    @Nullable
    public String getBundleAssetName() {
        return "index.android.bundle";
    }


    @Override
    protected ReactNativeHostable nativeInstanceManager() {
        return mReactNativeHostable;
    }

    @Nullable
    public String getJSBundleFile() {
        String jsBundleFile = bundleReactConfig.getExternalJsBundle(appResource);
        Timber.d("jsBundleFile %s", jsBundleFile);
        return jsBundleFile;
    }

    @Nullable
    protected Bundle getLaunchOptions() {
        return mLaunchOptions;
    }


    public String getJSMainModuleName() {
        return "index.android";
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs(savedInstanceState);
    }

    protected void initArgs(Bundle savedInstanceState) {
        mComponentName = ModuleName.PAYMENT_MAIN;

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            appResource = args.getParcelable("appResource");
            mLaunchOptions = args.getBundle("launchOptions");
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
            if (appResource.appid == RECHARGE_MONEY_PHONE_APP_ID) {
                launchOption.putString("user_phonenumber", String.valueOf(mUser.phonenumber));
            } else if (appResource.appid == PaymentAppConfig.Constants.SHOW_SHOW) {
                launchOption.putString("url", BuildConfig.APP22_URL);
            }
        }

        launchOption.putInt("environment", BuildConfig.ENVIRONMENT);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (appResource != null) {
            outState.putParcelable("appResource", appResource);
        }

        outState.putBundle("launchOptions", mLaunchOptions);
    }

    @Subscribe
    public void onUncaughtRuntimeException(UncaughtRuntimeExceptionEvent event) {
        reactInstanceCaughtError();
        handleException(event.getInnerException());
    }

}
