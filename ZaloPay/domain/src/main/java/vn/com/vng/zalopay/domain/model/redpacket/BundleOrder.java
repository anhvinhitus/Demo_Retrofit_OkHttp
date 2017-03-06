package vn.com.vng.zalopay.domain.model.redpacket;

import vn.com.vng.zalopay.domain.model.Order;

/**
 * Created by longlv on 13/07/2016.
 * *
 */
public class BundleOrder extends Order {

    public long bundleId;

    public BundleOrder(long appid, String zptranstoken,
                       String apptransid, String appuser,
                       long apptime, String embeddata,
                       String item, long amount,
                       String description, String payoption,
                       String mac, long bundleId) {

        super(appid, zptranstoken, apptransid, appuser, apptime, embeddata, item, amount, description, payoption, mac);
        this.bundleId = bundleId;
    }

    @Override
    public String toString() {
        return super.toString() + bundleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BundleOrder that = (BundleOrder) o;

        return bundleId == that.bundleId;

    }
}
