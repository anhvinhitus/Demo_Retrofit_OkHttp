package vn.com.vng.zalopay.data.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransactionLog;
import vn.com.vng.zalopay.data.cache.model.TransactionLogDao;
import vn.com.vng.zalopay.data.util.ConvertHelper;
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

    @Override
    public void put(List<TransHistoryEntity> val) {
        try {
            List<TransactionLog> list = transform(val);
            getDaoSession().getTransactionLogDao().insertOrReplaceInTx(list);
            Timber.d("put list transaction %s", list.size());
        } catch (Exception e) {
            Timber.w("Exception while trying to put transaction histories to local storage: %s", e.getMessage());
        }
    }

    @Override
    public List<TransHistoryEntity> get(int pageIndex, int limit, int status) {
        if (pageIndex < 0 || limit <= 0) {
            return Collections.emptyList();
        }
        List<TransHistoryEntity> ret = queryList(pageIndex, limit, status);
        Timber.d("get list transaction size %s", ret.size());
        return ret;
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
        int offset = pageIndex * limit;
        Timber.d("queryList: offset %s", offset);
        return transform2Entity(
                getDaoSession()
                        .getTransactionLogDao()
                        .queryBuilder()
                        .where(TransactionLogDao.Properties.Statustype.eq(statusType))
                        .limit(limit)
                        .offset(offset)
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

        TransactionLog transDao = new TransactionLog();
        transDao.transid = transEntity.transid;
        transDao.appuser = (transEntity.appuser);
        transDao.appid = (transEntity.appid);
        transDao.description = (transEntity.description);
        transDao.userchargeamt = (transEntity.userchargeamt);
        transDao.userfeeamt = (transEntity.userfeeamt);
        transDao.amount = (transEntity.amount);
        transDao.platform = (transEntity.platform);
        transDao.pmcid = (transEntity.pmcid);
        transDao.type = (transEntity.type);
        transDao.reqdate = (transEntity.reqdate);
        transDao.userid = (transEntity.userid);
        transDao.sign = (transEntity.sign);
        transDao.username = (transEntity.username);
        transDao.appusername = (transEntity.appusername);
        transDao.statustype = (transEntity.statustype);
        return transDao;
    }

    private TransHistoryEntity transform(TransactionLog transDao) {
        if (transDao == null) {
            return null;
        }

        TransHistoryEntity entity = new TransHistoryEntity();
        entity.appid = transDao.appid;
        entity.appuser = transDao.appuser;
        entity.description = transDao.description;
        entity.userchargeamt = transDao.userchargeamt;
        entity.userfeeamt = transDao.userfeeamt;
        entity.amount = transDao.amount;
        entity.platform = transDao.platform;
        entity.pmcid = transDao.pmcid;
        entity.reqdate = ConvertHelper.unboxValue(transDao.reqdate, 0);
        entity.transid = transDao.transid;
        entity.type = transDao.type;
        entity.userid = transDao.userid;
        entity.sign = transDao.sign;
        entity.username = transDao.username;
        entity.appusername = transDao.appusername;
        entity.statustype = transDao.statustype;
        return entity;
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
    public TransHistoryEntity getTransaction(long id) {
        return getTransactionById(id);
    }


    private TransHistoryEntity getTransactionById(long id) {
        TransactionLog transactionLog = queryTransactionById(id);
        return transform(transactionLog);
    }

    private TransactionLog queryTransactionById(long id) {
        TransactionLog ret = getDaoSession().getTransactionLogDao().load(id);
        return ret;
    }

    @Override
    public void updateStatusType(long transId, int status) {
        TransactionLog transactionLog = queryTransactionById(transId);
        transactionLog.statustype = (long) (status);
        getDaoSession().getTransactionLogDao().insertOrReplaceInTx(transactionLog);
    }

    @Override
    public void setLoadedTransactionSuccess(boolean loaded) {
        insertDataManifest(Constants.MANIFEST_LOADED_TRANSACTION_SUCCESS, loaded ? String.valueOf(1) : String.valueOf(0));
    }

    @Override
    public void setLoadedTransactionFail(boolean loaded) {
        insertDataManifest(Constants.MANIFEST_LOADED_TRANSACTION_FAIL, loaded ? String.valueOf(1) : String.valueOf(0));
    }

    @Override
    public boolean isLoadedTransactionSuccess() {
        return getDataManifest(Constants.MANIFEST_LOADED_TRANSACTION_SUCCESS, 0) == 1;
    }

    @Override
    public boolean isLoadedTransactionFail() {
        return getDataManifest(Constants.MANIFEST_LOADED_TRANSACTION_FAIL, 0) == 1;
    }

    @Override
    public long getLatestTimeTransaction(int statusType) {
        long timeUpdate = 0;
        List<TransactionLog> log = getTransactionLogDao().queryBuilder()
                .where(TransactionLogDao.Properties.Reqdate.isNotNull(), TransactionLogDao.Properties.Statustype.eq(statusType))
                .orderDesc(TransactionLogDao.Properties.Reqdate)
                .limit(1).list();
        if (!Lists.isEmptyOrNull(log)) {
            timeUpdate = ConvertHelper.unboxValue(log.get(0).reqdate, 0);
        }
        Timber.d("getLatestTimeTransaction timeUpdate %s", timeUpdate);
        return timeUpdate;
    }

    @Override
    public long getOldestTimeTransaction(int statusType) {
        long timeUpdate = 0;
        List<TransactionLog> log = getTransactionLogDao().queryBuilder()
                .where(TransactionLogDao.Properties.Reqdate.isNotNull(), TransactionLogDao.Properties.Statustype.eq(statusType))
                .orderAsc(TransactionLogDao.Properties.Reqdate)
                .limit(1).list();
        if (!Lists.isEmptyOrNull(log)) {
            timeUpdate = ConvertHelper.unboxValue(log.get(0).reqdate, 0);
        }
        Timber.d("getOldestTimeTransaction timeUpdate %s", timeUpdate);
        return timeUpdate;
    }
}

