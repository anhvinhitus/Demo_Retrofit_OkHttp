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
import org.greenrobot.eventbus.ThreadMode;
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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.eventbus.DownloadAppEvent;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.iap.IPaymentService;
import vn.com.vng.zalopay.react.iap.ReactIAPPackage;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by hieuvm on 2/23/17.
 * *
 */

public class ExternalReactFragment extends ReactBaseFragment implements IExternalReactView {
    public static final String TAG = "ExternalReactFragment";

    public static ExternalReactFragment newInstance(AppResource app) {
        return newInstance(app, new HashMap<>());
    }

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
    ExternalReactPresenter mPresenter;

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

    private AppResource mAppResource;

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

        long appId = mAppResource == null ? 0 : mAppResource.appid;
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
        String jsBundleFile = bundleReactConfig.getExternalJsBundle(mAppResource);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.attachView(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            mPresenter.checkResourceReady(mAppResource.appid);
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    protected void initArgs(Bundle savedInstanceState) {
        mComponentName = ModuleName.PAYMENT_MAIN;

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            mAppResource = args.getParcelable("appResource");
            mLaunchOptions = args.getBundle("launchOptions");
        } else {
            mAppResource = savedInstanceState.getParcelable("appResource");
            mLaunchOptions = savedInstanceState.getBundle("launchOptions");
        }

        if (mLaunchOptions == null) {
            mLaunchOptions = new Bundle();
        }

        buildLaunchOptions(mLaunchOptions);

        Timber.d("Starting module: %s", mComponentName);
        Timber.d("mAppResource [appid: %d - appname: %s]", mAppResource == null ? 0 : mAppResource.appid,
                mAppResource == null ? "" : mAppResource.appname);
    }

    private void buildLaunchOptions(Bundle launchOption) {
        if (mAppResource != null) {
            if (mAppResource.appid == RECHARGE_MONEY_PHONE_APP_ID) {
                launchOption.putString("user_phonenumber", String.valueOf(mUser.phonenumber));
            } else if (mAppResource.appid == PaymentAppConfig.Constants.SHOW_SHOW) {
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
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mAppResource != null) {
            outState.putParcelable("appResource", mAppResource);
        }

        outState.putBundle("launchOptions", mLaunchOptions);
    }

    @Subscribe
    public void onUncaughtRuntimeException(UncaughtRuntimeExceptionEvent event) {
        reactInstanceCaughtError();
        handleException(event.getInnerException());
    }

    @Override
    public void startReactApplication() {
        Timber.d("startReactApplication ");
        DialogHelper.closeAllDialog();
        super.startReactApplication();
    }

    public void showWaitingDownloadApp() {
        if (isResumed()) {
            DialogHelper.showCustomDialog(getActivity(),
                    getActivity().getString(R.string.application_downloading),
                    getActivity().getString(R.string.txt_close),
                    SweetAlertDialog.NORMAL_TYPE,
                    null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadAppEvent(DownloadAppEvent event) {
        Timber.d("onDownloadAppEvent appId[%s] result[%s]",
                event.mDownloadInfo.appid,
                event.isDownloadSuccess);
        if (!event.isDownloadSuccess || event.mDownloadInfo == null) {
            return;
        }
        if (event.mDownloadInfo.appid == mAppResource.appid) {
            mPresenter.checkResourceReadyWithoutDownload(mAppResource.appid);
        }
    }
}
