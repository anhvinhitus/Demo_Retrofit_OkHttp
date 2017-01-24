package vn.com.vng.zalopay.data;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.LogGD;
import vn.com.vng.zalopay.data.cache.model.LogGDDao;

/**
 * Created by khattn on 1/24/17.
 */

public class LogLocalStorage extends SqlBaseScopeImpl {

    public LogLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    public void put(LogGD val) {
        if (val == null) {
            return;
        }
        try {
            getDaoSession().getLogGDDao().insertOrReplace(val);
        } catch (Exception e) {
            Timber.d(e, "Insert log error");
            return;
        }
    }

    public LogGD get(String apptransid) {
        List<LogGD> list = new ArrayList<>();
        if (apptransid != null) {
            list = getDaoSession().getLogGDDao()
                    .queryBuilder()
                    .where(LogGDDao.Properties.Apptransid.eq(apptransid))
                    .list();
        }
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }
}
