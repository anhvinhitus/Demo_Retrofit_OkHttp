package vn.com.zalopay.wallet.business.entity.enumeration;

import com.google.gson.annotations.SerializedName;

public enum EPaymentChannelStatus {

    @SerializedName("1")
    ENABLE(1),

    @SerializedName("0")
    DISABLE(0),

    @SerializedName("2")
    MAINTENANCE(2);

    private byte mValue = 0;

    private EPaymentChannelStatus(int pNum) {
        this.mValue = (byte) pNum;
    }

    public static EPaymentChannelStatus fromInt(int pNum) {
        if (pNum < 0 || pNum > 2)
            return EPaymentChannelStatus.DISABLE;

        byte num = (byte) pNum;

        for (EPaymentChannelStatus status : EPaymentChannelStatus.values()) {
            if (status.mValue == num)
                return status;
        }
        return null;
    }

    public int getValue() {
        return mValue;
    }
}
