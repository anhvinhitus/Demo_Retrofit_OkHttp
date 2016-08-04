package vn.com.vng.iot.debugviewer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;

/**
 * Created by huuhoa on 12/25/15.
 */
public class DebugViewer {
    private static final int NOTIFICATION_ID = 1000;

    public static void registerInstance(Context context) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, LogActivity.class), 0); //this is how we will launch an Activity when the user clicks the notification
        Notification.Builder mBuilder = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setOngoing(true)
//                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentText(context.getText(R.string.debugger_content))
                .setContentTitle(context.getText(R.string.debugger_title))
                .setContentIntent(contentIntent);


        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    public static void postLog(int level, String tag, String message) {
        instance().mLogCacheStorage.postLog(level, tag, message);
    }

    static DebugViewer mInstance = null;
    private final LogCacheStorage mLogCacheStorage = new LogCacheStorage();

    static DebugViewer instance() {
        if (mInstance == null) {
            mInstance = new DebugViewer();
        }
        return mInstance;
    }

    void registerMessageListener(LogCacheStorage.ILogListener listener) {
        mLogCacheStorage.setListener(listener);
    }

    void unregisterMessageListener() {
        mLogCacheStorage.setListener(null);
    }

    ArrayList<String> getLogs() {
        return mLogCacheStorage.getLogs();
    }

    void clearLogs() {
        mLogCacheStorage.clear();
    }
}
