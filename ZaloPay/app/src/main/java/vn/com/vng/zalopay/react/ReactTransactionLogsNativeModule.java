package vn.com.vng.zalopay.react;

import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import android.util.Pair;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.eventbus.TransactionDetailChangeEvent;
import vn.com.vng.zalopay.data.eventbus.TransactionChangeEvent;
import vn.com.vng.zalopay.data.exception.ArgumentException;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.navigation.INavigator;
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
    private INavigator mNavigator;
    private final EventBus mEventBus;
    private final NotificationStore.Repository mNotificationRepository;
    private AppResourceStore.Repository mResourceRepository;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private static final int ERR_CODE_OUT_OF_DATA = 2;

    ReactTransactionLogsNativeModule(ReactApplicationContext reactContext, INavigator navigator,
                                     TransactionStore.Repository repository, AppResourceStore.Repository resourceRepository,
                                     NotificationStore.Repository notificationRepository,
                                     EventBus eventBus) {
        super(reactContext);
        this.mTransactionRepository = repository;
        this.mEventBus = eventBus;
        this.mNotificationRepository = notificationRepository;
        this.mResourceRepository = resourceRepository;
        this.mNavigator = navigator;

        getReactApplicationContext().addLifecycleEventListener(this);
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ZaloPayTransactionLogs";
    }

    @ReactMethod
    public void getTransactionsSuccess(final int pageIndex, final int count, final Promise promise) {
        WritableMap item = Arguments.createMap();
        item.putInt("code", ERR_CODE_SUCCESS.value());
        promise.resolve(item);
    }

    @ReactMethod
    public void successfulTransactionWithTransTypes(final ReadableArray transTypes,
                                                    final String timeStamp,
                                                    final int offset,
                                                    final int count,
                                                    final int sign,
                                                    final Promise promise) {
        try {
            List<Integer> types = transform(transTypes);
            long timestamp = Long.valueOf(timeStamp);
            Subscription subscription = mTransactionRepository.getTransactions(
                    timestamp, types, offset, count, sign)
                    .map(datas -> {
                        int code = datas.first;
                        List<TransHistory> transactions = datas.second;

                        if (code != ERR_CODE_OUT_OF_DATA && Lists.isEmptyOrNull(transactions) && offset == 0) {
                            boolean isLoad = mTransactionRepository.isLoadedTransactionSuccess();
                            if (!isLoad) {
                                code = ERR_CODE_TRANSACTION_NOT_LOADED.value();
                            }
                        }

                        return new TransactionResult(code, "", transactions);
                    })
                    .doOnError(Timber::d)
                    .onErrorResumeNext(new Func1<Throwable, Observable<TransactionResult>>() {
                        @Override
                        public Observable<TransactionResult> call(Throwable throwable) {
                            return resolveTransactionSuccess(timestamp, types, offset, count, sign, throwable);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(new TransactionLogSubscriber(promise));
            mCompositeSubscription.add(subscription);
        } catch (Exception e) {
            Timber.d("Fail to trying get successful transaction with trans types: " + e.getMessage());
        }
    }

    @ReactMethod
    public void failedTransactionWithTransTypes(final ReadableArray transTypes,
                                                final String timeStamp,
                                                final int offset,
                                                final int count,
                                                final int sign,
                                                final Promise promise) {
        try {
            List<Integer> types = transform(transTypes);
            long timestamp = Long.valueOf(timeStamp);
            Subscription subscription = mTransactionRepository.getTransactionsFail(
                    timestamp, types, offset, count, sign)
                    .map(datas -> {
                        int code = datas.first;
                        List<TransHistory> transactions = datas.second;

                        if (code != ERR_CODE_OUT_OF_DATA && Lists.isEmptyOrNull(transactions) && offset == 0) {
                            boolean isLoad = mTransactionRepository.isLoadedTransactionSuccess();
                            if (!isLoad) {
                                code = ERR_CODE_TRANSACTION_NOT_LOADED.value();
                            }
                        }

                        return new TransactionResult(code, "", transactions);
                    })
                    .doOnError(Timber::d)
                    .onErrorResumeNext(new Func1<Throwable, Observable<TransactionResult>>() {
                        @Override
                        public Observable<TransactionResult> call(Throwable throwable) {
                            return resolveTransactionFail(timestamp, types, offset, count, sign, throwable);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(new TransactionLogSubscriber(promise));
            mCompositeSubscription.add(subscription);
        } catch (Exception e) {
            Timber.d("Fail to trying get failed transaction with trans types: " + e.getMessage());
        }
    }

    @ReactMethod
    public void removeTransactionWithId(String id, Promise promise) {

        Timber.d("removeTransactionWithId %s", id);

        long value;
        try {
            value = Long.parseLong(id);
        } catch (NumberFormatException e) {
            Timber.i("Invalid format for number: %s", id);
            Helpers.promiseResolveError(promise, ERR_CODE_FAIL.value(), "Invalid input");
            return;
        }

        Subscription subscription = mTransactionRepository.removeTransaction(value)
                .map(result -> new TransactionResult(result ? ERR_CODE_SUCCESS.value() : ERR_CODE_FAIL.value(), "", Collections.emptyList()))
                .subscribeOn(Schedulers.io())
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

                .map(transactions -> {
                    if (transactions == null) {
                        return new TransactionResult(ERR_CODE_FAIL.value(), "", Collections.EMPTY_LIST);
                    }
                    return new TransactionResult(ERR_CODE_SUCCESS.value(), "", Collections.singletonList(transactions));
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new TransactionLogSubscriber(promise));

        mCompositeSubscription.add(subscription);
    }

    private class TransactionLogSubscriber extends DefaultSubscriber<TransactionResult> {

        WeakReference<Promise> mPromise;

        TransactionLogSubscriber(Promise promise) {
            mPromise = new WeakReference<>(promise);
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "error on getting transaction logs");
            Helpers.promiseResolveError(mPromise.get(), ERR_CODE_FAIL.value(), "get transaction error");
        }

        @Override
        public void onNext(TransactionResult result) {
            Timber.d(" transactions code [%s] message [%s] data size [%s]", result.code, result.message, result.data.size());
            if (result.code == ERR_CODE_TRANSACTION_NOT_LOADED.value()) {
                Helpers.promiseResolveError(mPromise.get(), result.code, "Transaction has not been loaded");
            } else {
                Helpers.promiseResolveSuccess(mPromise.get(), result.code, result.message, transform(result.data));
            }
        }
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
        //run on Main Thread
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        // updateTransactionLatest();
    }

    @Override
    public void onHostPause() {
        Timber.d("onPause");
        //run on Main Thread
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
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
        item.putString("userid", history.appuser);
        item.putString("thankmessage", history.thank_message);
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void onGetTransactionComplete(TransactionChangeEvent event) {
        Timber.d("send event zalopayTransactionsUpdated");
        WritableMap item = Arguments.createMap();
        item.putInt("statusType", event.typeSuccess);
        sendEvent("zalopayTransactionsUpdated", item);
        mEventBus.removeStickyEvent(event);
    }

    @ReactMethod
    public void reloadTransactionWithId(final String transactionId, String notificationId, final Promise promise) {
        Timber.d("reloadTransactionWithId: transactionId [%s] notificationId [%s]", transactionId, notificationId);
        final long notifyId;
        final long transId;
        try {
            notifyId = Long.valueOf(notificationId);
            transId = Long.valueOf(transactionId);
        } catch (NumberFormatException e) {
            Helpers.promiseResolveError(promise, -1, "Arguments invalid");
            return;
        }

        Subscription subscription = mNotificationRepository.getNotify(notifyId)
                .flatMap(new Func1<NotificationData, Observable<TransHistory>>() {
                    @Override
                    public Observable<TransHistory> call(NotificationData notify) {
                        if (notify.timestamp <= 0) {
                            return Observable.error(new ArgumentException());
                        } else {
                            return mTransactionRepository.reloadTransactionHistory(transId, notify.timestamp);
                        }
                    }
                })
                .map(transactions -> new TransactionResult(ERR_CODE_SUCCESS.value(), "", Collections.singletonList(transactions)))
                .subscribeOn(Schedulers.io())
                .subscribe(new TransactionLogSubscriber(promise));

        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void reloadTransaction(final String transactionId, String timestamp, final Promise promise) {
        Timber.d("reloadTransactionWithId: transactionId [%s] timestamp [%s]", transactionId, timestamp);
        final long transId;
        final long timeStamp;
        try {
            transId = Long.valueOf(transactionId);
            timeStamp = Long.valueOf(timestamp);
        } catch (NumberFormatException e) {
            Helpers.promiseResolveError(promise, -1, "Arguments invalid");
            return;
        }

        Subscription subscription = mTransactionRepository.reloadTransactionHistory(transId, timeStamp)
                .map(transactions -> new TransactionResult(ERR_CODE_SUCCESS.value(), "", Collections.singletonList(transactions)))
                .subscribeOn(Schedulers.io())
                .subscribe(new TransactionLogSubscriber(promise));

        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void showTransactionDetail(final int appid, final String transid, final Promise promise) {
        Timber.d("Show detail : appid [%s] transid [%s]", appid, transid);
        Subscription subscription = mResourceRepository.existResource(appid)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            Helpers.promiseResolveSuccess(promise, "");
                            startPaymentApp(appid, transid);
                        } else {
                            Helpers.promiseResolveError(promise, -1, "App disabled");
                        }
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    private void startPaymentApp(int appid, String transid) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("view", "history");
        options.put("transid", transid);

        Intent intent = mNavigator.intentPaymentApp(activity, new AppResource(appid), options);
        if (intent != null) {
            activity.startActivity(intent);
        }
    }

    private Observable<TransactionResult> resolveTransactionFail(
            long timestamp, List<Integer> transTypes, int offset, int count, int sign, final Throwable error) {
        return mTransactionRepository.getTransactionsFailLocal(timestamp, transTypes, offset, count, sign)
                .map(histories -> {
                    Pair<Integer, String> pairError = Helpers.createReactError(getReactApplicationContext(), error);
                    return new TransactionResult(pairError.first, pairError.second, histories);
                });
    }

    private Observable<TransactionResult> resolveTransactionSuccess(
            long timestamp, List<Integer> transTypes, int offset, int count, int sign, final Throwable error) {
        return mTransactionRepository.getTransactionsLocal(timestamp, transTypes, offset, count, sign)
                .map(histories -> {
                    Pair<Integer, String> pairError = Helpers.createReactError(getReactApplicationContext(), error);
                    return new TransactionResult(pairError.first, pairError.second, histories);
                });
    }

    private List<Integer> transform(ReadableArray types) {
        List<Integer> typeList = new ArrayList<>();
        if (types == null || types.size() <= 0) {
            return typeList;
        }
        for (int i = 0; i < types.size(); i++) {
            typeList.add(types.getInt(i));
        }
        return typeList;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void onUpdateTransactionDetail(TransactionDetailChangeEvent event) {
        Timber.d("send event zalopayTransactionDetailUpdated");
        WritableMap item = Arguments.createMap();
        item.putString("transid", String.valueOf(event.transid));
        sendEvent("zalopayTransactionDetailUpdated", item);
        mEventBus.removeStickyEvent(event);
    }

    @ReactMethod
    public void sendThankMessage(String transId, String message, String receiverId, String displayName, String zalopayid, String timestamp, Promise promise) {
        long transid, time;
        try {
            transid = Long.parseLong(transId);
            time = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            Timber.i("Invalid format for number: %s", transId);
            Helpers.promiseResolveError(promise, ERR_CODE_FAIL.value(), "Invalid input");
            return;
        }

        String embeddata = generateEmbeddata(transid, message, displayName, zalopayid, time);
        Subscription subscription = mNotificationRepository.sendNotification(receiverId, embeddata)
                .flatMap(aBoolean -> mTransactionRepository.updateThankMessage(transid, message))
                .doOnError(throwable -> {
                    Timber.d(throwable);
                    Helpers.promiseResolve(promise, -1);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        Helpers.promiseResolve(promise, aBoolean ? 1 : -1);
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    private String generateEmbeddata(long transId, String message, String displayName, String zalopayid, long timestamp) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", Constants.AppP2PNotificationType.SEND_THANK_MESSAGE);
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("timestamp", timestamp);
        jsonObject.addProperty("transid", transId);
        jsonObject.addProperty("displayname", displayName);
        jsonObject.addProperty("zalopayid", zalopayid);

        String embeddata = jsonObject.toString();
        Timber.d("Send notification: %s", embeddata);
        embeddata = Base64.encodeToString(embeddata.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);

        return embeddata;
    }

    @ReactMethod
    public void updateThankMessage(String transId, String message, Promise promise) {
        long transid;
        try {
            transid = Long.parseLong(transId);
        } catch (NumberFormatException e) {
            Timber.i("Invalid format for number: %s", transId);
            Helpers.promiseResolveError(promise, ERR_CODE_FAIL.value(), "Invalid input");
            return;
        }

        Subscription subscription = mTransactionRepository.updateThankMessage(transid, message)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        Helpers.promiseResolve(promise, aBoolean ? 1 : -1);
                    }
                });
        mCompositeSubscription.add(subscription);
    }
}
