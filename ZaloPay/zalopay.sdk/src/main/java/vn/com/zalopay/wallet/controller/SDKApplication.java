package vn.com.zalopay.wallet.controller;

import android.app.Application;
import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
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
import vn.com.zalopay.wallet.listener.ILoadAppInfoListener;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.listener.ZPWSaveMapCardListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;

public class SDKApplication extends Application {
    protected static SDKConfiguration mConfig;
    protected static Application mApplication = null;

    public static void initialize(Application pApplication, SDKConfiguration pConfig) {
        SDKApplication.mApplication = pApplication;
        SDKApplication.mConfig = pConfig;
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable e) {
                    handleUncaughtException(thread, e);
                }
            });
        }
    }

    private static void handleUncaughtException(Thread thread, Throwable e) {
        SDKReport.makeReportError(null, e != null ? GsonUtils.toJsonString(e) : "handleUncaughtException e=null");
        Log.e("handleUncaughtException", e != null ? GsonUtils.toJsonString(e) : "error");
        //System.exit(1); // kill off the crashed app
    }

    /***
     * this call by app to delete 1 map card.
     *
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
     * app call to save card
     * rule: user just can save card if in level 2.
     * user in level 1, after pay successfully, he/she need to back app update level to 2,
     * after that, app call this to auto save card which paid before again.
     *
     * @param pPaymentInfo
     * @param pListener
     */
    public synchronized static void saveCardMap(ZPWPaymentInfo pPaymentInfo, ZPWSaveMapCardListener pListener) {
        SDKPayment.saveCard(pPaymentInfo, pListener);
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
        AppInfoLoader.getInstance(BuildConfig.ZALOAPP_ID, ETransactionType.WALLET_TRANSFER, pZaloPayUserId, pAccessToken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
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
        }).execureForMerchant();
    }

    /***
     * load app withdraw info
     *
     * @param pZaloPayUserId
     * @param pAccessToken
     */
    private static void loadAppWithDrawInfo(String pZaloPayUserId, String pAccessToken) {
        AppInfoLoader.getInstance(BuildConfig.WITHDRAWAPP_ID, ETransactionType.WITHDRAW, pZaloPayUserId, pAccessToken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
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
        }).execureForMerchant();
    }

    public static Application getInstance() {
        return mApplication;
    }

    public static OkHttpClient getHttpClientTimeoutLonger() {
        return mConfig.getHttpClientTimeoutLonger();
    }

    public static Retrofit getRetrofit() {
        return mConfig.getRetrofit();
    }

    public static boolean isReleaseBuild() {
        return mConfig.isReleaseBuild();
    }

    public static String getBaseHostUrl() {
        return mConfig.getBaseHostUrl();
    }

    public static SDKConfiguration.Builder getBuilder() {
        return mConfig.getBuilder();
    }

    public static Context getZaloPayContext() throws Exception {
        return mApplication.getApplicationContext();
    }
}