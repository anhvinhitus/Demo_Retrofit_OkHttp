package vn.com.vng.zalopay.notification;

import android.app.Activity;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.NotificationActivity;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.controller.SDKPayment;
import vn.com.zalopay.wallet.utils.Log;
import vn.zalopay.promotion.ActionType;
import vn.zalopay.promotion.IPromotionListener;
import vn.zalopay.promotion.PromotionAction;
import vn.zalopay.promotion.PromotionEvent;

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

    void processNotification(NotificationData notify) {
        processNotification(notify, false);
    }

    void processNotification(NotificationData notify, boolean isNotificationRecovery) {

        if (notify == null) {
            return;
        }
        Timber.d("processNotification: %s %s", notify.message, isNotificationRecovery);

        if (!isNotificationRecovery) {
            this.shouldUpdateTransAndBalance(notify);

            if (notify.transid > 0) {
                CShareDataWrapper.notifyTransactionFinish(notify.notificationtype, notify.transid, notify.timestamp);
            }
        }

        this.shouldMarkRead(notify);

        boolean skipStorage = false;

        int notificationType = (int) notify.notificationtype;

        switch (notificationType) {
            case NotificationType.UPDATE_PROFILE_LEVEL_OK:
                updateLevelProfile(notify, true);
                break;
            case NotificationType.UPDATE_PROFILE_LEVEL_FAILED:
                updateLevelProfile(notify, false);
                break;
            case NotificationType.SEND_RED_PACKET:
                extractRedPacketFromNotification(notify, isNotificationRecovery);
                break;
            case NotificationType.RETRY_TRANSACTION:
                updateTransactionStatus(notify);
                break;
            case NotificationType.DONATE_MONEY:
                if (isNotificationRecovery) {
                    break;
                }

                showAlertNotification(notify, mContext.getString(R.string.donate_money));
                break;
            case NotificationType.MONEY_TRANSFER:
                if (isNotificationRecovery) {
                    break;
                }

                if (!notify.isRead()) {
                    mEventBus.post(notify);
                }

                break;
            case NotificationType.APP_P2P_NOTIFICATION:

                if (isNotificationRecovery) {
                    break;
                }
                // post notification and skip write to db
                mEventBus.post(notify);
                skipStorage = true;
                break;
            case NotificationType.RESET_PAYMENT_PASSWORD:
                resetPaymentPassword();
                break;
            case NotificationType.LINK_CARD_EXPIRED:
                reloadMapCardList(notify);
                break;
            case NotificationType.MERCHANT_BILL:
                if (isNotificationRecovery) {
                    break;
                }

                payOrderFromNotify(notify);
                break;
            case NotificationType.UNLINK_ACCOUNT:
                if (!isNotificationRecovery) {
                    CShareDataWrapper.pushNotificationToSdk(mUser, notificationType, notify.message);
                }
                break;
            case NotificationType.LINK_ACCOUNT:
                if (!isNotificationRecovery) {
                    CShareDataWrapper.pushNotificationToSdk(mUser, notificationType, notify.message);
                }
                break;
            case NotificationType.PROMOTION:
                if (!isNotificationRecovery) {
                    postPromotion(notify);
                }
                break;
            default:
                Timber.d("undefine notification type %d", notificationType);
        }

        if (!skipStorage && !isNotificationRecovery) {
            this.putNotification(notify);
        }
    }

    private void putNotification(NotificationData notify) {
        Subscription subscription = mNotifyRepository.putNotify(notify)
                .filter(rowId -> rowId > 0)
                .doOnNext(rowId -> notify.notificationId = rowId)
                .flatMap(new Func1<Long, Observable<NotificationData>>() {
                    @Override
                    public Observable<NotificationData> call(Long rowId) {
                        return Observable.just(notify);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<NotificationData>() {
                    @Override
                    public void onNext(NotificationData notify) {
                        postNotification(notify);
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    private void postNotification(NotificationData notify) {
        if (notify == null) {
            Timber.d("post notification is null");
            return;
        }
        switch ((int) notify.notificationtype) {
            case NotificationType.PROMOTION:
                postPromotion(notify); //post update again promotion notification with new notification id inserted in local db
                break;
            default:
                Timber.d("undefine notification type");
        }
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
        Timber.d("should Update Trans And Balance");
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

    private void postPromotion(NotificationData data) {
        try {
            JsonObject embeddata = data.getEmbeddata();
            if (embeddata == null) {
                return;
            }
            int type = embeddata.get("type").getAsInt();
            String title = embeddata.get("title").getAsString();
            long amount = embeddata.get("amount").getAsLong();
            String campaign = embeddata.get("campaign").getAsString();
            List<PromotionAction> actions = new ArrayList<>();
            JsonArray jsonArrayActions = embeddata.get("actions").getAsJsonArray();
            for (int i = 0; i < jsonArrayActions.size(); i++) {
                JsonObject jsonObjectAction = jsonArrayActions.get(i).getAsJsonObject();
                String titleAction = jsonObjectAction.get("title").getAsString();
                int action = jsonObjectAction.get("action").getAsInt();
                PromotionAction promotionAction = new PromotionAction(titleAction, action);
                actions.add(promotionAction);
            }

            PromotionEvent promotionEvent = new PromotionEvent(type, title, amount, campaign, actions, data.transid, data.notificationId);
            //send into sdk if user in payment
            if (SDKPayment.isOpenSdk()) {
                CShareDataWrapper.notifyPromotionEventToSdk(promotionEvent, new IPromotionListener() {
                    @Override
                    public void onReceiverNotAvailable() {
                        //notification come late and user enter sdk for another payment
                        mEventBus.postSticky(promotionEvent);
                    }

                    @Override
                    public void onPromotionAction(Context pContext, PromotionEvent pPromotionEvent) {
                        if (pPromotionEvent != null && pPromotionEvent.actions != null && !pPromotionEvent.actions.isEmpty()) {
                            switch (pPromotionEvent.actions.get(0).action) {
                                case ActionType.TRANSACTION_DETAIL:
                                    Navigator navigator = AndroidApplication.instance().getAppComponent().navigator();
                                    if (pPromotionEvent.notificationId > 0) {
                                        navigator.startTransactionDetail(pContext, String.valueOf(pPromotionEvent.transid), String.valueOf(pPromotionEvent.notificationId));
                                    } else {
                                        navigator.startMiniAppActivity((Activity) pContext, ModuleName.NOTIFICATIONS);
                                    }
                                    break;
                                default:
                                    Timber.d("undefine action on promotion");
                            }
                        }
                    }

                    @Override
                    public void onClose() {
                    }
                });
                Log.d(this, "post promotion event from notification to sdk", promotionEvent);
            } else {
                /***
                 * send to subscriber on {@link vn.com.vng.zalopay.ui.presenter.MainPresenter}
                 */
                mEventBus.postSticky(promotionEvent);
                Log.d(this, "post promotion event from notification to subscriber", promotionEvent);
            }
        } catch (Exception ex) {
            Timber.e(ex, "Extract PromotionEvent data error");
        }
    }

    private void extractRedPacketFromNotification(NotificationData data, boolean addToRecovery) {
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

            Subscription subscription = mRedPacketRepository.addReceivedRedPacket(packageid, bundleid, senderName, senderAvatar, message)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<>());
            mCompositeSubscription.add(subscription);

            if (addToRecovery) {
                mListPacketIdToRecovery.add(packageid);
            }
        } catch (Exception ex) {
            Timber.e(ex, "Extract RedPacket error");
        }
    }

    private void reloadMapCardList(NotificationData data) {
        CShareDataWrapper.reloadMapCardList("", "", mUser, null);
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
        try {
            JsonObject embeddata = notify.getEmbeddata();
            if (embeddata != null) {
                int status = embeddata.get("status").getAsInt();
                int profileLevel = embeddata.get("profilelevel").getAsInt();
                if (profileLevel > 2 && status == 1) {
                    Subscription subscription = mAccountRepository.getUserProfileLevelCloud()
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultSubscriber<>());
                    mCompositeSubscription.add(subscription);
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
        mCompositeSubscription.add(subscription);
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
                                    new Intent(mContext, NotificationActivity.class),
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
    public Observable<Void> recoveryNotification(final List<NotificationData> listMessage) {
        Timber.d("Recovery notification size [%s]", listMessage.size());
        return Observable.from(listMessage)
                .filter(notify -> !mNotifyRepository.isNotifyExisted(notify.mtaid, notify.mtuid))
                .doOnNext(notify -> processNotification(notify, true))
                .lastOrDefault(new NotificationData())
                .flatMap(new Func1<NotificationData, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(NotificationData notify) {
                        return mNotifyRepository.recoveryNotify(listMessage);
                    }
                });
    }

    public void recoveryRedPacketStatus() {
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

    public void recoveryTransaction() {
        Timber.d("recovery Transaction");
        Subscription subscription = mNotifyRepository.getOldestTimeNotification()
                .filter(time -> time > 0)
                .flatMap(mTransactionRepository::fetchTransactionHistoryOldest)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    public Observable<Long> getOldestTimeRecoveryNotification(final boolean isFirst) {
        return mNotifyRepository.getOldestTimeRecoveryNotification()
                .filter(time -> !(isFirst && mNotifyRepository.isRecovery()) && (!isFirst || time <= 0));
    }

    private void resetPaymentPassword() {
        refreshGatewayInfo();
        mUserConfig.removeFingerprint();
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

}
