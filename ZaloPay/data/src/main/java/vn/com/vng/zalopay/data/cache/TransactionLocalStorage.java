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

//    String transactionToString(TransactionLog v) {
//        return String.format("\"userid\":\"%s\",\"transid\":%d,\"appid\":%d,\"appuser\":\"%s\",\"platform\":\"%s\",\"description\":\"%s\",\"pmcid\":%d,\"reqdate\":%d,\"userchargeamt\":%d,\"amount\":%d,\"userfeeamt\":%d,\"type\":%d,\"sign\":%d,\"username\":\"%s\",\"appusername\":\"%s\"",
//                v.getUserid(), v.getTransid(), v.getAppid(), v.getAppuser(), v.getPlatform(), v.getDescription(), v.getPmcid(), v.getReqdate(), v.getUserchargeamt(), v.getAmount(), v.getUserfeeamt(), v.getType(), v.getSign(), v.getUsername(), v.getAppusername());
//    }
//    List<String> logsString(Collection<TransactionLog> logs) {
//        List<String> result = new ArrayList<>();
//        for (TransactionLog v : logs) {
//            result.add(transactionToString(v));
//        }
//        return result;
//    }

    @Override
    public void write(List<TransHistoryEntity> val) {
        try {
            getDaoSession().getTransactionLogDao().insertOrReplaceInTx(mDaoMapper.transform(val));

            Timber.d("write list transaction %s", val.size());
        } catch (Exception e) {
            Timber.w("Exception while trying to write transaction histories to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<List<TransHistoryEntity>> transactionHistories(int pageIndex, int limit) {
        return ObservableHelper.makeObservable(() -> listTransHistories(pageIndex, limit))
                .doOnNext(transHistoryEntities -> Timber.d("transactionHistories %s", transHistoryEntities.size()))
                ;
    }

    @Override
    public List<TransHistoryEntity> listTransHistories(int pageIndex, int limit) {
        return mDaoMapper.transform2Entity(
                getDaoSession()
                        .getTransactionLogDao()
                        .queryBuilder()
                        .limit(limit)
                        .offset(pageIndex * limit)
                        .orderDesc(TransactionLogDao.Properties.Reqdate)
                        .list());
    }

    @Override
    public boolean isHaveTransactionInDb() {
        return getDaoSession().getTransactionLogDao().queryBuilder().count() > 0;
    }
}
