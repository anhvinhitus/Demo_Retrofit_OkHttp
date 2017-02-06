package vn.com.vng.zalopay.withdraw.models;

/**
 * Created by longlv on 2/4/17.
 * *
 */

public enum  BankType {
    // TODO: 2/4/17 - longlv: waiting PaymentSDK to confirm value.
    SUPPORT_LINK_ACCOUNT(0), SUPPORT_LINK_CARD(2);

    private int value;

    BankType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
