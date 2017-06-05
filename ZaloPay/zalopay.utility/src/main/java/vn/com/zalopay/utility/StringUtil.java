package vn.com.zalopay.utility;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class StringUtil {

    public static String formatVnCurrence(String price) {

        NumberFormat format = new DecimalFormat("#,##0.00");

        format.setCurrency(Currency.getInstance(Locale.US));//Or default locale

        price = (!TextUtils.isEmpty(price)) ? price : "0";

        price = price.trim();
        price = format.format(Math.ceil(Double.parseDouble(price)));

        price = price.replaceAll(",", "\\.");

        if (price.endsWith(".00")) {
            int centsIndex = price.lastIndexOf(".00");
            if (centsIndex != -1) {
                price = price.substring(0, centsIndex);
            }
        }

        price = String.format("%s", price);

        return price;
    }

    public static String getFirstStringWithSize(String pString, int pSize) {
        return pString.substring(0, pSize);
    }

    public static String getLastStringWithSize(String pString, int pSize) {
        return pString.substring(pString.length() - pSize);
    }
}
