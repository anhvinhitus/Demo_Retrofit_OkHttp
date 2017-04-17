package vn.com.vng.zalopay.bank.models;

/**
 * Created by longlv on 4/17/17.
 * Type link bank
 */

public enum LinkBankType {
    LINK_BANK_CARD(0), LINK_BANK_ACCOUNT(1);

    int value;

    LinkBankType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
