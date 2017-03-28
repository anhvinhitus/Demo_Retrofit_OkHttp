package vn.com.vng.zalopay.location;

import android.text.TextUtils;

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
        mLocalStore.save(new AppLocation(latitude, longitude, address, timeget));
        return Boolean.TRUE;
    }

    @Override
    public AppLocation getLocationCache() {
        return mLocalStore.get();
    }
}
