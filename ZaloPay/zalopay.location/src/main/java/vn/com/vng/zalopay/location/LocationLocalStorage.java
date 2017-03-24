package vn.com.vng.zalopay.location;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.data.cache.global.LocationLogGD;

/**
 * Created by khattn on 3/22/17.
 * Class helps save and get location from cache
 */

public class LocationLocalStorage implements LocationStore.LocalStorage {
    private DaoSession mDaoSession;

    public LocationLocalStorage(DaoSession daoSession) {
        mDaoSession = daoSession;
    }

    @Override
    public void save(LocationLogGD newLocation) {
        LocationLogGD locationLogGD = new LocationLogGD();
        locationLogGD.latitude = newLocation.latitude;
        locationLogGD.longitude = newLocation.longitude;
        locationLogGD.address = newLocation.address;
        locationLogGD.timeget = newLocation.timeget;

        try {
            mDaoSession.getLocationLogGDDao().insertOrReplaceInTx(locationLogGD);
        } catch (Exception e) {
            Timber.d(e, "Save location error");
        }
    }

    @Override
    public LocationLogGD get() {
        List<LocationLogGD> list = mDaoSession.getLocationLogGDDao()
                .queryBuilder()
                .list();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(list.size() - 1);
    }
}
