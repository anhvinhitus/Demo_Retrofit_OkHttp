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
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.ProfilePermission;
import vn.com.vng.zalopay.event.NotificationUpdatedEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by AnhHieu on 6/15/16.
 *
 */

public class NotificationHelper {

    final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();

    final NotificationStore.LocalStorage notificationStore;
    final AccountStore.Repository accountRepository;
    final Context context;
    final RedPacketStore.Repository mRedPacketRepository;

    public NotificationHelper(Context applicationContext,
                              NotificationStore.LocalStorage notificationStore,
                              AccountStore.Repository accountRepository,
                              RedPacketStore.Repository redPacketRepository) {
        this.notificationStore = notificationStore;
        this.context = applicationContext;
        this.accountRepository = accountRepository;
        this.mRedPacketRepository = redPacketRepository;
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
        if (NotificationType.isTransactionNotification(notify.getNotificationType())) {
            this.updateTransaction();
            this.updateBalance();
        }

        if (notify.getNotificationType() == NotificationType.UPDATE_PROFILE_LEVEL_OK) {
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
        } else if (notify.getNotificationType() == NotificationType.SEND_RED_PACKET) {
            // Process received red packet
            // {"userid":"160526000000502","destuserid":"160601000000002","message":"Nguyễn Hữu Hoà đã lì xì cho bạn.","zaloMessage":"da gui li xi cho ban. Vui long vao ... de nhan li xi.","embeddata":{"bundleid":160722000000430,"packageid":1607220000004300001,"avatar":"http://avatar.talk.zdn.vn/e/d/e/2/4/75/f1898a0a0a3f05bbb11088cb202d1c02.jpg","name":"Nguyễn Hữu Hoà","liximessage":"Best wishes."},"timestamp":1469190991786,"notificationtype":103}
            extractRedPacketFromNotification(notify);
        }

        notificationStore.put(notify);
        this.showNotification(notify);

        NotificationUpdatedEvent event = new NotificationUpdatedEvent();
        AndroidApplication.instance().getAppComponent().eventBus().post(event);
    }

    private void extractRedPacketFromNotification(NotificationData data) {
        try {
            JsonObject embeddata = data.embeddata;
            if (embeddata == null) {
                return;
            }

            long bundleid = embeddata.get("bundleid").getAsLong();
            long packageid = embeddata.get("packageid").getAsLong();
            String senderAvatar = embeddata.get("avatar").getAsString();
            String senderName = embeddata.get("name").getAsString();
            String message = embeddata.get("liximessage").getAsString();

            mRedPacketRepository.addReceivedRedPacket(packageid, bundleid, senderName, senderAvatar, message)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        } catch (Exception ex) {
            Timber.e(ex, "exception");
        }
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
