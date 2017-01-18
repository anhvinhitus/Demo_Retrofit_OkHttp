package vn.com.vng.zalopay.bank.models;

/**
 * Created by longlv on 1/17/17.
 * *
 */

public class BankCardStyle {
    public final int bankIcon;
    public final int backgroundGradientStart;
    public final int backgroundGradientEnd;

    public BankCardStyle(int bankIcon, int backgroundGradientStart, int backgroundGradientEnd) {
        this.bankIcon = bankIcon;
        this.backgroundGradientStart = backgroundGradientStart;
        this.backgroundGradientEnd = backgroundGradientEnd;
    }
}
