package vn.com.vng.zalopay.bank.models;

/**
 * Created by longlv on 2/6/17.
 * *
 */

public enum LinkBankPagerIndex {
    LINK_CARD(0), LINK_ACCOUNT(1);

    private int value;

    LinkBankPagerIndex(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
