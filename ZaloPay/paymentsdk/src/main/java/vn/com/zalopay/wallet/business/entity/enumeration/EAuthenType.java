package vn.com.zalopay.wallet.business.entity.enumeration;


public enum EAuthenType {
    SMS("otp"),
    TOKEN("token");

    private final String name;

    private EAuthenType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
