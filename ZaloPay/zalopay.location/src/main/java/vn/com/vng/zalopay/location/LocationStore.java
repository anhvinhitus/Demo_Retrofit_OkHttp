package vn.com.vng.zalopay.location;

import rx.Observable;

/**
 * Created by khattn on 3/22/17.
 * Interface of class helps save and get location from cache
 */

public interface LocationStore {

    interface LocalStorage {
        void saveCache(AppLocation newLocation);

        void saveStorage(AppLocation newLocation);

        AppLocation getCache();

        AppLocation getStorage();
    }

    interface Repository {
        Observable<Boolean> saveLocation(double latitude, double longitude, long timeget);

        AppLocation getLocation();
    }
}
