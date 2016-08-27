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
        public static final int INTERNAL = 1;
        public static final int RECEIVE_MONEY = 3;
        public static final int RED_PACKET = 6;
        public static final int RECHARGE_MONEY_PHONE = 11;
        public static final int BUY_PHONE_CARD = 12;
        public static final int ZING_XU = 14;
        public static final int BUY_GAME_CARD = 15;
        public static final int ELECTRIC_BILL = 17;
    }

    static final HashMap<Integer, AppResource> APP_RESOURCE_MAP;
    public static final List<AppResource> APP_RESOURCE_LIST;

    static {
        APP_RESOURCE_MAP = new HashMap<>();
        APP_RESOURCE_MAP.put(Constants.INTERNAL, new AppResource(Constants.INTERNAL, AndroidApplication.instance().getString(R.string.transfer_money), String.valueOf(R.drawable.ic_chuyentien)));
        APP_RESOURCE_MAP.put(Constants.RED_PACKET, new AppResource(Constants.RED_PACKET, AndroidApplication.instance().getString(R.string.red_envelope), String.valueOf(R.drawable.ic_lixi)));
        APP_RESOURCE_MAP.put(Constants.RECHARGE_MONEY_PHONE, new AppResource(Constants.RECHARGE_MONEY_PHONE, AndroidApplication.instance().getString(R.string.recharge_money_phone), String.valueOf(R.drawable.ic_naptiendt)));
        APP_RESOURCE_MAP.put(Constants.RECEIVE_MONEY, new AppResource(Constants.RECEIVE_MONEY, AndroidApplication.instance().getString(R.string.receive_money), String.valueOf(R.drawable.ic_muathedt)));
        APP_RESOURCE_MAP.put(Constants.ZING_XU, new AppResource(Constants.ZING_XU, AndroidApplication.instance().getString(R.string.zing_xu), String.valueOf(R.drawable.ic_zingxu)));
        APP_RESOURCE_MAP.put(Constants.BUY_GAME_CARD, new AppResource(Constants.BUY_GAME_CARD, AndroidApplication.instance().getString(R.string.buy_game_card), String.valueOf(R.drawable.ic_muathegame)));
        APP_RESOURCE_MAP.put(Constants.ELECTRIC_BILL, new AppResource(Constants.ELECTRIC_BILL, AndroidApplication.instance().getString(R.string.electric_bill), String.valueOf(R.drawable.ic_tiendien)));

        APP_RESOURCE_LIST = new ArrayList<>();
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.INTERNAL));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.RED_PACKET));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.RECHARGE_MONEY_PHONE));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.RECEIVE_MONEY));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.ZING_XU));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.BUY_GAME_CARD));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.ELECTRIC_BILL));

    }

    public static AppResource getAppResource(int appId) {
        return APP_RESOURCE_MAP.get(appId);
    }

}
