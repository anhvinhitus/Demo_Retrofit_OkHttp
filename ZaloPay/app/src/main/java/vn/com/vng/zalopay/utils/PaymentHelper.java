package vn.com.vng.zalopay.utils;

import android.text.TextUtils;

import vn.com.vng.zalopay.domain.model.Order;

/**
 * Created by longlv on 2/9/17.
 * Methods support pay.
 */

public class PaymentHelper {

    public static boolean validOrder(Order order) {
        return order != null
                && order.appid >= 0
                && !TextUtils.isEmpty(order.apptransid)
                && !TextUtils.isEmpty(order.appuser)
                && order.apptime > 0
                && !TextUtils.isEmpty(order.item)
                && order.amount >= 0
                && !TextUtils.isEmpty(order.description)
                && !TextUtils.isEmpty(order.mac);
    }
}
