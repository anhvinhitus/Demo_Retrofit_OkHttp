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
    public void put(ApptransidLogGD val) {
        if (val == null) {
            return;
        }
        try {
            getDaoSession().getApptransidLogGDDao().insertOrReplaceInTx(val);
        } catch (Exception e) {
            Timber.d(e, "Insert log error");
            return;
        }
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
}
