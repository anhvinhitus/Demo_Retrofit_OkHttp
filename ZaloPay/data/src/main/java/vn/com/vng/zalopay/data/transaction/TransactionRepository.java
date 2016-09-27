package vn.com.vng.zalopay.data.transaction;

import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.eventbus.TransactionChangeEvent;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.model.User;

import static vn.com.vng.zalopay.data.util.ObservableHelper.makeObservable;

/**
 * Created by huuhoa on 6/15/16.
 * Implementation for transaction repository
 */
public class TransactionRepository implements TransactionStore.Repository {
    private ZaloPayEntityDataMapper zaloPayEntityDataMapper;
    private TransactionStore.LocalStorage mTransactionLocalStorage;
    private TransactionStore.RequestService mTransactionRequestService;
    private User mUser;
    private final SqlZaloPayScope mSqlZaloPayScope;
    private final EventBus mEventBus;
    private final RxBus mRxBus;

    private static final int TRANSACTION_STATUS_SUCCESS = 1;
    private static final int TRANSACTION_STATUS_FAIL = 2;

    private static final int TRANSACTION_ORDER_LATEST = 1;
    private static final int TRANSACTION_ORDER_OLDEST = -1;

    private static final int TRANSACTION_LENGTH = 20;

    public TransactionRepository(
            ZaloPayEntityDataMapper zaloPayEntityDataMapper,
            User user,
            SqlZaloPayScope sqlZaloPayScope,
            TransactionStore.LocalStorage transactionLocalStorage,
            TransactionStore.RequestService transactionRequestService,
            EventBus eventBus, RxBus rxBus) {

        this.zaloPayEntityDataMapper = zaloPayEntityDataMapper;
        mUser = user;
        mSqlZaloPayScope = sqlZaloPayScope;
        mTransactionLocalStorage = transactionLocalStorage;
        mTransactionRequestService = transactionRequestService;
        mEventBus = eventBus;
        this.mRxBus = rxBus;
        subscribeFetchTransactionLatest();
    }

    private void subscribeFetchTransactionLatest() {
        mRxBus.toObserverable().subscribe(new DefaultSubscriber<Object>() {
            @Override
            public void onNext(Object o) {
                if (o instanceof RecursiveData) {
                    Timber.d("recursive fetch transaction %s", ((RecursiveData) o).statusType);
                    fetchTransactionHistoryLatest(((RecursiveData) o).statusType);
                }
            }
        });
    }

    @Override
    public Observable<List<TransHistory>> getTransactions(int pageIndex, int count) {

        Observable<List<TransHistoryEntity>> _observableTransLocal = getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_SUCCESS)
                .filter(entities -> entities != null && entities.size() >= TRANSACTION_LENGTH);

