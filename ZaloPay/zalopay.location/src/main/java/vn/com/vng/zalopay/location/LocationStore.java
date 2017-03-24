package vn.com.vng.zalopay.location;

import vn.com.vng.zalopay.data.cache.global.LocationLogGD;

/**
 * Created by khattn on 3/22/17.
 * Interface of class helps save and get location from cache
 */

public interface LocationStore {

    interface LocalStorage {
        void save(LocationLogGD newLocation);

        LocationLogGD get();
    }

    interface Repository {
        Boolean saveLocationCache(double latitude, double longitude, String address, long timeget);

        AppLocation getLocationCache();
    }
}
