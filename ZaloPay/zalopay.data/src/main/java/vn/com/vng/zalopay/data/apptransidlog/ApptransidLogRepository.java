package vn.com.vng.zalopay.data.apptransidlog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.ApptransidLogEntityDataMapper;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogApiCallGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogTimingGD;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.zalopay.analytics.ZPApptransidLog;
import vn.com.zalopay.analytics.ZPApptransidLogApiCall;

/**
 * Created by khattn on 1/24/17.
 * Apptransid
 */

public class ApptransidLogRepository implements ApptransidLogStore.Repository {
    private final ApptransidLogStore.LocalStorage mLocalStorage;
    private final ApptransidLogStore.TimingLocalStorage mTimingLocalStorage;
    private final ApptransidLogStore.ApiCallLocalStorage mApiCallLocalStorage;
    private final ApptransidLogEntityDataMapper mMapper;

    public ApptransidLogRepository(ApptransidLogStore.LocalStorage mLocalStorage,
                                   ApptransidLogStore.TimingLocalStorage mTimingLocalStorage,
                                   ApptransidLogStore.ApiCallLocalStorage mApiCallLocalStorage,
                                   ApptransidLogEntityDataMapper mMapper) {
        this.mLocalStorage = mLocalStorage;
        this.mTimingLocalStorage = mTimingLocalStorage;
        this.mApiCallLocalStorage = mApiCallLocalStorage;
        this.mMapper = mMapper;
    }

    @Override
    public Observable<Boolean> putApiCall(ZPApptransidLogApiCall val) {
        return ObservableHelper.makeObservable(() -> {
            mApiCallLocalStorage.put(mMapper.transformApiCall(val));
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> put(ZPApptransidLog val) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.updateLog(mMapper.transform(val));

            ApptransidLogTimingGD timingGD = mMapper.transformTiming(val);
            if (timingGD != null) {
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
            List<ApptransidLogApiCallGD> apiCallList = mApiCallLocalStorage.get(apptransidLogGD.apptransid);
            return mMapper.transform(apptransidLogGD, timingList, apiCallList);
        });
    }

    @Override
    public Observable<JSONArray> getWithStatusDone() {
        return ObservableHelper.makeObservable(() -> {
            JSONArray array = new JSONArray();
            List<ApptransidLogGD> list = mLocalStorage.getWithStatusDone();
            for (int i = 0; i < list.size(); i++) {
                ApptransidLogGD entity = list.get(i);
                if (entity == null) {
                    continue;
                }
                List<ApptransidLogTimingGD> timingList = mTimingLocalStorage.get(entity.apptransid);
                List<ApptransidLogApiCallGD> apiCallList = mApiCallLocalStorage.get(entity.apptransid);
                array.put(mMapper.transform(entity, timingList, apiCallList));
            }
            return array;
        });
    }

    @Override
    public Observable<Boolean> remove(String apptransid) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(apptransid);
            mTimingLocalStorage.delete(apptransid);
            mApiCallLocalStorage.delete(apptransid);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeWithStatusDone() {
        return ObservableHelper.makeObservable(() -> {
            List<ApptransidLogGD> list = mLocalStorage.getWithStatusDone();
            for (int i = 0; i < list.size(); i++) {
                ApptransidLogGD entity = list.get(i);
                if (entity == null) {
                    continue;
                }
                mTimingLocalStorage.delete(entity.apptransid);
                mApiCallLocalStorage.delete(entity.apptransid);
            }
            mLocalStorage.deleteWithStatusDone();
            return Boolean.TRUE;
        });
    }
}
