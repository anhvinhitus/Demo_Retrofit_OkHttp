package vn.com.vng.zalopay.domain.model;

/**
 * Created by AnhHieu on 5/10/16.
 */
public class BankCard {

    public String cardname;
    public String first6cardno;
    public String last4cardno;
    public String bankcode;
    public long expiretime;

    public String type;

    public BankCard(String cardname, String first6cardno, String last4cardno, String bankcode, long expiretime) {
        this.cardname = cardname;
        this.first6cardno = first6cardno;
        this.last4cardno = last4cardno;
        this.bankcode = bankcode;
        this.expiretime = expiretime;
    }
}
