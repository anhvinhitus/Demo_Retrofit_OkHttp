package vn.com.zalopay.wallet.helper;

/**
 * Created by lytm on 28/06/2017.
 */

public class FormatHelper {
    public static String formatTransID(String pTransID) {
        return (pTransID.length() >= 6) ? pTransID.substring(0, 6) + "-" + pTransID.substring(6, pTransID.length()) : "";

    }
}
