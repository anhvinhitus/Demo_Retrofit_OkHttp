package vn.com.vng.zalopay.data.apptransidlog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogApiCallGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogTimingGD;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPApptransidLogApiCall;

/**
 * Created by khattn on 1/24/17.
 */

public interface ApptransidLogStore {
    interface TimingLocalStorage {

        List<ApptransidLogTimingGD> get(String apptransid);

        void put(ApptransidLogTimingGD newLog);

        void delete(String apptransid);
    }

    interface ApiCallLocalStorage {

        List<ApptransidLogApiCallGD> get(String apptransid);

        void put(ApptransidLogApiCallGD newLog);

        void delete(String apptransid);
    }

    interface LocalStorage {

        ApptransidLogGD get(String apptransid);

        List<ApptransidLogGD> getWithStatusDone();

        void updateLog(ApptransidLogGD newLog);

        void delete(String apptransid);

        void deleteWithStatusDone();
    }

    interface Repository {

        Observable<Boolean> putApiCall(ZPApptransidLogApiCall val);

        Observable<Boolean> put(ZPApptransidLog val);

        Observable<JSONObject> get(String apptransid);

        Observable<JSONArray> getWithStatusDone();

        Observable<Boolean> remove(String apptransid);

        Observable<Boolean> removeWithStatusDone();
    }
}
