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

import org.pgsqlite.SQLitePluginPackage;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.event.InternalAppExceptionEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.react.MiniApplicationBaseActivity;
import vn.com.vng.zalopay.react.ReactInternalPackage;


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
    NotificationStore.Repository notificationRepository;

    @Inject
    ReactNativeHostable mReactNativeHostable;

    @Inject
    ReactInternalPackage mReactInternalPackage;

    private Bundle mLaunchOptions = null;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

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
                new RNDeviceInfo(),
                new PickerViewPackage());
    }

    protected ReactPackage reactInternalPackage() {
        return mReactInternalPackage;
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
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    private void handleScene(String moduleName) {
        if (ModuleName.NOTIFICATIONS.equals(moduleName)) {
            markAllNotify();
        } else if (ModuleName.TRANSACTION_LOGS.equals(moduleName)) {
        }
    }
}
