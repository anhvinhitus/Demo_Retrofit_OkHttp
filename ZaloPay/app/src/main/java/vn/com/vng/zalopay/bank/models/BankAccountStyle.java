package vn.com.vng.zalopay.bank.models;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

/**
 * Created by longlv on 1/20/17.
 * *
 */

public class BankAccountStyle {
    public final int mBankIcon;
    public final int mLineColor;

    public BankAccountStyle(@StringRes int bankIcon,
                            @ColorRes int lineColor) {
        mBankIcon = bankIcon;
        mLineColor = lineColor;
    }
}
