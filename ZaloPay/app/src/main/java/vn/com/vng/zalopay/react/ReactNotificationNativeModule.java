package vn.com.vng.zalopay.react;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.react.error.PaymentError;

/**
 * Created by huuhoa on 6/10/16.
 * Notification for react native
 */
class ReactNotificationNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private NotificationStore.Repository mNotificationRepository;
    private TransactionStore.Repository mTransactionRepository;
    private final EventBus mEventBus;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    ReactNotificationNativeModule(ReactApplicationContext reactContext,
                                  NotificationStore.Repository notificationRepository,
                                  TransactionStore.Repository transactionRepository,
                                  EventBus eventBus) {
        super(reactContext);
        this.mNotificationRepository = notificationRepository;
        this.mEventBus = eventBus;
        this.mTransactionRepository = transactionRepository;
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayNotification";
    }

    @ReactMethod
    public void getNotification(int pageIndex, int count, Promise promise) {
        Timber.d("get notification index %s count %s", pageIndex, count);
        Subscription subscription = mNotificationRepository.getNotification(pageIndex, count)
                .map(new Func1<List<NotificationData>, WritableArray>() {

                    @Override
                    public WritableArray call(List<NotificationData> transHistory) {
                        return transform(transHistory);
                    }
                }).subscribe(new NotificationSubscriber(promise));

        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void reloadTransactionWithId(String transactionId, String notificationId, Promise promise) {
        Timber.d("Reload transaction transId [%s] notificationId [%s] ", transactionId, notificationId);
/*
        long _notificationId = 0;
        try {
            _notificationId = Long.valueOf(notificationId);
        } catch (NumberFormatException e) {
            //empty
        }

        if (_notificationId >= 0) {
            Subscription subscription = mNotificationRepository.getNotify(_notificationId)
                    .flatMap(new Func1<NotificationData, Observable<TransHistory>>() {
                        @Override
                        public Observable<TransHistory> call(NotificationData notificationData) {
                            return null;
                        }
                    })
                    .map(new Func1<TransHistory, Pair<Integer, WritableArray>>() {
                        @Override
                        public Pair<Integer, WritableArray> call(TransHistory transHistory) {
                            int code = PaymentError.ERR_CODE_SUCCESS.value();
                            return new Pair<>(code, transformHistory(Collections.singletonList(transHistory)));
                        }
                    })
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ReloadTransactionHistorySubscriber(promise));
            compositeSubscription.add(subscription);
        } else {
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_FAIL.value(), " Input NotificationId Invalid " + notificationId);
        }*/
    }

    @ReactMethod
    public void updateStateReadWithNotificationId(String notificationid, Promise promise) {
        Timber.d("updateStateReadWithNotificationId %s ", notificationid);
        try {
            mNotificationRepository.markAsRead(Long.parseLong(notificationid));
        } catch (Exception ex) {
            Timber.w(ex, "message exception");
        }
    }


    @ReactMethod
    public void removeNotification(String notificationId, Promise promise) {

        long notifyId = -1;
        try {
            notifyId = Long.parseLong(notificationId);
        } catch (NumberFormatException e) {
            Timber.e(e, "exception");
            Helpers.promiseResolveError(promise, -1, "Notification parse error");
            return;
        }

        Subscription subscription = mNotificationRepository.removeNotification(notifyId)
                .subscribe(new RemoveNotifySubscriber(promise));
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void removeAllNotification(Promise promise) {
        Subscription subscription = mNotificationRepository.removeAllNotification()
                .subscribe(new RemoveNotifySubscriber(promise));

        compositeSubscription.add(subscription);
    }

    private class NotificationSubscriber extends DefaultSubscriber<WritableArray> {

        WeakReference<Promise> promiseWeakReference;

        public NotificationSubscriber(Promise promise) {
            promiseWeakReference = new WeakReference<>(promise);
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "error on getting notification logs");
            Helpers.promiseResolveError(promiseWeakReference.get(), PaymentError.ERR_CODE_FAIL.value(), "get notification error");
        }

        @Override
        public void onNext(WritableArray writableArray) {
            Timber.d("notification array %s", writableArray);
            Helpers.promiseResolveSuccess(promiseWeakReference.get(), writableArray);
        }
    }

    private WritableMap transform(NotificationData entity) {
        if (entity == null) {
            return null;
        }

        WritableMap item = Arguments.createMap();
        item.putDouble("transid", entity.getTransid());
        item.putInt("appid", entity.appid);
        item.putDouble("timestamp", entity.timestamp);
        item.putString("message", entity.message);
        if (entity.embeddata != null && entity.embeddata.object != null) {
            item.putString("embeddata", entity.embeddata.object.toString());
        }
        item.putInt("transtype", entity.transtype);
        item.putInt("notificationtype", entity.notificationtype);
        item.putString("userid", entity.userid);
        item.putString("destuserid", entity.destuserid);
        item.putInt("area", entity.area);
        item.putBoolean("unread", !entity.isRead());
        item.putString("notificationid", String.valueOf(entity.notificationId));
        return item;
    }

    private WritableArray transform(List<NotificationData> notificationEntities) {
        WritableArray result = Arguments.createArray();
        for (NotificationData entity : notificationEntities) {
            WritableMap item = transform(entity);
            if (item == null) {
                continue;
            }
            result.pushMap(item);
        }
        return result;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    /**
     * Called when a new intent is passed to the activity
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        Timber.d("onNewIntent called from based");
    }

    @Override
    public void onHostResume() {
        Timber.d("Activity onResume");
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void onHostPause() {
        Timber.d("Activity onPause");
        mEventBus.unregister(this);
    }

    @Override
    public void onHostDestroy() {
        Timber.d("Activity onDestroy");
        unsubscribeIfNotNull(compositeSubscription);

        getReactApplicationContext().removeActivityEventListener(this);
        getReactApplicationContext().removeLifecycleEventListener(this);
    }

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    public void sendEvent(String eventName) {
        ReactApplicationContext reactContext = getReactApplicationContext();
        if (reactContext == null) {
            return;
        }

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, null);
    }

    @Subscribe
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("on receive notification event");

        sendEvent("zalopayNotificationsAdded");
    }

    private class RemoveNotifySubscriber extends DefaultSubscriber<Boolean> {
        private Promise promise;

        RemoveNotifySubscriber(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onError(Throwable e) {
            Helpers.promiseResolveError(promise, -1, "Remove notification error");
        }

        @Override
        public void onCompleted() {
            Helpers.promiseResolveSuccess(promise, null);
        }
    }

}
