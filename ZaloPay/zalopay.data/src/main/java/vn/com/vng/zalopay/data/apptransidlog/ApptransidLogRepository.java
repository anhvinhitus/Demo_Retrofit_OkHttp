package vn.com.vng.zalopay.data.apptransidlog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.ApptransidLogEntityDataMapper;
import vn.com.vng.zalopay.data.cache.global.ApptransidLogGD;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.zalopay.analytics.ZPApptransidLog;

/**
 * Created by khattn on 1/24/17.
 * Apptransid
 */

public class ApptransidLogRepository implements ApptransidLogStore.Repository {
    private final ApptransidLogStore.LocalStorage mLocalStorage;
    private final ApptransidLogEntityDataMapper mMapper;

    public ApptransidLogRepository(ApptransidLogStore.LocalStorage mLocalStorage,
                                   ApptransidLogEntityDataMapper mMapper) {
        this.mLocalStorage = mLocalStorage;
        this.mMapper = mMapper;
    }

    @Override
    public Observable<Boolean> put(ZPApptransidLog val) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.updateLog(mMapper.transform(val));
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<JSONObject> get(String apptransid) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.get(apptransid)).map(mMapper::transform);
    }

    @Override
    public Observable<JSONArray> getAll() {
        return ObservableHelper.makeObservable(() -> {
            JSONArray array = new JSONArray();
            List<ApptransidLogGD> list = mLocalStorage.getAll();
            for (int i = 0; i < list.size(); i++) {
                array.put(mMapper.transform(list.get(i)));
            }
            return array;
        });
    }

    @Override
    public Observable<Boolean> remove(String apptransid) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(apptransid);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<Boolean> removeAll() {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.deleteAll();
            return Boolean.TRUE;
        });
    }
}
