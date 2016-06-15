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

import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.TransactionStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;

/**
 * Created by huuhoa on 5/8/16.
 */
public class ReactTransactionLogsNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private TransactionStore.Repository mRepository;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ReactTransactionLogsNativeModule(ReactApplicationContext reactContext, TransactionStore.Repository repository) {
        super(reactContext);
        this.mRepository = repository;
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

        Subscription subscription = mRepository.getTransactions(pageIndex, count)
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
        Subscription subscription = mRepository.reloadListTransaction(count)
                .map(new Func1<List<TransHistory>, WritableArray>() {
                    @Override
                    public WritableArray call(List<TransHistory> transHistories) {
                        Timber.d("list transaction : %d", transHistories.size());
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
            Timber.e(e, "error on getting transaction logs");
        }

        @Override
        public void onNext(WritableArray writableArray) {

            Timber.d("transaction log %s", writableArray);

            if (promiseWeakReference == null) {
                return;
            }

            Promise promise = promiseWeakReference.get();
            promise.resolve(writableArray);
            promiseWeakReference.clear();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    @Override
    public void onHostResume() {
        Timber.d("onResume");
    }

    @Override
    public void onHostPause() {
        Timber.d("onPause");
    }

    @Override
    public void onHostDestroy() {

        unsubscribeIfNotNull(compositeSubscription);

        getReactApplicationContext().removeActivityEventListener(this);
        getReactApplicationContext().removeLifecycleEventListener(this);
        Timber.d("onDestroy");
    }

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    private WritableMap transform(TransHistory history) {
        if (history == null) {
            return null;
        }
        WritableMap item = Arguments.createMap();
        item.putDouble("transid", history.transid);
        item.putDouble("reqdate", history.reqdate);
        item.putString("description", history.description);
        item.putInt("amount", history.amount);
        item.putInt("userfeeamt", history.userfeeamt);
        item.putInt("type", history.type);
        item.putInt("sign", history.sign);
        item.putString("username", history.username);
        item.putString("appusername", history.appusername);
        return item;
    }


    private WritableArray transform(List<TransHistory> histories) {
        WritableArray result = Arguments.createArray();
        for (TransHistory history : histories) {
            WritableMap item = transform(history);
            if (item == null) {
                continue;
            }
            result.pushMap(item);
        }
        return result;
    }

}
