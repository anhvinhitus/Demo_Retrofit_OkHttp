package vn.com.vng.zalopay.scanners.controller;

/**
 * Created by huuhoa on 6/4/16.
 */
public class PaymentRecord {
    public final long manufacturerId;
    public final long amount;
    public final long appId;
    public final String transactionToken;
    public final long crc16;

    public PaymentRecord(long manufacturerId, long amount, long appId, String transactionToken, long crc16) {
        this.manufacturerId = manufacturerId;
        this.amount = amount;
        this.appId = appId;
        this.transactionToken = transactionToken;
        this.crc16 = crc16;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PaymentRecord)) {
            return super.equals(o);
        }

        PaymentRecord obj = (PaymentRecord)o;
        return obj.transactionToken.equalsIgnoreCase(transactionToken);
    }

    @Override
    public String toString() {
        return String.format("appId: %d, token: %s", appId, transactionToken);
    }

    public final static PaymentRecord Invalid = new PaymentRecord(-1, -1, -1, "", -1);
}
