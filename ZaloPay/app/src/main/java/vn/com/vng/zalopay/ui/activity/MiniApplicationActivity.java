package vn.com.vng.zalopay.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.MiniApplicationBaseActivity;
import com.zalopay.apploader.ReactNativeHostable;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.apploader.network.NetworkService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.pgsqlite.SQLitePluginPackage;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.ThrowToLoginScreenEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.ForceUpdateAppEvent;
import vn.com.vng.zalopay.event.InternalAppExceptionEvent;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.event.UncaughtRuntimeExceptionEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.INavigator;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.ReactInternalPackage;
import vn.com.vng.zalopay.react.redpacket.AlertDialogProvider;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.utils.ToastUtil;

/**
 * Created by huuhoa on 4/26/16.
 * Mini (Internal) application
 */
public class MiniApplicationActivity extends MiniApplicationBaseActivity {

    protected final String TAG = getClass().getSimpleName();

    public static final String ACTION_SUPPORT_CENTER = "vn.com.vng.zalopay.action.SUPPORT_CENTER";

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

    @Inject
    @Named("NetworkServiceWithRetry")
    NetworkService mNetworkServiceWithRetry;

    @Inject
    @Named("NetworkServiceWithoutRetry")
    NetworkService mNetworkServiceWithoutRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleScene(getMainComponentName());
    }

    @Nullable
    private String getComponentNameFromIntentAction(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return null;
        }

        if (action.equals(ACTION_SUPPORT_CENTER)) {
            return ModuleName.SUPPORT_CENTER;
        }

        return null;
    }

    @Override
    public String getMainComponentName() {
        String componentName = getComponentNameFromIntentAction(getIntent());

        if (!TextUtils.isEmpty(componentName)) {
            return componentName;
        }

        return super.getMainComponentName();
    }

    @Override
    protected void initArgs(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mLaunchOptions = getIntent().getBundleExtra("launchOptions");
        } else {
            mLaunchOptions = savedInstanceState.getBundle("launchOptions");
        }

        if (mLaunchOptions == null) {
            mLaunchOptions = new Bundle();
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
    public void onPause() {
        Timber.d("onPause");
        super.onPause();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("launchOptions", mLaunchOptions);
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

    @Nullable
    protected Bundle getLaunchOptions() {
        mLaunchOptions.putString("zalopay_userid", getUserComponent().currentUser().zaloPayId);

        Timber.d("getLaunchOptions: mLaunchOptions %s", mLaunchOptions);

        return mLaunchOptions;
    }


    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.asList(
                new MainReactPackage(),
                reactInternalPackage(),
                new SQLitePluginPackage(),
                // new ReactReceiveMoneyPackage(mUser, eventBus),
                new RNDeviceInfo());
    }

    protected ReactPackage reactInternalPackage() {
        return new ReactInternalPackage(transactionRepository,
                notificationRepository, redPackageRepository,
                friendRepository, mBalanceRepository, paymentService,
                sweetAlertDialog, navigator, eventBus,
                mReactNativeHostable, appRepository, mUser, mZaloPayRepository,
                mNetworkServiceWithRetry,
                mNetworkServiceWithoutRetry
        );
    }

    private void createUserComponent() {
        Timber.d(" user component %s", getUserComponent());
        if (getUserComponent() != null) {
            return;
        }

        UserConfig userConfig = getAppComponent().userConfig();
        Timber.d(" mUserConfig %s", userConfig.isSignIn());
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
    protected ReactNativeHostable nativeInstanceManager() {
        return mReactNativeHostable;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onThrowToLoginScreen(ThrowToLoginScreenEvent event) {
        Timber.d("onThrowToLoginScreen: in Screen %s ", TAG);
        User user = getAppComponent().userConfig().getCurrentUser();
        clearUserSession(ErrorMessageFactory.create(this, event.getThrowable(), user));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onTokenPaymentExpired(TokenPaymentExpiredEvent event) {
        Timber.i("SESSION EXPIRED in Screen %s", TAG);
        clearUserSession(getString(R.string.exception_token_expired_message));
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onForceUpdateApp(ForceUpdateAppEvent event) {
        Timber.i("Force update app in Screen %s", TAG);
        clearUserSession(null);
    }

    protected boolean clearUserSession(String message) {
        getAppComponent().applicationSession().setMessageAtLogin(message);
        getAppComponent().applicationSession().clearUserSession();
        return true;
    }

    @Subscribe
    public void onUncaughtRuntimeException(UncaughtRuntimeExceptionEvent event) {
        reactInstanceCaughtError();
        handleException(event.getInnerException());
    }

    private void markAllNotify() {
        Subscription subscription = notificationRepository.markViewAllNotify()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        mCompositeSubscription.add(subscription);
    }

    private void handleScene(String moduleName) {
        if (ModuleName.NOTIFICATIONS.equals(moduleName)) {
            markAllNotify();
        } else if (ModuleName.TRANSACTION_LOGS.equals(moduleName)) {
            navigator.showSuggestionDialog(this);
        }
    }
}
