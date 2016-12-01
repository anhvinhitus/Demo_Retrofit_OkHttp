package vn.com.vng.zalopay.data.appresources;

import android.app.IntentService;
import android.content.Intent;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by AnhHieu on 5/20/16.
 *
 */
public abstract class AbsDownloadService extends IntentService {

    public abstract void doInject();

    @Inject
    public DownloadAppResourceTaskQueue mTaskQueue;

    private final static String TAG = "AbsDownloadService";

    private boolean mRunning;

    public AbsDownloadService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate");
        doInject();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("onHandleIntent");
        executeNext();
    }

    private void executeNext() {
        Timber.d("executeNext");
        if (mRunning) {
            Timber.d("Skip executeing download since there is running task");
            return; // Only one task at a time.
        }

        Timber.d(" executeNext isEmpty %s", mTaskQueue.isEmpty());
        while (!mTaskQueue.isEmpty()) {
            DownloadAppResourceTask task = mTaskQueue.peek();
            if (task != null) {
                mRunning = true;
                boolean result = task.execute();

                if (result) {
                    Timber.d("download success");
                } else {
                    Timber.d("download failed");
                }

                mRunning = false;
                mTaskQueue.dequeue();
            }
        }

        Timber.i("Service stopping!");
        stopSelf(); // No more tasks are present. Stop.
    }
}
