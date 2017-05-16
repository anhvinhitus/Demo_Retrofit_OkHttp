package vn.com.vng.zalopay.data.apptransidlog;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogTimingGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogTimingGDDao;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by khattn on 5/16/17.
 * Apptransid log timing local storage
 */

public class ApptransidLogTimingLocalStorage implements ApptransidLogTimingStore.LocalStorage {
    private final DaoSession daoSession;

    public ApptransidLogTimingLocalStorage(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    public List<ApptransidLogTimingGD> get(String apptransid) {
        List<ApptransidLogTimingGD> list = new ArrayList<>();
        if (apptransid != null) {
            list = daoSession.getApptransidLogTimingGDDao()
                    .queryBuilder()
                    .where(ApptransidLogTimingGDDao.Properties.Apptransid.eq(apptransid))
                    .list();
        }
        return list;
    }

    @Override
    public void put(ApptransidLogTimingGD newLog) {
        try {
            daoSession.getApptransidLogTimingGDDao().insertOrReplaceInTx(newLog);
        } catch (Exception e) {
            Timber.d(e, "Update log error");
        }
    }

    @Override
    public void delete(String apptransid) {
        try {
            ApptransidLogTimingGDDao logTimingGDDao = daoSession.getApptransidLogTimingGDDao();
            List<ApptransidLogTimingGD> logGDList = logTimingGDDao.queryBuilder()
                    .where(ApptransidLogTimingGDDao.Properties.Apptransid.eq(apptransid))
                    .list();
            if(!Lists.isEmptyOrNull(logGDList)) {
                logTimingGDDao.deleteInTx(logGDList);
            }
        } catch (Exception e) {
            Timber.d(e, "Delete log error");
        }
    }
}
