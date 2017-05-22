package vn.com.vng.zalopay.data.transaction;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.entity.TransactionFragmentEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.eventbus.TransactionChangeEvent;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by huuhoa on 6/15/16.
 * Implementation for transaction repository
 */
public class TransactionRepository implements TransactionStore.Repository {

    private ZaloPayEntityDataMapper mDataMapper;
    private TransactionStore.LocalStorage mLocalStorage;
    private TransactionFragmentStore.LocalStorage mFragmentLocalStorage;
    private TransactionStore.RequestService mRequestService;
    private User mUser;
    private final EventBus mEventBus;

    private static final int TRANSACTION_STATUS_SUCCESS = 1;
    private static final int TRANSACTION_STATUS_FAIL = 2;

    private static final int TRANSACTION_ORDER_LATEST = 1;
    private static final int TRANSACTION_ORDER_OLDEST = -1;

    private static final int TRANSACTION_LENGTH = 20;

    private static final int RECURSE_TIME = 5;

    private static final int ERR_CODE_SUCCESS = 1;
    private static final int ERR_CODE_OUT_OF_DATA = 2;

    public TransactionRepository(
            ZaloPayEntityDataMapper dataMapper,
            User user,
            @NonNull TransactionStore.LocalStorage localStorage,
            @NonNull TransactionFragmentStore.LocalStorage fragmentLocalStorage,
            @NonNull TransactionStore.RequestService requestService,
            EventBus eventBus) {

        this.mDataMapper = dataMapper;
        mUser = user;
        mLocalStorage = localStorage;
        mFragmentLocalStorage = fragmentLocalStorage;
        mRequestService = requestService;
        mEventBus = eventBus;
    }

    @Override
    public Observable<Pair<Integer, List<TransHistory>>> getTransactions(long timestamp, List<Integer> transTypes, int offset, int count, int sign) {
        if (offset < 0 || count <= 0) {
            return Observable.just(new Pair<>(ERR_CODE_SUCCESS, Collections.emptyList()));
        }

        return fetchTransactionHistoryOldest(TRANSACTION_STATUS_SUCCESS, timestamp, transTypes, offset, count, sign)
                .flatMap(response -> getTransactionHistoryLocal(timestamp, transTypes, offset, count, TRANSACTION_STATUS_SUCCESS, sign))
                .map(entities -> new Pair<>(checkOutOfData(timestamp, TRANSACTION_STATUS_SUCCESS) && entities.size() < count ?
                        ERR_CODE_OUT_OF_DATA : ERR_CODE_SUCCESS, Lists.transform(entities, mDataMapper::transform)))
                .doOnError(Timber::d)
                .onErrorResumeNext(new Func1<Throwable, Observable<Pair<Integer, List<TransHistory>>>>() {
                    @Override
                    public Observable<Pair<Integer, List<TransHistory>>> call(Throwable throwable) {
                        return ObservableHelper.makeObservable(() ->
                                new Pair<Integer, List<TransHistory>>(checkOutOfData(timestamp, TRANSACTION_STATUS_SUCCESS) ? ERR_CODE_OUT_OF_DATA : ERR_CODE_SUCCESS, Collections.emptyList()));
                    }
                });
    }

    @Override
    public Observable<Pair<Integer, List<TransHistory>>> getTransactionsFail(long timestamp, List<Integer> transTypes, int offset, int count, int sign) {
        if (offset < 0 || count <= 0) {
            return Observable.just(new Pair<>(ERR_CODE_SUCCESS, Collections.emptyList()));
        }

        return fetchTransactionHistoryOldest(TRANSACTION_STATUS_FAIL, timestamp, transTypes, offset, count, sign)
                .flatMap(response -> getTransactionHistoryLocal(timestamp, transTypes, offset, count, TRANSACTION_STATUS_FAIL, sign))
                .map(entities -> new Pair<>(checkOutOfData(timestamp, TRANSACTION_STATUS_FAIL) && entities.size() < count ?
                        ERR_CODE_OUT_OF_DATA : ERR_CODE_SUCCESS, Lists.transform(entities, mDataMapper::transform)))
                .doOnError(Timber::d)
                .onErrorResumeNext(new Func1<Throwable, Observable<Pair<Integer, List<TransHistory>>>>() {
                    @Override
                    public Observable<Pair<Integer, List<TransHistory>>> call(Throwable throwable) {
                        return ObservableHelper.makeObservable(() ->
                                new Pair<Integer, List<TransHistory>>(checkOutOfData(timestamp, TRANSACTION_STATUS_FAIL) ? ERR_CODE_OUT_OF_DATA : ERR_CODE_SUCCESS, Collections.emptyList()));
                    }
                });
    }

