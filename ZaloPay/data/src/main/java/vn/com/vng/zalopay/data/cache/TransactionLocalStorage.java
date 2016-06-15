package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.helper.ObservableHelper;
import vn.com.vng.zalopay.data.cache.mapper.ZaloPayDaoMapper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransactionLogDao;

/**
 * Created by huuhoa on 6/15/16.
 * Implementation of TransactionStore.LocalStorage
 */
public class TransactionLocalStorage extends SqlBaseScopeImpl implements TransactionStore.LocalStorage {
    private ZaloPayDaoMapper mDaoMapper;

    public TransactionLocalStorage(DaoSession daoSession, ZaloPayDaoMapper zaloCacheMapper) {
        super(daoSession);
        this.mDaoMapper = zaloCacheMapper;
    }

    @Override
    public void write(List<TransHistoryEntity> val) {
        getDaoSession().getTransactionLogDao().insertOrReplaceInTx(mDaoMapper.transform(val));

        Timber.d("write list transaction %s", val.size(), listTransHistories(10));
    }

    @Override
    public void write(TransHistoryEntity val) {
        getDaoSession().getTransactionLogDao().insertOrReplace(mDaoMapper.transform(val));
    }

    @Override
    public Observable<List<TransHistoryEntity>> transactionHistories() {
        return ObservableHelper.makeObservable(() -> listTransHistories(Integer.MAX_VALUE))
                .doOnNext(transHistoryEntities -> Timber.d("transactionHistories %s", transHistoryEntities.size()))
                ;
    }

    @Override
    public Observable<List<TransHistoryEntity>> transactionHistories(int limit) {
        return ObservableHelper.makeObservable(() -> listTransHistories(limit));
    }

    @Override
    public List<TransHistoryEntity> listTransHistories(int limit) {
        return mDaoMapper.transform2Entity(
                getDaoSession()
                        .getTransactionLogDao()
                        .queryBuilder()
                        .limit(limit)
                        .orderDesc(TransactionLogDao.Properties.Reqdate)
                        .list());
    }

    @Override
    public Observable<TransHistoryEntity> transactionHistory() {
        //return makeObservable(() -> mDaoMapper.transform(getDaoSession().getTransactionLogDao().queryBuilder().w));
        return null;
    }

    @Override
    public boolean isHaveTransactionInDb() {
        return getDaoSession().getTransactionLogDao().queryBuilder().count() > 0;
    }
}
