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
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.NotificationActivity;

/**
 * Created by AnhHieu on 6/15/16.
 */

public class NotificationHelper {

    final Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();
    final EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();

    final NotificationStore.Repository notifyRepository;
    final AccountStore.Repository accountRepository;
    final Context context;
    final RedPacketStore.Repository mRedPacketRepository;
    final TransactionStore.Repository transactionRepository;
    final User mUser;


    public NotificationHelper(Context applicationContext, User user,
                              NotificationStore.Repository notifyRepository,
                              AccountStore.Repository accountRepository,
                              RedPacketStore.Repository redPacketRepository,
                              TransactionStore.Repository transactionRepository
    ) {
        this.notifyRepository = notifyRepository;
        this.context = applicationContext;
        this.accountRepository = accountRepository;
        this.mRedPacketRepository = redPacketRepository;
        this.mUser = user;
        this.transactionRepository = transactionRepository;
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

    public void processNotification(NotificationData notify) {
        if (notify == null) {
            return;
        }

        this.shouldUpdateTransAndBalance(notify);
        this.shouldMarkRead(notify);

        int notificationType = notify.getNotificationType();

        if (notificationType == NotificationType.UPDATE_PROFILE_LEVEL_OK) {
            updateProfilePermission(notify);
        } else if (notificationType == NotificationType.SEND_RED_PACKET) {
            extractRedPacketFromNotification(notify);
        } else if (notificationType == NotificationType.RETRY_TRANSACTION) {
            updateTransactionStatus(notify);
        }


        this.putNotification(notify);
        this.showNotificationSystem(notify);
    }

    private void putNotification(NotificationData notify) {
        Subscription subscription = notifyRepository.putNotify(notify)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    private void shouldMarkRead(NotificationData notify) {
        if (NotificationType.shouldMarkRead(notify.notificationtype)) {
            notify.setRead(true);
        }

        if (notify.notificationtype == NotificationType.MONEY_TRANSFER
                && mUser.uid.equals(notify.userid)) {
            notify.setRead(true);
        }
    }

    private void shouldUpdateTransAndBalance(NotificationData notify) {
        if (NotificationType.isTransactionNotification(notify.notificationtype)) {
            this.updateTransaction();
            this.updateBalance();
        }
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
            Timber.e(ex, "Extract RedPacket error");
        }
    }

    private void updateTransactionStatus(NotificationData notify) {
        Subscription subscription = transactionRepository.updateTransactionStatusSuccess(notify.transid)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }


    private void updateProfilePermission(NotificationData notify) {
        try {
            JsonObject embeddata = notify.embeddata;
            if (embeddata != null) {
                int status = embeddata.get("status").getAsInt();
                int profileLevel = embeddata.get("profilelevel").getAsInt();
                if (profileLevel > 2 && status == 1) {
                    Subscription subscription = accountRepository.getUserProfileLevelCloud()
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultSubscriber<Boolean>());
                }
            }
        } catch (Exception ex) {
            Timber.e(ex, "exception");
        }
    }

    private void showNotificationSystem(NotificationData notify) {
        if (notify.read) {
            return;
        }

        eventBus.post(new NotificationChangeEvent());

        notifyRepository.totalNotificationUnRead()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NotificationSubscriber());

    }

    private void showNotificationSystem(int numberUnread) {

        if (numberUnread == 0) {
            return;
        }

        String title = context.getString(R.string.app_name);
        String message = String.format(context.getString(R.string.you_have_unread_messages), numberUnread);
        int notificationId = 200;
        Intent intent = new Intent(context, NotificationActivity.class);

        this.create(context, notificationId,
                intent,
                R.mipmap.ic_launcher,
                title, message);
    }

    private class NotificationSubscriber extends DefaultSubscriber<Integer> {

        @Override
        public void onNext(Integer integer) {
            showNotificationSystem(integer);
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "Show notify error");
        }
    }


    public void closeNotificationSystem(long notifyId) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancelAll();
        // nm.cancel(getNotificationIdSystem(notifyType));
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


   /* private void showNotification(NotificationData event) {
        if (event.read) {
            return;
        }

        String message = TextUtils.isEmpty(event.message) ? context.getString(R.string.notify_from_zalopay) : event.message;
        String title = context.getString(R.string.app_name);

        int notificationType = event.getNotificationType();

        int notificationId = this.getNotificationSystemId(notificationType);
        Intent intent = this.intentByNotificationType(notificationType);

        create(context, notificationId,
                intent,
                R.mipmap.ic_launcher,
                title, message);
    }*/

  /*  private int getNotificationSystemId(int notifyType) {
        int notificationId = 100;

        if (NotificationType.isTransactionNotification(notifyType)) {
            notificationId = 1;
        } else if (NotificationType.isProfileNotification(notifyType)) {
            notificationId = 2;
        } else if (NotificationType.isRedPacket(notifyType)) {
            notificationId = 103;
        }

        return notificationId;
    }

    private Intent intentByNotificationType(int notifyType) {
        Intent intent;
        if (NotificationType.isTransactionNotification(notifyType)) {
            intent = navigator.getIntentMiniAppActivity(context, Constants.ModuleName.TRANSACTION_LOGS);
        } else if (NotificationType.isProfileNotification(notifyType)) {
            intent = navigator.intentProfile(context);
        } else if (NotificationType.isRedPacket(notifyType)) {
            intent = navigator.getIntentMiniAppActivity(context, Constants.ModuleName.NOTIFICATIONS);
        } else {
            intent = navigator.intentHomeActivity(context, false);
        }
        return intent;
    }*/

}
