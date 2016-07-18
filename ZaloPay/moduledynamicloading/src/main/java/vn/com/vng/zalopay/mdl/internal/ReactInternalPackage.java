package vn.com.vng.zalopay.mdl.internal;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.com.vng.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPackageStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.mdl.INavigator;

/**
 * Created by huuhoa on 4/25/16.
 * Internal package manager
 */
public class ReactInternalPackage implements ReactPackage {

    private TransactionStore.Repository mRepository;
    private RedPackageStore.Repository mRedPackageRepository;
    private FriendStore.Repository mFriendRepository;

    private NotificationStore.Repository mNotificationRepository;
    private INavigator navigator;

    private ZPAnalytics zpAnalytics;

    public ReactInternalPackage(TransactionStore.Repository repository, NotificationStore.Repository notificationRepository,
                                RedPackageStore.Repository redPackageRepository,
                                FriendStore.Repository friendRepository,
                                INavigator navigator, ZPAnalytics zpAnalytics) {
        this.mRepository = repository;
        this.mNotificationRepository = notificationRepository;
        this.mRedPackageRepository = redPackageRepository;
        this.mFriendRepository = friendRepository;
        this.navigator = navigator;
        this.zpAnalytics = zpAnalytics;
    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new ReactInternalNativeModule(reactContext, navigator, zpAnalytics));
        modules.add(new ReactTransactionLogsNativeModule(reactContext, mRepository));
        modules.add(new ReactRedPackageNativeModule(reactContext, mRedPackageRepository, mFriendRepository));
        modules.add(new ReactNotificationNativeModule(reactContext, mNotificationRepository));
        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
