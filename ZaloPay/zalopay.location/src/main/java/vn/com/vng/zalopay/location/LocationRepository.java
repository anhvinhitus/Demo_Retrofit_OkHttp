package vn.com.vng.zalopay.location;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ObservableHelper;

/**
 * Created by khattn on 3/22/17.
 * Class helps presenter call function save location
 */

public class LocationRepository implements LocationStore.Repository {
    private final LocationStore.LocalStorage mLocalStore;

    public LocationRepository(LocationStore.LocalStorage localStorage) {
        this.mLocalStore = localStorage;
    }

    @Override
    public Observable<Boolean> saveLocation(double latitude, double longitude, long timestamp) {
        return ObservableHelper.makeObservable(() -> {
            if (latitude == 0 && longitude == 0) {
                return Boolean.FALSE;
            }

            AppLocation location = new AppLocation(latitude, longitude, timestamp);
            mLocalStore.saveCache(location);
            mLocalStore.saveStorage(location);
            return Boolean.TRUE;
        });
    }

    @Override
    public AppLocation getLocation() {
        AppLocation location = mLocalStore.getCache();
        Timber.d("location in cache: %s", location == null ? "null" : "not null");
        if (location == null || location.latitude == 0 && location.longitude == 0) {
            location = mLocalStore.getStorage();
        }
        return location;
    }
}
