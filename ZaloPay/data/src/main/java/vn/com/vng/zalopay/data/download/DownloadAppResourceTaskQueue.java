package vn.com.vng.zalopay.data.download;

import android.content.Context;
import android.content.Intent;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by AnhHieu on 5/21/16.
 */
public class DownloadAppResourceTaskQueue {
    private final LinkedList<DownloadAppResourceTask> tasklist;
    private final Context context;

    public DownloadAppResourceTaskQueue(Context context) {
        this.tasklist = new LinkedList();
        this.context = context;
    }

    private void startService() {
        context.startService(new Intent(context, DownloadService.class));
    }

    public boolean isEmpty() {
        return (tasklist.size() == 0);
    }

    public void enqueue(DownloadAppResourceTask item) {
        tasklist.add(item);
        startService();
    }

    public void enqueue(Collection<DownloadAppResourceTask> tasks) {
        tasklist.addAll(tasks);
        startService();
    }

  /*  public Object dequeue() {
        Object item = list.getFirst();

        list.removeFirst();
        return item;
    }*/

    public DownloadAppResourceTask peek() {
        return tasklist.getFirst();
    }

    public static final DownloadAppResourceTaskQueue create(Context context) {
        return new DownloadAppResourceTaskQueue(context);
    }
}
