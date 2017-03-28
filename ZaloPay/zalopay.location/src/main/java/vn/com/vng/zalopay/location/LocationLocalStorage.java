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
    private AppStorage mAppStorage;

    public LocationLocalStorage(AppStorage appStorage) {
        mAppStorage = appStorage;
    }

    @Override
    public void save(AppLocation newLocation) {
        try {
            Map<String, String> multi = new HashMap<>();
            multi.put("location:timestamp", String.valueOf(newLocation.timeget));
            multi.put("location:address", newLocation.address);
            multi.put("location:latitude", String.valueOf(newLocation.latitude));
            multi.put("location:longitude", String.valueOf(newLocation.longitude));
            mAppStorage.putAll(multi);
        } catch (Exception e) {
            Timber.d(e, "Save location error");
        }
    }

    @Override
    public AppLocation get() {
        Map<String, String> item = mAppStorage.getAll(
            "location:timestamp", "location:address", "location:latitude", "location:longitude"
        );

        if (item == null) {
            return null;
        }

        double latitude = Double.valueOf(item.getOrDefault("location:latitude", "0"));
        double longitude = Double.valueOf(item.getOrDefault("location:longitude", "0"));
        String address = item.getOrDefault("location:address", "");
        long timestamp = Long.valueOf(item.getOrDefault("location:timestamp", "0"));
        return new AppLocation(latitude, longitude, address, timestamp);
    }
}
