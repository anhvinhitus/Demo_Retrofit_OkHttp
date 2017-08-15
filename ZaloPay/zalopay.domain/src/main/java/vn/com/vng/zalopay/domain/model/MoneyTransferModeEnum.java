package vn.com.vng.zalopay.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huuhoa on 8/15/17.
 *
 * Recent transaction mode
 */

public enum MoneyTransferModeEnum {
    TransferInvalid(0),
    TransferToZaloPayContact(2),
    TransferToZaloPayUser(3),
    TransferToZaloPayID(4);

    private int value;
    private static Map<Integer, MoneyTransferModeEnum> map;
    static {
        map = new HashMap<>();
        for (MoneyTransferModeEnum v : MoneyTransferModeEnum.values()) {
            map.put(v.value, v);
        }
    }

    MoneyTransferModeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MoneyTransferModeEnum fromInt(int value) {
        if (map.containsKey(value)) {
            return map.get(value);
        }

        return TransferInvalid;
    }
}
