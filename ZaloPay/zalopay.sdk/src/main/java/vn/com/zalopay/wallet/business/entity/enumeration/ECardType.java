package vn.com.zalopay.wallet.business.entity.enumeration;

import android.text.TextUtils;

public enum ECardType {
    VISA("VISA"),
    MASTER("MASTER"),
    JCB("JCB"),

    PVTB("123PVTB"),
    PBIDV("123PBIDV"),
    PSCB("123PSCB"),
    PSGCB("123PSGCB"),
    PVCB("ZPVCB"),

    UNDEFINE("UND");

    private final String name;

    private ECardType(String s) {
        name = s;
    }

    public static ECardType fromString(String pString) {
        if (!TextUtils.isEmpty(pString)) {
            for (ECardType value : ECardType.values()) {
                if (pString.equalsIgnoreCase(value.name)) {
                    return value;
                }
            }
        }
        return UNDEFINE;
    }

    public static boolean isBankAccount(ECardType pCardType) {
        if (pCardType == ECardType.PVCB)
            return true;
        return false;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
