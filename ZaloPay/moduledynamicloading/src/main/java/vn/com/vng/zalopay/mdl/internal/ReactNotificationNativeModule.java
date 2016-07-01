package vn.com.vng.zalopay.mdl.internal;

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
import com.google.gson.JsonObject;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.cache.NotificationStore;
import vn.com.vng.zalopay.data.ws.message.TransactionType;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by huuhoa on 6/10/16.
 * Notification for react native
 */
public class ReactNotificationNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private NotificationStore.Repository repository;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ReactNotificationNativeModule(ReactApplicationContext reactContext, NotificationStore.Repository repository) {
        super(reactContext);
        this.repository = repository;
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayNotification";
    }

    @ReactMethod
    public void getNotification(int pageIndex, int count, Promise promise) {
        Timber.d("get transaction index %s count %s", pageIndex, count);
        Subscription subscription = repository.getNotification(pageIndex, count)
                .map(new Func1<List<NotificationData>, WritableArray>() {

                    @Override
                    public WritableArray call(List<NotificationData> transHistory) {
                        return transform(transHistory);
                    }
                }).subscribe(new NotificationSubscriber(promise));

        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void updateStateReadWithNotificationId(String notificationid, Promise promise) {
        Timber.d("updateStateReadWithNotificationId %s ", notificationid);
        try {
            repository.markAsRead(Long.parseLong(notificationid));
        } catch (Exception ex) {
            Timber.w(ex, "message exception");
        }
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
            Timber.w(e, "error on getting transaction logs");
        }

        @Override
        public void onNext(WritableArray writableArray) {

            Timber.d("notification array %s", writableArray);

            Promise promise = promiseWeakReference.get();
            if (promise != null) {
                promise.resolve(writableArray);
            }
        }
    }

    private WritableMap transform(NotificationData entity) {
        if (entity == null) {
            return null;
        }

        WritableMap item = Arguments.createMap();
        item.putBoolean("unread", !entity.read);

        item.putString("message", entity.message);
        item.putDouble("timestamp", entity.timestamp);

        item.putInt("appid", entity.appid);
        item.putString("destuserid", entity.destuserid);

        int transtype = 0;
        int notificationtype = 1;

        try {
            JsonObject embeddata = entity.getEmbeddata();
            if (embeddata.has(Constants.PARAM_RESPONSE_NOTIFICATION_TYPE)) {
                notificationtype = embeddata.get(Constants.PARAM_RESPONSE_NOTIFICATION_TYPE).getAsInt();
            }

            if (embeddata.has(Constants.TRANSTYPE)) {
                transtype = embeddata.get(Constants.TRANSTYPE).getAsInt();
            }
        } catch (Exception ex) {
            Timber.w(ex, " exception parse");
        }

        Timber.d("transtype %s notificationtype %s", transtype, notificationtype);

        item.putString("title", TransactionType.getTitle(transtype));
        item.putInt("transtype", transtype);
        item.putInt("notificationtype", notificationtype);
        item.putDouble("transid", entity.getTransid());

        Timber.d("notify Id %s", entity.notificationId);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    @Override
    public void onHostResume() {
        Timber.d(" Actvity `onResume`");
    }

    @Override
    public void onHostPause() {
        Timber.d(" Actvity `onPause`");
    }

    @Override
    public void onHostDestroy() {

        unsubscribeIfNotNull(compositeSubscription);

        getReactApplicationContext().removeActivityEventListener(this);
        getReactApplicationContext().removeLifecycleEventListener(this);
        Timber.d("Actvity `onDestroy");
    }

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }


}
