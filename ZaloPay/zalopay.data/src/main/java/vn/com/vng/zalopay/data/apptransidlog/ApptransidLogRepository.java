package vn.com.vng.zalopay.data.apptransidlog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.mapper.ApptransidLogEntityDataMapper;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogTimingGD;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.zalopay.analytics.ZPApptransidLog;

/**
 * Created by khattn on 1/24/17.
 * Apptransid
 */

public class ApptransidLogRepository implements ApptransidLogStore.Repository {
    private final ApptransidLogStore.LocalStorage mLocalStorage;
    private final ApptransidLogTimingStore.LocalStorage mTimingLocalStorage;
    private final ApptransidLogEntityDataMapper mMapper;

    public ApptransidLogRepository(ApptransidLogStore.LocalStorage mLocalStorage,
                                   ApptransidLogTimingStore.LocalStorage mTimingLocalStorage,
                                   ApptransidLogEntityDataMapper mMapper) {
        this.mLocalStorage = mLocalStorage;
        this.mTimingLocalStorage = mTimingLocalStorage;
        this.mMapper = mMapper;
    }

    @Override
    public Observable<Boolean> put(ZPApptransidLog val) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.updateLog(mMapper.transform(val));
            ApptransidLogTimingGD timingGD = mMapper.transformTiming(val);
            if(timingGD != null) {
                mTimingLocalStorage.put(timingGD);
            }
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<JSONObject> get(String apptransid) {
        return ObservableHelper.makeObservable(() -> {
            ApptransidLogGD apptransidLogGD = mLocalStorage.get(apptransid);
            List<ApptransidLogTimingGD> timingList = mTimingLocalStorage.get(apptransidLogGD.apptransid);
            return mMapper.transform(apptransidLogGD, timingList);
        });
    }

    @Override
    public Observable<JSONArray> getAll() {
        return ObservableHelper.makeObservable(() -> {
            JSONArray array = new JSONArray();
            List<ApptransidLogGD> list = mLocalStorage.getAll();
            for (int i = 0; i < list.size(); i++) {
                List<ApptransidLogTimingGD> timingList = mTimingLocalStorage.get(list.get(i).apptransid);
                array.put(mMapper.transform(list.get(i), timingList));
            }
            return array;
        });
    }

    @Override
    public Observable<Boolean> remove(String apptransid) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(apptransid);
            mTimingLocalStorage.delete(apptransid);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeAll() {
        return ObservableHelper.makeObservable(() -> {
            List<ApptransidLogGD> list = mLocalStorage.getAll();
            for (int i = 0; i < list.size(); i++) {
                mTimingLocalStorage.delete(list.get(i).apptransid);
            }
            mLocalStorage.deleteAll();
            return Boolean.TRUE;
        });
    }
}
