package vn.com.zalopay.wallet.business.entity.enumeration;

public enum ETransactionType {
    PAY("1"),
    TOPUP("2"),
    LINK_CARD("3"),
    WALLET_TRANSFER("4"),
    WITHDRAW("5"),
    LINK_ACC("3");

    private final String name;

    private ETransactionType(String s) {
        name = s;
    }

    public static boolean isMember(int pTransType) {
        ETransactionType[] transactionTypes = ETransactionType.values();
        for (ETransactionType transactionType : transactionTypes)
            if (transactionType.name.equals(String.valueOf(pTransType))) {
                return true;
            }
        return false;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
