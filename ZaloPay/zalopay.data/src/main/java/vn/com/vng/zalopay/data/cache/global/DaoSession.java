package vn.com.vng.zalopay.data.cache.global;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import vn.com.vng.zalopay.data.cache.global.KeyValueGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;

import vn.com.vng.zalopay.data.cache.global.KeyValueGDDao;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGDDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig keyValueGDDaoConfig;
    private final DaoConfig apptransidLogGDDaoConfig;

    private final KeyValueGDDao keyValueGDDao;
    private final ApptransidLogGDDao apptransidLogGDDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        keyValueGDDaoConfig = daoConfigMap.get(KeyValueGDDao.class).clone();
        keyValueGDDaoConfig.initIdentityScope(type);

        apptransidLogGDDaoConfig = daoConfigMap.get(ApptransidLogGDDao.class).clone();
        apptransidLogGDDaoConfig.initIdentityScope(type);

        keyValueGDDao = new KeyValueGDDao(keyValueGDDaoConfig, this);
        apptransidLogGDDao = new ApptransidLogGDDao(apptransidLogGDDaoConfig, this);

        registerDao(KeyValueGD.class, keyValueGDDao);
        registerDao(ApptransidLogGD.class, apptransidLogGDDao);
    }
    
    public void clear() {
        keyValueGDDaoConfig.clearIdentityScope();
        apptransidLogGDDaoConfig.clearIdentityScope();
    }

    public KeyValueGDDao getKeyValueGDDao() {
        return keyValueGDDao;
    }

    public ApptransidLogGDDao getApptransidLogGDDao() {
        return apptransidLogGDDao;
    }

}
