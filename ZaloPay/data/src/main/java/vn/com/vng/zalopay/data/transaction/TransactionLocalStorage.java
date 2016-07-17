package vn.com.vng.zalopay.data.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransactionLog;
import vn.com.vng.zalopay.data.cache.model.TransactionLogDao;
import vn.com.vng.zalopay.data.util.Lists;

import static java.util.Collections.emptyList;

/**
 * Created by huuhoa on 6/15/16.
 * Implementation of TransactionStore.LocalStorage
 */
public class TransactionLocalStorage extends SqlBaseScopeImpl implements TransactionStore.LocalStorage {

    public TransactionLocalStorage(DaoSession daoSession) {
        super(daoSession);
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
    public void put(List<TransHistoryEntity> val) {
        try {
            getDaoSession().getTransactionLogDao().insertOrReplaceInTx(transform(val));

            Timber.d("put list transaction %s", val.size());
        } catch (Exception e) {
            Timber.w("Exception while trying to put transaction histories to local storage: %s", e.getMessage());
        }
    }

    @Override
    public Observable<List<TransHistoryEntity>> get(int pageIndex, int limit, int status) {
        return ObservableHelper.makeObservable(() -> queryList(pageIndex, limit, status))
                .doOnNext(transHistoryEntities -> Timber.d("get %s", transHistoryEntities.size()))
                ;
    }

    @Override
    public boolean isHaveTransactionInDb() {
        return getDaoSession().getTransactionLogDao().queryBuilder().count() > 0;
    }

    private List<TransHistoryEntity> queryList(int pageIndex, int limit) {
        return transform2Entity(
                getDaoSession()
                        .getTransactionLogDao()
                        .queryBuilder()
                        .limit(limit)
                        .offset(pageIndex * limit)
                        .orderDesc(TransactionLogDao.Properties.Reqdate)
                        .list());
    }

    private List<TransHistoryEntity> queryList(int pageIndex, int limit, int statusType) {
        return transform2Entity(
                getDaoSession()
                        .getTransactionLogDao()
                        .queryBuilder()
                        .where(TransactionLogDao.Properties.Statustype.eq(statusType))
                        .limit(limit)
                        .offset(pageIndex * limit)
                        .orderDesc(TransactionLogDao.Properties.Reqdate)
                        .list());
    }

    // Data transformation

    private List<TransactionLog> transform(Collection<TransHistoryEntity> transHistoryEntities) {
        if (Lists.isEmptyOrNull(transHistoryEntities)) {
            return emptyList();
        }

        List<TransactionLog> transactionLogs = new ArrayList<>(transHistoryEntities.size());
        for (TransHistoryEntity transHistoryEntity : transHistoryEntities) {
            TransactionLog transactionLog = transform(transHistoryEntity);
            if (transactionLog == null) {
                continue;
            }

            transactionLogs.add(transactionLog);
        }

        return transactionLogs;
    }

    private TransactionLog transform(TransHistoryEntity transEntity) {
        if (transEntity == null) {
            return null;
        }

        TransactionLog transDao = new TransactionLog(transEntity.transid);
        transDao.setAppuser(transEntity.appuser);
        transDao.setAppid(transEntity.appid);
        transDao.setDescription(transEntity.description);
        transDao.setUserchargeamt(transEntity.userchargeamt);
        transDao.setUserfeeamt(transEntity.userfeeamt);
        transDao.setAmount(transEntity.amount);
        transDao.setPlatform(transEntity.platform);
        transDao.setPmcid(transEntity.pmcid);
        transDao.setType(transEntity.type);
        transDao.setReqdate(transEntity.reqdate);
        transDao.setUserid(transEntity.userid);
        transDao.setSign(transEntity.sign);
        transDao.setUsername(transEntity.username);
        transDao.setAppusername(transEntity.appusername);
        transDao.setStatustype(transEntity.statustype);
        return transDao;
    }

    private TransHistoryEntity transform(TransactionLog transDao) {
        if (transDao == null) {
            return null;
        }

        TransHistoryEntity transHistoryEntity = new TransHistoryEntity();
        transHistoryEntity.appid = transDao.getAppid();
        transHistoryEntity.appuser = transDao.getAppuser();
        transHistoryEntity.description = transDao.getDescription();
        transHistoryEntity.userchargeamt = transDao.getUserchargeamt();
        transHistoryEntity.userfeeamt = transDao.getUserfeeamt();
        transHistoryEntity.amount = transDao.getAmount();
        transHistoryEntity.platform = transDao.getPlatform();
        transHistoryEntity.pmcid = transDao.getPmcid();
        transHistoryEntity.reqdate = transDao.getReqdate();
        transHistoryEntity.transid = transDao.getTransid();
        transHistoryEntity.type = transDao.getType();
        transHistoryEntity.userid = transDao.getUserid();
        transHistoryEntity.sign = transDao.getSign();
        transHistoryEntity.username = transDao.getUsername();
        transHistoryEntity.appusername = transDao.getAppusername();
        transHistoryEntity.statustype = transDao.getStatustype();
        return transHistoryEntity;
    }

    private List<TransHistoryEntity> transform2Entity(Collection<TransactionLog> transactionLogs) {
        if (Lists.isEmptyOrNull(transactionLogs)) {
            return emptyList();
        }

        List<TransHistoryEntity> transHistoryEntities = new ArrayList<>(transactionLogs.size());
        for (TransactionLog transactionLog : transactionLogs) {
            TransHistoryEntity transHistoryEntity = transform(transactionLog);
            if (transHistoryEntity == null) {
                continue;
            }

            transHistoryEntities.add(transHistoryEntity);
        }
        return transHistoryEntities;
    }

    @Override
    public Observable<TransHistoryEntity> getTransaction(long id) {
        return Observable.just(getTransactionById(id));
    }


    private TransHistoryEntity getTransactionById(long id) {
        List<TransactionLog> list = getDaoSession().getTransactionLogDao().queryBuilder().where(TransactionLogDao.Properties.Transid.eq(id)).list();
        if (Lists.isEmptyOrNull(list)) {
            return null;
        }
        TransactionLog transactionLog = list.get(0);
        return transform(transactionLog);
    }
}

