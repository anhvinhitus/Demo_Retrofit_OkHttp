package vn.com.vng.zalopay.paymentapps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 7/1/16.
 * Static configuration for PaymentApps
 */
public class PaymentAppConfig {

    public static class Constants {
        public static final int TRANSFER_MONEY = 1;
        public static final int RECEIVE_MONEY = 3;
        public static final int RED_PACKET = 6;
        public static final int RECHARGE_MONEY_PHONE = 11;
        public static final int BUY_PHONE_CARD = 12;
        public static final int SERVICE = 15;
        public static final int ELECTRIC_BILL = 17;
    }

    static final HashMap<Integer, AppResource> APP_RESOURCE_MAP;
    public static final List<AppResource> APP_RESOURCE_LIST;

    static {
        APP_RESOURCE_MAP = new HashMap<>();
        APP_RESOURCE_MAP.put(Constants.TRANSFER_MONEY,
                new AppResource(Constants.TRANSFER_MONEY,
                        PaymentAppTypeEnum.NATIVE.value,
                        AndroidApplication.instance().getString(R.string.transfer_money),
                        String.valueOf(R.drawable.ic_chuyentien)));
        APP_RESOURCE_MAP.put(Constants.RECEIVE_MONEY,
                new AppResource(Constants.RECEIVE_MONEY,
                        PaymentAppTypeEnum.NATIVE.value,
                        AndroidApplication.instance().getString(R.string.receive_money),
                        String.valueOf(R.drawable.ic_nhantien)));
        APP_RESOURCE_MAP.put(Constants.RED_PACKET,
                new AppResource(Constants.RED_PACKET,
                        PaymentAppTypeEnum.NATIVE.value,
                        AndroidApplication.instance().getString(R.string.red_envelope),
                        String.valueOf(R.drawable.ic_lixi)));

        APP_RESOURCE_LIST = new ArrayList<>();
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.TRANSFER_MONEY));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.RECEIVE_MONEY));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.RED_PACKET));

    }

    public static AppResource getAppResource(int appId) {
        return APP_RESOURCE_MAP.get(appId);
    }

}
