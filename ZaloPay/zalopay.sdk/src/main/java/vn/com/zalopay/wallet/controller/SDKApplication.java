package vn.com.zalopay.wallet.controller;

import android.app.Application;

import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.enumeration.ETransactionType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfoResponse;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.datasource.request.RemoveMapCard;
import vn.com.zalopay.wallet.datasource.request.SDKReport;
import vn.com.zalopay.wallet.di.component.ApplicationComponent;
import vn.com.zalopay.wallet.di.component.DaggerApplicationComponent;
import vn.com.zalopay.wallet.di.component.PaymentSessionComponent;
import vn.com.zalopay.wallet.di.module.ApplicationModule;
import vn.com.zalopay.wallet.di.module.ConfigurationModule;
import vn.com.zalopay.wallet.di.module.PaymentSessionModule;
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;

public class SDKApplication extends Application {
    protected static ApplicationComponent mApplicationComponent;
    protected static PaymentSessionComponent mPaymentSessionComponent;

    public static PaymentSessionComponent createPaymentInfoComponent(ZPWPaymentInfo pPaymentInfo) {
        mPaymentSessionComponent = getApplicationComponent().plus(new PaymentSessionModule(pPaymentInfo));
        return mPaymentSessionComponent;
    }

    public static PaymentSessionComponent getPaymentSessionComponent() {
        return mPaymentSessionComponent;
    }

    public static void releasePaymentSession() {
        mPaymentSessionComponent = null;
    }

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
        SDKReport.makeReportError(null, e != null ? GsonUtils.toJsonString(e) : "handleUncaughtException e=null");
        Log.e("handleUncaughtException", e != null ? GsonUtils.toJsonString(e) : "error");
        //System.exit(1); // kill off the crashed app
    }

    /***
     * this call by app to delete 1 map card.
     * @param pParams
     * @param pListener
     */
    public synchronized static void removeCardMap(ZPWRemoveMapCardParams pParams, ZPWRemoveMapCardListener pListener) {
        try {
            BaseRequest removeMapCardTask = new RemoveMapCard(pParams, pListener);
            removeMapCardTask.makeRequest();

        } catch (Exception e) {
            if (pListener != null) {
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.returnmessage = "Không thể xóa thẻ đã lưu.Vui lòng kiểm tra kết nối mạng và thử lại!";
                baseResponse.returncode = -1;
                pListener.onError(baseResponse);
            }
            Log.e("removeCardMap", e);
        }
    }
    /***
     * clear all cache if user use
     * newer version
     * @throws Exception
     */
    private static void checkClearCacheIfHasNewVersion() throws Exception {
        if (ZPWUtils.isNewVersion()) {
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
     *
     * @param pPaymentInfo
     * @param pGatewayInfoCallback
     */
    public synchronized static void loadGatewayInfo(ZPWPaymentInfo pPaymentInfo, ZPWGatewayInfoCallback pGatewayInfoCallback) {
        try {
            //prevent load gateway if user in sdk
            if (GlobalData.isUserInSDK() && pGatewayInfoCallback != null) {
                pGatewayInfoCallback.onFinish();
                Log.d("loadGatewayInfo", "===loadGatewayInfo===user in sdk,delay load gateway info now====");
                return;
            }
            GlobalData.initApplication(pPaymentInfo);
            checkClearCacheIfHasNewVersion();
            loadPlatformInfo(pGatewayInfoCallback);
            BankLoader.loadBankList(null);
            loadAppWalletInfo(pPaymentInfo.userInfo.zaloPayUserId, pPaymentInfo.userInfo.accessToken);
            loadAppWithDrawInfo(pPaymentInfo.userInfo.zaloPayUserId, pPaymentInfo.userInfo.accessToken);
        } catch (Exception e) {
            if (pGatewayInfoCallback != null)
                pGatewayInfoCallback.onError(null);
        }
    }

    /***
     * app need to call this to update user's info on cache(channels,map cards) after user reset PIN
     *
     * @param pPaymentInfo
     * @param pGatewayInfoCallback
     */
    public synchronized static void refreshGatewayInfo(ZPWPaymentInfo pPaymentInfo, ZPWGatewayInfoCallback pGatewayInfoCallback) {
        try {
            Log.d("refreshGatewayInfo", "===refreshGatewayInfo===pPaymentInfo=" + GsonUtils.toJsonString(pPaymentInfo));
            GlobalData.initApplicationUserInfo(pPaymentInfo);
            refreshGatewayInfo(pGatewayInfoCallback);
        } catch (Exception e) {
            if (pGatewayInfoCallback != null) {
                pGatewayInfoCallback.onError(null);
            }
        }
    }

    private static void loadPlatformInfo(ZPWGatewayInfoCallback pGatewayInfoCallback) {
        try {
            BGatewayInfo.getInstance().execute(pGatewayInfoCallback);
        } catch (Exception e) {
            if (pGatewayInfoCallback != null)
                pGatewayInfoCallback.onError(e != null ? e.getMessage() : null);

            Log.e("loadPlatformInfo", e);
        }
    }

    private static void refreshGatewayInfo(ZPWGatewayInfoCallback pGatewayInfoCallback) {
        BGatewayInfo.getInstance().refreshPlatformInfo(pGatewayInfoCallback);
    }

    /***
     * load app wallet info
     *
     * @param pZaloPayUserId
     * @param pAccessToken
     */
    private static void loadAppWalletInfo(String pZaloPayUserId, String pAccessToken) {
        AppInfoLoader.get(BuildConfig.ZALOAPP_ID, ETransactionType.WALLET_TRANSFER, pZaloPayUserId, pAccessToken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
            @Override
            public void onProcessing() {
                Log.d("loadAppWalletInfo", "onProcessing");
            }

            @Override
            public void onSuccess() {
                Log.d("loadAppWalletInfo", "onSuccess");
            }

            @Override
            public void onError(DAppInfoResponse message) {
                Log.d("loadAppWalletInfo", "onError");
            }
        }).execute();
    }

    /***
     * load app withdraw info
     *
     * @param pZaloPayUserId
     * @param pAccessToken
     */
    private static void loadAppWithDrawInfo(String pZaloPayUserId, String pAccessToken) {
        AppInfoLoader.get(BuildConfig.WITHDRAWAPP_ID, ETransactionType.WITHDRAW, pZaloPayUserId, pAccessToken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
            @Override
            public void onProcessing() {
                Log.d("loadAppWithDrawInfo", "onProcessing");
            }

            @Override
            public void onSuccess() {
                Log.d("loadAppWithDrawInfo", "onSuccess");
            }

            @Override
            public void onError(DAppInfoResponse message) {
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