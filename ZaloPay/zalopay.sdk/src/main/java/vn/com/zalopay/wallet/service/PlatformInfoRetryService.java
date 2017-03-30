package vn.com.zalopay.wallet.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPlatformInfo;
import vn.com.zalopay.wallet.datasource.RequestKeeper;
import vn.com.zalopay.wallet.listener.ZPWGetGatewayInfoListener;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;

/***
 * service retry load platform info
 */
public class PlatformInfoRetryService extends Service {
    private static int countRetry;
    private Timer mTimer;
    //private GetPlatformInfo getGatewayInfoTask;

    private ZPWGetGatewayInfoListener mLoadGatewayInfoListener = new ZPWGetGatewayInfoListener() {
        @Override
        public void onProcessing() {
            Log.d("PlatformInfoRetryService.onProcessing", "Service count: " + countRetry);
        }

        @Override
        public void onSuccess() {
            if (GlobalData.getMerchantCallBack() != null) {
                GlobalData.getMerchantCallBack().onFinish();
            }

            Log.d("PlatformInfoRetryService.onSuccess", "Service count: " + countRetry);

            stopService();
        }

        @Override
        public void onError(DPlatformInfo pMessage) {
            if (GlobalData.getMerchantCallBack() != null) {
                GlobalData.getMerchantCallBack().onError(pMessage != null ? pMessage.toJsonString() : null);
            }
            Log.d("PlatformInfoRetryService.onError", pMessage != null ? pMessage.toJsonString() : "onError");
        }

        @Override
        public void onUpVersion(boolean pForceUpdate, String pVersion, String pMessage) {
            if (GlobalData.getMerchantCallBack() != null) {
                GlobalData.getMerchantCallBack().onUpVersion(pForceUpdate, pVersion, pMessage);
            }
            Log.d("PlatformInfoRetryService.onUpVersion", pVersion + "," + pMessage);
        }
    };

    public static synchronized void start(Context pContext) {
        if (pContext == null)
            return;

        Intent msgIntent = new Intent(pContext, PlatformInfoRetryService.class);
        pContext.startService(msgIntent);
    }

    public static synchronized void stop(Context pContext) {
        if (pContext != null) {
            Intent msgIntent = new Intent(pContext, PlatformInfoRetryService.class);
            pContext.stopService(msgIntent);
        }
    }

    private void stopService() {
        if (mTimer != null)
            mTimer.cancel();
        stopSelf();

        Log.d(this, "Stopping...service...load...gatewayinfo");
    }

    private void retryGatewayInfo() throws Exception {
        countRetry++;

        if (countRetry > Constants.PLATFORM_MAX_RETRY) {
            stopService();

            mLoadGatewayInfoListener.onError(null);

            return;
        }

       /* if (getGatewayInfoTask != null && getGatewayInfoTask.isProcessing()) {
            Log.d(getClass().getName(), "===there're a task retry platforminfo is running in background===count: " + countRetry);

            return;
        }

        if (!ConnectionUtil.isOnline(getApplicationContext())) {
            Log.e(getClass().getName(), "no connection !!!!");

            return;
        }

        if (getGatewayInfoTask == null)
            getGatewayInfoTask = GetPlatformInfo.getInstance(mLoadGatewayInfoListener, true);

        if (RequestKeeper.isCanRetryFlatformInfo())
            getGatewayInfoTask.makeRetry();
        else
            getGatewayInfoTask.makeRequest();*/
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTimer = new Timer();
        countRetry = 0;
        //start timer.
        mTimer.scheduleAtFixedRate(new DownloadTask(), 0, Constants.PLATFORM_RETRY_INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //this service no need system restart if run out of resource
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //this task run every interval timer.
    private class DownloadTask extends TimerTask {
        public void run() {
            try {
                retryGatewayInfo();
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }
}
