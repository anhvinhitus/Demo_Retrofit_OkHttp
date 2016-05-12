package vn.com.vng.zalopay.domain.model;

import vn.com.vng.zalopay.domain.Enums;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class BankCard {

    public Enums.BankCard type;
    public String subAccNumber;
    public String userName;

    public BankCard(Enums.BankCard type, String subAccNumber, String userName) {
        this.type = type;
        this.subAccNumber = subAccNumber;
        this.userName = userName;
    }
}
