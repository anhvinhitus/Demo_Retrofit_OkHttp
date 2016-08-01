package vn.com.vng.zalopay.data.transaction;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.model.User;

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

    private static final int TRANSACTION_STATUS_SUCCESS = 1;
    private static final int TRANSACTION_STATUS_FAIL = 2;
    private static final int TRANSACTION_LENGTH = 30;

    public TransactionRepository(
            ZaloPayEntityDataMapper zaloPayEntityDataMapper,
            User user,
            SqlZaloPayScope sqlZaloPayScope,
            TransactionStore.LocalStorage transactionLocalStorage,
            TransactionStore.RequestService transactionRequestService) {
        this.zaloPayEntityDataMapper = zaloPayEntityDataMapper;
        mUser = user;
        mSqlZaloPayScope = sqlZaloPayScope;
        mTransactionLocalStorage = transactionLocalStorage;
        mTransactionRequestService = transactionRequestService;
    }

    @Override
    public Observable<List<TransHistory>> getTransactions(int pageIndex, int count) {
        return mTransactionLocalStorage.get(pageIndex, count, TRANSACTION_STATUS_SUCCESS)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public Observable<List<TransHistory>> getTransactionsFail(int pageIndex, int count) {
        return mTransactionLocalStorage.get(pageIndex, count, TRANSACTION_STATUS_FAIL)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
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

    public void reloadListTransactionSync(int count, int statusType) {
        long lastUpdated = 0;
        if (statusType == TRANSACTION_STATUS_FAIL) {
            lastUpdated = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION_FAIL, 0);
        } else {
            lastUpdated = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, 0);
        }
        transactionHistoryServer(lastUpdated, count, 1, statusType);
    }

    private void transactionHistoryServer(final long timestamp, final int count, final int sortOrder, int statusType) {
        Timber.d("transactionHistoryServer %s ", timestamp);
        mTransactionRequestService.getTransactionHistories(mUser.uid, mUser.accesstoken, timestamp, count, sortOrder, statusType)
                .doOnNext(transactionHistoryResponse -> writeTransactionResp(transactionHistoryResponse, statusType))
                .doOnNext(response -> {
                    if (response.data.size() >= count) {
                        transactionHistoryServer(response.data.get(0).reqdate, count, sortOrder, statusType);
                    }
                })
                .subscribe(new DefaultSubscriber<>());
    }

    private void writeTransactionResp(TransactionHistoryResponse response, int statusType) {
        List<TransHistoryEntity> list = response.data;
        int size = list.size();
        if (size > 0) {
            for (TransHistoryEntity transHistoryEntity : list) {
                transHistoryEntity.statustype = statusType;
            }
            mTransactionLocalStorage.put(response.data);
            if (statusType == TRANSACTION_STATUS_SUCCESS) {
                mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, String.valueOf(list.get(0).reqdate));
            } else {
                mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION_FAIL, String.valueOf(list.get(0).reqdate));
            }
        }

    }

    @Override
    public Observable<TransHistory> getTransaction(long id) {
        return mTransactionLocalStorage.getTransaction(id)
                .map(transHistoryEntity -> zaloPayEntityDataMapper.transform(transHistoryEntity)); //Todo: Test lại trường hợp result = null
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

}
