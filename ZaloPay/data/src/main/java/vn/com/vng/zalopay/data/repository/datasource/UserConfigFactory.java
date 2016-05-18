package vn.com.vng.zalopay.data.repository.datasource;

import android.content.Context;

import java.util.Collection;

import de.greenrobot.dao.AbstractDao;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by longlv on 17/05/2016.
 */
public class UserConfigFactory {

    private Context context;
    private UserConfig userConfig;
    private DaoSession daoSession;

    public UserConfigFactory(Context context, UserConfig userConfig, DaoSession daoSession) {
        this.context = context;
        this.userConfig = userConfig;
        this.daoSession = daoSession;
    }

    public void clearAllUserDB() {
        Timber.tag("UserConfigFactory").d("clearAllUserDB..............");
        userConfig.clearConfig();
        clearAllCacheDatabase();
        clearAllDatabase();
    }

    private void clearAllCacheDatabase() {
        daoSession.clear();
    }

    private void clearAllDatabase() {
        Collection<AbstractDao<?, ?>> daoCollection = daoSession.getAllDaos();
        for (AbstractDao<?,?> dao: daoCollection) {
            if (dao != null) {
                dao.deleteAll();
            }
        }
    }
}
