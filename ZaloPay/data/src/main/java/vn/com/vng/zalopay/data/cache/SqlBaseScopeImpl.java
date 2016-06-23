package vn.com.vng.zalopay.data.cache;

import vn.com.vng.zalopay.data.cache.model.AppResourceGDDao;
import vn.com.vng.zalopay.data.cache.model.BankCardGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.DataManifest;
import vn.com.vng.zalopay.data.cache.model.DataManifestDao;
import vn.com.vng.zalopay.data.cache.model.PaymentTransTypeGDDao;
import vn.com.vng.zalopay.data.cache.model.TransactionLogDao;

/**
 * Created by AnhHieu on 5/5/16.
 */
public class SqlBaseScopeImpl {

    protected final DaoSession daoSession;

    public SqlBaseScopeImpl(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public void insertDataManifest(String key, String values) {
        daoSession.getDataManifestDao().insertOrReplace(new DataManifest(key, values));
    }

    public String getDataManifest(String key) {
        DataManifest dataManifest = daoSession.getDataManifestDao().queryBuilder()
                .where(DataManifestDao.Properties.Key.eq(key)).unique();
        if (dataManifest != null) {
            return dataManifest.getValue();
        }
        return null;
    }

    public long getDataManifest(String key, long def) {

        try {
            def = Long.valueOf(getDataManifest(key));
        } catch (Exception e) {
        }

        return def;
    }

    public int getDataManifest(String key, int def) {
        try {
            def = Integer.valueOf(getDataManifest(key));
        } catch (Exception e) {
        }
        return def;
    }

    public AppResourceGDDao getAppInfoDao() {
        return daoSession.getAppResourceGDDao();
    }

    public TransactionLogDao getTransactionLogDao() {
        return daoSession.getTransactionLogDao();
    }

    public DataManifestDao getDataManifestDao() {
        return daoSession.getDataManifestDao();
    }

    public BankCardGDDao getBankCardDao() {
        return daoSession.getBankCardGDDao();
    }

    public PaymentTransTypeGDDao getPaymentTransDao() {
        return daoSession.getPaymentTransTypeGDDao();
    }

}
