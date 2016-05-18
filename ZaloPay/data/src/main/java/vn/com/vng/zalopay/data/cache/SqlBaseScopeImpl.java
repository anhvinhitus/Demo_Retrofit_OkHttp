package vn.com.vng.zalopay.data.cache;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import vn.com.vng.zalopay.data.cache.model.AppInfoDao;
import vn.com.vng.zalopay.data.cache.model.BankCardGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.DataManifest;
import vn.com.vng.zalopay.data.cache.model.DataManifestDao;
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

    public static <T> Observable<T> makeObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            subscriber.onNext(func.call());
                            subscriber.onCompleted();
                        } catch (Exception ex) {
                            try {
                                subscriber.onError(ex);
                            } catch (Exception ex2) {
                            }
                        }
                    }
                });
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

    public AppInfoDao getAppInfoDao() {
        return daoSession.getAppInfoDao();
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


}
