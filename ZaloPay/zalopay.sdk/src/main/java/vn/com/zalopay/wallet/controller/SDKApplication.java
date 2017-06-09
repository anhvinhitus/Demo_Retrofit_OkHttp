package vn.com.zalopay.wallet.controller;

import android.app.Application;
import android.content.Context;

import rx.Observer;
import rx.Subscription;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.RemoveMapCardTask;
import vn.com.zalopay.wallet.di.component.ApplicationComponent;
import vn.com.zalopay.wallet.di.component.DaggerApplicationComponent;
import vn.com.zalopay.wallet.di.module.ApplicationModule;
import vn.com.zalopay.wallet.di.module.ConfigurationModule;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBankList;
import vn.com.zalopay.wallet.interactor.IPlatformInfo;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;

public class SDKApplication extends Application {
    protected static ApplicationComponent mApplicationComponent;

    public static ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    //use for mock testing purpose
    public static void setApplicationComponent(ApplicationComponent mApplicationComponent) {
        SDKApplication.mApplicationComponent = mApplicationComponent;
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
        Log.e("handleUncaughtException", e != null ? GsonUtils.toJsonString(e) : "error");
        //System.exit(1); // kill off the crashed app
    }

    /***
     * this call by app to delete map card.
     * @param pParams
     * @param pListener
     */
    public synchronized static void removeCardMap(ZPWRemoveMapCardParams pParams, ZPWRemoveMapCardListener pListener) {
        BaseTask removeMapCardTask = new RemoveMapCardTask(pParams, pListener);
        removeMapCardTask.makeRequest();
    }

    /***
     * clear all cache if this is new user setup
     * @param pAppVersion
     */
    private static void removeCacheOnSetupOverride(String pAppVersion) {
        IPlatformInfo platformInfo = getApplicationComponent().platformInfoInteractor();
        IAppInfo appInfo = getApplicationComponent().appInfoInteractor();
        IBankList bankList = getApplicationComponent().bankListInteractor();
        if (platformInfo.isNewVersion(pAppVersion) && platformInfo != null && appInfo != null && bankList != null) {
            Log.d("removeCacheOnSetupOverride", "start clear cache in previous version");
            //clear banklist
            bankList.clearCheckSum();
            bankList.clearConfig();
            //clear map card checksum
            platformInfo.clearCardMapCheckSum();
            //clear clear map account checksum
            platformInfo.clearBankAccountMapCheckSum();
            //reset expire time app info
            appInfo.setExpireTime(BuildConfig.ZALOAPP_ID, 0);
            appInfo.setExpireTime(BuildConfig.WITHDRAWAPP_ID, 0);
        }
    }

    /***
     * app call this after user login to load everything belong to sdk.
     * @param pUserInfo
     * @param pAppVersion
     * @param pObserver
     * @return
     */
    public synchronized static Subscription[] loadSDKData(UserInfo pUserInfo, String pAppVersion, Observer pObserver) {
        try {
            //prevent load gateway if user in sdk
            if (GlobalData.isUserInSDK() && pObserver != null) {
                pObserver.onCompleted();
                Log.d("loadSDKData", "user in sdk - delay load gateway info");
                return null;
            }
            removeCacheOnSetupOverride(pAppVersion);

            String userId = pUserInfo.zalopay_userid;
            String accessToken = pUserInfo.accesstoken;
            long currentTime = System.currentTimeMillis();
            Subscription[] subscription = new Subscription[3];
            //load platform info
            getApplicationComponent().platformInfoInteractor()
                    .loadPlatformInfoCloud(userId, accessToken, false, true, currentTime, pAppVersion)
                    .subscribe(pObserver);
            //load bank list
            Subscription subscription0 = getApplicationComponent().bankListInteractor().getBankList(pAppVersion, currentTime)
                    .subscribe(bankConfigResponse -> {
                    });
            subscription[0] = subscription0;
            //load app zalopay with 4 transtype
            Subscription subscription1 = getApplicationComponent().appInfoInteractor().loadAppInfo(BuildConfig.ZALOAPP_ID,
                    new int[]{TransactionType.PAY, TransactionType.TOPUP, TransactionType.LINK_CARD, TransactionType.MONEY_TRANSFER}, userId, accessToken, pAppVersion, currentTime)
                    .subscribe(appInfo -> Timber.d("load app info %s", appInfo),
                            throwable -> Timber.e("load app info on error %s", throwable),
                            () -> Timber.d("load app 1 completed"));
            subscription[1] = subscription1;
            //load app withdraw (appid = 2)
            Subscription subscription2 = getApplicationComponent().appInfoInteractor().loadAppInfo(BuildConfig.WITHDRAWAPP_ID,
                    new int[]{TransactionType.WITHDRAW}, userId, accessToken, pAppVersion, currentTime)
                    .subscribe(appInfo -> Timber.d("load app info %s", appInfo),
                            throwable -> Timber.e("load app info on error %s", throwable),
                            () -> Timber.d("load app 2 completed"));
            subscription[2] = subscription2;
            return subscription;
        } catch (Exception e) {
            if (pObserver != null)
                pObserver.onError(e);
        }
        return null;
    }

    /***
     * app need to call this to update user's info on cache(channels,map cards) after user reset PIN
     * @param pUserInfo
     * @param pObserver
     */
    public synchronized static Subscription refreshSDKData(UserInfo pUserInfo, String pAppVersion, Observer pObserver) {
        try {
            long currentTime = System.currentTimeMillis();
            //load platform info
            return getApplicationComponent().platformInfoInteractor()
                    .loadPlatformInfoCloud(pUserInfo.zalopay_userid, pUserInfo.accesstoken, true, false, currentTime, pAppVersion)
                    .subscribe(pObserver);
        } catch (Exception e) {
            if (pObserver != null)
                pObserver.onError(e);
        }
        return null;
    }

    private static void loadPlatformInfo(UserInfo pUserInfo, ZPWGatewayInfoCallback pGatewayInfoCallback) {
        try {
            BGatewayInfo.getInstance(pUserInfo).execute(pGatewayInfoCallback);
        } catch (Exception e) {
            if (pGatewayInfoCallback != null)
                pGatewayInfoCallback.onError(e != null ? e.getMessage() : null);

            Log.e("loadPlatformInfo", e);
        }
    }

    /***
     *
     * @param pUserInfo
     */
    private static void loadAppWalletInfo(UserInfo pUserInfo) {
        AppInfoLoader.get(BuildConfig.ZALOAPP_ID, TransactionType.MONEY_TRANSFER, pUserInfo.zalopay_userid, pUserInfo.accesstoken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
            @Override
            public void onProcessing() {
                Log.d("loadAppWalletInfo", "onProcessing");
            }

            @Override
            public void onSuccess() {
                Log.d("loadAppWalletInfo", "onSuccess");
            }

            @Override
            public void onError(AppInfoResponse message) {
                Log.d("loadAppWalletInfo", "onError");
            }
        }).execute();
    }

    /**
     * @param pUserInfo
     */
    private static void loadAppWithDrawInfo(UserInfo pUserInfo) {
        AppInfoLoader.get(BuildConfig.WITHDRAWAPP_ID, TransactionType.WITHDRAW, pUserInfo.zalopay_userid, pUserInfo.accesstoken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
            @Override
            public void onProcessing() {
                Log.d("loadAppWithDrawInfo", "onProcessing");
            }

            @Override
            public void onSuccess() {
                Log.d("loadAppWithDrawInfo", "onSuccess");
            }

            @Override
            public void onError(AppInfoResponse message) {
                Log.d("loadAppWithDrawInfo", "onError");
            }
        }).execute();
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
}