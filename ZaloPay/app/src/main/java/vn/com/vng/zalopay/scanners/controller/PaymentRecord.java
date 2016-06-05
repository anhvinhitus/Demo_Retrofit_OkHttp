package vn.com.vng.zalopay.scanners.controller;

import android.util.Base64;

import timber.log.Timber;
import vn.com.vng.zalopay.utils.DebugUtils;
import vn.com.vng.zalopay.utils.MemoryUtils;

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
    public static PaymentRecord from(byte[] data) {
        if (data == null) {
            Timber.d("Invalid scanRecord. Specific data not found.");
            return null;
        }

        Timber.d("Data [%d]-[%s]", data.length, DebugUtils.bytesToHex(data));

        int currentPos = 0;

        // The first two bytes of the manufacturer specific data are
        // manufacturer ids in little endian.

        long manufacturerId = 0x1710;
//        currentPos += 2;
//        long amount = 0;
        long amount = MemoryUtils.extractLong(data, currentPos);
        currentPos += 4;
        long appid = MemoryUtils.extractShort(data, currentPos);
        currentPos += 2;
        byte[] transactionTokenBytes = MemoryUtils.extractBytes(data, currentPos, 16);
        String transactionToken = Base64.encodeToString(transactionTokenBytes, Base64.URL_SAFE | Base64.NO_PADDING);
//        transactionToken = "DQ5ZWRbtdc4NKCQckstZLg";
        currentPos += 16;
        long crc16 = MemoryUtils.extractShort(data, currentPos);

        return new PaymentRecord(manufacturerId, amount, appid, transactionToken, crc16);
    }
}
