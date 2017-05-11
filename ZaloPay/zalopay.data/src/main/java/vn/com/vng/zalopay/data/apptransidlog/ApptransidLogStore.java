package vn.com.vng.zalopay.data.apptransidlog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;
import vn.com.zalopay.analytics.ZPApptransidLog;

/**
 * Created by khattn on 1/24/17.
 */

public interface ApptransidLogStore {
    interface LocalStorage {

        ApptransidLogGD get(String apptransid);

        List<ApptransidLogGD> getAll();

        void updateLog(ApptransidLogGD newLog);

        void delete(String apptransid);

        void deleteAll();
    }

    interface Repository {

        Observable<Boolean> put(ZPApptransidLog val);

        Observable<JSONObject> get(String apptransid);

        Observable<JSONArray> getAll();

        Observable<Boolean> remove(String apptransid);

        Observable<Boolean> removeAll();
    }
}
