package vn.com.zalopay.utility;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by longlv on 05/05/2016.
 */
public class CurrencyUtil {

    public static final String CURRENCY_UNIT = "VND";
    public static final Locale VIETNAMESE_LOCAL = new Locale("vi", "VN");

    public static String formatCurrency(double money) {
        return formatCurrency(money, true);
    }

    public static String formatCurrency(double money, boolean showCurrencySymbol) {
        if (money <= 0) {
            if (showCurrencySymbol) {
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
                return fmt.format(money) + " " + CURRENCY_UNIT;
            } else {
                return fmt.format(money);
            }
        }
    }

    public static SpannableString spanFormatCurrency(double money, boolean isBold) {
        String moneyString = (money <= 0) ? CurrencyUtil.formatCurrency(money, false) : CurrencyUtil.formatCurrency(money, true);
        SpannableString span = new SpannableString(moneyString);
        int indexSuffix = moneyString.indexOf(CurrencyUtil.CURRENCY_UNIT);
        if (indexSuffix > 0) {
            span.setSpan(new RelativeSizeSpan(0.8f),
                    indexSuffix,
                    moneyString.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (isBold) {
            span.setSpan(new StyleSpan(Typeface.BOLD),
                    0,
                    indexSuffix,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return span;
    }
}
