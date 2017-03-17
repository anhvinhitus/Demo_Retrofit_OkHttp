package vn.com.vng.zalopay.data.appresources;

import android.app.IntentService;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.DownloadAppEvent;
import vn.com.vng.zalopay.data.eventbus.DownloadZaloPayResourceEvent;

/**
 * Created by AnhHieu on 5/20/16.
 */
public abstract class AbsDownloadService extends IntentService {

    protected abstract void doInject();

    private int mZaloPayAppId;

    @Inject
    public DownloadAppResourceTaskQueue mTaskQueue;

    private final static String TAG = "DownloadService";

    private boolean mRunning;

    public AbsDownloadService(int zaloPayAppId) {
        super(TAG);
        mZaloPayAppId = zaloPayAppId;
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

            if (task == null) {
                break;
            }

            mRunning = true;
            boolean result = task.execute();

            if (task.getDownloadInfo().appid == mZaloPayAppId) {
                EventBus.getDefault().postSticky(new DownloadZaloPayResourceEvent(result));
            } else {
                EventBus.getDefault().postSticky(new DownloadAppEvent(result, task.getDownloadInfo()));
            }

            if (result) {
                Timber.d("download success");
            } else {
                Timber.d("download failed");
            }

            mRunning = false;
            mTaskQueue.dequeue();
        }

        Timber.d("Service stopping!");
        stopSelf(); // No more tasks are present. Stop.
    }
}
