package vn.com.vng.zalopay.location;

/**
 * Created by khattn on 3/21/17.
 * Model of user's current location
 */

public class AppLocation {
    public double latitude;

    public double longitude;

    public long timestamp;

    public AppLocation() {
        this.latitude = 0;
        this.longitude = 0;
        this.timestamp = 0;
    }

    public AppLocation(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
