package vn.com.zalopay.wallet.controller;

import android.app.Application;

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.RemoveMapCardTask;
import vn.com.zalopay.wallet.datasource.task.SDKReportTask;
import vn.com.zalopay.wallet.di.component.ApplicationComponent;
import vn.com.zalopay.wallet.di.component.DaggerApplicationComponent;
import vn.com.zalopay.wallet.di.module.ApplicationModule;
import vn.com.zalopay.wallet.di.module.ConfigurationModule;
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

    private static boolean newVersion() {
        String checksumSDKV = null;
        try {
            checksumSDKV = SharedPreferencesManager.getInstance().getChecksumSDKversion();
        } catch (Exception ignored) {
        }
        return !SdkUtils.getAppVersion(GlobalData.getAppContext()).equals(checksumSDKV);
    }

    /***
     * clear all cache if user use
     * newer version
     * @throws Exception
     */
    private static void checkClearCacheIfHasNewVersion() throws Exception {
        if (newVersion()) {
            //clear banklist
            SharedPreferencesManager.getInstance().setCheckSumBankList(null);
            SharedPreferencesManager.getInstance().setBankConfigMap(null);
            //clear map card checksum
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
            //clear clear map account checksum
            SharedPreferencesManager.getInstance().setBankAccountCheckSum(null);
            //reset expire time app info
            SharedPreferencesManager.getInstance().setExpiredTimeAppChannel(String.valueOf(BuildConfig.ZALOAPP_ID), 0);
            SharedPreferencesManager.getInstance().setExpiredTimeAppChannel(String.valueOf(BuildConfig.WITHDRAWAPP_ID), 0);
        }
    }

    /***
     * app call this after user login to load everything belong to sdk.
     * @param pUserInfo
     * @param pGatewayInfoCallback
     */
    public synchronized static void loadGatewayInfo(UserInfo pUserInfo, ZPWGatewayInfoCallback pGatewayInfoCallback) {
        try {
            //prevent load gateway if user in sdk
            if (GlobalData.isUserInSDK() && pGatewayInfoCallback != null) {
                pGatewayInfoCallback.onFinish();
                Log.d("loadGatewayInfo", "===loadGatewayInfo===user in sdk,delay load gateway info now====");
                return;
            }
            checkClearCacheIfHasNewVersion();
            loadPlatformInfo(pUserInfo, pGatewayInfoCallback);
            BankLoader.loadBankList(null);
            loadAppWalletInfo(pUserInfo);
            loadAppWithDrawInfo(pUserInfo);
        } catch (Exception e) {
            if (pGatewayInfoCallback != null)
                pGatewayInfoCallback.onError(null);
        }
    }

    /***
     * app need to call this to update user's info on cache(channels,map cards) after user reset PIN
     * @param pUserInfo
     * @param pGatewayInfoCallback
     */
    public synchronized static void refreshGatewayInfo(UserInfo pUserInfo, ZPWGatewayInfoCallback pGatewayInfoCallback) {
        try {
            Log.d("refreshGatewayInfo", "pUserInfo", pUserInfo);
            BGatewayInfo.getInstance(pUserInfo).refreshPlatformInfo(pGatewayInfoCallback);
        } catch (Exception e) {
            if (pGatewayInfoCallback != null) {
                pGatewayInfoCallback.onError(null);
            }
        }
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

    public static boolean isReleaseBuild() {
        return getApplicationComponent().sdkConfiguration().isReleaseBuild();
    }

    public static SDKConfiguration.Builder getBuilder() {
        return getApplicationComponent().sdkConfiguration().getBuilder();
    }
}