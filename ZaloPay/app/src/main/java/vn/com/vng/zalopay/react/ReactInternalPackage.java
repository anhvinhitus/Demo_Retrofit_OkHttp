package vn.com.vng.zalopay.react;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import com.zalopay.apploader.ReactNativeHostable;
import vn.com.vng.zalopay.navigation.INavigator;
import vn.com.vng.zalopay.react.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.react.redpacket.AlertDialogProvider;
import vn.com.vng.zalopay.react.redpacket.RedPacketNativeModule;
import com.zalopay.apploader.zpmodal.ReactModalHostManager;

/**
 * Created by huuhoa on 4/25/16.
 * Internal package manager
 */
public class ReactInternalPackage implements ReactPackage {

    private TransactionStore.Repository mTransactionRepository;
    private RedPacketStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;
    private BalanceStore.Repository mBalanceRepository;
    private IRedPacketPayService paymentService;
    private AlertDialogProvider sweetAlertDialog;

    private NotificationStore.Repository mNotificationRepository;
    private INavigator navigator;

    private EventBus mEventBus;
    private ReactNativeHostable mReactNativeHostable;

    private UserConfig mUserConfig;

    public ReactInternalPackage(TransactionStore.Repository repository, NotificationStore.Repository notificationRepository,
                                RedPacketStore.Repository redPackageRepository,
                                FriendStore.Repository friendRepository,
                                BalanceStore.Repository balanceRepository,
                                IRedPacketPayService paymentService,
                                AlertDialogProvider sweetAlertDialog,
                                INavigator navigator,
                                EventBus eventBus,
                                ReactNativeHostable reactNativeHostable,
                                UserConfig userConfig) {
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
        this.mUserConfig = userConfig;
    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new ReactInternalNativeModule(reactContext, navigator));
        modules.add(new ReactTransactionLogsNativeModule(reactContext, mTransactionRepository, mEventBus));
        modules.add(new RedPacketNativeModule(reactContext, mRedPackageRepository, mFriendRepository, mBalanceRepository, paymentService, mUserConfig, sweetAlertDialog));
        modules.add(new ReactNotificationNativeModule(reactContext, mNotificationRepository, mEventBus));
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
