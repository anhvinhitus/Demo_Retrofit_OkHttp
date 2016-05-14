package vn.com.vng.zalopay.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import vn.com.vng.zalopay.BuildConfig;

/**
 * Created by longlv on 05/05/2016.
 */
public class CurrencyUtil {

    public static String CURRENCY_UNIT = "VNĐ";
    public static Locale VIETNAMESE_LOCAL = new Locale("vi", "VN");

    public static String formatCurrency(double money){
        return formatCurrency(money,true);
    }

    public static String formatCurrency(double money, boolean showCurrencySymbol){
        if (money <=0) {
            if (showCurrencySymbol){
                return "0 " + CURRENCY_UNIT;
            } else {
                return "0";
            }
        } else {
//			NumberFormat fmt = NumberFormat.getCurrencyInstance(VIETNAMESE_LOCAL);
            DecimalFormatSymbols vietnamSymbols = new DecimalFormatSymbols(VIETNAMESE_LOCAL);
            vietnamSymbols.setDecimalSeparator(',');
            vietnamSymbols.setGroupingSeparator('.');
            DecimalFormat fmt = new DecimalFormat("#,###.##", vietnamSymbols);
            fmt.setMaximumFractionDigits(0);
            if (showCurrencySymbol) {
                return fmt.format(money) +" " + CURRENCY_UNIT;
            } else {
                return fmt.format(money);
            }
        }
    }

    public static String formatCurrency(String strCurrency, boolean showCurrencySymbol){
        double money = 0;
        try {
            money = Double.valueOf(strCurrency);
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return strCurrency;
        }
        return formatCurrency(money,showCurrencySymbol);
    }

    public static String formatCurrency(String strCurrency){
        return formatCurrency(strCurrency,true);
    }
}