    private Observable<List<TransHistoryEntity>> getTransactionHistoryLocal(long timestamp,
                                                                            List<Integer> transTypes,
                                                                            int offset,
                                                                            int count,
                                                                            int statusType,
                                                                            int sign) {
        Timber.d("get transaction local with timestamp [%s] page index [%s] count [%s] status type [%s] sign [%s]",
                timestamp, offset, count, statusType, sign);

        return getTimestampInFragment(timestamp, statusType)
                .filter(reqdate -> reqdate != null)
                .flatMap(reqdate -> ObservableHelper.makeObservable(() -> mLocalStorage.get(offset, count, statusType,
                        timestamp == 0 ? reqdate.maxreqdate : timestamp, reqdate.minreqdate, transTypes, sign)));
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryLatest(int statusType) {
        return getLatestTimeTransaction(statusType)
                .flatMap(timeStamp -> fetchTransactionHistoryRecurse(timeStamp, TRANSACTION_ORDER_LATEST, statusType, -1));
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryOldest(int statusType, long timestamp, List<Integer> transTypes, int offset, int count, int sign) {
        if (checkOutOfData(timestamp, statusType)) {
            return Observable.just(Collections.emptyList());
        }

        return getTimestampInFragment(timestamp, statusType)
                .flatMap(entity -> {
                    long timeStamp = (entity != null && (entity.minreqdate < timestamp || timestamp == 0)) ? entity.minreqdate : timestamp;
                    if (transTypes.size() == 0) {
                        return fetchTransactionHistory(timeStamp, TRANSACTION_ORDER_OLDEST, statusType, -1);
                    }
                    return fetchTransactionHistoryWithTransType(timeStamp, TRANSACTION_ORDER_OLDEST, statusType, -1, transTypes, offset, count, sign);
                });
    }

    private Observable<TransactionFragmentEntity> getTimestampInFragment(long timestamp, int statusType) {
        return ObservableHelper.makeObservable(() -> {
            if (timestamp <= 0) {
                return mFragmentLocalStorage.getLatestFragment(statusType);
            }
            List<TransactionFragmentEntity> entities = mFragmentLocalStorage.get(timestamp, statusType);
            if (Lists.isEmptyOrNull(entities)) {
                return null;
            }
            return entities.get(0);
        });
    }

    private boolean checkOutOfData(long timestamp, int statusType) {
        if (timestamp <= 0) {
            return mFragmentLocalStorage.getLatestFragment(statusType).outofdata;
        }
        return mFragmentLocalStorage.isOutOfData(timestamp, statusType);
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
        Observable<TransHistoryEntity> observable = ObservableHelper.makeObservable(() -> mLocalStorage.getTransaction(id))
                .filter(entity -> entity != null);
        Observable<TransHistoryEntity> observableBackup = ObservableHelper.makeObservable(() -> mLocalStorage.getBackup(id));
        return Observable.concat(observable, observableBackup)
                .first()
                .map(mDataMapper::transform);
    }

    @Override
    public Observable<Boolean> updateTransactionStatusSuccess(final long transId) {
        return ObservableHelper.makeObservable(() -> {
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

    private Observable<List<TransHistoryEntity>> fetchTransactionHistory(long timestamp, int sortOrder, int statusType, long thresholdTime) {
        return Observable.create(subscriber -> fetchTransactionHistoryRecurse(null, 0, 0, 0,
                timestamp, sortOrder, statusType, thresholdTime, subscriber, 0, false));
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryRecurse(long timestamp, int sortOrder, int statusType, long thresholdTime) {
        return Observable.create(subscriber -> fetchTransactionHistoryRecurse(null, 0, 0, 0,
                timestamp, sortOrder, statusType, thresholdTime, subscriber, 0, true));
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryWithTransType(long timestamp, int sortOrder, int statusType, long thresholdTime, List<Integer> transTypes, int offset, int count, int sign) {
        return Observable.create(subscriber -> fetchTransactionHistoryRecurse(transTypes, offset, count, sign,
                timestamp, sortOrder, statusType, thresholdTime, subscriber, 0, true));
    }

    private void updateTransactionFragment(long timestamp, int statusType, long maxreqdate, long minreqdate, int sortOrder) {
        Timber.d("put transaction fragment maxreqdate [%s] minreqdate [%s] statustype [%s]", maxreqdate, minreqdate, statusType);

        TransactionFragmentEntity entity = null;
        if (timestamp <= 0) {
            entity = mFragmentLocalStorage.getLatestFragment(statusType);
        } else {
            List<TransactionFragmentEntity> entities = mFragmentLocalStorage.get(timestamp, statusType);
            if (!Lists.isEmptyOrNull(entities)) {
                entity = entities.get(0);
            }
        }

        if (entity == null) {
            mFragmentLocalStorage.put(new TransactionFragmentEntity(statusType, maxreqdate, minreqdate, false));
        } else {
            mFragmentLocalStorage.remove(entity.minreqdate);

            if (sortOrder == TRANSACTION_ORDER_LATEST) {
                entity.maxreqdate = maxreqdate;
            }

            if (sortOrder == TRANSACTION_ORDER_OLDEST) {
                entity.minreqdate = minreqdate;
                List<TransactionFragmentEntity> fragments = mFragmentLocalStorage.get(minreqdate, statusType);
                for (int i = 0; i < fragments.size(); i++) {
                    TransactionFragmentEntity fragmentEntity = fragments.get(i);
                    if (entity.minreqdate > fragmentEntity.minreqdate) {
                        entity.minreqdate = fragmentEntity.minreqdate;
                        entity.outofdata = fragmentEntity.outofdata;
                    }
                    mFragmentLocalStorage.remove(fragmentEntity.minreqdate);
                }
            }

            mFragmentLocalStorage.put(entity);
        }
    }

    private void fetchTransactionHistoryRecurse(List<Integer> transTypes, int offset, int count, int sign,
                                                long timestamp, int sortOrder,
                                                int statusType, long thresholdTime,
                                                Subscriber<? super List<TransHistoryEntity>> subscriber,
                                                final int deep, boolean recurse) {
        Timber.d("fetch transaction timestamp [%s] sortOrder [%s] statusType [%s] thresholdTime [%s] deep [%s]", timestamp, sortOrder, statusType, thresholdTime, deep);
        Subscription subscription = fetchTransactionHistoryCloud(timestamp, sortOrder, statusType)
                .doOnNext(data -> {
                    boolean recurseWithTranstype = false;
                    if (data.size() != 0) {
                        updateTransactionFragment(timestamp, statusType, timestamp > data.get(0).reqdate ? timestamp : data.get(0).reqdate, data.get(data.size() - 1).reqdate, sortOrder);
                        recurseWithTranstype = (sortOrder == TRANSACTION_ORDER_OLDEST) && !Lists.isEmptyOrNull(transTypes);
                        if (recurseWithTranstype) {
                            List<TransactionFragmentEntity> reqdate = mFragmentLocalStorage.get(timestamp, statusType);
                            data = mLocalStorage.get(offset, count, statusType, reqdate.get(0).maxreqdate, reqdate.get(0).minreqdate, transTypes, sign);
                        }
                    }

                    long nextTimestamp = getNextTimestamp(sortOrder, statusType, timestamp);
                    boolean isRecurse = shouldRecurse(data, statusType, timestamp, nextTimestamp, sortOrder, thresholdTime, recurseWithTranstype) && recurse;

                    Timber.d("fetch transaction is recurse [%s] nextTimeStamp [%s] ", isRecurse, nextTimestamp);

                    if (subscriber.isUnsubscribed()) {
                        Timber.i("fetch transaction is UnSubscribed");
                        return;
                    }

                    int _deep = deep + 1;
                    if (isRecurse && (_deep < RECURSE_TIME && sortOrder == TRANSACTION_ORDER_OLDEST)) {
                        fetchTransactionHistoryRecurse(transTypes, offset, count, sign,
                                nextTimestamp, sortOrder, statusType, thresholdTime, subscriber, _deep, recurse);
                    } else {
                        Timber.d("fetch transaction Completed");
                        subscriber.onCompleted();
                    }
                })
                .subscribe(subscriber::onNext, subscriber::onError);
    }

    private long getNextTimestamp(int sortOrder, int statusType, long timestamp) {
        long ret = -1;
        if (sortOrder == TRANSACTION_ORDER_LATEST) {
            ret = mFragmentLocalStorage.getLatestFragment(statusType).maxreqdate;
        } else if (sortOrder == TRANSACTION_ORDER_OLDEST) {
            List<TransactionFragmentEntity> entities = mFragmentLocalStorage.get(timestamp, statusType);
            if (entities.size() != 0) {
                ret = entities.get(0).minreqdate;
            }
        }

        return ret;
    }


    private boolean shouldRecurse(List<TransHistoryEntity> data, int statusType, long timestamp, long nextTimestamp, int sortOrder, long thresholdTime, boolean recurseWithTranstype) {
        if (timestamp <= 0 && thresholdTime <= 0) { // lần đầu k req hết.
            return false;
        }

        if (sortOrder == TRANSACTION_ORDER_LATEST) {
            if (nextTimestamp <= timestamp && timestamp > 0) {
                //   Timber.d("nextTimestamp lt timestamp [%s] lt [%s]", nextTimestamp, timestamp);
                return false;
            }
        } else if (sortOrder == TRANSACTION_ORDER_OLDEST) {
            if (nextTimestamp >= timestamp && timestamp > 0) {
                updateOutOfData(timestamp, statusType, true);
                // Timber.d("nextTimestamp gt timestamp [%s] gt [%s]", nextTimestamp, timestamp);
                return false;
            }
        }

        if (recurseWithTranstype) {
            if (data.size() < TRANSACTION_LENGTH) {
                return true;
            }
            return false;
        }

        if (Lists.isEmptyOrNull(data) || data.size() < TRANSACTION_LENGTH) { // hết data để lấy else if (sortOrder == TRANSACTION_ORDER_OLDEST) {
            updateOutOfData(timestamp, statusType, true);
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

    private void updateOutOfData(long timestamp, int statusType, boolean isOutOfData) {
        if (!mFragmentLocalStorage.isHasData(timestamp, statusType)) {
            mFragmentLocalStorage.put(new TransactionFragmentEntity(statusType, timestamp, timestamp, isOutOfData));
        } else {
            mFragmentLocalStorage.updateOutOfData(timestamp, statusType, isOutOfData);
        }
    }

    private Observable<List<TransHistoryEntity>> fetchTransactionHistoryCloud(long timestamp, int sortOrder, int statusType) {
        return mRequestService.getTransactionHistories(mUser.zaloPayId, mUser.accesstoken, timestamp, TRANSACTION_LENGTH, sortOrder, statusType)
                .map(response -> response.data)
                .doOnNext(data -> {
                    this.writeTransactionEntity(data, statusType);
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

    private void writeTransactionEntity(List<TransHistoryEntity> data, int statusType) {
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
        return ObservableHelper.makeObservable(() -> mLocalStorage.getLatestTimeTransaction(statusType));
    }

    private Observable<Long> getOldestTimeTransaction(int statusType) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.getOldestTimeTransaction(statusType));
    }

    private Observable<Boolean> fetchTransactionHistoryOldestSuccess(long thresholdTime) {
        Observable<Long> observableOldestSuccess = getOldestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        return observableOldestSuccess.filter(oldestTime -> thresholdTime > 0 && (oldestTime == 0 || oldestTime > thresholdTime))
                .flatMap(oldestTime -> fetchTransactionHistoryRecurse(oldestTime, TRANSACTION_ORDER_OLDEST, TRANSACTION_STATUS_SUCCESS, thresholdTime))
                .map(entities -> Boolean.TRUE);
    }

    private Observable<Boolean> fetchTransactionHistoryOldestFail(long thresholdTime) {
        Observable<Long> observableOldestFail = getOldestTimeTransaction(TRANSACTION_STATUS_FAIL);
        return observableOldestFail.filter(oldestTime -> thresholdTime > 0 && (oldestTime == 0 || oldestTime > thresholdTime))
                .flatMap(oldestTime -> fetchTransactionHistoryRecurse(oldestTime, TRANSACTION_ORDER_OLDEST, TRANSACTION_STATUS_FAIL, thresholdTime))
                .map(entities -> Boolean.TRUE);
    }

    private Observable<Boolean> fetchTransactionHistoryLatestSuccess(long thresholdTime) {
        Observable<Long> latestTimeTransaction = getLatestTimeTransaction(TRANSACTION_STATUS_SUCCESS);
        return latestTimeTransaction.filter(latestTime -> thresholdTime > latestTime)
                .flatMap(latestTime -> fetchTransactionHistoryRecurse(latestTime, TRANSACTION_ORDER_LATEST, TRANSACTION_STATUS_SUCCESS, thresholdTime))
                .map(entities -> Boolean.TRUE);
    }

    private Observable<Boolean> fetchTransactionHistoryLatestFail(long thresholdTime) {
        Observable<Long> latestTimeTransaction = getLatestTimeTransaction(TRANSACTION_STATUS_FAIL);
        return latestTimeTransaction.filter(latestTime -> thresholdTime > latestTime)
                .flatMap(latestTime -> fetchTransactionHistoryRecurse(latestTime, TRANSACTION_ORDER_LATEST, TRANSACTION_STATUS_FAIL, thresholdTime))
                .map(entities -> Boolean.TRUE);
    }

    @Override
    public Observable<List<TransHistory>> getTransactionsLocal(
            long timestamp, List<Integer> transTypes, int offset, int count, int sign) {
        return getTransactionHistoryLocal(timestamp, transTypes, offset, count, TRANSACTION_STATUS_SUCCESS, sign)
                .map(entities -> Lists.transform(entities, mDataMapper::transform));
    }

    @Override
    public Observable<List<TransHistory>> getTransactionsFailLocal(
            long timestamp, List<Integer> transTypes, int offset, int count, int sign) {
        return getTransactionHistoryLocal(timestamp, transTypes, offset, count, TRANSACTION_STATUS_FAIL, sign)
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
