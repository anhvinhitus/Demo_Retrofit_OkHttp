package vn.com.vng.zalopay.location;

/**
 * Created by khattn on 3/21/17.
 * Model of user's current location
 */

public class UserLocation {
    public double latitude;

    public double longitude;

    public String address;

    public long timeget;

    public UserLocation() {

    }

    public UserLocation(double latitude, double longitude, String address, long timeget) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.timeget = timeget;
    }
}
