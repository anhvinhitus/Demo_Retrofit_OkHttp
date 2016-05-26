package vn.com.vng.zalopay.data.download;

import android.content.Context;
import android.content.Intent;

import java.util.Collection;
import java.util.LinkedList;

import timber.log.Timber;

/**
 * Created by AnhHieu on 5/21/16.
 */
public class DownloadAppResourceTaskQueue {

    private final LinkedList<DownloadAppResourceTask> tasklist;
    private final Context context;
    private final Class<?> cls;

    public DownloadAppResourceTaskQueue(Context context, Class<?> cls) {
        this.tasklist = new LinkedList();
        this.context = context;
        this.cls = cls;
    }

    private void startService() {
        context.startService(new Intent(context, cls));
    }

    public boolean isEmpty() {
        return (tasklist.size() == 0);
    }

    public void enqueue(DownloadAppResourceTask item) {
        tasklist.add(item);
        startService();
    }

    public void enqueue(Collection<DownloadAppResourceTask> tasks) {
        Timber.d(" enqueue currentThread %s", Thread.currentThread().getName());
        tasklist.addAll(tasks);
        startService();
    }

    public void dequeue() {
        //   Object item = tasklist.getFirst();
//
        if (!isEmpty()) {
            tasklist.removeFirst();
        }
        //  return item;
    }

    public DownloadAppResourceTask peek() {
        if (!isEmpty()) {
            return tasklist.getFirst();
        }

        return null;
    }

    public static final DownloadAppResourceTaskQueue create(Context context, Class<?> cls) {
        return new DownloadAppResourceTaskQueue(context, cls);
    }
}
