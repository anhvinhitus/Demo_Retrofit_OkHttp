package vn.com.vng.zalopay.location;

/**
 * Created by khattn on 3/22/17.
 * Interface of class helps save and get location from cache
 */

public interface LocationStore {

    interface LocalStorage {
        void save(AppLocation newLocation);
        AppLocation get();
    }

    interface Repository {
        Boolean saveLocationCache(double latitude, double longitude, String address, long timeget);

        AppLocation getLocationCache();
    }
}
