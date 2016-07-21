package vn.com.vng.zalopay.mdl.redpacket;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.BundleOrder;

/**
 * Created by longlv on 19/07/2016.
 * define methods that RedPacket will use to pay
 */
public interface IRedPacketPayService {
    void pay(Activity activity, BundleOrder bundleOrder, IRedPacketPayListener listener);
}
