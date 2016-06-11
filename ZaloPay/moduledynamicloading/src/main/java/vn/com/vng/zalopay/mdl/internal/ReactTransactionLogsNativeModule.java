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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.internal.operators.OperatorToMultimap;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by huuhoa on 5/8/16.
 */
public class ReactTransactionLogsNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private ZaloPayRepository repository;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ReactTransactionLogsNativeModule(ReactApplicationContext reactContext, ZaloPayRepository repository) {
        super(reactContext);
        this.repository = repository;
        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayTransactionLogs";
    }

    @ReactMethod
    public void getTransactions(int pageIndex, int count, Promise promise) {

        Timber.d("get transaction index %s count %s", pageIndex, count);

        Subscription subscription = repository.getTransactions(pageIndex, count)
                .map(new Func1<List<TransHistory>, WritableArray>() {
                    @Override
                    public WritableArray call(List<TransHistory> transHistories) {
                        return transform(transHistories);
                    }
                })
                .subscribe(new TransactionLogSubscriber(promise));

        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void reloadListTransaction(int count, Promise promise) {
        Timber.d("reload transaction count %s", count);
        Subscription subscription = repository.reloadListTransaction(count)
                .map(new Func1<List<TransHistory>, WritableArray>() {
                    @Override
                    public WritableArray call(List<TransHistory> transHistories) {
                        Timber.d("list transaction : %s", transHistories);
                        return transform(transHistories);
                    }
                })
                .subscribe(new TransactionLogSubscriber(promise));

        compositeSubscription.add(subscription);
    }

    private class TransactionLogSubscriber extends DefaultSubscriber<WritableArray> {

        WeakReference<Promise> promiseWeakReference;


        public TransactionLogSubscriber(Promise promise) {
            promiseWeakReference = new WeakReference<>(promise);
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, " onError ");
        }

        @Override
        public void onNext(WritableArray writableArray) {

            Timber.d(" transaction log %s", writableArray);

            if (promiseWeakReference != null) {
                Promise promise = promiseWeakReference.get();
                promise.resolve(writableArray);
                promiseWeakReference.clear();
            }
        }
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

    private WritableMap transform(TransHistory history) {
        if (history == null) return null;
        WritableMap item = Arguments.createMap();
        item.putDouble("transid", history.transid);
        item.putDouble("reqdate", history.reqdate);
        item.putString("description", history.description);
        item.putInt("amount", history.amount);
        item.putInt("type", history.type);
        return item;
    }


    private WritableArray transform(List<TransHistory> historys) {
        WritableArray result = Arguments.createArray();
        for (TransHistory history : historys) {
            WritableMap item = transform(history);
            if (item == null) continue;
            result.pushMap(item);
        }
        return result;
    }

}
