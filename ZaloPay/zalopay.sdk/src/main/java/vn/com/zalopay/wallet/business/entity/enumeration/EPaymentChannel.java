package vn.com.zalopay.wallet.business.entity.enumeration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum EPaymentChannel {
    LINK_CARD("zingpaysdk_conf_gwinfo_channel_link_card"),
    ATM("zingpaysdk_conf_gwinfo_channel_atm"),
    ZALO_WALLET("zingpaysdk_conf_gwinfo_channel_zalopay"),
    CREDIT_CARD("zingpaysdk_conf_gwinfo_channel_credit_card"),
    WALLET_TRANSFER("zingpaysdk_conf_gwinfo_channel_wallet_transfer"),
    WITHDRAW("zingpaysdk_conf_gwinfo_channel_withdraw"),
    LINK_ACC("zingpaysdk_conf_gwinfo_channel_link_acc");

    private final String name;

    private EPaymentChannel(String pName) {
        name = pName;
    }

    public static EPaymentChannel fromString(String pString) {
        if (pString != null) {
            for (EPaymentChannel value : EPaymentChannel.values()) {
                if (pString.equalsIgnoreCase(value.name)) {
                    return value;
                }
            }
        }
        return null;
    }

    public static List<EPaymentChannel> all() {
        List<EPaymentChannel> all = new ArrayList<EPaymentChannel>();
        Collections.addAll(all, EPaymentChannel.values());
        return all;
    }

    public boolean equalsName(String pOtherName) {
        return (pOtherName == null) ? false : name.equals(pOtherName);
    }

    public String toString() {
        return name;
    }
}
