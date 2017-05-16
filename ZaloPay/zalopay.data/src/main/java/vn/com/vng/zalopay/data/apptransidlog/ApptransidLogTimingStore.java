package vn.com.vng.zalopay.data.apptransidlog;

import java.util.List;

import vn.com.vng.zalopay.data.cache.global.ApptransidLogTimingGD;

/**
 * Created by khattn on 5/16/17.
 * Apptransid log timing store
 */

public interface ApptransidLogTimingStore {
    interface LocalStorage {

        List<ApptransidLogTimingGD> get(String apptransid);

        void put(ApptransidLogTimingGD newLog);

        void delete(String apptransid);
    }
}
