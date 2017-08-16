package vn.com.vng.zalopay.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;

import rx.Observer;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ObservableHelper;

/**
 * Created by khattn on 3/20/17.
 * Class helps track user's location by GPS or network
 */

public class LocationProvider extends Service {
    private final static int TIME_REFRESH = 300000;
    private static LocationStore.RepositoryFactory mRepositoryFactory;
    private static Context mApplicationContext;

    private static Location location;

    private static double latitude;
    private static double longitude;

    public static void init(LocationStore.RepositoryFactory repositoryFactory, Context applicationContext) {
        mRepositoryFactory = repositoryFactory;
        mApplicationContext = applicationContext;
    }

    public static Boolean findLocation() {
        boolean canGetLocation = false;
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
                        Timber.w("Get location by network failed with: %s", e.getMessage());
                    }
                }

                if (checkGPS) {
                    try {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        updateCoordinates();
                        Timber.d("Get location by gps with lat: %s, long: %s", latitude, longitude);
                    } catch (SecurityException e) {
                        Timber.w("Get location by gps failed with: %s", e.getMessage());
                    }
                }

                saveLocation();
            }
        } catch (Exception e) {
            Timber.w("Get location failed with: %s", e.getMessage());
        }

        return canGetLocation;
    }

    private static void updateCoordinates() throws Exception {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static void saveLocation() {
        if (mRepositoryFactory == null || mRepositoryFactory.get() == null) {
            return;
        }
        mRepositoryFactory.get().saveLocation(latitude, longitude, System.currentTimeMillis()).subscribe();
    }

    public static void updateLocation() {
        AppLocation location = getLocation();
        if (location == null || Math.abs(System.currentTimeMillis() - location.timestamp) > TIME_REFRESH) {
            ObservableHelper.makeObservable(LocationProvider::findLocation).subscribe(new Observer<Boolean>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Boolean aBoolean) {
                    if (aBoolean && latitude != 0 || longitude != 0) {
                        saveLocation();
                    }
                }
            });
        }
    }

    public static AppLocation getLocation() {
        if (mRepositoryFactory == null || mRepositoryFactory.get() == null) {
            return null;
        }

        AppLocation location = mRepositoryFactory.get().getLocation();

        if ((location.latitude == 0 && location.longitude == 0) ||
                ((System.currentTimeMillis() - location.timestamp) > TIME_REFRESH)) {
            return null;
        }
        return location;
    }
}
