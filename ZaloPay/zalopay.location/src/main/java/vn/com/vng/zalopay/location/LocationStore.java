package vn.com.vng.zalopay.location;

import java.util.List;
import java.util.Map;

/**
 * Created by khattn on 3/22/17.
 * Interface of class helps save and get location from cache
 */

public interface LocationStore {

    interface LocalStorage {
        void save(double latitude, double longitude, String address, long timeget);

        Map<String, String> get();

        List<UserLocation> getListLocation();
    }

    interface Repository {
        Boolean saveLocationCache(double latitude, double longitude, String address, long timeget);

        UserLocation getLocationCache();

        List<UserLocation> getListLocation();
    }
}
