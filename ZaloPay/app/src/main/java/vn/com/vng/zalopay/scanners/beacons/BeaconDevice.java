package vn.com.vng.zalopay.scanners.beacons;

import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.scanners.models.PaymentRecord;

/**
 * Created by huuhoa on 6/4/16.
 * Hold beacon information
 */
public class BeaconDevice {
    public final String id;
    public final int rssi;
    public final PaymentRecord paymentRecord;
    public Order order;
    public final long timestamp;

    public BeaconDevice(String id, int rssi, PaymentRecord paymentRecord, Order order) {
        this.id = id;
        this.paymentRecord = paymentRecord;
        this.rssi = rssi;
        this.order = order;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BeaconDevice) {
            return id.equalsIgnoreCase(((BeaconDevice) o).id);
        }
        return super.equals(o);
    }

    public BeaconDevice cloneWithOrder(Order newOrder) {
        return new BeaconDevice(id, rssi, paymentRecord, newOrder);
    }

    boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - timestamp > 4 * 1000);
    }
}
