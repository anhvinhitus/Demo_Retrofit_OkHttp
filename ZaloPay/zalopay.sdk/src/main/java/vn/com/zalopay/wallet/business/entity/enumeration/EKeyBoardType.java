package vn.com.zalopay.wallet.business.entity.enumeration;

import android.text.TextUtils;

public enum EKeyBoardType {
    NUMBER("1"),
    TEXT("2");

    private final String name;

    private EKeyBoardType(String s) {
        name = s;
    }

    public static EKeyBoardType fromString(String pString) {
        if (!TextUtils.isEmpty(pString)) {
            for (EKeyBoardType value : EKeyBoardType.values()) {
                if (pString.equalsIgnoreCase(value.name)) {
                    return value;
                }
            }
        }
        return null;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
