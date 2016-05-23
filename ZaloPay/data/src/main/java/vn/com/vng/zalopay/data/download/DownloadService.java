package vn.com.vng.zalopay.data.download;

import android.app.IntentService;
import android.content.Intent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;

/**
 * Created by AnhHieu on 5/20/16.
 */
public class DownloadService extends IntentService {

   /* @Inject
    public DownloadAppResourceTaskQueue queue;*/

    private boolean running;

    public DownloadService() {
        super("DownloadService");
        Timber.tag("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("on create ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("on handle intent %s", intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        Timber.d(" onStartCommand %s", intent);
        executeNext();
        return START_STICKY;
    }

    private void executeNext() {
     /*   if (running) return; // Only one task at a time.

        if (!listResource.isEmpty()) {
            running = true;
            AppResourceEntity appResourceEntity = listResource.getFirst();


            Timber.d("appResourceEntity %s",appResourceEntity.appid );

       //     listResource.removeFirst();
        } else {
            Timber.d("Service stopping!");
            stopSelf(); // No more tasks are present. Stop.
        }*/
    }





}
