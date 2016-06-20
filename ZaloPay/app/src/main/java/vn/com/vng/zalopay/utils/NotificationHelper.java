package vn.com.vng.zalopay.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Created by AnhHieu on 6/15/16.
 */
@Singleton
public class NotificationHelper {

    @Inject
    public NotificationHelper() {
    }

    public void create(Context context, int id, Intent intent, int smallIcon, String contentTitle, String contentText) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent p = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentIntent(p)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(smallIcon)
                .setAutoCancel(true);

        Notification n = builder.build();
        manager.notify(id, n);

        Timber.d("Notification criada com sucesso");
    }

    public void createStackNotification(Context context, int id, String groupId, Intent intent, int smallIcon, String contentTitle, String contentText) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para disparar o broadcast
        PendingIntent p = intent != null ? PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT) : null;

        // Cria a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(p)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(smallIcon)
                .setGroup(groupId)
                .setAutoCancel(true);

        Notification n = builder.build();
        manager.notify(id, n);

    }

    public void create(Context context, int smallIcon, String contentTitle, String contentText) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(smallIcon)
                .setAutoCancel(true);

        Notification n = builder.build();
        manager.notify(0, n);
    }

    public void cancell(Context context, int id) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancel(id);
    }

    public void cancellAll(Context context) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancelAll();
    }

  /*  public static void showNotification(Context context, int notificationId, Notification notification) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    public static Notification getBigTextNotification(Context context, int reqCode, Intent target, String title, String content, String bigTitle, String bigText, int iconResource) {

//        Intent intent = new Intent(context, WelcomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, target,
                PendingIntent.FLAG_UPDATE_CURRENT);

        android.support.v4.app.NotificationCompat.BigTextStyle notiStyle = new
                android.support.v4.app.NotificationCompat.BigTextStyle();
        notiStyle.setBigContentTitle(bigTitle);
        notiStyle.bigText(bigText);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(context)
                .setSmallIcon(iconResource)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_ticket_leftmenu))
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(notiStyle);
        return notificationBuilder.build();
    }

    public static Notification getDefaultNotification(Context context, String title, String content, int iconResource, Activity activity) {

        Intent intent = new Intent(context, activity.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(context)
                .setSmallIcon(iconResource)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_ticket))
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        return notificationBuilder.build();
    }*/
}