        Observable<List<TransHistoryEntity>> _observableTransCloud = fetchTransactionHistoryOldest(TRANSACTION_STATUS_SUCCESS)
                .flatMap(response -> getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_SUCCESS));

        return Observable.concat(_observableTransLocal, _observableTransCloud)
                .first()
                .map(entities -> zaloPayEntityDataMapper.transform(entities));
    }

    @Override
    public Observable<List<TransHistory>> getTransactionsFail(int pageIndex, int count) {
        Observable<List<TransHistoryEntity>> _observableTransLocal = getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_FAIL)
                .filter(entities -> entities != null && entities.size() >= TRANSACTION_LENGTH);

        Observable<List<TransHistoryEntity>> _observableTransCloud = fetchTransactionHistoryOldest(TRANSACTION_STATUS_FAIL)
                .flatMap(response -> getTransactionHistoryLocal(pageIndex, count, TRANSACTION_STATUS_FAIL));

        return Observable.concat(_observableTransLocal, _observableTransCloud)
                .first()
                .map(entities -> zaloPayEntityDataMapper.transform(entities));
    }

    private Observable<TransactionHistoryResponse> fetchTransactionHistoryOldest(int statusType) {
        return makeObservable(() -> mTransactionLocalStorage.getOldestTimeTransaction(statusType))
                .flatMap(timeStamp -> fetchTransactionHistory(timeStamp, TRANSACTION_ORDER_OLDEST, statusType));
    }

    private Observable<TransactionHistoryResponse> fetchTransactionHistoryLatest(int statusType) {
        return makeObservable(() -> mTransactionLocalStorage.getLatestTimeTransaction(statusType))
                .flatMap(timeStamp -> fetchTransactionHistory(timeStamp, TRANSACTION_ORDER_LATEST, statusType));
    }

    private Observable<List<TransHistoryEntity>> getTransactionHistoryLocal(int pageIndex, int count, int statusType) {
        return makeObservable(() -> mTransactionLocalStorage.get(pageIndex, count, statusType));
    }

    private Observable<TransactionHistoryResponse> fetchTransactionHistory(long timestamp, int sortOrder, int statusType) {
        return mTransactionRequestService.getTransactionHistories(mUser.zaloPayId, mUser.accesstoken, timestamp, TRANSACTION_LENGTH, sortOrder, statusType)
                .doOnNext(response -> {
                    //write data
                    this.writeTransactionEntity(response.data, sortOrder, statusType, 0);
                    int size = response.data.size();
                    if (size < TRANSACTION_LENGTH) {
                        boolean hasData = (size == 0 && timestamp == 0) // Update Ui cho lần đâu tiên.
                                || size > 0;
                        this.onLoadedTransactionComplete(statusType, hasData);
                    }
                })
                .doOnNext(response -> shouldContinueFetchTransaction(response, timestamp, sortOrder, statusType))
                ;
    }

    private void shouldContinueFetchTransaction(TransactionHistoryResponse response, long timestamp, int sortOrder, int statusType) {
        if (timestamp == 0) { // lần đầu k req hết.
            return;
        }
        if (sortOrder == TRANSACTION_ORDER_OLDEST) { // k đệ quy với type oldest
            return;
        }
        if (Lists.isEmptyOrNull(response.data) || response.data.size() < TRANSACTION_LENGTH) { // hết data để lấy
            return;
        }

        if (mRxBus.hasObservers()) {
            mRxBus.send(new RecursiveData(statusType));
        }
    }

    private Observable<Boolean> updateTransactionHistory() {
        return Observable.merge(fetchTransactionHistoryLatest(TRANSACTION_STATUS_SUCCESS).onErrorResumeNext(throwable -> Observable.empty()),
                fetchTransactionHistoryLatest(TRANSACTION_STATUS_FAIL).onErrorResumeNext(throwable -> Observable.empty()))
                .map(response -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> updateTransaction() {
        return ObservableHelper.makeObservable(() -> {
            //update transaction
            reloadAllListTransactionSync(TRANSACTION_LENGTH);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> initialize() {
        return ObservableHelper.makeObservable(() -> {
            reloadAllListTransactionSync(TRANSACTION_LENGTH);
            return Boolean.TRUE;
        }).delaySubscription(10, TimeUnit.SECONDS);
    }

    private void reloadAllListTransactionSync(int count) {
        reloadListTransactionSync(count, TRANSACTION_STATUS_SUCCESS);
        reloadListTransactionSync(count, TRANSACTION_STATUS_FAIL);
    }

    private void reloadListTransactionSync(int count, int statusType) {
        long lastUpdated;
        if (statusType == TRANSACTION_STATUS_SUCCESS) {
            lastUpdated = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, 0);
        } else {
            lastUpdated = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION_FAIL, 0);
        }

        int sortOrder = lastUpdated == 0 ? TRANSACTION_ORDER_OLDEST : TRANSACTION_ORDER_LATEST;

        transactionHistoryServer(lastUpdated, count, sortOrder, statusType, 0);
    }

    private void transactionHistoryServer(final long timestamp, final int count, final int sortOrder, int statusType, int deep) {
        Timber.d("get transaction from server [%s] statusType [%s] deep[%s] ", timestamp, statusType, deep);
        mTransactionRequestService.getTransactionHistories(mUser.zaloPayId, mUser.accesstoken, timestamp, count, sortOrder, statusType)
                .map(response -> response.data)
                .doOnNext(data -> {

                    this.writeTransactionEntity(data, sortOrder, statusType, deep);
                    int size = data.size();

                    if (size >= count && deep < 16) {

                        long nextTimestamp;
                        if (sortOrder == TRANSACTION_ORDER_OLDEST) {
                            nextTimestamp = data.get(size - 1).reqdate;
                        } else {
                            nextTimestamp = data.get(0).reqdate;
                        }

                        this.transactionHistoryServer(nextTimestamp, count, sortOrder, statusType, deep + 1);
                    } else {
                        boolean hasData = (size == 0 && timestamp == 0) // Update Ui cho lần đâu tiên.
                                || size > 0 || deep > 0;

                        onLoadedTransactionComplete(statusType, hasData);
                    }

                })
                .subscribe(new DefaultSubscriber<>());
    }

    private void onLoadedTransactionComplete(int statusType, boolean hasData) {

        boolean typeSuccess = statusType == TRANSACTION_STATUS_SUCCESS;
        if (typeSuccess) {
            mTransactionLocalStorage.setLoadedTransactionSuccess(true);
        } else {
            mTransactionLocalStorage.setLoadedTransactionFail(true);
        }

        if (hasData) {
            mEventBus.post(new TransactionChangeEvent(statusType));
        }
    }

    private void writeTransactionEntity(List<TransHistoryEntity> data, int sortOder, int statusType, int deep) {
        int size = data.size();
        if (size > 0) {

            for (TransHistoryEntity transHistoryEntity : data) {
                transHistoryEntity.statustype = statusType;
            }

            mTransactionLocalStorage.put(data);

            String lastTime = null;
            if (sortOder == TRANSACTION_ORDER_LATEST || deep == 0) {
                lastTime = String.valueOf(data.get(0).reqdate);
            }

            if (TextUtils.isEmpty(lastTime)) {
                return;
            }

            if (statusType == TRANSACTION_STATUS_SUCCESS) {
                mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, lastTime);
            } else {
                mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION_FAIL, lastTime);
            }
        }

    }

    @Override
    public Observable<TransHistory> getTransaction(long id) {
        return ObservableHelper.makeObservable(() -> mTransactionLocalStorage.getTransaction(id))
                .map(entity -> zaloPayEntityDataMapper.transform(entity));
    }

    @Override
    public Observable<Boolean> updateTransactionFail() {
        return ObservableHelper.makeObservable(() -> {
            reloadListTransactionSync(TRANSACTION_LENGTH, TRANSACTION_STATUS_FAIL);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> updateTransactionSuccess() {
        return ObservableHelper.makeObservable(() -> {
            reloadListTransactionSync(TRANSACTION_LENGTH, TRANSACTION_STATUS_SUCCESS);
            return Boolean.TRUE;
        });

    }


    @Override
    public Observable<Boolean> updateTransactionStatusSuccess(final long transId) {
        return ObservableHelper.makeObservable(() -> {
            mTransactionLocalStorage.updateStatusType(transId, TRANSACTION_STATUS_SUCCESS);
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean isLoadedTransactionSuccess() {
        return mTransactionLocalStorage.isLoadedTransactionSuccess();
    }

    @Override
    public Boolean isLoadedTransactionFail() {
        return mTransactionLocalStorage.isLoadedTransactionFail();
    }

    private static class RecursiveData {
        public int statusType;

        public RecursiveData(int statusType) {
            this.statusType = statusType;
        }
    }
}
