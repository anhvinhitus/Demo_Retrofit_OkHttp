package vn.com.vng.zalopay.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfilePermission;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by AnhHieu on 6/15/16.
 */

public class NotificationHelper {

    final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    final NotificationStore.LocalStorage notificationStore;
    final AccountStore.Repository accountRepository;
    final Context context;

    public NotificationHelper(Context applicationContext, NotificationStore.LocalStorage notificationStore, AccountStore.Repository accountRepository) {
        this.notificationStore = notificationStore;
        this.context = applicationContext;
        this.accountRepository = accountRepository;
    }


    public void create(Context context, int id, Intent intent, int smallIcon, String contentTitle, String contentText) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (intent != null) {
            PendingIntent p = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(p);
        }

        if (AppLifeCycle.isBackGround()) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(uri);
        }

        builder.setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            builder.setLargeIcon(bm);
            builder.setSmallIcon(R.drawable.ic_notify);
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }

        Notification n = builder.build();
        manager.notify(id, n);
    }

    public void createStackNotification(Context context, int id, String groupId, Intent intent, int smallIcon, String contentTitle, String contentText) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent p = intent != null ? PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT) : null;

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

  /*  public void create(Context context, int smallIcon, String contentTitle, String contentText) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(contentTitle)
                .setContentText(contentText)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
            builder.setLargeIcon(bm);
            builder.setSmallIcon(R.drawable.ic_notify);
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }

        Notification n = builder.build();
        manager.notify(0, n);
    }*/

    public void processNotification(NotificationData notify) {

        if (notify.getTransType() > 0) {
            this.updateTransaction();
            this.updateBalance();
        } else if (notify.getNotificationType() == 2) {
            try {
                JsonObject embeddata = notify.embeddata;
                if (embeddata != null) {
                    int status = embeddata.get("status").getAsInt();
                    int profileLevel = embeddata.get("profilelevel").getAsInt();
                    if (profileLevel > 2 && status == 1) {
                        updateProfilePermission();
                    }
                }
            } catch (Exception ex) {
                Timber.e(ex, "exception");
            }
        }

        notificationStore.put(notify);
        this.showNotification(notify);

    }


    private void updateProfilePermission() {
        accountRepository.getUserProfileLevel()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<ProfilePermission>());
    }

    private void showNotification(NotificationData event) {
        if (!event.read) {
            String message = TextUtils.isEmpty(event.message) ? context.getString(R.string.notify_from_zalopay) : event.message;
            String title = context.getString(R.string.app_name);

            int notificationId = (int)event.getTransid();
            int notificationType = event.getNotificationType();
            int transType = event.getTransType();

            Intent intent = null;

            if (transType > 0) {
                intent = navigator.getIntentMiniAppActivity(context, Constants.ModuleName.NOTIFICATIONS);
                notificationId = transType;
            } else if (notificationType == 2) {
                intent = navigator.intentProfile(context);
                notificationId = notificationType;
            }

            create(context, notificationId,
                    intent,
                    R.mipmap.ic_launcher,
                    title, message);
        }
    }


    private void updateTransaction() {
        UserComponent userComponent = getUserComponent();
        if (userComponent != null) {
            userComponent.transactionRepository().updateTransaction()
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Boolean>());
        }
    }

    private void updateBalance() {
        UserComponent userComponent = getUserComponent();
        if (userComponent != null) {
            userComponent.balanceRepository().updateBalance()
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<>());
        }
    }

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }
}
