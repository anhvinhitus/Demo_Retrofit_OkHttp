package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EFeeCalType {
    SUM("SUM"),
    MAX("MAX");

    private final String name;

    private EFeeCalType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
