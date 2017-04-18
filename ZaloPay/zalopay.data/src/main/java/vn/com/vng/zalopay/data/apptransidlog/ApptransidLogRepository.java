package vn.com.vng.zalopay.data.apptransidlog;

import java.util.Map;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.ApptransidLogEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.ApptransidLogEntityDataMapper;
import vn.com.vng.zalopay.data.util.ObservableHelper;

/**
 * Created by khattn on 1/24/17.
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
    public Observable<Void> put(Map<String, String> val) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.updateLog(mMapper.transform(val));
            return null;
        });
    }

    @Override
    public Observable<ApptransidLogEntity> get(String apptransid) {
        return ObservableHelper.makeObservable(() -> mLocalStorage.get(apptransid))
                .map(mMapper::transform);
    }

    @Override
    public Observable<Void> remove(String apptransid) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.delete(apptransid);
            return null;
        });
    }
}
