package vn.com.vng.zalopay.data.apptransidlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGDDao;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by khattn on 1/24/17.
 * apptransid log local storage
 */

public class ApptransidLogLocalStorage implements ApptransidLogStore.LocalStorage {
    private final int STATUS_DONE = 1;
    private final DaoSession daoSession;

    public ApptransidLogLocalStorage(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    public ApptransidLogGD get(String apptransid) {
        List<ApptransidLogGD> list = new ArrayList<>();
        if (apptransid != null) {
            list = daoSession.getApptransidLogGDDao()
                    .queryBuilder()
                    .where(ApptransidLogGDDao.Properties.Apptransid.eq(apptransid))
                    .limit(1)
                    .list();
        }
        if (Lists.isEmptyOrNull(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<ApptransidLogGD> getAll() {
        try {
            return daoSession.getApptransidLogGDDao()
                    .queryBuilder()
                    .where(ApptransidLogGDDao.Properties.Status.eq(STATUS_DONE))
                    .list();
        } catch (Exception e) {
            Timber.d(e, "Get all log error");
            return Collections.emptyList();
        }
    }

    @Override
    public void updateLog(ApptransidLogGD newLog) {
        ApptransidLogGD apptransidLogGD = get(newLog.apptransid);

        if (apptransidLogGD == null) {
            apptransidLogGD = new ApptransidLogGD();
        }

        if (apptransidLogGD.status != null && apptransidLogGD.status == STATUS_DONE) {
            return;
        }

        apptransidLogGD.apptransid = (newLog.apptransid == null) ? apptransidLogGD.apptransid : newLog.apptransid;
        apptransidLogGD.appid = (newLog.appid == null) ? apptransidLogGD.appid : newLog.appid;
        apptransidLogGD.step = (newLog.step == null) ? apptransidLogGD.step : newLog.step;
        apptransidLogGD.step_result = (newLog.step_result == null) ? apptransidLogGD.step_result : newLog.step_result;
        apptransidLogGD.pcmid = (newLog.pcmid == null) ? apptransidLogGD.pcmid : newLog.pcmid;
        apptransidLogGD.transtype = (newLog.transtype == null) ? apptransidLogGD.transtype : newLog.transtype;
        apptransidLogGD.transid = (newLog.transid == null) ? apptransidLogGD.transid : newLog.transid;
        apptransidLogGD.sdk_result = (newLog.sdk_result == null) ? apptransidLogGD.sdk_result : newLog.sdk_result;
        apptransidLogGD.server_result = (newLog.server_result == null) ? apptransidLogGD.server_result : newLog.server_result;
        apptransidLogGD.source = (newLog.source == null) ? apptransidLogGD.source : newLog.source;
        apptransidLogGD.start_time = (newLog.start_time == null) ? apptransidLogGD.start_time : newLog.start_time;
        apptransidLogGD.finish_time = (newLog.finish_time == null) ? apptransidLogGD.finish_time : newLog.finish_time;
        apptransidLogGD.bank_code = (newLog.bank_code == null) ? apptransidLogGD.bank_code : newLog.bank_code;
        apptransidLogGD.status = (newLog.status == null) ? apptransidLogGD.status : newLog.status;

        try {
            daoSession.getApptransidLogGDDao().insertOrReplaceInTx(apptransidLogGD);
        } catch (Exception e) {
            Timber.d(e, "Update log error");
        }
    }

    @Override
    public void delete(String apptransid) {
        try {
            daoSession.getApptransidLogGDDao().deleteByKeyInTx(apptransid);
        } catch (Exception e) {
            Timber.d(e, "Delete log error");
        }
    }

    @Override
    public void deleteAll() {
        try {
            ApptransidLogGDDao logGDDao = daoSession.getApptransidLogGDDao();
            List<ApptransidLogGD> logGDList = logGDDao.queryBuilder()
                    .where(ApptransidLogGDDao.Properties.Status.eq(STATUS_DONE))
                    .list();
            if (!Lists.isEmptyOrNull(logGDList)) {
                logGDDao.deleteInTx(logGDList);
            }
        } catch (Exception e) {
            Timber.d(e, "Delete all log error");
        }
    }
}
