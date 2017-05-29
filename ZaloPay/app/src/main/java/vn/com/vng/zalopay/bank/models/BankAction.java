package vn.com.vng.zalopay.bank.models;

/**
 * Created by longlv on 5/29/17.
 * Define bank action.
 */

public enum BankAction {
    LINK_CARD(0), UNLink_CARD(1), LINK_ACCOUNT(2), UNLINK_ACCOUNT(3);

    int value;

    BankAction(int value) {
        this.value = value;
    }
}
