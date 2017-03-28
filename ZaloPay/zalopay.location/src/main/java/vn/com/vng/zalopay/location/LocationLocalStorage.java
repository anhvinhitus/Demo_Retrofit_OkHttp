package vn.com.vng.zalopay.location;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.AppStorage;

/**
 * Created by khattn on 3/22/17.
 * Class helps save and get location from cache
 */

public class LocationLocalStorage implements LocationStore.LocalStorage {
    private static final String LOCATION_TIMESTAMP = "location:timestamp";
    private static final String LOCATION_ADDRESS = "location:address";
    private static final String LOCATION_LATITUDE = "location:latitude";
    private static final String LOCATION_LONGITUDE = "location:longitude";
    private AppStorage mAppStorage;

    public LocationLocalStorage(AppStorage appStorage) {
        mAppStorage = appStorage;
    }

    @Override
    public void save(AppLocation newLocation) {
        try {
            Map<String, String> multi = new HashMap<>();
            multi.put(LOCATION_TIMESTAMP, String.valueOf(newLocation.timeget));
            multi.put(LOCATION_ADDRESS, newLocation.address);
            multi.put(LOCATION_LATITUDE, String.valueOf(newLocation.latitude));
            multi.put(LOCATION_LONGITUDE, String.valueOf(newLocation.longitude));
            mAppStorage.putAll(multi);
        } catch (Exception e) {
            Timber.d(e, "Save location error");
        }
    }

    @Override
    public AppLocation get() {
        Map<String, String> item = mAppStorage.getAll(
            LOCATION_TIMESTAMP, LOCATION_ADDRESS, LOCATION_LATITUDE, LOCATION_LONGITUDE
        );

        if (item == null) {
            return null;
        }

        double latitude = Double.valueOf(item.getOrDefault(LOCATION_LATITUDE, "0"));
        double longitude = Double.valueOf(item.getOrDefault(LOCATION_LONGITUDE, "0"));
        String address = item.getOrDefault(LOCATION_ADDRESS, "");
        long timestamp = Long.valueOf(item.getOrDefault(LOCATION_TIMESTAMP, "0"));
        return new AppLocation(latitude, longitude, address, timestamp);
    }
}
