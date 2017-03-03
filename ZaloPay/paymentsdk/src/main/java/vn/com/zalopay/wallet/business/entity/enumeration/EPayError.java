package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EPayError {
    COMPONENT_NULL("1"),
    DATA_INVALID("2"),
    NETWORKING_ERROR("3");

    private final String name;

    private EPayError(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
