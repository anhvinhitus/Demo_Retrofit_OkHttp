package vn.com.vng.zalopay.data.location;

import android.text.TextUtils;

import java.util.Map;

import vn.com.vng.zalopay.domain.model.UserLocation;

/**
 * Created by khattn on 3/22/17.
 * Class helps presenter call function save location to cache
 */

public class LocationRepository implements LocationStore.Repository {
    private final LocationStore.LocalStorage mLocalStore;

    public LocationRepository(LocationStore.LocalStorage localStorage) {
        this.mLocalStore = localStorage;
    }

    @Override
    public Boolean saveLocationCache(double latitude, double longitude, String address, long timeget) {
        if (latitude == 0 && longitude == 0 && TextUtils.isEmpty(address)) {
            return Boolean.FALSE;
        }
        mLocalStore.save(latitude, longitude, address, timeget);
        return Boolean.TRUE;
    }

    @Override
    public UserLocation getLocationCache() {
        Map<String, String> map = mLocalStore.get();
        UserLocation location = new UserLocation();
        try {
            location.latitude = Double.parseDouble(map.get("latitude"));
            location.longitude = Double.parseDouble(map.get("longitude"));
            location.address = map.get("address");
            location.timeget = Long.parseLong(map.get("timeget"));
        } catch (Exception e) {
            return null;
        }
        return location;
    }
}
