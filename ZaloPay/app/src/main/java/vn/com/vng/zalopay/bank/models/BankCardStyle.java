package vn.com.vng.zalopay.bank.models;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankCardStyle {
    public final int bankIcon;
    public final int backgroundGradientStart;
    public final int backgroundGradientEnd;

    public BankCardStyle(@StringRes int bankIcon,
                         @ColorRes int backgroundGradientStart,
                         @ColorRes int backgroundGradientEnd) {
        this.bankIcon = bankIcon;
        this.backgroundGradientStart = backgroundGradientStart;
        this.backgroundGradientEnd = backgroundGradientEnd;
    }
}
