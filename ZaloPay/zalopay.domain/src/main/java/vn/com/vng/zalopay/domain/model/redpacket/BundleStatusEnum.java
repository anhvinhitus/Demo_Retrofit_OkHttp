package vn.com.vng.zalopay.domain.model.redpacket;

/**
 * Created by longlv on 11/08/2016.
 *
 */
public enum BundleStatusEnum {
    UNKNOWN(0),
    INIT(1),
    PACKAGE_GEN(2),
    AVAILABLE(3),
    REFUND(4);

    int value;

    BundleStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}