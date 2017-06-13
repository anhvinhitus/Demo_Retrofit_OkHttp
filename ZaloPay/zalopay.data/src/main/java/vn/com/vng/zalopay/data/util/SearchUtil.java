package vn.com.vng.zalopay.data.util;

/**
 * Created by lytm on 13/06/2017.
 */

public class SearchUtil {
    public static int TopRateApp = 3;

    public static void setTopRateApp(int pNumber) {
        if (pNumber > 0) {
            TopRateApp = pNumber;
        }
    }

    public static int getTopRateApp() {
        return TopRateApp;
    }
}
