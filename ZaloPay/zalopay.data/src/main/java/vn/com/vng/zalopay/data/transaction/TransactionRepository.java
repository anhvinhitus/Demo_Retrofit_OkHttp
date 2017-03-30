package vn.com.vng.zalopay.data.transaction;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.TimeUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.eventbus.TransactionChangeEvent;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.model.User;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by huuhoa on 6/15/16.
 * Implementation for transaction repository
 */
public class TransactionRepository implements TransactionStore.Repository {

    private ZaloPayEntityDataMapper mDataMapper;
    private TransactionStore.LocalStorage mLocalStorage;
    private TransactionStore.RequestService mRequestService;
    private User mUser;
    private final EventBus mEventBus;

    private static final int TRANSACTION_STATUS_SUCCESS = 1;
    private static final int TRANSACTION_STATUS_FAIL = 2;

    private static final int TRANSACTION_ORDER_LATEST = 1;
    private static final int TRANSACTION_ORDER_OLDEST = -1;

    private static final int TRANSACTION_LENGTH = 20;

    public TransactionRepository(
            ZaloPayEntityDataMapper dataMapper,
            User user,
            @NonNull TransactionStore.LocalStorage localStorage,
            @NonNull TransactionStore.RequestService requestService,
            EventBus eventBus) {

        this.mDataMapper = dataMapper;
        mUser = user;
        mLocalStorage = localStorage;
        mRequestService = requestService;
        mEventBus = eventBus;
    }

    /**
     * pageIndex bắt đầu từ 0
     */
    @Override
    public Observable<List<TransHistory>> getTransactions(int pageIndex, int count) {

        if (pageIndex < 0 || count <= 0) {
            return Observable.just(Collections.emptyList());
        }

        Observable<List<TransHistoryEntity>> _observableTransLocal = getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_SUCCESS)
                .filter(entities -> entities != null && entities.size() >= count);

