package vn.com.vng.zalopay.location;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.cache.global.LocationLogGD;

/**
 * Created by khattn on 3/24/17.
 * Class map location data in db and app
 */

@Singleton
public class LocationDataMapper {
    @Inject
    public LocationDataMapper() {

    }

    public LocationLogGD transform(AppLocation data) {
        if (data == null) {
            return null;
        }

        LocationLogGD log = new LocationLogGD();
        log.latitude = data.latitude;
        log.longitude = data.longitude;
        log.address = data.address;
        log.timeget = data.timeget;
        return log;
    }

    public AppLocation transform(LocationLogGD data) {
        if (data == null) {
            return null;
        }

        AppLocation location = new AppLocation();
        location.latitude = data.latitude;
        location.longitude = data.longitude;
        location.address = data.address;
        location.timeget = data.timeget;
        return location;
    }
}
