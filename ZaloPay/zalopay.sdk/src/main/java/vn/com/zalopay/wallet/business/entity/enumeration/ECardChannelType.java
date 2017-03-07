package vn.com.zalopay.wallet.business.entity.enumeration;


public enum ECardChannelType {
    ATM("atm"),
    CC("cc");

    private final String name;

    private ECardChannelType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
