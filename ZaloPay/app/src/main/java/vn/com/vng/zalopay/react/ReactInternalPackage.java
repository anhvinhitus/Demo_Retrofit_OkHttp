package vn.com.vng.zalopay.react;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.zalopay.apploader.ReactNativeHostable;
import com.zalopay.apploader.network.NetworkService;
import com.zalopay.apploader.zpmodal.ReactModalHostManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.ws.connection.NotificationService;
import vn.com.vng.zalopay.data.zpc.ZPCStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.redpacket.AlertDialogProvider;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.react.redpacket.ReactRedPacketNativeModule;

/**
 * Created by huuhoa on 4/25/16.
 * Internal package manager
 */
public class ReactInternalPackage implements ReactPackage {

    private final TransactionStore.Repository mTransactionRepository;
    private final RedPacketStore.Repository mRedPackageRepository;
    private final ZPCStore.Repository mFriendRepository;
    private final BalanceStore.Repository mBalanceRepository;
    private final IRedPacketPayService paymentService;
    private final AlertDialogProvider sweetAlertDialog;
    private final AppResourceStore.Repository resourceRepository;
    private final NotificationStore.Repository mNotificationRepository;
    private final Navigator navigator;
    private final EventBus mEventBus;
    private final ReactNativeHostable mReactNativeHostable;
    private final User mUser;
    private final NetworkService mNetworkServiceWithRetry;
    private final NotificationService mNotificationService;

    @Inject
    public ReactInternalPackage(TransactionStore.Repository repository,
                                NotificationStore.Repository notificationRepository,
                                RedPacketStore.Repository redPackageRepository,
                                ZPCStore.Repository friendRepository,
                                BalanceStore.Repository balanceRepository,
                                IRedPacketPayService paymentService,
                                AlertDialogProvider sweetAlertDialog,
                                Navigator navigator,
                                EventBus eventBus,
                                ReactNativeHostable reactNativeHostable,
                                AppResourceStore.Repository resourceRepository,
                                User user,
                                @Named("NetworkServiceWithRetry") NetworkService networkServiceWithRetry,
                                NotificationService notificationService

    ) {
        this.mTransactionRepository = repository;
        this.mNotificationRepository = notificationRepository;
        this.mRedPackageRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        this.mBalanceRepository = balanceRepository;
        this.paymentService = paymentService;
        this.sweetAlertDialog = sweetAlertDialog;
        this.navigator = navigator;
        this.mEventBus = eventBus;
        this.mReactNativeHostable = reactNativeHostable;
        this.resourceRepository = resourceRepository;
        this.mUser = user;
        this.mNetworkServiceWithRetry = networkServiceWithRetry;
        this.mNotificationService = notificationService;

    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new ReactInternalNativeModule(reactContext, mUser, navigator, mNotificationRepository, mNetworkServiceWithRetry));
        modules.add(new ReactTransactionLogsNativeModule(reactContext, navigator, mTransactionRepository, resourceRepository, mNotificationRepository, mEventBus));
        modules.add(new ReactRedPacketNativeModule(reactContext, mRedPackageRepository, mFriendRepository, mBalanceRepository, paymentService, mUser, sweetAlertDialog));
        modules.add(new ReactNotificationNativeModule(reactContext, mUser, mNotificationRepository, mEventBus, mNotificationService));

        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        List<ViewManager> viewManagers = new ArrayList<>();
        viewManagers.add(new ReactModalHostManager(reactContext, mReactNativeHostable));
        return viewManagers;
    }
}
