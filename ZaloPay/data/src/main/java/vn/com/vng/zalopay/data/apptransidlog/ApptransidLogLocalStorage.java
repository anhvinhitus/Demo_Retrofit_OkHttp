package vn.com.vng.zalopay.data.apptransidlog;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.SqlBaseScopeImpl;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGD;
import vn.com.vng.zalopay.data.cache.model.ApptransidLogGDDao;
import vn.com.vng.zalopay.data.cache.model.DaoSession;

/**
 * Created by khattn on 1/24/17.
 */

public class ApptransidLogLocalStorage extends SqlBaseScopeImpl implements ApptransidLogStore.LocalStorage {

    public ApptransidLogLocalStorage(DaoSession daoSession) {
        super(daoSession);
    }

    @Override
    public ApptransidLogGD get(String apptransid) {
        List<ApptransidLogGD> list = new ArrayList<>();
        if (apptransid != null) {
            list = getDaoSession().getApptransidLogGDDao()
                    .queryBuilder()
                    .where(ApptransidLogGDDao.Properties.Apptransid.eq(apptransid))
                    .list();
        }
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public void updateLog(ApptransidLogGD newLog) {
        ApptransidLogGD apptransidLogGD = get(newLog.apptransid);
        if (apptransidLogGD == null) {
            apptransidLogGD = new ApptransidLogGD();
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

        try {
            getDaoSession().getApptransidLogGDDao().insertOrReplaceInTx(apptransidLogGD);
        } catch (Exception e) {
            Timber.d(e, "Update log error");
            return;
        }
    }

    @Override
    public void delete(String apptransid) {
        try {
            getDaoSession().getApptransidLogGDDao().deleteByKeyInTx(apptransid);
        } catch (Exception e) {
            Timber.d(e, "Delete log error");
            return;
        }
    }
}
