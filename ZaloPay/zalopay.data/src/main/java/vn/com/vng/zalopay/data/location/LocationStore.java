package vn.com.vng.zalopay.data.location;

import java.util.Map;

import vn.com.vng.zalopay.domain.model.UserLocation;

/**
 * Created by khattn on 3/22/17.
 * Interface of class helps save and get location from cache
 */

public interface LocationStore {

    interface LocalStorage {
        void save(double latitude, double longitude, String address, long timeget);

        Map<String, String> get();
    }

    interface Repository {
        Boolean saveLocationCache(double latitude, double longitude, String address, long timeget);

        UserLocation getLocationCache();
    }
}
