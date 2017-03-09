package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EBankStatus {
    ACTIVE("1"),
    MAINTENANCE("2");

    private final String name;

    private EBankStatus(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
