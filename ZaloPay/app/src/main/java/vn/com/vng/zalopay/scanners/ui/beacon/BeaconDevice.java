package vn.com.vng.zalopay.scanners.ui.beacon;

import vn.com.vng.zalopay.scanners.controller.PaymentRecord;

/**
 * Created by huuhoa on 6/4/16.
 * Hold beacon information
 */
public class BeaconDevice {
    public final String id;
    public final int rssi;
    public final PaymentRecord paymentRecord;

    public BeaconDevice(String id, int rssi, PaymentRecord paymentRecord) {
        this.id = id;
        this.paymentRecord = paymentRecord;
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
