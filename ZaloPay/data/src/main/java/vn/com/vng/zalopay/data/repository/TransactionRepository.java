package vn.com.vng.zalopay.data.repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
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
    private static final int LENGTH_TRANS_HISTORY = 25;

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
    public Observable<List<TransHistory>> initializeTransHistory() {
        return transactionHistorysServer(0, 1)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public Observable<List<TransHistory>> loadMoreTransHistory() {
        return null;
    }

    @Override
    public Observable<List<TransHistory>> getTransactions(int pageIndex, int count) {
        return transactionHistorysLocal(count)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public Observable<List<TransHistory>> reloadListTransaction(int count) {
        return transactionHistorysLocal(count)
                .map(transHistoryEntities -> zaloPayEntityDataMapper.transform(transHistoryEntities));
    }

    @Override
    public void reloadListTransaction(int count, Subscriber<List<TransHistory>> subscriber) {
        //   zaloPayFactory.reloadListTransactionSync(count, subscriber);
    }

    @Override
    public void getTransactions(int pageIndex, int count, Subscriber<List<TransHistory>> subscriber) {
        // zaloPayFactory.getTransactions(pageIndex, count, subscriber);
    }

    public void requestTransactionsHistory() {
        reloadListTransactionSync(30, null);
    }

    @Override
    public Observable<Boolean> transactionUpdate() {
        return ObservableHelper.makeObservable(() -> {
            //update transaction
            reloadListTransactionSync(30, null);

            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> initialize() {
        return ObservableHelper.makeObservable(() -> {
            requestTransactionsHistory();
            return Boolean.TRUE;
        }).delaySubscription(5, TimeUnit.SECONDS);
    }

    public Observable<List<TransHistoryEntity>> transactionHistorysServer(long timestamp, int order) {
        return mTransactionRequestService.getTransactionHistories(mUser.uid, mUser.accesstoken, timestamp, LENGTH_TRANS_HISTORY, order)
                .map(transactionHistoryResponse -> transactionHistoryResponse.data)
                .doOnNext(transHistoryEntities -> {
                    //(4)
                    if (transHistoryEntities.size() > 0) {
                        mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, String.valueOf(transHistoryEntities.get(0).transid));
                        mTransactionLocalStorage.write(transHistoryEntities);
                    }
                })
                ;
    }


    public Observable<List<TransHistoryEntity>> transactionHistorysLocal() {
        return mTransactionLocalStorage.transactionHistories();
    }

    public Observable<List<TransHistoryEntity>> transactionHistorysLocal(int limit) {
        return mTransactionLocalStorage.transactionHistories(limit);
    }

    public void reloadListTransactionSync(int count, Subscriber<List<TransHistory>> subscriber) {
        if (mTransactionLocalStorage.isHaveTransactionInDb()) {
            long lasttime = mSqlZaloPayScope.getDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, 0);
            transactionHistoryServer(lasttime, count, 1, subscriber);
        } else {
            transactionHistoryServer(0, count, 1, subscriber);
        }
    }

    private void transactionHistoryServer(final long timestamp, final int count, final int odder, final Subscriber<List<TransHistory>> subscriber) {
        Timber.d("transactionHistoryServer %s ", timestamp);
        mTransactionRequestService.getTransactionHistories(mUser.uid, mUser.accesstoken, timestamp, count, odder)
                .doOnNext(response -> writeTransactionResp(response))
                .doOnNext(new Action1<TransactionHistoryResponse>() {
                    @Override
                    public void call(TransactionHistoryResponse response) {
                        if (response.data.size() >= count) {
                            transactionHistoryServer(response.data.get(0).reqdate, count, odder, subscriber);
                        }
                    }
                })
                .subscribe(new DefaultSubscriber<>());
    }

    private void writeTransactionResp(TransactionHistoryResponse response) {
        List<TransHistoryEntity> list = response.data;
        int size = list.size();
        if (size > 0) {
            mTransactionLocalStorage.write(response.data);
            mSqlZaloPayScope.insertDataManifest(Constants.MANIF_LASTTIME_UPDATE_TRANSACTION, String.valueOf(list.get(0).reqdate));
        }
    }
}
