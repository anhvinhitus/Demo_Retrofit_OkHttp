package vn.com.vng.zalopay.network;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import timber.log.Timber;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by huuhoa on 1/16/17.
 * Hold map between merchant api path to tracker event
 */

public class MerchantApiMap {
    private static Map<String, Integer> sApiTopupMapEvent;
    public static String sHostTopup = "sbtopup.zalopay.vn";

    static {
        sApiTopupMapEvent = new HashMap<>();
        sApiTopupMapEvent.put("/createorder", ZPEvents.API_TOPUP_CREATEORDER);
        sApiTopupMapEvent.put("/getpricelist", ZPEvents.API_TOPUP_GETPRICELIST);
        sApiTopupMapEvent.put("/getrecentlist", ZPEvents.API_TOPUP_GETRECENTLIST);
        sApiTopupMapEvent.put("/gettransbyzptransid", ZPEvents.API_TOPUP_GETTRANSBYZPTRANSID);
        sApiTopupMapEvent.put("/updateorder", ZPEvents.API_TOPUP_UPDATEORDER);
    }

    static int getEventIdByRequest(Request request) {
        String host = request.url().host();
        if (!sHostTopup.equals(host)) {
            return -1;
        }

        String path = request.url().encodedPath();
        Timber.d("API Request: path %s", path);
        if (!sApiTopupMapEvent.containsKey(path)) {
            return -1;
        }

        Timber.d("Found API Request");

        return sApiTopupMapEvent.get(path);
    }
}
