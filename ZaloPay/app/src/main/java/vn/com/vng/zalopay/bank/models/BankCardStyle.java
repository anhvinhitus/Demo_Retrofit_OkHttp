package vn.com.vng.zalopay.bank.models;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankCardStyle {
    public final int bankIcon;
    public final int backgroundGradientStart;
    public final int backgroundGradientEnd;

    public BankCardStyle(@DrawableRes int bankIcon,
                         @ColorRes int backgroundGradientStart,
                         @ColorRes int backgroundGradientEnd) {
        this.bankIcon = bankIcon;
        this.backgroundGradientStart = backgroundGradientStart;
        this.backgroundGradientEnd = backgroundGradientEnd;
    }
}