        Observable<List<TransHistoryEntity>> _observableTransCloud = fetchTransactionHistoryOldest(TRANSACTION_STATUS_SUCCESS)
                .flatMap(response -> getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_SUCCESS));

        return Observable.concat(_observableTransLocal, _observableTransCloud)
                .first()
                .map(entities -> Lists.transform(entities, mDataMapper::transform));
    }

    @Override
    public Observable<List<TransHistory>> getTransactionsFail(int pageIndex, int count) {
        if (pageIndex < 0 || count <= 0) {
            return Observable.just(Collections.emptyList());
        }

        Observable<List<TransHistoryEntity>> _observableTransLocal = getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_FAIL)
                .filter(entities -> entities != null && entities.size() >= count);

        Observable<List<TransHistoryEntity>> _observableTransCloud = fetchTransactionHistoryOldest(TRANSACTION_STATUS_FAIL)
                .flatMap(response -> getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_FAIL));

        return Observable.concat(_observableTransLocal, _observableTransCloud)
                .first()
                .map(entities -> Lists.transform(entities, mDataMapper::transform));
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryOldest(int statusType) {
        return getOldestTimeTransaction(statusType)
                .flatMap(timeStamp -> fetchTransactionHistory(timeStamp, TRANSACTION_ORDER_OLDEST, statusType));
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryLatest(int statusType) {
        return getLatestTimeTransaction(statusType)
                .flatMap(timeStamp -> fetchTransactionHistoryRecurse(timeStamp, TRANSACTION_ORDER_LATEST, statusType));
    }

    @Override
    public Observable<Boolean> fetchTransactionHistoryLatest() {

        Timber.d("Begin fetch transaction latest");

        Observable<Boolean> _ObservableSuccessLatest = fetchTransactionHistorySuccessLatest().doOnError(throwable -> Observable.empty());
        Observable<Boolean> _ObservableFailLatest = fetchTransactionHistoryFailLatest().doOnError(throwable -> Observable.empty());
        return Observable.zip(_ObservableSuccessLatest, _ObservableFailLatest,
                (aBoolean, aBoolean2) -> aBoolean && aBoolean2);
    }

    @Override
    public Observable<Boolean> fetchTransactionHistorySuccessLatest() {
        return fetchTransactionHistoryLatest(TRANSACTION_STATUS_SUCCESS)
                .map(response -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> fetchTransactionHistoryFailLatest() {
        return fetchTransactionHistoryLatest(TRANSACTION_STATUS_FAIL)
                .map(response -> Boolean.TRUE);
    }

    @Override
    public Observable<TransHistory> getTransaction(long id) {
        Observable<TransHistoryEntity> observable = makeObservable(() -> mLocalStorage.getTransaction(id))
                .filter(entity -> entity != null);
        Observable<TransHistoryEntity> observableBackup = makeObservable(() -> mLocalStorage.getBackup(id));
        return Observable.concat(observable, observableBackup)
                .first()
                .map(mDataMapper::transform);
    }

    @Override
    public Observable<Boolean> updateTransactionStatusSuccess(final long transId) {
        return makeObservable(() -> {
            mLocalStorage.updateStatusType(transId, TRANSACTION_STATUS_SUCCESS);
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean isLoadedTransactionSuccess() {
        return mLocalStorage.isLoadedTransactionSuccess();
    }

    @Override
    public Boolean isLoadedTransactionFail() {
        return mLocalStorage.isLoadedTransactionFail();
    }

    private Observable<List<TransHistoryEntity>> getTransactionHistoryLocal(int pageIndex, int count, int statusType) {
        return makeObservable(() -> mLocalStorage.get(pageIndex, count, statusType));
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistory(long timestamp, int sortOrder, int statusType) {
        return fetchTransactionHistoryRecurse(timestamp, sortOrder, statusType, -1, false);
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryRecurse(long timestamp, int sortOrder, int statusType) {
        return fetchTransactionHistoryRecurse(timestamp, sortOrder, statusType, -1, true);
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryRecurse(long timestamp, int sortOrder, int statusType, long thresholdTime, boolean recurse) {
        return Observable.create(subscriber -> fetchTransactionHistoryRecurse(timestamp, sortOrder, statusType, thresholdTime, subscriber, 0, recurse));
    }

    private void fetchTransactionHistoryRecurse(long timestamp, int sortOrder,
                                                int statusType, long thresholdTime,
                                                Subscriber<? super List<TransHistoryEntity>> subscriber,
                                                final int deep, boolean recurse) {
        Timber.d("fetch transaction timestamp [%s] sortOrder [%s] statusType [%s] thresholdTime [%s] deep [%s]", timestamp, sortOrder, statusType, thresholdTime, deep);
        Subscription subscription = fetchTransactionHistoryCloud(timestamp, sortOrder, statusType)
                .doOnNext(data -> {
                    long nextTimestamp = getNextTimestamp(sortOrder, statusType);
                    boolean isRecurse = shouldRecurse(data, timestamp, nextTimestamp, sortOrder, statusType, thresholdTime) && recurse;

                    Timber.d("fetch transaction is recurse [%s] nextTimeStamp [%s] ", isRecurse, nextTimestamp);

                    if (subscriber.isUnsubscribed()) {
                        Timber.i("fetch transaction is UnSubscribed");
                        return;
                    }

                    if (isRecurse) {
                        int _deep = deep + 1;
                        Timber.d("fetch transaction next recurse deep [%s]", _deep);
                        fetchTransactionHistoryRecurse(nextTimestamp, sortOrder, statusType, thresholdTime, subscriber, _deep, recurse);
                    } else {
                        Timber.d("fetch transaction Completed");
                        subscriber.onCompleted();
                    }
                })
                .subscribe(subscriber::onNext, subscriber::onError);
    }

    private long getNextTimestamp(int sortOrder, int statusType) {
        long ret = -1;
        if (sortOrder == TRANSACTION_ORDER_LATEST) {
            ret = mLocalStorage.getLatestTimeTransaction(statusType);
        } else if (sortOrder == TRANSACTION_ORDER_OLDEST) {
            ret = mLocalStorage.getOldestTimeTransaction(statusType);
        }

        return ret;
    }

    private boolean shouldRecurse(List<TransHistoryEntity> data, long timestamp, long nextTimestamp, int sortOrder, int statusType, long thresholdTime) {
        if (timestamp == 0 && thresholdTime == 0) { // lần đầu k req hết.
            return false;
        }

        if (sortOrder == TRANSACTION_ORDER_LATEST) {
            if (nextTimestamp <= timestamp && timestamp > 0) {
                //   Timber.d("nextTimestamp lt timestamp [%s] lt [%s]", nextTimestamp, timestamp);
                return false;
            }
        } else if (sortOrder == TRANSACTION_ORDER_OLDEST) {
            if (nextTimestamp >= timestamp && timestamp > 0) {
                // Timber.d("nextTimestamp gt timestamp [%s] gt [%s]", nextTimestamp, timestamp);
                return false;
            }
        }

        if (Lists.isEmptyOrNull(data) || data.size() < TRANSACTION_LENGTH) { // hết data để lấy
            return false;
        }

        if (thresholdTime > 0) { // đệ quy đến 1 giới hạn thời gian nào đó.
            if (sortOrder == TRANSACTION_ORDER_LATEST) {
                if (nextTimestamp >= thresholdTime) {
                    //Timber.d("nextTimestamp [%s] gt thresholdTime[%s]", nextTimestamp, thresholdTime);
                    return false;
                }
            } else if (sortOrder == TRANSACTION_ORDER_OLDEST) {
                if (nextTimestamp <= thresholdTime) {
                    //  Timber.d("nextTimestamp [%s] lt thresholdTime[%s]", nextTimestamp, thresholdTime);
                    return false;
                }
            }
        }

        return true;
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryCloud(long timestamp, int sortOrder, int statusType) {
        return mRequestService.getTransactionHistories(mUser.zaloPayId, mUser.accesstoken, timestamp, TRANSACTION_LENGTH, sortOrder, statusType)
                .map(response -> response.data)
                .doOnNext(data -> {
                    this.writeTransactionEntity(data, sortOrder, statusType);
                    int size = data.size();
                    if (size < TRANSACTION_LENGTH) {
                        boolean hasData = (size == 0 && timestamp == 0) // Update Ui cho lần đâu tiên.
                                || size > 0;
                        this.onLoadedTransactionComplete(statusType, hasData, sortOrder);
                    }
                });
    }

    private void onLoadedTransactionComplete(int statusType, boolean hasData, int sortOrder) {

        boolean typeSuccess = statusType == TRANSACTION_STATUS_SUCCESS;
        if (typeSuccess) {
            mLocalStorage.setLoadedTransactionSuccess(true);
        } else {
            mLocalStorage.setLoadedTransactionFail(true);
        }

        if (hasData && sortOrder == TRANSACTION_ORDER_LATEST) {
            mEventBus.post(new TransactionChangeEvent(statusType));
        }
    }

    private void writeTransactionEntity(List<TransHistoryEntity> data, int sortOder, int statusType) {
        int size = data.size();
        if (size > 0) {
            for (TransHistoryEntity transHistoryEntity : data) {
                transHistoryEntity.statustype = statusType;
            }
            mLocalStorage.put(data);
        }
    }

    @Override
    public Observable<Boolean> fetchTransactionHistoryOldest(long thresholdTime) {
        Observable<Boolean> observableOldestSuccess = fetchTransactionHistoryOldestSuccess(thresholdTime).doOnError(throwable -> Observable.empty());
        Observable<Boolean> observableOldestFail = fetchTransactionHistoryOldestFail(thresholdTime).doOnError(throwable -> Observable.empty());

        return Observable.zip(observableOldestSuccess, observableOldestFail,
                (result1, result2) -> result1 && result2);
    }

    @Override
    public Observable<Boolean> fetchTransactionHistoryLatest(long threshold) {
        Observable<Boolean> observableLatestSuccess = fetchTransactionHistoryLatestSuccess(threshold).doOnError(throwable -> Observable.empty());
        Observable<Boolean> observableLatestFail = fetchTransactionHistoryLatestFail(threshold).doOnError(throwable -> Observable.empty());
        return Observable.zip(observableLatestSuccess, observableLatestFail,
                (result1, result2) -> result1 && result2);
    }

    private Observable<Long> getLatestTimeTransaction(int statusType) {
        return makeObservable(() -> mLocalStorage.getLatestTimeTransaction(statusType));
    }

    private Observable<Long> getOldestTimeTransaction(int statusType) {
        return makeObservable(() -> mLocalStorage.getOldestTimeTransaction(statusType));
    }

    private Observable<Boolean> fetchTransactionHistoryOldestSuccess(long thresholdTime) {
        Observable<Long> observableOldestSuccess = getOldestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        return observableOldestSuccess.filter(oldestTime -> thresholdTime > 0 && (oldestTime == 0 || oldestTime > thresholdTime))
                .flatMap(oldestTime -> fetchTransactionHistoryRecurse(oldestTime, TRANSACTION_ORDER_OLDEST, TRANSACTION_STATUS_SUCCESS, thresholdTime, true))
                .map(entities -> Boolean.TRUE);
    }

    private Observable<Boolean> fetchTransactionHistoryOldestFail(long thresholdTime) {
        Observable<Long> observableOldestFail = getOldestTimeTransaction(TRANSACTION_STATUS_FAIL);
        return observableOldestFail.filter(oldestTime -> thresholdTime > 0 && (oldestTime == 0 || oldestTime > thresholdTime))
                .flatMap(oldestTime -> fetchTransactionHistoryRecurse(oldestTime, TRANSACTION_ORDER_OLDEST, TRANSACTION_STATUS_FAIL, thresholdTime, true))
                .map(entities -> Boolean.TRUE);
    }

    private Observable<Boolean> fetchTransactionHistoryLatestSuccess(long thresholdTime) {
        Observable<Long> latestTimeTransaction = getLatestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        return latestTimeTransaction.filter(latestTime -> thresholdTime > latestTime)
                .flatMap(latestTime -> fetchTransactionHistoryRecurse(latestTime, TRANSACTION_ORDER_LATEST, TRANSACTION_STATUS_SUCCESS, thresholdTime, true))
                .map(entities -> Boolean.TRUE);
    }

    private Observable<Boolean> fetchTransactionHistoryLatestFail(long thresholdTime) {
        Observable<Long> latestTimeTransaction = getLatestTimeTransaction(TRANSACTION_STATUS_FAIL);
        return latestTimeTransaction.filter(latestTime -> thresholdTime > latestTime)
                .flatMap(latestTime -> fetchTransactionHistoryRecurse(latestTime, TRANSACTION_ORDER_LATEST, TRANSACTION_STATUS_FAIL, thresholdTime, true))
                .map(entities -> Boolean.TRUE);
    }

    @Override
    public Observable<List<TransHistory>> getTransactionsLocal(int pageIndex, int count) {
        return getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_SUCCESS)
                .map(entities -> Lists.transform(entities, mDataMapper::transform));
    }

    @Override
    public Observable<List<TransHistory>> getTransactionsFailLocal(int pageIndex, int count) {
        return getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_FAIL)
                .map(entities -> Lists.transform(entities, mDataMapper::transform));
    }

    /**
     * time là millis
     */
    @Override
    public Observable<TransHistory> reloadTransactionHistory(long transId, long time) {
        long timestamp = time + 5000;
        Timber.d("Reload transaction history: transactionId [%s] timeStamp [%s]", transId, timestamp);
        Observable<List<TransHistoryEntity>> observableSuccess = fetchTransactionToBackup(timestamp, TRANSACTION_LENGTH, TRANSACTION_STATUS_SUCCESS)
                .onErrorResumeNext(throwable -> Observable.just(Collections.emptyList()));
        Observable<List<TransHistoryEntity>> observableFail = fetchTransactionToBackup(timestamp, TRANSACTION_LENGTH, TRANSACTION_STATUS_FAIL)
                .onErrorResumeNext(throwable -> Observable.just(Collections.emptyList()));

        return Observable
                .zip(observableSuccess, observableFail, (entitiesSuccess, entitiesFail) -> {
                    TransHistoryEntity entity = findTransaction(entitiesSuccess, transId);
                    Timber.d("find transaction in list success %s", entity);
                    if (entity != null) {
                        return entity;
                    }
                    return findTransaction(entitiesFail, transId);
                })
                .map(entity -> {
                    if (entity != null) {
                        mLocalStorage.putBackup(entity);
                        return mDataMapper.transform(entity);
                    }
                    return null;
                });

    }


    @Nullable
    private TransHistoryEntity findTransaction(List<TransHistoryEntity> entities, long transId) {
        if (Lists.isEmptyOrNull(entities)) {
            return null;
        }
        for (TransHistoryEntity entity : entities) {
            if (entity == null) {
                continue;
            }

            if (entity.transid == transId) {
                return entity;
            }
        }
        return null;
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionToBackup(long timestamp, int length, int statusType) {
        return mRequestService.getTransactionHistories(mUser.zaloPayId, mUser.accesstoken, timestamp, length,
                TRANSACTION_ORDER_OLDEST, statusType)
                .map(response -> response.data);
    }
}
