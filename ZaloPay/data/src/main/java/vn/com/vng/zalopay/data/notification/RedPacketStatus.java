package vn.com.vng.zalopay.data.notification;

/**
 * Created by longlv on 01/08/2016.
 *
 */
public enum RedPacketStatus {
    Unknown(0),
    CanOpen(1),
    Opened(2),
    Invalid(3);

    private int value;

    RedPacketStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
