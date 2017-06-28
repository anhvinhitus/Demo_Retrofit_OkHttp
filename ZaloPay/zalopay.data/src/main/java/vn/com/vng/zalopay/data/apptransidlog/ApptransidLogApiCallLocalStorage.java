package vn.com.vng.zalopay.data.apptransidlog;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogApiCallGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogApiCallGDDao;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by khattn on 6/27/17.
 * apptransid log api call local storage
 */

public class ApptransidLogApiCallLocalStorage implements ApptransidLogStore.ApiCallLocalStorage {
    private final DaoSession daoSession;

    public ApptransidLogApiCallLocalStorage(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    public List<ApptransidLogApiCallGD> get(String apptransid) {
        List<ApptransidLogApiCallGD> list = new ArrayList<>();
        if (apptransid != null) {
            list = daoSession.getApptransidLogApiCallGDDao()
                    .queryBuilder()
                    .where(ApptransidLogApiCallGDDao.Properties.Apptransid.eq(apptransid))
                    .list();
        }
        return list;
    }

    @Override
    public void put(ApptransidLogApiCallGD newLog) {
        try {
            daoSession.getApptransidLogApiCallGDDao().insertOrReplaceInTx(newLog);
        } catch (Exception e) {
            Timber.d(e, "Update log error");
        }
    }

    @Override
    public void delete(String apptransid) {
        try {
            ApptransidLogApiCallGDDao logApiCallGDDao = daoSession.getApptransidLogApiCallGDDao();
            List<ApptransidLogApiCallGD> logGDList = logApiCallGDDao.queryBuilder()
                    .where(ApptransidLogApiCallGDDao.Properties.Apptransid.eq(apptransid))
                    .list();
            if (!Lists.isEmptyOrNull(logGDList)) {
                logApiCallGDDao.deleteInTx(logGDList);
            }
        } catch (Exception e) {
            Timber.d(e, "Delete log error");
        }
    }
}
