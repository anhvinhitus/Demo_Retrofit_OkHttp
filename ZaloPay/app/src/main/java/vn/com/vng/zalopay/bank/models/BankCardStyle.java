package vn.com.vng.zalopay.bank.models;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankCardStyle {
    public final String bankIcon;

    @ColorInt
    public final int backgroundGradientStart;

    @ColorInt
    public final int backgroundGradientEnd;

    public BankCardStyle(@StringRes int bankIcon,
                         @ColorRes int backgroundGradientStart,
                         @ColorRes int backgroundGradientEnd) {
        Context context = AndroidApplication.instance();
        this.bankIcon = bankIcon > 0 ? context.getString(bankIcon) : "";
        this.backgroundGradientStart = ContextCompat.getColor(context, backgroundGradientStart);
        this.backgroundGradientEnd = ContextCompat.getColor(context, backgroundGradientEnd);
    }
}
