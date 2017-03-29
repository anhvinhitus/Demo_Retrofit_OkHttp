package vn.com.vng.zalopay.location;

import android.util.LruCache;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ObservableHelper;

/**
 * Created by khattn on 3/22/17.
 * Class helps presenter call function save location
 */

public class LocationRepository implements LocationStore.Repository {
    private static final String LOCATION = "location";

    private final LocationStore.LocalStorage mLocalStore;
    private LruCache<String, AppLocation> mLocationCache = new LruCache<>(1);

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
            // save cache
            mLocationCache.put(LOCATION, location);
            // save storage
            mLocalStore.save(location);
            return Boolean.TRUE;
        });
    }

    @Override
    public AppLocation getLocation() {
        // get cache
        AppLocation location = mLocationCache.get(LOCATION);
        Timber.d("location in cache: %s", location == null ? "null" : "not null");

        if (location == null || location.latitude == 0 && location.longitude == 0) {
            // get storage
            location = mLocalStore.get();
        }
        return location;
    }
}
