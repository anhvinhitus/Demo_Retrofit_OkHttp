package vn.com.vng.zalopay.react.redpacket;

import android.app.Activity;
import android.content.Intent;

import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;

/**
 * Created by longlv on 19/07/2016.
 * define methods that RedPacket will use to pay
 */
public interface IRedPacketPayService {
    void pay(Activity activity, BundleOrder bundleOrder, RedPacketPayListener listener);
//    void payPendingOrder();
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
