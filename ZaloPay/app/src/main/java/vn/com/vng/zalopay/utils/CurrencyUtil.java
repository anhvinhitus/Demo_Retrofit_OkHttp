package vn.com.vng.zalopay.utils;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;

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
        String _temp = CurrencyUtil.formatCurrency(money, true);
        SpannableString span = new SpannableString(_temp);

        int indexSuffix = _temp.indexOf(CurrencyUtil.CURRENCY_UNIT);

        span.setSpan(new RelativeSizeSpan(0.8f), indexSuffix, _temp.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (isBold) {
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, indexSuffix,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return span;
    }
}
