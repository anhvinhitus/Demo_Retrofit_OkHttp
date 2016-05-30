package vn.com.vng.zalopay.data.download;

import android.app.IntentService;
import android.content.Intent;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by AnhHieu on 5/20/16.
 */
public abstract class AbsDownloadService extends IntentService implements DownloadAppResourceTask.Callback {

    public abstract void doInject();

    @Inject
    public DownloadAppResourceTaskQueue queue;

    private final static String TAG = "AbsDownloadService";

    private boolean running;

    public AbsDownloadService() {
        super(TAG);
        Timber.tag(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        doInject();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        executeNext();
    }

    private void executeNext() {
        if (running) return; // Only one task at a time.

        Timber.d(" executeNext isEmpty %s", queue.isEmpty());
        DownloadAppResourceTask task = queue.peek();
        if (task != null) {
            running = true;
            task.execute(this);
        } else {
            Timber.i("Service stopping!");
            stopSelf(); // No more tasks are present. Stop.
        }
    }

    @Override
    public void onSuccess() {
        running = false;
        queue.dequeue();
        executeNext();
    }

    @Override
    public void onFailure() {
        running = false;
        queue.dequeue();
        executeNext();
    }

    @Override
    public void onProgress(int progress) {

    }
}
