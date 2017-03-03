package vn.com.zalopay.wallet.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.Constants;

public class NotificationUtils {
    private static NotificationUtils _object;
    private WeakReference<Context> mContext;
    private NotificationManager notificationManager;

    public NotificationUtils(Context context) throws Exception {
        this.mContext = new WeakReference<Context>(context);
        this.notificationManager = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
    }

    public synchronized static NotificationUtils getInstance(Context context) throws Exception {
        if (NotificationUtils._object == null)
            NotificationUtils._object = new NotificationUtils(context);
        return NotificationUtils._object;
    }

    protected Context getContext() {
        if (mContext != null) {
            return mContext.get();
        }
        return null;
    }

    public void notify(String title, String content, boolean pIsOpenApp) throws Exception {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse("http://www.google.com"));

        //PackageManager pm = mContext.getPackageManager();
        //Intent resultIntent =   pm.getLaunchIntentForPackage(Constants.CLIENT_PACKAGE);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // build notification
        Notification n = new Notification.Builder(getContext())
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                //.setOngoing(true)//setOngoing keeps the notification always in status bar
                // This sets the pending intent that should be fired when the user clicks the
                // notification. Clicking the notification launches a new activity.
                .setContentIntent(pIsOpenApp ? resultPendingIntent : null)
                //.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)// requires VIBRATE permission
                .build();
        notificationManager.notify(Constants.NOTIFICATION_NETWORKING_ID, n);

    }

    public void cancel() {
        try {
            notificationManager.cancel(Constants.NOTIFICATION_NETWORKING_ID);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }
}
