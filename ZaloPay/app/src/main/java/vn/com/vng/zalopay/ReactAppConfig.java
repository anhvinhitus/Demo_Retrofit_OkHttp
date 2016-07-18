package vn.com.vng.zalopay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 7/1/16.
 *
 */
public class ReactAppConfig {

    static final HashMap<Integer, AppResource> APP_RESOURCE_MAP;
    public static final List<AppResource> APP_RESOURCE_LIST;

    static {
        APP_RESOURCE_MAP = new HashMap<>();
        APP_RESOURCE_MAP.put(Constants.Apps.INTERNAL, new AppResource(Constants.Apps.INTERNAL, AndroidApplication.instance().getString(R.string.transfer_money), String.valueOf(R.drawable.ic_chuyentien)));
        APP_RESOURCE_MAP.put(Constants.Apps.RED_PACKET, new AppResource(Constants.Apps.RED_PACKET, AndroidApplication.instance().getString(R.string.red_envelope), String.valueOf(R.drawable.ic_lixi)));
        APP_RESOURCE_MAP.put(Constants.Apps.RECHARGE_MONEY_PHONE, new AppResource(Constants.Apps.RECHARGE_MONEY_PHONE, AndroidApplication.instance().getString(R.string.recharge_money_phone), String.valueOf(R.drawable.ic_naptiendt)));
//        APP_RESOURCE_MAP.put(Constants.Apps.BUY_PHONE_CARD, new AppResource(Constants.Apps.BUY_PHONE_CARD, AndroidApplication.instance().getString(R.string.buy_phone_card), String.valueOf(R.drawable.ic_muathedt)));
        APP_RESOURCE_MAP.put(Constants.Apps.ZING_XU, new AppResource(Constants.Apps.ZING_XU, AndroidApplication.instance().getString(R.string.zing_xu), String.valueOf(R.drawable.ic_zingxu)));
        APP_RESOURCE_MAP.put(Constants.Apps.BUY_GAME_CARD, new AppResource(Constants.Apps.BUY_GAME_CARD, AndroidApplication.instance().getString(R.string.buy_game_card), String.valueOf(R.drawable.ic_muathegame)));
        APP_RESOURCE_MAP.put(Constants.Apps.ELECTRIC_BILL, new AppResource(Constants.Apps.ELECTRIC_BILL, AndroidApplication.instance().getString(R.string.electric_bill), String.valueOf(R.drawable.ic_tiendien)));

        APP_RESOURCE_LIST = new ArrayList<>();
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.Apps.INTERNAL));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.Apps.RED_PACKET));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.Apps.RECHARGE_MONEY_PHONE));
//        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.Apps.BUY_PHONE_CARD));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.Apps.ZING_XU));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.Apps.BUY_GAME_CARD));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.Apps.ELECTRIC_BILL));

              /*  new AppResource(13, getString(R.string.buy_game_card), String.valueOf(R.drawable.ic_muathegame)),
                new AppResource(4, getString(R.string.internet_bill), String.valueOf(R.drawable.ic_internet), 1),
                new AppResource(6, getString(R.string.water_bill), String.valueOf(R.drawable.ic_tiennuoc), 1)*/
    }

    public static AppResource getAppResource(int appId) {
        return APP_RESOURCE_MAP.get(appId);
    }

}
