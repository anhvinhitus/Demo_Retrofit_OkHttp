package vn.com.vng.zalopay.data.repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.ZaloPayEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.TransactionHistoryResponse;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.TransactionStore;
import vn.com.vng.zalopay.data.cache.helper.ObservableHelper;
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
        return mTransactionLocalStorage.get(pageIndex, count)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public Observable<Boolean> updateTransaction() {
        return ObservableHelper.makeObservable(() -> {
            //update transaction
            reloadListTransactionSync(30);

            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> initialize() {
        return ObservableHelper.makeObservable(() -> {
            reloadListTransactionSync(30);
            return Boolean.TRUE;
        }).delaySubscription(10, TimeUnit.SECONDS);
    }
//
//    public Observable<List<TransHistoryEntity>> transactionHistorysServer(long timestamp, int order) {
//        return mTransactionRequestService.getTransactionHistories(mUser.uid, mUser.accesstoken, timestamp, LENGTH_TRANS_HISTORY, order)
//                .map(transactionHistoryResponse -> transactionHistoryResponse.data)
//                .doOnNext(transHistoryEntities -> {
//                    //(4)
//                    if (transHistoryEntities.size() > 0) {
//                        mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, String.valueOf(transHistoryEntities.get(0).transid));
//                        mTransactionLocalStorage.put(transHistoryEntities);
//                    }
//                })
//                ;
//    }

    public void reloadListTransactionSync(int count) {
        if (mTransactionLocalStorage.isHaveTransactionInDb()) {
            long lasttime = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, 0);
            transactionHistoryServer(lasttime, count, 1);
        } else {
            transactionHistoryServer(0, count, 1);
        }
    }

    private void transactionHistoryServer(final long timestamp, final int count, final int sortOrder) {
        Timber.d("transactionHistoryServer %s ", timestamp);
        mTransactionRequestService.getTransactionHistories(mUser.uid, mUser.accesstoken, timestamp, count, sortOrder)
                .doOnNext(this::writeTransactionResp)
                .doOnNext(response -> {
                    if (response.data.size() >= count) {
                        transactionHistoryServer(response.data.get(0).reqdate, count, sortOrder);
                    }
                })
                .subscribe(new DefaultSubscriber<>());
    }

    private void writeTransactionResp(TransactionHistoryResponse response) {
        List<TransHistoryEntity> list = response.data;
        int size = list.size();
        if (size > 0) {
            mTransactionLocalStorage.put(response.data);
            mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, String.valueOf(list.get(0).reqdate));
        }
    }
}
