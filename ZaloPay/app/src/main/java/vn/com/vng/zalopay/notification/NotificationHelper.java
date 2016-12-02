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

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.AppLifeCycle;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.AlertNotificationEvent;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.activity.NotificationActivity;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 6/15/16.
 * *
 */

public class NotificationHelper {
    private final int NOTIFICATION_ID = 1;
    private final NotificationStore.Repository mNotifyRepository;
    private final AccountStore.Repository mAccountRepository;
    private final Context mContext;
    private final RedPacketStore.Repository mRedPacketRepository;
    private final TransactionStore.Repository mTransactionRepository;
    private final BalanceStore.Repository mBalanceRepository;
    private final User mUser;
    private final EventBus mEventBus;
    private final UserConfig mUserConfig;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    NotificationHelper(Context applicationContext, User user,
                       NotificationStore.Repository notifyRepository,
                       AccountStore.Repository accountRepository,
                       RedPacketStore.Repository redPacketRepository,
                       TransactionStore.Repository transactionRepository,
                       BalanceStore.Repository balanceRepository,
                       EventBus eventBus, UserConfig userConfig) {
        Timber.d("Create new instance of NotificationHelper");
        this.mNotifyRepository = notifyRepository;
        this.mContext = applicationContext;
        this.mAccountRepository = accountRepository;
        this.mRedPacketRepository = redPacketRepository;
        this.mUser = user;
        this.mTransactionRepository = transactionRepository;
        this.mBalanceRepository = balanceRepository;
        this.mEventBus = eventBus;
        this.mUserConfig = userConfig;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Timber.d("Finalize NotificationHelper");
    }

