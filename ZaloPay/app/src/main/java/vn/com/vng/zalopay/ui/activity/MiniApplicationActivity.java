package vn.com.vng.zalopay.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.zalopay.apploader.BundleReactConfig;
import com.zalopay.apploader.ReactNativeHostable;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.apploader.network.NetworkService;

import org.greenrobot.eventbus.EventBus;
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
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.InternalAppExceptionEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.MiniApplicationBaseActivity;
import vn.com.vng.zalopay.react.ReactInternalPackage;
import vn.com.vng.zalopay.react.redpacket.AlertDialogProvider;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isUserSessionStarted()) {
            return;
        }

        handleScene(getMainComponentName());
    }

    @Override
    protected void onUserComponentSetup(@NonNull UserComponent userComponent) {
        userComponent.inject(this);
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
    public void onDestroy() {

        if (!isUserSessionStarted()) {
            super.onDestroy();
            return;
        }

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
    public boolean getUseDeveloperSupport() {
        return bundleReactConfig.isInternalDevSupport();
    }

    @Nullable
    @Override
    public String getJSBundleFile() {
        return bundleReactConfig.getInternalJsBundle();
    }

    @Nullable
    protected Bundle getLaunchOptions() {
        mLaunchOptions.putString("zalopay_userid", getUserComponent().currentUser().zaloPayId);

        Timber.d("getLaunchOptions: mLaunchOptions %s", mLaunchOptions);

        return mLaunchOptions;
    }


    @Override
    public List<ReactPackage> getPackages() {
        return Arrays.asList(
                new MainReactPackage(),
                reactInternalPackage(),
                new SQLitePluginPackage(),
                // new ReactReceiveMoneyPackage(mUser, eventBus),
                new RNDeviceInfo(),
                new PickerViewPackage());
    }

    protected ReactPackage reactInternalPackage() {
        return new ReactInternalPackage(transactionRepository,
                notificationRepository, redPackageRepository,
                friendRepository, mBalanceRepository, paymentService,
                sweetAlertDialog, navigator, eventBus,
                mReactNativeHostable, appRepository, mUser,
                mNetworkServiceWithRetry
        );
    }

    @Override
    public void handleException(Throwable e) {
        eventBus.post(new InternalAppExceptionEvent(e));
        super.handleException(e);
    }

    @Override
    protected ReactNativeHostable nativeInstanceManager() {
        return mReactNativeHostable;
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
        }
    }
}
