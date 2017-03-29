package vn.com.vng.zalopay.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import timber.log.Timber;

/**
 * Created by khattn on 3/20/17.
 * Class helps track user's location by GPS or network
 */

public class LocationProvider extends Service {
    private final static int TIME_REFRESH = 300000;
    private static LocationStore.Repository mRepository;
    private static Context mApplicationContext;

    private static Location location;

    private static double latitude;
    private static double longitude;
//    private static Address address;
    private static boolean canGetLocation = false;

    public static void init(LocationStore.Repository repository, Context applicationContext) {
        mRepository = repository;
        mApplicationContext = applicationContext;
    }

    public static void findLocation() {
        try {
            LocationManager locationManager = (LocationManager) mApplicationContext.getSystemService(LOCATION_SERVICE);
            boolean checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean checkNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                canGetLocation = false;
                Timber.d("No service provider available");
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
//                    if (location == null) {
                        try {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            updateCoordinates();
                            Timber.d("Get location by gps with lat: %s, long: %s", latitude, longitude);
                        } catch (SecurityException e) {
                            Timber.e("Get location by gps failed with: %s", e.getMessage());
                        }
//                    }
                }

                saveLocation();
            }
        } catch (Exception e) {
            Timber.e("Get location failed with: %s", e.getMessage());
        }
    }

    private static void updateCoordinates() throws Exception {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

//            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
//            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
//            if (addresses != null) {
//                address = addresses.get(0);
//            }
        }
    }

//    private static String getAddress() {
//        if (address != null) {
//            return String.format("%s, %s, %s, %s",
//                    address.getAddressLine(0),
//                    address.getAddressLine(1),
//                    address.getAddressLine(2),
//                    address.getAddressLine(3));
//        }
//        return null;
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static void saveLocation() {
        if(mRepository == null) {
            return;
        }
        mRepository.saveLocation(latitude, longitude, System.currentTimeMillis()).subscribe();
    }

    private static AppLocation getUpdateLocation(AppLocation location) {
        if (location == null || Math.abs(System.currentTimeMillis() - location.timestamp) > TIME_REFRESH) {
            findLocation();
            if (canGetLocation && latitude != 0 || longitude != 0) {
                saveLocation();
                return new AppLocation(latitude, longitude, System.currentTimeMillis());
            }
            return null;
        }
        return location;
    }

    public static AppLocation getLocation() {
        if(mRepository == null) {
            return null;
        }
        AppLocation location = mRepository.getLocation();
        Timber.d("location in get location: %s", location.latitude);
        return getUpdateLocation(location);
    }
}
