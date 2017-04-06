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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
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
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.Enums;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.AlertNotificationEvent;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.activity.NotificationEmptyActivity;
import vn.com.vng.zalopay.utils.CShareDataWrapper;

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

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private List<Long> mListPacketIdToRecovery = new ArrayList<>();

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

    private void processNotification(@NonNull NotificationData notify, boolean isRecovery) {

        this.shouldMarkRead(notify);

        int notificationType = (int) notify.notificationtype;
        switch (notificationType) {
            case NotificationType.UPDATE_PROFILE_LEVEL_OK:
                updateLevelProfile(notify, true);
                break;
            case NotificationType.UPDATE_PROFILE_LEVEL_FAILED:
                updateLevelProfile(notify, false);
                break;
            case NotificationType.SEND_RED_PACKET:
                extractRedPacketFromNotification(notify, isRecovery);
                break;
            case NotificationType.RETRY_TRANSACTION:
                updateTransactionStatus(notify);
                break;
            case NotificationType.RESET_PAYMENT_PASSWORD:
                resetPaymentPassword();
                break;
            case NotificationType.LINK_CARD_EXPIRED:
                removeLinkCard(notify);
                break;
        }

        if (isRecovery) {
            return;
        }

        if (notify.transid > 0) {
            CShareDataWrapper.notifyTransactionFinish(notify.transid, notify.timestamp);
        }

        boolean skipStorage = false;
        this.shouldUpdateTransAndBalance(notify);

        switch (notificationType) {
            case NotificationType.DONATE_MONEY:
                showAlertNotification(notify, mContext.getString(R.string.donate_money));
                break;
            case NotificationType.MONEY_TRANSFER:
                if (!notify.isRead()) {
                    mEventBus.post(notify);
                }
                break;
            case NotificationType.APP_P2P_NOTIFICATION:
                mEventBus.post(notify);
                skipStorage = true;
                break;
            case NotificationType.MERCHANT_BILL:
                payOrderFromNotify(notify);
                break;
            case NotificationType.UNLINK_ACCOUNT:
                CShareDataWrapper.pushNotificationToSdk(notificationType, notify.message);
                break;
            case NotificationType.LINK_ACCOUNT:
                CShareDataWrapper.pushNotificationToSdk(notificationType, notify.message);
                break;
        }

        if (!skipStorage) {
            this.putNotification(notify);
        }

    }

    void processImmediateNotification(NotificationData notify) {
        processNotification(notify, false);
    }

    private void processRecoveryNotification(NotificationData notify) {
        processNotification(notify, true);
    }

    private void putNotification(NotificationData notify) {
        Subscription subscription = mNotifyRepository.putNotify(notify)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    private void updateLevelProfile(NotificationData notify, boolean isSuccess) {
        if (isSuccess) {
            updateProfilePermission(notify);
            refreshGatewayInfo();
        }

        mUserConfig.setWaitingApproveProfileLevel3(false);
    }

    private void shouldMarkRead(NotificationData notify) {

        notify.notificationstate = (Enums.NotificationState.UNREAD.getId());

        if (NotificationType.shouldMarkRead(notify.notificationtype)) {
            notify.notificationstate = (Enums.NotificationState.READ.getId());
        }

        if (notify.notificationtype == NotificationType.MONEY_TRANSFER
                && mUser.zaloPayId.equals(notify.userid)) {
            notify.notificationstate = (Enums.NotificationState.READ.getId());
        }
    }

    private void shouldUpdateTransAndBalance(NotificationData notify) {
        Timber.d("Should update transaction and balance");
        if (NotificationType.isTransactionNotification(notify.notificationtype)) {
            this.updateTransaction();
            this.updateBalance();
        }
    }

    private void showAlertNotification(final NotificationData notify, String title) {
        AlertNotificationEvent event = new AlertNotificationEvent(notify);
        event.mTitle = title;
        mEventBus.post(event);
    }

    private void extractRedPacketFromNotification(NotificationData data, boolean addToRecovery) {

        JsonObject embeddata = data.getEmbeddata();
        if (embeddata == null) {
            return;
        }
        try {
            long bundleid = embeddata.get("bundleid").getAsLong();
            long packageid = embeddata.get("packageid").getAsLong();
            String senderAvatar = embeddata.get("avatar").getAsString();
            String senderName = embeddata.get("name").getAsString();
            String message = embeddata.get("liximessage").getAsString();

            Subscription subscription = mRedPacketRepository.addReceivedRedPacket(packageid, bundleid, senderName, senderAvatar, message)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Void>());
            mCompositeSubscription.add(subscription);

            if (addToRecovery) {
                mListPacketIdToRecovery.add(packageid);
            }
        } catch (Exception ex) {
            Timber.d(ex, "Extract RedPacket error");
        }

    }

    private void removeLinkCard(NotificationData data) {
        JsonObject embeddata = data.getEmbeddata();
        if (embeddata == null) {
            return;
        }
        int last4cardno = 0;
        int first6cardno = 0;

        try {
            if (embeddata.has("last4cardno")) {
                last4cardno = embeddata.get("last4cardno").getAsInt();
            }
            if (embeddata.has("first6cardno")) {
                first6cardno = embeddata.get("first6cardno").getAsInt();
            }
        } catch (Exception e) {
            return;
        }

        Timber.d("Remove link card last4cardno [%s] first6cardno [%s]", last4cardno, first6cardno);

        if (last4cardno <= 0 || first6cardno <= 0) {
            return;
        }

        CShareDataWrapper.reloadMapCardList(String.valueOf(last4cardno), String.valueOf(first6cardno), mUser, null);
    }

    private void payOrderFromNotify(NotificationData notify) {
        JsonObject embeddata = notify.getEmbeddata();
        Timber.d("pay order notificationId [%s] embeddata [%s]", notify.notificationId, embeddata);
        if (embeddata == null) {
            return;
        }

        if (!embeddata.has("zptranstoken") || !embeddata.has("appid")) {
            return;
        }

        String zptranstoken;
        long appId;

        try {
            zptranstoken = embeddata.get("zptranstoken").getAsString();
            appId = embeddata.get("appid").getAsLong();
        } catch (Exception e) {
            return;
        }

        if (!TextUtils.isEmpty(zptranstoken) && appId > 0) {
            PaymentDataEvent event = new PaymentDataEvent(appId, zptranstoken, false, true);
            event.notification = notify;

            mEventBus.postSticky(event);
        }
    }

    private void updateTransactionStatus(NotificationData notify) {
        Subscription subscription = mTransactionRepository.updateTransactionStatusSuccess(notify.transid)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }


    private void updateProfilePermission(NotificationData notify) {

        JsonObject embeddata = notify.getEmbeddata();
        if (embeddata == null) {
            return;
        }

        try {
            int status = embeddata.get("status").getAsInt();
            int profileLevel = embeddata.get("profilelevel").getAsInt();
            if (profileLevel > 2 && status == 1) {
                Subscription subscription = mAccountRepository.getUserProfileLevelCloud()
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultSubscriber<>());
                mCompositeSubscription.add(subscription);
            }
        } catch (Exception ex) {
            Timber.d(ex);
        }
    }

    /**
     * Get count notification unread from DB & show
     */
    void showNotificationSystem() {
        Subscription subscription = mNotifyRepository.totalNotificationUnRead()
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NotificationSubscriber());
        mCompositeSubscription.add(subscription);
    }

    private class NotificationSubscriber extends DefaultSubscriber<Integer> {

        @Override
        public void onNext(Integer integer) {
            showNotificationSystem(integer);
        }
    }

    /**
     * Show notification numbers
     */
    private void showNotificationSystem(int numberUnread) {
        Timber.d("Show notification system : numberUnread [%s]", numberUnread);

        if (numberUnread == 0) {
            return;
        }

        throwNotification(mContext,
                NOTIFICATION_ID,
                new Intent(mContext, NotificationEmptyActivity.class),
                R.mipmap.ic_launcher,
                mContext.getString(R.string.app_name),
                String.format(mContext.getString(R.string.you_have_unread_messages), numberUnread));
    }

    /**
     * Show notification from Gcm
     */
    void handleNotificationFromGcm(@NonNull final String message, @Nullable final EmbedDataGcm embedDataGcm) {
        if (embedDataGcm == null) {
            throwNotification(mContext,
                    NOTIFICATION_ID,
                    new Intent(mContext, NotificationEmptyActivity.class),
                    R.mipmap.ic_launcher,
                    mContext.getString(R.string.app_name),
                    message);
        } else {
            Subscription subscription = mNotifyRepository.isNotificationExisted(embedDataGcm.mtaid, embedDataGcm.mtuid)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Boolean>() {
                        @Override
                        public void onNext(Boolean isExisted) {
                            if (isExisted) {
                                return;
                            }
                            NotificationData notificationData = new NotificationData();
                            notificationData.mtaid = embedDataGcm.mtaid;
                            notificationData.mtuid = embedDataGcm.mtuid;
                            shouldMarkRead(notificationData);
                            putNotification(notificationData);
                            throwNotification(mContext,
                                    NOTIFICATION_ID,
                                    new Intent(mContext, NotificationEmptyActivity.class),
                                    R.mipmap.ic_launcher,
                                    mContext.getString(R.string.app_name),
                                    message);
                        }
                    });
            mCompositeSubscription.add(subscription);
        }
    }

    void closeNotificationSystem() {
        NotificationManagerCompat nm = NotificationManagerCompat.from(mContext);
        nm.cancelAll();
    }

    private void updateTransaction() {
        Subscription subscriptionSuccess = mTransactionRepository.fetchTransactionHistoryLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscriptionSuccess);
    }

    private void updateBalance() {
        Subscription subscription = mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    private void refreshGatewayInfo() {
        mEventBus.post(new RefreshPaymentSdkEvent());
    }

    /**
     * Kiểm tra từng notify, Nếu notify đã tồn tại trong db thì không thực hiện process.
     * Sau đó save all notify
     */
    Observable<Void> recoveryNotification(List<NotificationData> listMessage) {
        Timber.d("Recovery notification : size [%s]", listMessage.size());
        return Observable.from(listMessage)
                .filter(notify -> !mNotifyRepository.isNotifyExisted(notify.mtaid, notify.mtuid))
                .doOnNext(this::processRecoveryNotification)
                .lastOrDefault(new NotificationData())
                .flatMap(notify -> mNotifyRepository.recoveryNotify(listMessage));
    }

    void recoveryRedPacketStatus() {
        if (Lists.isEmptyOrNull(mListPacketIdToRecovery)) {
            return;
        }

        Subscription subscription = mRedPacketRepository.getListPackageStatus(mListPacketIdToRecovery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        mListPacketIdToRecovery.clear();
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    void recoveryTransaction() {
        Timber.d("Recovery transaction");
        Subscription subscription = mNotifyRepository.getOldestTimeNotification()
                .filter(time -> time > 0)
                .flatMap(mTransactionRepository::fetchTransactionHistoryOldest)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    Observable<Long> getOldestTimeRecoveryNotification(final boolean isFirst) {
        return mNotifyRepository.getOldestTimeRecoveryNotification()
                .filter(time -> !(isFirst && mNotifyRepository.isRecovery()) && (!isFirst || time <= 0));
    }

    private void resetPaymentPassword() {
        refreshGatewayInfo();
        mUserConfig.removeFingerprint();
    }

}
