package vn.com.vng.zalopay.location;

import android.util.LruCache;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.AppStorage;
import vn.com.vng.zalopay.data.util.Utils;

/**
 * Created by khattn on 3/22/17.
 * Class helps save and get location
 */

public class LocationLocalStorage implements LocationStore.LocalStorage {
    private static final String LOCATION_TIMESTAMP = "location:timestamp";
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
            multi.put(LOCATION_TIMESTAMP, String.valueOf(newLocation.timestamp));
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
                LOCATION_TIMESTAMP, LOCATION_LATITUDE, LOCATION_LONGITUDE
        );

        if (item == null) {
            return new AppLocation();
        }

        double latitude = Double.valueOf(Utils.mapGetOrDefault(item, LOCATION_LATITUDE, "0"));
        double longitude = Double.valueOf(Utils.mapGetOrDefault(item, LOCATION_LONGITUDE, "0"));
        long timestamp = Long.valueOf(Utils.mapGetOrDefault(item, LOCATION_TIMESTAMP, "0"));
        return new AppLocation(latitude, longitude, timestamp);
    }
}
