package vn.com.vng.zalopay.data.transaction;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

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

/**
 * Created by huuhoa on 6/15/16.
 * Implementation of TransactionStore.LocalStorage
 */
public class TransactionLocalStorage extends SqlBaseScopeImpl implements TransactionStore.LocalStorage {

    private final static int TRANSFER_TYPE = 4;
    private final static int CASHINMERCHANT_TYPE = 11;

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
    public List<TransHistoryEntity> get(int offset, int limit, int status, long maxreqdate, long minreqdate, List<Integer> types, int sign) {
        if (offset < 0 || limit <= 0) {
            return Collections.emptyList();
        }
        List<TransHistoryEntity> ret = queryList(maxreqdate, minreqdate, types, offset, limit, status, sign);
        Timber.d("get list transaction size %s", ret.size());
        return ret;
    }

    @Override
    public void remove(long id) {
        TransactionLog transactionLog = queryTransactionById(id);
        getDaoSession().getTransactionLogDao().delete(transactionLog);
    }

    @Override
    public boolean isHaveTransactionInDb() {
        return getDaoSession().getTransactionLogDao().queryBuilder().count() > 0;
    }

    private List<TransHistoryEntity> queryList(long maxreqdate, long minreqdate, List<Integer> transTypes, int offset, int limit, int statusType, int sign) {
        Timber.d("queryList: offset [%s], maxreqdate [%s], minreqdate [%s], sign [%s], statustype [%s]",
                offset, maxreqdate, minreqdate, sign, statusType);

        QueryBuilder<TransactionLog> queryBuilder = getDaoSession().getTransactionLogDao().queryBuilder();
        WhereCondition where = queryBuilder.and(TransactionLogDao.Properties.Reqdate.le(maxreqdate),
                TransactionLogDao.Properties.Reqdate.ge(minreqdate),
                TransactionLogDao.Properties.Statustype.eq(statusType));

        if (Lists.isEmptyOrNull(transTypes)) {
            return queryList(offset, limit, where);
        }

        if (!transTypes.contains(TRANSFER_TYPE) || !transTypes.contains(CASHINMERCHANT_TYPE) || sign == 0) {
            where = queryBuilder.and(where, TransactionLogDao.Properties.Type.in(transTypes));
            return queryList(offset, limit, where);
        }

        WhereCondition whereType = TransactionLogDao.Properties.Type.in(TRANSFER_TYPE, CASHINMERCHANT_TYPE);
        if (sign == 1) {
            whereType = queryBuilder.and(whereType, TransactionLogDao.Properties.Sign.eq(1));
        } else {
            whereType = queryBuilder.and(whereType, TransactionLogDao.Properties.Sign.notEq(1));
        }

        for (int i = 0; i < transTypes.size(); i++) {
            if (transTypes.get(i) != TRANSFER_TYPE && transTypes.get(i) != CASHINMERCHANT_TYPE) {
                whereType = queryBuilder.or(whereType, TransactionLogDao.Properties.Type.eq(transTypes.get(i)));
            }
        }
        where = queryBuilder.and(where, whereType);
        return queryList(offset, limit, where);
    }

    private List<TransHistoryEntity> queryList(int offset, int limit, WhereCondition where) {
        QueryBuilder<TransactionLog> queryBuilder = getDaoSession().getTransactionLogDao().queryBuilder();
        return transform2Entity(
                queryBuilder
                        .where(where)
                        .limit(limit)
                        .offset(offset)
                        .orderDesc(TransactionLogDao.Properties.Reqdate)
                        .list());
    }

    // Data transformation

    private List<TransactionLog> transform(Collection<TransHistoryEntity> transHistoryEntities) {
        if (Lists.isEmptyOrNull(transHistoryEntities)) {
            return Collections.emptyList();
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
        transDao.thank_message = (transEntity.thank_message);
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
        entity.thank_message = transDao.thank_message;
        return entity;
    }

    private List<TransHistoryEntity> transform2Entity(Collection<TransactionLog> transactionLogs) {
        if (Lists.isEmptyOrNull(transactionLogs)) {
            return Collections.emptyList();
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


    @Override
    public void updateThankMessage(long transId, String message) {
        Timber.d("updateThankMessage for transactionId: %s", transId);
        try {
            TransactionLog transactionLog = queryTransactionById(transId);
            if (transactionLog == null) {
                return;
            }
            transactionLog.thank_message = message;
            getDaoSession().getTransactionLogDao().insertOrReplaceInTx(transactionLog);
        } catch (Exception e) {
            Timber.w("Exception while trying update thank message: %s", e.getMessage());
        }
    }


    private TransHistoryEntity getTransactionById(long id) {
        TransactionLog transactionLog = queryTransactionById(id);
        return transform(transactionLog);
    }

    private TransactionLog queryTransactionById(long id) {
        return getDaoSession().getTransactionLogDao().load(id);
    }

    @Override
    public void updateStatusType(long transId, int status) {
        TransactionLog transactionLog = queryTransactionById(transId);
        if (transactionLog == null) {
            return;
        }
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
}

