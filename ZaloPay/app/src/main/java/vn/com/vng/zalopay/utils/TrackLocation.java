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
    private static LocationStore.Repository mRepository;

    private static Location location;

    private static double latitude;
    private static double longitude;
    private static Address address;
    private static boolean canGetLocation = false;

    public static void init(LocationStore.Repository repository) {
        mRepository = repository;
    }

    public static void findLocation(Context context) {
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
                        updateCoordinates(context);
                        Timber.d("Get location by network with lat: %s, long: %s", latitude, longitude);
                    } catch (SecurityException e) {
                        Timber.e("Get location by network failed with: %s", e.getMessage());
                    }
                }

                if (checkGPS) {
                    if (location == null) {
                        try {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            updateCoordinates(context);
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

    private static void updateCoordinates(Context context) throws Exception {
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

    private static String getAddress() {
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

    private static void saveLocation() {
        if(mRepository == null) {
            return;
        }
        mRepository.saveLocationCache(latitude, longitude, getAddress(), System.currentTimeMillis());
    }

    private static UserLocation getUpdateLocation(Context context, UserLocation location) {
        if (Math.abs(System.currentTimeMillis() - location.timeget) > TIME_REFRESH) {
            findLocation(context);
            if (canGetLocation) {
                saveLocation();
                return new UserLocation(latitude, longitude, getAddress(), System.currentTimeMillis());
            }
            return null;
        }
        return location;
    }

    public static UserLocation getLocation(Context context) {
        if(mRepository == null) {
            return null;
        }
        UserLocation location = mRepository.getLocationCache();
        return getUpdateLocation(context, location);
    }
}
