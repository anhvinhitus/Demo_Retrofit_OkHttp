package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EBankFunction {
    LINK_CARD("301"),
    LINK_BANK_ACCOUNT("302"),
    PAY("-1"),//temp enum to detect other pay type
    PAY_BY_CARD("103"),
    PAY_BY_BANK_ACCOUNT("104"),
    PAY_BY_CARD_TOKEN("105"),
    PAY_BY_BANKACCOUNT_TOKEN("106"),
    WITHDRAW("5");

    private final String name;

    private EBankFunction(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
