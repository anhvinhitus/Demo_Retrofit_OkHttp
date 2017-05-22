package vn.com.vng.zalopay.data.transaction;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.TransactionFragmentEntity;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.TransactionFragmentGD;
import vn.com.vng.zalopay.data.cache.model.TransactionFragmentGDDao;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by khattn on 4/10/17.
 * Implementation of TransactionFragmentStore.LocalStorage
 */

public class TransactionFragmentLocalStorage extends SqlBaseScopeImpl implements TransactionFragmentStore.LocalStorage {

    public TransactionFragmentLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public void put(TransactionFragmentEntity val) {
        try {
            TransactionFragmentGD transDao = transform(val);
            getDaoSession().getTransactionFragmentGDDao().insertOrReplace(transDao);
        } catch (Exception e) {
            Timber.w("Exception while trying to put transaction fragment to local storage: %s", e.getMessage());
        }
    }

    @Override
    public void updateOutOfData(long timestamp, int statustype, boolean outofdata) {
        List<TransactionFragmentGD> transDao = getDaoSession()
                .getTransactionFragmentGDDao()
                .queryBuilder()
                .where(TransactionFragmentGDDao.Properties.Maxreqdate.ge(timestamp),
                        TransactionFragmentGDDao.Properties.Minreqdate.le(timestamp),
                        TransactionFragmentGDDao.Properties.Statustype.eq(statustype))
                .orderDesc(TransactionFragmentGDDao.Properties.Maxreqdate)
                .limit(1)
                .list();

        if (!Lists.isEmptyOrNull(transDao)) {
            transDao.get(0).outofdata = outofdata;
            getDaoSession().getTransactionFragmentGDDao().insertOrReplace(transDao.get(0));
        }
    }

    @Override
    public boolean isOutOfData(long timestamp, int statustype) {
        QueryBuilder<TransactionFragmentGD> queryBuilder = getDaoSession().getTransactionFragmentGDDao().queryBuilder();
        WhereCondition timeCondition = queryBuilder.and(TransactionFragmentGDDao.Properties.Maxreqdate.ge(timestamp),
                TransactionFragmentGDDao.Properties.Minreqdate.le(timestamp));
        List<TransactionFragmentGD> transDao = queryBuilder
                .where(timeCondition,
                        TransactionFragmentGDDao.Properties.Outofdata.eq(true),
                        TransactionFragmentGDDao.Properties.Statustype.eq(statustype))
                .list();

        return !Lists.isEmptyOrNull(transDao);
    }

    @Override
    public void remove(long minreqdate) {
        try {
            getDaoSession().getTransactionFragmentGDDao().deleteByKey(minreqdate);
        } catch (Exception e) {
            Timber.w("Exception while trying to delete transaction fragment: %s", e.getMessage());
        }
    }

    @Override
    public List<TransactionFragmentEntity> get(long timestamp, int statustype) {
        List<TransactionFragmentGD> transDao = getDaoSession()
                .getTransactionFragmentGDDao()
                .queryBuilder()
                .where(TransactionFragmentGDDao.Properties.Maxreqdate.ge(timestamp),
                        TransactionFragmentGDDao.Properties.Minreqdate.le(timestamp),
                        TransactionFragmentGDDao.Properties.Statustype.eq(statustype))
                .orderDesc(TransactionFragmentGDDao.Properties.Maxreqdate)
                .list();

        return Lists.transform(transDao, this::transform);
    }

    @Override
    public TransactionFragmentEntity getLatestFragment(int statustype) {
        List<TransactionFragmentGD> transDao = getDaoSession()
                .getTransactionFragmentGDDao()
                .queryBuilder()
                .where(TransactionFragmentGDDao.Properties.Statustype.eq(statustype))
                .orderDesc(TransactionFragmentGDDao.Properties.Maxreqdate)
                .limit(1)
                .list();

        if (transDao.size() == 0) {
            return null;
        }
        return transform(transDao.get(0));
    }

    @Override
    public boolean isHasData(long timestamp, int statustype) {
        List<TransactionFragmentGD> transDao = getDaoSession()
                .getTransactionFragmentGDDao()
                .queryBuilder()
                .where(TransactionFragmentGDDao.Properties.Maxreqdate.ge(timestamp),
                        TransactionFragmentGDDao.Properties.Minreqdate.le(timestamp),
                        TransactionFragmentGDDao.Properties.Statustype.eq(statustype))
                .orderDesc(TransactionFragmentGDDao.Properties.Maxreqdate)
                .list();

        return !Lists.isEmptyOrNull(transDao);
    }

    private TransactionFragmentGD transform(TransactionFragmentEntity transEntity) {
        if (transEntity == null) {
            return null;
        }

        TransactionFragmentGD transDao = new TransactionFragmentGD();
        transDao.statustype = transEntity.statustype;
        transDao.maxreqdate = transEntity.maxreqdate;
        transDao.minreqdate = transEntity.minreqdate;
        transDao.outofdata = transEntity.outofdata;
        return transDao;
    }

    private TransactionFragmentEntity transform(TransactionFragmentGD transDao) {
        if (transDao == null) {
            return null;
        }

        TransactionFragmentEntity entity = new TransactionFragmentEntity();
        entity.statustype = transDao.statustype;
        entity.maxreqdate = transDao.maxreqdate;
        entity.minreqdate = transDao.minreqdate;
        entity.outofdata = transDao.outofdata;
        return entity;
    }
}
