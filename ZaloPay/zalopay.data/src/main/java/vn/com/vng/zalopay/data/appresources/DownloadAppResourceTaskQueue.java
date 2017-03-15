package vn.com.vng.zalopay.data.appresources;

import android.content.Context;
import android.content.Intent;

import java.util.Collection;
import java.util.LinkedList;

import timber.log.Timber;

/**
 * Created by AnhHieu on 5/21/16.
 *
 */
public class DownloadAppResourceTaskQueue {

    private final LinkedList<DownloadAppResourceTask> mTasklist;
    private final Context mContext;
    private final Class<?> mServiceClass;

    public DownloadAppResourceTaskQueue(Context context, Class<?> serviceClass) {
        this.mTasklist = new LinkedList<>();
        this.mContext = context;
        this.mServiceClass = serviceClass;
    }

    public boolean isEmpty() {
        return (mTasklist.size() == 0);
    }

    void enqueue(Collection<DownloadAppResourceTask> tasks) {
        Timber.d("enqueue tasks size %s", mTasklist.size());
        for (DownloadAppResourceTask task : tasks) {
            if (mTasklist.contains(task)) {
                continue;
            }

            mTasklist.add(task);
        }
        Timber.d("start tasks size %s", mTasklist.size());

        // start service
        mContext.startService(new Intent(mContext, mServiceClass));
    }

    void dequeue() {
        mTasklist.poll();
    }

    DownloadAppResourceTask peek() {
        return mTasklist.peek();
    }

    public static DownloadAppResourceTaskQueue create(Context context, Class<?> serviceClass) {
        return new DownloadAppResourceTaskQueue(context, serviceClass);
    }
}