package vn.com.vng.zalopay.scanners.ui.beacon;

/**
 * Created by huuhoa on 6/4/16.
 * Hold beacon information
 */
public class BeaconDevice {
    public final String id;
    public final byte[] content;
    public final int rssi;

    public BeaconDevice(String id, byte[] content, int rssi) {
        this.id = id;
        this.content = content;
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BeaconDevice) {
            return id.equalsIgnoreCase(((BeaconDevice)o).id);
        }
        return super.equals(o);
    }
}
