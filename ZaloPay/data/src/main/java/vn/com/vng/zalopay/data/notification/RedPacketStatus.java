package vn.com.vng.zalopay.data.notification;

/**
 * Created by longlv on 01/08/2016.
 * Mapping with return code that BE response
 * Ref: https://docs.google.com/spreadsheets/d/1DqIpeLnB-ihMdtd7LCtNAm1obPhbCGSlZrElvEsbZ9A/edit#gid=0
 */
public enum RedPacketStatus {
    Unknown(0),
    CanOpen(1),
    Opened(-27), //PACKAGE_HAS_OPENED
    Refunded(-24); //PACKAGE_HAS_REFUND

    private int value;

    RedPacketStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
