package vn.com.vng.zalopay.react.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.ReactBaseFragment;
import com.zalopay.apploader.ReactNativeHostable;

import org.greenrobot.eventbus.EventBus;
import org.pgsqlite.SQLitePluginPackage;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.ReactInternalPackage;
import vn.com.vng.zalopay.react.redpacket.AlertDialogProvider;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;

/**
 * Created by hieuvm on 2/22/17.
 */

public class InternalReactFragment extends ReactBaseFragment {

    public static InternalReactFragment newInstance() {

        Bundle args = new Bundle();

        InternalReactFragment fragment = new InternalReactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected boolean getUseDeveloperSupport() {
        return bundleReactConfig.isInternalDevSupport();
    }

    @Override
    protected List<ReactPackage> getPackages() {
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
        String componentName = getIntent().getStringExtra("moduleName");

        Timber.d("Starting module: %s", componentName);
        return componentName;
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

    CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Inject
    ZaloPayRepository mZaloPayRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs(savedInstanceState);
    }

    public UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    protected void initArgs(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mLaunchOptions = intent.getBundleExtra("launchOptions");
        } else {
            mLaunchOptions = savedInstanceState.getBundle("launchOptions");
        }

        if (mLaunchOptions == null) {
            mLaunchOptions = new Bundle();
        }

    }

    @Override
    protected Bundle getLaunchOptions() {
        mLaunchOptions.putString("zalopay_userid", mUser.zaloPayId);
        return mLaunchOptions;
    }

    @Nullable
    @Override
    protected String getJSBundleFile() {
        return bundleReactConfig.getInternalJsBundle();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("launchOptions", mLaunchOptions);
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
}
