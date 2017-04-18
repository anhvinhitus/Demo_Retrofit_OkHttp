package vn.com.vng.zalopay.data.apptransidlog;

import java.util.Map;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.ApptransidLogEntity;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;

/**
 * Created by khattn on 1/24/17.
 */

public interface ApptransidLogStore {
    interface LocalStorage {

        ApptransidLogGD get(String apptransid);

        void updateLog(ApptransidLogGD newLog);

        void delete(String apptransid);
    }

    interface Repository {

        Observable<Void> put(Map<String, String> val);

        Observable<ApptransidLogEntity> get(String apptransid);

        Observable<Void> remove(String apptransid);
    }
}
