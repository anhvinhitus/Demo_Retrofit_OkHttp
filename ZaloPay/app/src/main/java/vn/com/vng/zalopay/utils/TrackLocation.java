package vn.com.vng.zalopay.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

import java.util.List;
import java.util.Locale;

import timber.log.Timber;
import vn.com.vng.zalopay.data.location.LocationStore;
import vn.com.vng.zalopay.domain.model.UserLocation;

/**
 * Created by khattn on 3/20/17.
 * Class helps track user's location by GPS or network
 */

public class TrackLocation extends Service {
    private final static int TIME_REFRESH = 300000;
    private final Context context;
    private final LocationStore.Repository repository;

    private Location location;

    private double latitude;
    private double longitude;
    private Address address;
    private boolean canGetLocation = false;

    public TrackLocation(Context context,
                         LocationStore.Repository repository) {
        this.context = context;
        this.repository = repository;
    }

    public void findLocation() {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            boolean checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean checkNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                canGetLocation = false;
                Timber.d("Get location failed with: no service provider available");
            } else {
                canGetLocation = true;
                if (checkNetwork) {
                    try {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        updateCoordinates();
                        Timber.d("Get location by network with lat: %s, long: %s", latitude, longitude);
                    } catch (SecurityException e) {
                        Timber.e("Get location by network failed with: %s", e.getMessage());
                    }
                }

                if (checkGPS) {
                    if (location == null) {
                        try {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            updateCoordinates();
                            Timber.d("Get location by gps with lat: %s, long: %s", latitude, longitude);
                        } catch (SecurityException e) {
                            Timber.e("Get location by gps failed with: %s", e.getMessage());
                        }
                    }
                }

                saveLocation();
            }
        } catch (Exception e) {
            Timber.e("Get location failed with: %s", e.getMessage());
        }
    }

    private void updateCoordinates() throws Exception {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                address = addresses.get(0);
            }
        }
    }

    private String getAddress() {
        if (address != null) {
            return String.format("%s, %s, %s, %s",
                    address.getAddressLine(0),
                    address.getAddressLine(1),
                    address.getAddressLine(2),
                    address.getAddressLine(3));
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveLocation() {
        repository.saveLocationCache(latitude, longitude, getAddress(), System.currentTimeMillis());
    }

    private UserLocation getUpdateLocation(UserLocation location) {
        if (Math.abs(System.currentTimeMillis() - location.timeget) > TIME_REFRESH) {
            findLocation();
            if (canGetLocation) {
                saveLocation();
                return new UserLocation(latitude, longitude, getAddress(), System.currentTimeMillis());
            }
            return null;
        }
        return location;
    }

    public UserLocation getLocation() {
        UserLocation location = repository.getLocationCache();
        return getUpdateLocation(location);
    }
}
