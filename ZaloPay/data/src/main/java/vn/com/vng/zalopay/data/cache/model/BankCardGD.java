package vn.com.vng.zalopay.data.cache.model;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "BANK_CARD_GD".
 */
public class BankCardGD {

    /** Not-null value. */
    private String cardhash;
    /** Not-null value. */
    private String cardname;
    /** Not-null value. */
    private String first6cardno;
    /** Not-null value. */
    private String last4cardno;
    /** Not-null value. */
    private String bankcode;

    public BankCardGD() {
    }

    public BankCardGD(String cardhash) {
        this.cardhash = cardhash;
    }

    public BankCardGD(String cardhash, String cardname, String first6cardno, String last4cardno, String bankcode) {
        this.cardhash = cardhash;
        this.cardname = cardname;
        this.first6cardno = first6cardno;
        this.last4cardno = last4cardno;
        this.bankcode = bankcode;
    }

    /** Not-null value. */
    public String getCardhash() {
        return cardhash;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCardhash(String cardhash) {
        this.cardhash = cardhash;
    }

    /** Not-null value. */
    public String getCardname() {
        return cardname;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    /** Not-null value. */
    public String getFirst6cardno() {
        return first6cardno;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setFirst6cardno(String first6cardno) {
        this.first6cardno = first6cardno;
    }

    /** Not-null value. */
    public String getLast4cardno() {
        return last4cardno;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setLast4cardno(String last4cardno) {
        this.last4cardno = last4cardno;
    }

    /** Not-null value. */
    public String getBankcode() {
        return bankcode;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

}
