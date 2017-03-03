package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EStatusActionType {
    THREE3DS("2"),
    OTP("1");

    private final String name;

    private EStatusActionType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
