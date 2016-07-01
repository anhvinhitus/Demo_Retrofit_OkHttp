package vn.com.vng.zalopay;

import java.util.HashMap;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 7/1/16.
 */
public class ReactAppConfig {

    public static final HashMap<Integer, AppResource> APP_RESOURCE_MAP;

    static {
        APP_RESOURCE_MAP = new HashMap<>();
        APP_RESOURCE_MAP.put(1, new AppResource(1, AndroidApplication.instance().getString(R.string.transfer_money), String.valueOf(R.drawable.ic_chuyentien)));
        APP_RESOURCE_MAP.put(11, new AppResource(11, AndroidApplication.instance().getString(R.string.recharge_money_phone), String.valueOf(R.drawable.ic_naptiendt)));
        APP_RESOURCE_MAP.put(12, new AppResource(12, AndroidApplication.instance().getString(R.string.buy_phone_card), String.valueOf(R.drawable.ic_muathedt)));
        APP_RESOURCE_MAP.put(14, new AppResource(14, AndroidApplication.instance().getString(R.string.zing_xu), String.valueOf(R.drawable.ic_zingxu)));

              /*  new AppResource(13, getString(R.string.buy_game_card), String.valueOf(R.drawable.ic_muathegame)),
                new AppResource(3, getString(R.string.electric_bill), String.valueOf(R.drawable.ic_tiendien), 1),
                new AppResource(4, getString(R.string.internet_bill), String.valueOf(R.drawable.ic_internet), 1),
                new AppResource(5, getString(R.string.red_envelope), String.valueOf(R.drawable.ic_lixi), 1),
                new AppResource(6, getString(R.string.water_bill), String.valueOf(R.drawable.ic_tiennuoc), 1)*/
    }

    public static AppResource getAppResource(int appId) {
        return APP_RESOURCE_MAP.get(appId);
    }

}
