package vn.com.zalopay.wallet.controller;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.SdkErrorReporter;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.di.component.ApplicationComponent;
import vn.com.zalopay.wallet.di.component.DaggerApplicationComponent;
import vn.com.zalopay.wallet.di.module.ApplicationModule;
import vn.com.zalopay.wallet.di.module.ConfigurationModule;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;
import vn.com.zalopay.wallet.repository.bank.BankStore;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStore;

public class SDKApplication extends Application {
    protected static ApplicationComponent mApplicationComponent;

    public static ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    public static void initialize(Application pApplication, SDKConfiguration pConfig) {
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(pApplication))
                .configurationModule(new ConfigurationModule(pConfig))
                .build();
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(SDKApplication::handleUncaughtException);
        }
    }

    private static void handleUncaughtException(Thread thread, Throwable e) {
        Timber.w(e, "handleUncaughtException in SDK");
        //System.exit(1); // kill off the crashed app
    }

    /***
     * clear all cache if this is new user setup
     */
    private static void clearCache(String userId, String pAppVersion) {
        PlatformInfoStore.Interactor platformInfo = getApplicationComponent().platformInfoInteractor();
        if (platformInfo.isNewVersion(pAppVersion) || platformInfo.isNewUser(userId)) {
            BankStore.Interactor bankList = getApplicationComponent().bankListInteractor();
            AppInfoStore.Interactor appInfo = getApplicationComponent().appInfoInteractor();
            bankList.clearConfig();
            Timber.d("clearCache - bank list");
            //reset expire time app info
            appInfo.setExpireTime(BuildConfig.ZALOPAY_APPID, 0);
            appInfo.setExpireTime(BuildConfig.WITHDRAW_APPID, 0);
            Timber.d("clearCache - app info 1,2");
        }
    }

    /***
     * app call this after user login to load everything belong to sdk.
     * @param pUserInfo
     * @param pAppVersion
     * @param pObserver
     * @return
     */
    public synchronized static List<Subscription> loadSDKData(UserInfo pUserInfo, String pAppVersion, @NonNull Observer<PlatformInfoCallback> pObserver) {
        try {
            Timber.d("start load sdk payment data time");
            //prevent load gateway if user in sdk
            if (GlobalData.isUserInSDK()) {
                pObserver.onCompleted();
                Timber.d("user in sdk - delay load gateway info");
                return null;
            }
            String userId = pUserInfo.zalopay_userid;
            String accessToken = pUserInfo.accesstoken;
            long currentTime = System.currentTimeMillis();

            clearCache(userId, pAppVersion);

            List<Subscription> subscription = new ArrayList<>();
            //load platform info
            getApplicationComponent().platformInfoInteractor()
                    .loadSDKPlatform(userId, accessToken, currentTime)
                    .compose(SchedulerHelper.applySchedulers())
                    .subscribe(pObserver);
            //load bank list
            Subscription subscription0 = getApplicationComponent().bankListInteractor().getBankList(pAppVersion, currentTime)
                    .subscribeOn(Schedulers.io())
                    .subscribe(bankConfigResponse -> Timber.d("load bank list finish: %s", GsonUtils.toJsonString(bankConfigResponse)),
                            throwable -> Timber.d("load bank list on error: %s", throwable.getMessage()));
            if (subscription0 != null) {
                subscription.add(subscription0);
            }

            //load app zalopay with 4 transtype
            Subscription subscription1 = getApplicationComponent().appInfoInteractor().loadAppInfo(BuildConfig.ZALOPAY_APPID,
                    new int[]{TransactionType.PAY, TransactionType.TOPUP, TransactionType.LINK, TransactionType.MONEY_TRANSFER}, userId, accessToken, pAppVersion, currentTime)
                    .subscribeOn(Schedulers.io())
                    .subscribe(appInfo -> Timber.d("load app info: %s", GsonUtils.toJsonString(appInfo)),
                            throwable -> Timber.d("load app info on error: %s", throwable.getMessage()));
            if (subscription1 != null) {
                subscription.add(subscription1);
            }

            //load app withdraw (appid = 2)
            Subscription subscription2 = getApplicationComponent().appInfoInteractor().loadAppInfo(BuildConfig.WITHDRAW_APPID,
                    new int[]{TransactionType.WITHDRAW}, userId, accessToken, pAppVersion, currentTime)
                    .subscribeOn(Schedulers.io())
                    .subscribe(appInfo -> Timber.d("load app info: %s", GsonUtils.toJsonString(appInfo)),
                            throwable -> Timber.d("load app info on error: %s", throwable.getMessage()));

            if (subscription2 != null) {
                subscription.add(subscription2);
            }
            return subscription;
        } catch (Exception e) {
            Timber.w(e.getMessage());
            pObserver.onError(e);
        }
        return null;
    }

    /***
     * update user's info on cache(channels,map cards) after user reset PIN
     */
    public synchronized static Subscription refreshSDKData(UserInfo pUserInfo, @NonNull Observer<PlatformInfoCallback> pObserver) {
        return getApplicationComponent().platformInfoInteractor()
                .loadSDKPlatformFromCloud(pUserInfo.zalopay_userid, pUserInfo.accesstoken, true, false)
                .subscribeOn(Schedulers.io())
                .subscribe(pObserver);
    }

    public static Application getApplication() {
        return getApplicationComponent().application();
    }

    public static Context getContext() {
        return getApplication().getBaseContext();
    }

    public static boolean isReleaseBuild() {
        return getApplicationComponent().sdkConfiguration().isReleaseBuild();
    }

    public static SDKConfiguration.Builder getBuilder() {
        return getApplicationComponent().sdkConfiguration().getBuilder();
    }

    public static SdkErrorReporter sdkErrorReporter() {
        return new SdkErrorReporter(getContext());
    }
}
