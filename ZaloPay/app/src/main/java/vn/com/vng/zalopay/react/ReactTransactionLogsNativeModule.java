package vn.com.vng.zalopay.react;

import android.content.Intent;
import android.util.Pair;

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
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.TransactionChangeEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.react.model.TransactionResult;

import static vn.com.vng.zalopay.react.error.PaymentError.ERR_CODE_FAIL;
import static vn.com.vng.zalopay.react.error.PaymentError.ERR_CODE_SUCCESS;
import static vn.com.vng.zalopay.react.error.PaymentError.ERR_CODE_TRANSACTION_NOT_LOADED;

/**
 * Created by huuhoa on 5/8/16.
 * Native module for providing transaction logs to react native module
 */
class ReactTransactionLogsNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private TransactionStore.Repository mTransactionRepository;
    private final EventBus mEventBus;
    private final NotificationStore.Repository mNotificationRepository;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    ReactTransactionLogsNativeModule(ReactApplicationContext reactContext,
                                     TransactionStore.Repository repository, NotificationStore.Repository notificationRepository,
                                     EventBus eventBus) {
        super(reactContext);
        this.mTransactionRepository = repository;
        this.mEventBus = eventBus;
        this.mNotificationRepository = notificationRepository;

        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayTransactionLogs";
    }

    @ReactMethod
    public void getTransactionsSuccess(final int pageIndex, final int count, final Promise promise) {

        Timber.d("get transaction success index %s count %s", pageIndex, count);

        Subscription subscription = mTransactionRepository.getTransactions(pageIndex, count)
                .map(new Func1<List<TransHistory>, TransactionResult>() {
                    @Override
                    public TransactionResult call(List<TransHistory> transactions) {
                        int code = ERR_CODE_SUCCESS.value();
                        if (Lists.isEmptyOrNull(transactions) && pageIndex == 0) {
                            boolean isLoad = mTransactionRepository.isLoadedTransactionSuccess();
                            if (!isLoad) {
                                code = ERR_CODE_TRANSACTION_NOT_LOADED.value();
                            }
                        }

                        return new TransactionResult(code, "", transactions);
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<TransactionResult>>() {
                    @Override
                    public Observable<TransactionResult> call(Throwable throwable) {
                        return resolveTransactionSuccess(pageIndex, count, throwable);
                    }
                })
                .subscribe(new TransactionLogSubscriber(promise));
        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void getTransactionsFail(final int pageIndex, final int count, Promise promise) {

        Timber.d("get transaction fail index %s count %s", pageIndex, count);

        Subscription subscription = mTransactionRepository.getTransactionsFail(pageIndex, count)
                .map(new Func1<List<TransHistory>, TransactionResult>() {
                    @Override
                    public TransactionResult call(List<TransHistory> transactions) {
                        int code = ERR_CODE_SUCCESS.value();
                        if (Lists.isEmptyOrNull(transactions) && pageIndex == 0) {
                            boolean isLoad = mTransactionRepository.isLoadedTransactionFail();
                            if (!isLoad) {
                                code = ERR_CODE_TRANSACTION_NOT_LOADED.value();
                            }
                        }
                        return new TransactionResult(code, "", transactions);
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<TransactionResult>>() {
                    @Override
                    public Observable<TransactionResult> call(Throwable throwable) {
                        return resolveTransactionFail(pageIndex, count, throwable);
                    }
                })
                .subscribe(new TransactionLogSubscriber(promise));

        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void loadTransactionWithId(String id, Promise promise) {

        Timber.d("loadTransactionWithId %s", id);

        long value;
        try {
            value = Long.parseLong(id);
        } catch (NumberFormatException e) {
            Timber.i("Invalid format for number: %s", id);
            Helpers.promiseResolveError(promise, ERR_CODE_FAIL.value(), "Invalid input");
            return;
        }

        Subscription subscription = mTransactionRepository.getTransaction(value)
                .map(new Func1<TransHistory, TransactionResult>() {
                    @Override
                    public TransactionResult call(TransHistory transactions) {
                        return new TransactionResult(ERR_CODE_SUCCESS.value(), "", Collections.singletonList(transactions));
                    }
                })
                .subscribe(new TransactionLogSubscriber(promise));

        mCompositeSubscription.add(subscription);
    }

    private class TransactionLogSubscriber extends DefaultSubscriber<TransactionResult> {

        WeakReference<Promise> mPromise;

        TransactionLogSubscriber(Promise promise) {
            mPromise = new WeakReference<>(promise);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "error on getting transaction logs");
            Helpers.promiseResolveError(mPromise.get(), ERR_CODE_FAIL.value(), "get transaction error");
        }

        @Override
        public void onNext(TransactionResult result) {
            Timber.d(" transactions code [%s] message [%s] data [%s]", result.code, result.message, result.data);
            if (result.code == ERR_CODE_TRANSACTION_NOT_LOADED.value()) {
                Helpers.promiseResolveError(mPromise.get(), result.code, "Transaction has not been loaded");
            } else {
                Helpers.promiseResolveSuccess(mPromise.get(), result.code, result.message, transform(result.data));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        //run on Main Thread
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        updateTransactionLatest();
    }

    @Override
    public void onHostPause() {
        Timber.d("onPause");
        //run on Main Thread
        mEventBus.unregister(this);
    }

    @Override
    public void onHostDestroy() {
        //run on Main Thread
        unsubscribeIfNotNull(mCompositeSubscription);

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
        item.putDouble("amount", history.amount);
        item.putDouble("userfeeamt", history.userfeeamt);
        item.putDouble("type", history.type);
        item.putDouble("sign", history.sign);
        item.putString("username", history.username);
        item.putString("appusername", history.appusername);
        item.putString("appid", String.valueOf(history.appid));
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

    private void sendEvent(String eventName, Object param) {
        ReactApplicationContext reactContext = getReactApplicationContext();
        if (reactContext == null) {
            return;
        }

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, param);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onGetTransactionComplete(TransactionChangeEvent event) {
        Timber.d("send event zalopayTransactionsUpdated");
        WritableMap item = Arguments.createMap();
        item.putInt("statusType", event.typeSuccess);
        sendEvent("zalopayTransactionsUpdated", item);
    }

    private void updateTransactionLatest() {
        Subscription subscription = mTransactionRepository.fetchTransactionHistoryLatest()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        mCompositeSubscription.add(subscription);
    }


    @ReactMethod
    public void reloadTransactionWithId(final String transactionId, String notificationId, final Promise promise) {
        Timber.d("reloadTransactionWithId: transactionId [%s] notificationId [%s]", transactionId, notificationId);
        final long notifyId;
        try {
            notifyId = Long.valueOf(notificationId);
        } catch (NumberFormatException e) {
            Helpers.promiseResolveError(promise, -1, "Arguments invalid");
            return;
        }

        Subscription subscription = mNotificationRepository.getNotify(notifyId)
                .filter(new Func1<NotificationData, Boolean>() {
                    @Override
                    public Boolean call(NotificationData notify) {
                        return notify.timestamp > 0;
                    }
                }).flatMap(new Func1<NotificationData, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(NotificationData notify) {
                        return mTransactionRepository.reloadTransactionHistory(notify.timestamp);
                    }
                })
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        loadTransactionWithId(transactionId, promise);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Helpers.promiseResolveError(promise, -1, "Reload error");
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    private Observable<TransactionResult> resolveTransactionFail(int pageIndex, int count, final Throwable error) {
        return mTransactionRepository.getTransactionsFailLocal(pageIndex, count)
                .map(new Func1<List<TransHistory>, TransactionResult>() {
                    @Override
                    public TransactionResult call(List<TransHistory> histories) {
                        Pair<Integer, String> pairError = Helpers.createReactError(getReactApplicationContext(), error);
                        return new TransactionResult(pairError.first, pairError.second, histories);
                    }
                });
    }

    private Observable<TransactionResult> resolveTransactionSuccess(int pageIndex, int count, final Throwable error) {
        return mTransactionRepository.getTransactionsLocal(pageIndex, count)
                .map(new Func1<List<TransHistory>, TransactionResult>() {
                    @Override
                    public TransactionResult call(List<TransHistory> histories) {
                        Pair<Integer, String> pairError = Helpers.createReactError(getReactApplicationContext(), error);
                        return new TransactionResult(pairError.first, pairError.second, histories);
                    }
                });
    }

}
