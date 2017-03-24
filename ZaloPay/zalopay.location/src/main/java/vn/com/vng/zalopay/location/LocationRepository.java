package vn.com.vng.zalopay.location;

import android.text.TextUtils;

import vn.com.vng.zalopay.data.cache.global.LocationLogGD;

/**
 * Created by khattn on 3/22/17.
 * Class helps presenter call function save location to cache
 */

public class LocationRepository implements LocationStore.Repository {
    private final LocationStore.LocalStorage mLocalStore;
    private final LocationDataMapper mMapper;

    public LocationRepository(LocationStore.LocalStorage localStorage,
                              LocationDataMapper mMapper) {
        this.mLocalStore = localStorage;
        this.mMapper = mMapper;
    }

    @Override
    public Boolean saveLocationCache(double latitude, double longitude, String address, long timeget) {
        if (latitude == 0 && longitude == 0 && TextUtils.isEmpty(address)) {
            return Boolean.FALSE;
        }
        mLocalStore.save(mMapper.transform(new AppLocation(latitude, longitude, address, timeget)));
        return Boolean.TRUE;
    }

    @Override
    public AppLocation getLocationCache() {
        LocationLogGD location = mLocalStore.get();
        return mMapper.transform(location);
    }
}