    private void throwNotification(Context context, int id, Intent intent, int smallIcon, String contentTitle, String contentText) {
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
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), smallIcon);
            builder.setLargeIcon(bm);
            builder.setSmallIcon(R.drawable.ic_notify_bar);
        } else {
            builder.setSmallIcon(smallIcon);
        }

        Notification n = builder.build();
        manager.notify(id, n);
    }

    void processNotification(NotificationData notify) {
        if (notify == null) {
            return;
        }

        this.shouldUpdateTransAndBalance(notify);
        this.shouldMarkRead(notify);

        boolean skipStorage = false;

        int notificationType = notify.getNotificationType();

        if (notificationType == NotificationType.UPDATE_PROFILE_LEVEL_OK) {
            updateProfilePermission(notify);
            mUserConfig.setWaitingApproveProfileLevel3(false);
            refreshGatewayInfo();
        } else if (notificationType == NotificationType.UPDATE_PROFILE_LEVEL_FAILED) {
            mUserConfig.setWaitingApproveProfileLevel3(false);
        } else if (notificationType == NotificationType.SEND_RED_PACKET) {
            extractRedPacketFromNotification(notify);
        } else if (notificationType == NotificationType.RETRY_TRANSACTION) {
            updateTransactionStatus(notify);
        } else if (notificationType == NotificationType.DONATE_MONEY) {
            showAlertNotification(notify, mContext.getString(R.string.donate_money));
        } else if (notificationType == NotificationType.MONEY_TRANSFER) {
            if (!notify.isRead()) {
                mEventBus.post(notify);
            }
        } else if (notificationType == NotificationType.APP_P2P_NOTIFICATION) {
            // post notification and skip write to db
            mEventBus.post(notify);
            skipStorage = true;
        } else if (notificationType == NotificationType.UPDATE_PLATFORMINFO) {
            refreshGatewayInfo();
        } else if (notificationType == NotificationType.LINK_CARD_EXPIRED) {
            removeLinkCard(notify);
        } else if (notificationType == NotificationType.MERCHANT_BILL) {
            payOrderFromNotify(notify);
        }

        if (!skipStorage) {
            this.putNotification(notify);
        }
    }

    private void putNotification(NotificationData notify) {
        Subscription subscription = mNotifyRepository.putNotify(notify)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onError(Throwable e) {
                        Timber.d("insert db error conflict MTAID, MTUID %s", e.getClass().getCanonicalName());
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void shouldMarkRead(NotificationData notify) {

        notify.setNotificationState(Enums.NotificationState.UNREAD.getId());

        if (NotificationType.shouldMarkRead(notify.notificationtype)) {
            notify.setNotificationState(Enums.NotificationState.READ.getId());
        }

        if (notify.notificationtype == NotificationType.MONEY_TRANSFER
                && mUser.zaloPayId.equals(notify.userid)) {
            notify.setNotificationState(Enums.NotificationState.READ.getId());
        }
    }

    private void shouldUpdateTransAndBalance(NotificationData notify) {
        Timber.d("should Update Trans And Balance");
        if (NotificationType.isTransactionNotification(notify.notificationtype)) {
            Timber.d("start update Trans And Balance");
            this.updateTransaction();
            this.updateBalance();
        }
    }

    private void showAlertNotification(final NotificationData notify, String title) {
        AlertNotificationEvent event = new AlertNotificationEvent(notify);
        event.mTitle = title;
        mEventBus.post(event);
    }

    private void extractRedPacketFromNotification(NotificationData data) {
        try {
            JsonObject embeddata = data.getEmbeddata();
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

    private void removeLinkCard(NotificationData data) {
        JsonObject embeddata = data.getEmbeddata();
        if (embeddata == null) {
            return;
        }
        int last4cardno = 0;
        int first6cardno = 0;
        if (embeddata.has("last4cardno")) {
            last4cardno = embeddata.get("last4cardno").getAsInt();
        }
        if (embeddata.has("first6cardno")) {
            first6cardno = embeddata.get("first6cardno").getAsInt();
        }

        Timber.d("Remove link card last4cardno [%s] first6cardno [%s]", last4cardno, first6cardno);
        if (last4cardno <= 0 || first6cardno <= 0) {
            return;
        }

        ZPWRemoveMapCardParams params = new ZPWRemoveMapCardParams();
        params.accessToken = mUser.accesstoken;
        params.userID = mUser.zaloPayId;
        DMappedCard mapCard = new DMappedCard();
        mapCard.cardname = "";
        mapCard.first6cardno = String.valueOf(first6cardno);
        mapCard.last4cardno = String.valueOf(last4cardno);
        params.mapCard = mapCard;
        CShareData.getInstance().removeCardOnCache(params);
    }

    private void payOrderFromNotify(NotificationData notify) {
        Timber.d("pay order via notify %s", notify);
        JsonObject embeddata = notify.getEmbeddata();
        Timber.d("pay order notificationId [%s] embeddata %s", notify.notificationId, embeddata);
        if (embeddata == null) {
            return;
        }

        if (!embeddata.has("zptranstoken") || !embeddata.has("appid")) {
            return;
        }

        String zptranstoken = embeddata.get("zptranstoken").getAsString();
        long appId = embeddata.get("appid").getAsLong();

        if (!TextUtils.isEmpty(zptranstoken) && appId > 0) {
            mEventBus.postSticky(new PaymentDataEvent(appId, zptranstoken, false, true));
        }
    }

    private void updateTransactionStatus(NotificationData notify) {
        Subscription subscription = mTransactionRepository.updateTransactionStatusSuccess(notify.transid)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        compositeSubscription.add(subscription);
    }


    private void updateProfilePermission(NotificationData notify) {
        try {
            JsonObject embeddata = notify.getEmbeddata();
            if (embeddata != null) {
                int status = embeddata.get("status").getAsInt();
                int profileLevel = embeddata.get("profilelevel").getAsInt();
                if (profileLevel > 2 && status == 1) {
                    Subscription subscription = mAccountRepository.getUserProfileLevelCloud()
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultSubscriber<Boolean>());
                    compositeSubscription.add(subscription);
                }
            }
        } catch (Exception ex) {
            Timber.e(ex, "exception");
        }
    }

    /**
     * Get count notification unread from DB & show
     */
    void showNotificationSystem() {
        Subscription subscription = mNotifyRepository.totalNotificationUnRead()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NotificationSubscriber());
        compositeSubscription.add(subscription);
    }

    /**
     * Show notification numbers
     */
    private void showNotificationSystem(int numberUnread) {
        Timber.d("Show notification system numberUnread %s", numberUnread);

        if (numberUnread == 0) {
            return;
        }

        throwNotification(mContext,
                NOTIFICATION_ID,
                new Intent(mContext, NotificationActivity.class),
                R.mipmap.ic_launcher,
                mContext.getString(R.string.app_name),
                String.format(mContext.getString(R.string.you_have_unread_messages), numberUnread));
    }

    /**
     * Show notification from Gcm
     */
    void handleNotificationFromGcm(final String message, final EmbedDataGcm embedDataGcm) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (embedDataGcm == null) {
            throwNotification(mContext,
                    NOTIFICATION_ID,
                    new Intent(mContext, NotificationActivity.class),
                    R.mipmap.ic_launcher,
                    mContext.getString(R.string.app_name),
                    message);
        } else {
            mNotifyRepository.isNotificationExisted(embedDataGcm.mtaid, embedDataGcm.mtuid)
                    .subscribe(new DefaultSubscriber<Boolean>() {
                        @Override
                        public void onNext(Boolean isExisted) {
                            if (isExisted) {
                                return;
                            }
                            NotificationData notificationData = new NotificationData();
                            notificationData.mtaid = embedDataGcm.mtaid;
                            notificationData.mtuid = embedDataGcm.mtuid;
                            putNotification(notificationData);
                            throwNotification(mContext,
                                    NOTIFICATION_ID,
                                    new Intent(mContext, NotificationActivity.class),
                                    R.mipmap.ic_launcher,
                                    mContext.getString(R.string.app_name),
                                    message);
                        }
                    });
        }
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

    void closeNotificationSystem() {
        NotificationManagerCompat nm = NotificationManagerCompat.from(mContext);
        nm.cancelAll();
    }

    private void updateTransaction() {
        Subscription subscriptionSuccess = mTransactionRepository.fetchTransactionHistoryLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        compositeSubscription.add(subscriptionSuccess);
    }

    private void updateBalance() {
        Subscription subscription = mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        compositeSubscription.add(subscription);
    }

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    private void refreshGatewayInfo() {
        mEventBus.post(new RefreshPaymentSdkEvent());
    }

    Observable<Long> getOldestTimeNotification() {
        return mNotifyRepository.getOldestTimeNotification();
    }


    void recoveryNotification(List<NotificationData> listMessage) {
        Timber.d("Recovery notification size [%s]", listMessage.size());
        Subscription subscription = mNotifyRepository.recoveryNotify(listMessage)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Void>());
        compositeSubscription.add(subscription);
    }

}
