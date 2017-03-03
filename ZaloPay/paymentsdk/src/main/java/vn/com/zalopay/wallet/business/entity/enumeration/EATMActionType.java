package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EATMActionType {
    THREE3DS("1"),
    OTP("2");

    private final String name;

    private EATMActionType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
