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
 * Service execute task which retrieves from DownloadAppResourceTaskQueue.
 */
public abstract class AbsDownloadService extends IntentService {

    protected abstract void doInject();

    private int mZaloPayAppId;

    @Inject
    public DownloadAppResourceTaskQueue mTaskQueue;

    private final static String TAG = "DownloadService";

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
        mTaskQueue.setRunningDownloadService(false);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Handle new intent.");
        executeNext();
    }

    private void executeNext() {
        if (mTaskQueue.isRunningDownloadService()) {
            Timber.d("Skip executing download since there is running task");
            return; // Only one task at a time.
        }
        mTaskQueue.setRunningDownloadService(true);

        Timber.d("Check task queue before execute, queue isEmpty: %s", mTaskQueue.isEmpty());
        while (!mTaskQueue.isEmpty()) {
            DownloadAppResourceTask task = mTaskQueue.peek();

            if (task == null) {
                break;
            }

            boolean result = task.execute();

            if (task.getDownloadInfo().appid == mZaloPayAppId) {
                EventBus.getDefault().postSticky(new DownloadZaloPayResourceEvent(result));
            } else {
                EventBus.getDefault().postSticky(new DownloadAppEvent(result, task.getDownloadInfo()));
            }

            mTaskQueue.dequeue();
        }

        Timber.d("Download service stopping!");
        stopSelf(); // No more tasks are present. Stop.
    }
}
