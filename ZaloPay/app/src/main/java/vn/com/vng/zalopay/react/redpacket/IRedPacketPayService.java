package vn.com.vng.zalopay.react.redpacket;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;

/**
 * Created by longlv on 19/07/2016.
 * define methods that RedPacket will use to pay
 */
public interface IRedPacketPayService {
    void pay(Activity activity, BundleOrder bundleOrder, RedPacketPayListener listener);
    void payPendingOrder();
}
