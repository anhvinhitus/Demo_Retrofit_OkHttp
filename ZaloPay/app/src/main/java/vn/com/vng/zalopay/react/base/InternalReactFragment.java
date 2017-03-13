package vn.com.vng.zalopay.react.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.ReactBaseFragment;
import com.zalopay.apploader.ReactNativeHostable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.pgsqlite.SQLitePluginPackage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.ReactInternalPackage;
import vn.com.vng.zalopay.react.redpacket.AlertDialogProvider;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;

/**
 * Created by hieuvm on 2/22/17.
 * *
 */

public class InternalReactFragment extends ReactBaseFragment {

    public static final String TAG = "InternalReactFragment";

    private static final String ARG_MODULE_NAME = "moduleName";
    private static final String ARG_LAUNCH_OPTIONS = "launchOptions";

    public static InternalReactFragment newInstance(String moduleName) {
        return newInstance(moduleName, new HashMap<>());
    }

    public static InternalReactFragment newInstance(String moduleName, HashMap<String, String> launchOptions) {

        Bundle options = new Bundle();
        for (Map.Entry<String, String> e : launchOptions.entrySet()) {
            options.putString(e.getKey(), e.getValue());
        }

        Bundle args = new Bundle();
        args.putString(ARG_MODULE_NAME, moduleName);
        args.putBundle(ARG_LAUNCH_OPTIONS, options);

        InternalReactFragment fragment = new InternalReactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    public boolean getUseDeveloperSupport() {
        return bundleReactConfig.isInternalDevSupport();
    }

    @Override
    public List<ReactPackage> getPackages() {
        return Arrays.asList(
                new MainReactPackage(),
                reactInternalPackage(),
                new SQLitePluginPackage(),
                new RNDeviceInfo());
    }

    protected ReactPackage reactInternalPackage() {
        return new ReactInternalPackage(transactionRepository,
                notificationRepository, redPackageRepository,
                friendRepository, mBalanceRepository, paymentService,
                sweetAlertDialog, navigator, eventBus,
                mReactNativeHostable, appRepository, mUser, mZaloPayRepository);
    }

    @Override
    protected String getMainComponentName() {
        return mModuleName;
    }

    @Inject
    BundleReactConfig bundleReactConfig;

    @Inject
    EventBus eventBus;

    @Inject
    NotificationStore.Repository notificationRepository;

    @Inject
    TransactionStore.Repository transactionRepository;

    @Inject
    RedPacketStore.Repository redPackageRepository;

    @Inject
    FriendStore.Repository friendRepository;

    @Inject
    IRedPacketPayService paymentService;

    @Inject
    AlertDialogProvider sweetAlertDialog;

    @Inject
    BalanceStore.Repository mBalanceRepository;

    @Inject
    AppResourceStore.Repository appRepository;

    @Inject
    Navigator navigator;

    @Inject
    ReactNativeHostable mReactNativeHostable;

    @Inject
    User mUser;

    Bundle mLaunchOptions = null;

    private String mModuleName;

    CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Inject
    ZaloPayRepository mZaloPayRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startReactApplication();
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    protected void initArgs(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            mLaunchOptions = bundle.getBundle(ARG_LAUNCH_OPTIONS);
            mModuleName = bundle.getString(ARG_MODULE_NAME);
        } else {
            mLaunchOptions = savedInstanceState.getBundle(ARG_LAUNCH_OPTIONS);
            mModuleName = savedInstanceState.getString(ARG_MODULE_NAME);
        }

        if (mLaunchOptions == null) {
            mLaunchOptions = new Bundle();
        }

    }

    @Override
    public Bundle getLaunchOptions() {
        mLaunchOptions.putString("zalopay_userid", mUser.zaloPayId);
        return mLaunchOptions;
    }

    @Nullable
    @Override
    public String getJSBundleFile() {
        return bundleReactConfig.getInternalJsBundle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(ARG_LAUNCH_OPTIONS, mLaunchOptions);
        outState.putString(ARG_MODULE_NAME, mModuleName);
    }

    @Override
    public void onDetach() {
        mCompositeSubscription.clear();
        super.onDetach();
    }

    @Override
    protected ReactNativeHostable nativeInstanceManager() {
        return mReactNativeHostable;
    }

    @Subscribe
    public void onUncaughtRuntimeException(UncaughtRuntimeExceptionEvent event) {
        reactInstanceCaughtError();
        handleException(event.getInnerException());
    }
}
