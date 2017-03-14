package vn.com.zalopay.wallet.controller;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import vn.com.zalopay.wallet.business.behavior.gateway.AppInfoLoader;
import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
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
import vn.com.zalopay.wallet.utils.StorageUtil;
import vn.com.zalopay.wallet.utils.ZPWUtils;

public class SDKApplication extends Application {
    protected static SDKConfiguration mConfig;
    protected static Application mApplication = null;

    public static void initialize(Application pApplication, SDKConfiguration pConfig) {
        SDKApplication.mApplication = pApplication;
        SDKApplication.mConfig = pConfig;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    private static void handleUncaughtException(Thread thread, Throwable e) {
        SDKReport.makeReportError(null, e != null ? GsonUtils.toJsonString(e) : "handleUncaughtException e=null");
        Log.e("handleUncaughtException", e != null ? GsonUtils.toJsonString(e) : "error");
        //System.exit(1); // kill off the crashed app
    }

    /**
     * Get Log
     *
     * @return
     */
    private static String getLogs() {
        try {
            String strLogs;

            String model = Build.MODEL;
            if (!model.startsWith(Build.MANUFACTURER))
                model = Build.MANUFACTURER + " " + model;

            InputStreamReader reader = null;
            try {
                // For Android 4.0 and earlier, you will get all app's log output, so filter it to
                // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
                String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                        "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                        "logcat -d -v time";

                // get input stream
                Process process = Runtime.getRuntime().exec(cmd);
                reader = new InputStreamReader(process.getInputStream());

                // write output stream
                strLogs = "\nAndroid version: " + Build.VERSION.SDK_INT + "\n";
                strLogs += "\nDevice: " + model + "\n";
                strLogs += "\nApp version: " + (ZPWUtils.getAppVersion(getInstance())) + "\n";

                char[] buffer = new char[10000];
                do {
                    int n = reader.read(buffer, 0, buffer.length);
                    if (n == -1)
                        break;

                    strLogs += "\n" + String.valueOf(buffer, 0, n);
                } while (true);

                reader.close();
            } catch (IOException e) {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                // You might want to write a failure message to the log here.
                return null;
            }

            return strLogs;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * extract Log to file
     *
     * @return name of file
     */
    private static String extractLogToFile() {
        try {
            String path = null;

            if (StorageUtil.isExternalStorageAvailable()) {
                path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "wallet" + File.separator + "logs" + File.separator;

                File f = new File(path);
                if (!f.isDirectory() || !f.exists()) {
                    f.mkdirs();
                }
            }

            if (TextUtils.isEmpty(path)) {
                Log.e("extractLogToFile", "Can not create log file");

                return null;
            }

            String fullName = path + "error_wallet.txt";

            // Extract to file.
            File file = new File(fullName);
            FileWriter writer = null;
            try {
                // write output stream
                writer = new FileWriter(file);
                writer.write(getLogs());

                writer.close();
            } catch (IOException e) {
                if (writer != null)
                    try {
                        writer.close();
                    } catch (IOException e1) {
                    }
                // You might want to write a failure message to the log here.
                return null;
            }

            return fullName;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Send Log
     */
    private static void sendLogFile() {
        String fullName = extractLogToFile();
        if (TextUtils.isEmpty(fullName))
            return;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chucvv@vng.com.vn"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "ZaloWallet log file");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
        intent.putExtra(Intent.EXTRA_TEXT, "Log file attached.");

        if (GlobalData.getMerchantActivity() != null)
            GlobalData.getMerchantActivity().startActivity(intent);

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
            //clear map card list
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
            //clear app info
            SharedPreferencesManager.getInstance().setExpiredTimeAppChannel("1", 0);
            SharedPreferencesManager.getInstance().setExpiredTimeAppChannel("2", 0);
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
            initGateway(pGatewayInfoCallback);
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
            GlobalData.initApplicationUserInfo(pPaymentInfo);
            refreshGatewayInfo(pGatewayInfoCallback);
            Log.d("refreshGatewayInfo", "===refreshGatewayInfo===pPaymentInfo=" + GsonUtils.toJsonString(pPaymentInfo));
        } catch (Exception e) {
            if (pGatewayInfoCallback != null)
                pGatewayInfoCallback.onError(null);
        }
    }

    private static void initGateway(ZPWGatewayInfoCallback pGatewayInfoCallback) {
        try {
            BGatewayInfo.getInstance().execute(pGatewayInfoCallback);
        } catch (Exception e) {
            if (pGatewayInfoCallback != null)
                pGatewayInfoCallback.onError(e != null ? e.getMessage() : null);

            Log.e("initGateway", e);
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
        AppInfoLoader.getInstance(1, ETransactionType.WALLET_TRANSFER, pZaloPayUserId, pAccessToken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
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
        AppInfoLoader.getInstance(2, ETransactionType.WITHDRAW, pZaloPayUserId, pAccessToken).setOnLoadAppInfoListener(new ILoadAppInfoListener() {
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

    public static OkHttpClient getHttpClient() {
        return mConfig.getHttpClient();
    }

    public static OkHttpClient getHttpClientTimeoutLonger() {
        return mConfig.getHttpClientTimeoutLonger();
    }

    public static boolean isReleaseBuild() {
        return mConfig.isReleaseBuild();
    }

    public static Constants.HostType getHostType() {
        return mConfig.getHostType();
    }

    public static Context getZaloPayContext() throws Exception {
        return mApplication.getApplicationContext();
    }
}