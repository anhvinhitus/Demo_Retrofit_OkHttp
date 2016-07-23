package vn.com.vng.zalopay.mdl.internal;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.mdl.INavigator;
import vn.com.vng.zalopay.mdl.ReactNativeInstanceManager;
import vn.com.vng.zalopay.mdl.redpacket.IRedPacketPayService;
import vn.com.vng.zalopay.mdl.AlertDialogProvider;
import vn.com.vng.zalopay.mdl.redpacket.ReactRedPacketNativeModule;
import vn.com.vng.zalopay.mdl.zpmodal.ReactModalHostManager;

/**
 * Created by huuhoa on 4/25/16.
 * Internal package manager
 */
public class ReactInternalPackage implements ReactPackage {

    private TransactionStore.Repository mRepository;
    private RedPacketStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;
    private IRedPacketPayService paymentService;
    private AlertDialogProvider sweetAlertDialog;

    private NotificationStore.Repository mNotificationRepository;
    private INavigator navigator;

    private ZPAnalytics zpAnalytics;
    private EventBus mEventBus;
    private ReactNativeInstanceManager mReactNativeInstanceManager;

    public ReactInternalPackage(TransactionStore.Repository repository, NotificationStore.Repository notificationRepository,
                                RedPacketStore.Repository redPackageRepository,
                                FriendStore.Repository friendRepository,
                                IRedPacketPayService paymentService,
                                AlertDialogProvider sweetAlertDialog,
                                INavigator navigator, ZPAnalytics zpAnalytics,
                                EventBus eventBus,
                                ReactNativeInstanceManager reactNativeInstanceManager
                                ) {
        this.mRepository = repository;
        this.mNotificationRepository = notificationRepository;
        this.mRedPackageRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        this.paymentService = paymentService;
        this.sweetAlertDialog = sweetAlertDialog;
        this.navigator = navigator;
        this.zpAnalytics = zpAnalytics;
        this.mEventBus = eventBus;
        this.mReactNativeInstanceManager = reactNativeInstanceManager;
    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new ReactInternalNativeModule(reactContext, navigator, zpAnalytics));
        modules.add(new ReactTransactionLogsNativeModule(reactContext, mRepository));
        modules.add(new ReactRedPacketNativeModule(reactContext, mRedPackageRepository, mFriendRepository, paymentService, sweetAlertDialog));
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
        viewManagers.add(new ReactModalHostManager(reactContext, mReactNativeInstanceManager));
        return viewManagers;
    }
}
