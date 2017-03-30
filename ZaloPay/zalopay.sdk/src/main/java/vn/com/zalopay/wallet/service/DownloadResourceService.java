package vn.com.zalopay.wallet.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

import vn.com.zalopay.wallet.business.behavior.gateway.BGatewayInfo;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.utils.Log;

/***
 * retry download resource class service
 */
public class DownloadResourceService extends Service {
    public static String urlResourceToDownload;
    public static String resourcePathInStorage;
    public static String resrcVer;
    private static int countRetry;
    private Timer mTimer;

    /**
     * Start service download resource
     *
     * @param pContext Context
     */
    public static synchronized void start(Context pContext) {
        if (pContext != null) {
            Intent msgIntent = new Intent(pContext, DownloadResourceService.class);
            pContext.startService(msgIntent);
        }
    }

    /**
     * Stop service download resource
     *
     * @param pContext Context
     */
    public static synchronized void stop(Context pContext) {
        if (pContext != null) {
            Intent msgIntent = new Intent(pContext, DownloadResourceService.class);
            pContext.stopService(msgIntent);
        }
    }

    /**
     * Stop service
     */
    private void stopService() {
        mTimer.cancel();
        stopSelf();

        urlResourceToDownload = null;
        resourcePathInStorage = null;
        resrcVer = null;

        Log.d(this, "Stopping...service...download...resource");
    }

    /**
     * call api download resource
     */
    private void download() {
        countRetry++;

        if (countRetry > Constants.DOWNLOAD_RESOURCE_MAX_RETRY || BGatewayInfo.isValidConfig()) {
            stopService();
            return;
        }

        Log.d(getClass().getName(), "===starting download resource again in service===count: " + countRetry);
        Log.d(getClass().getName(), "===url resource to download ===" + urlResourceToDownload + "==folder to unzip:" + resourcePathInStorage + "==version==" + resrcVer);

        /*if (DownloadBundle.processing) {
            Log.d(getClass().getName(), "===there're a task is running in background===count: " + countRetry);
            return;
        }

        if (!TextUtils.isEmpty(urlResourceToDownload) && ConnectionUtil.isOnline(getApplicationContext())) {
            DownloadBundle downloadResourceTask = new DownloadBundle(mDownloadResourceListener, urlResourceToDownload, resourcePathInStorage, resrcVer);
            downloadResourceTask.execute();
        }*/
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTimer = new Timer();
        countRetry = 0;

        mTimer.scheduleAtFixedRate(new DownloadTask(), Constants.DOWNLOAD_RESOURCE_RETRY_INTERVAL, Constants.DOWNLOAD_RESOURCE_RETRY_INTERVAL);
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

    private class DownloadTask extends TimerTask {
        public void run() {
            download();
        }
    }
}
