package vn.com.vng.zalopay.data.net.adapter;

import java.util.HashMap;
import java.util.Map;

import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by huuhoa on 1/16/17.
 * Hold map between merchant api path to tracker event
 */

class MerchantApiMap {
    static Map<String, Integer> gApiMapEvent;

    static {
        gApiMapEvent = new HashMap<>();
        gApiMapEvent.put("/esale/zalopayshop/v4/getshopitemlist", ZPEvents.ESALE_API_V4_GETSHOPITEMLIST);
        gApiMapEvent.put("/esale/zalopayshop/v4/createorder", ZPEvents.ESALE_API_V4_CREATEORDER);
        gApiMapEvent.put("/esale/zalopayshop/v4/gethistory", ZPEvents.ESALE_API_V4_GETHISTORY);
        gApiMapEvent.put("/esale/zalopayshop/v4/getresult", ZPEvents.ESALE_API_V4_GETRESULT);
        gApiMapEvent.put("/esale/zalopayshop/v4/carddetail", ZPEvents.ESALE_API_V4_CARDDETAIL);
        gApiMapEvent.put("/esale/zalopayshop/v4/getproviders", ZPEvents.ESALE_API_V4_GETPROVIDERS);
        gApiMapEvent.put("/esale/zalopayshop/v4/querybill", ZPEvents.ESALE_API_V4_QUERYBILL);
    }
}
