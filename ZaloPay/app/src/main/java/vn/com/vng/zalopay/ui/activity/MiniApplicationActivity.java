package vn.com.vng.zalopay.ui.activity;

import android.os.Bundle;

import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.remobile.toast.RCTToastPackage;

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
import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ServerMaintainEvent;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.data.exception.AccountSuspendedException;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.event.InternalAppExceptionEvent;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.mdl.BundleReactConfig;
import vn.com.vng.zalopay.mdl.INavigator;
import vn.com.vng.zalopay.mdl.MiniApplicationBaseActivity;
import vn.com.vng.zalopay.mdl.ReactNativeInstanceManager;
import vn.com.vng.zalopay.mdl.internal.ReactInternalPackage;
import vn.com.vng.zalopay.mdl.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.mdl.AlertDialogProvider;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.utils.ToastUtil;

/**
 * Created by huuhoa on 4/26/16.
 * Mini (Internal) application
 */
public class MiniApplicationActivity extends MiniApplicationBaseActivity {
    @Inject
    BundleReactConfig bundleReactConfig;

    @Inject
    GlobalEventHandlingService globalEventHandlingService;

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
    INavigator navigator;

    @Inject
    ZPAnalytics zpAnalytics;

    @Inject
    ReactNativeInstanceManager mReactNativeInstanceManager;

    @Inject
    UserConfig mUserConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        Timber.d("onPause");
        super.onPause();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
    }

    @Override
    public void onResume() {
        Timber.d("onResume");
        super.onResume();
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }


    @Override
    protected void doInjection() {
        createUserComponent();
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    @Override
    protected boolean getUseDeveloperSupport() {
        return bundleReactConfig.isInternalDevSupport();
    }

    @Nullable
    @Override
    protected String getJSBundleFile() {
        return bundleReactConfig.getInternalJsBundle();
    }

    protected
    @Nullable
    Bundle getLaunchOptions() {
        Bundle bundle = new Bundle();
        bundle.putString("zalopay_userid", getUserComponent().currentUser().uid);
        return bundle;
    }

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.asList(
                new MainReactPackage(),
                new RCTToastPackage(),
                reactInternalPackage(),
                new RNDeviceInfo());
    }

    protected ReactPackage reactInternalPackage() {
        return new ReactInternalPackage(transactionRepository,
                notificationRepository, redPackageRepository,
                friendRepository, mBalanceRepository, paymentService,
                sweetAlertDialog, navigator, zpAnalytics, eventBus,
                mReactNativeInstanceManager, mUserConfig);
    }

    private void createUserComponent() {
        Timber.d(" user component %s", getUserComponent());
        if (getUserComponent() != null) {
            return;
        }

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

    @Override
    protected void handleException(Throwable e) {
        eventBus.post(new InternalAppExceptionEvent(e));
        super.handleException(e);
    }

    @Override
    protected ReactNativeInstanceManager nativeInstanceManager() {
        return mReactNativeInstanceManager;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredMain(TokenExpiredEvent event) {
        Timber.d("Receive token expired");
        showToast(R.string.exception_token_expired_message);
        getAppComponent().applicationSession().clearUserSession();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerMaintain(ServerMaintainEvent event) {
        Timber.i("Receive server maintain event");
        getAppComponent().applicationSession().setMessageAtLogin(getString(R.string.exception_server_maintain));
        getAppComponent().applicationSession().clearUserSession();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountSuspended(AccountSuspendedException event) {
        Timber.i("Receive Suspended event");
        getAppComponent().applicationSession().setMessageAtLogin(getString(R.string.exception_zpw_account_suspended));
        getAppComponent().applicationSession().clearUserSession();
    }


    @Subscribe
    public void onUncaughtRuntimeException(UncaughtRuntimeExceptionEvent event) {
        reactInstanceCaughtError();
        handleException(event.getInnerException());
    }

    public void showToast(String message) {
        ToastUtil.showToast(this, message);
    }

    public void showToast(int message) {
        ToastUtil.showToast(this, message);
    }
}
