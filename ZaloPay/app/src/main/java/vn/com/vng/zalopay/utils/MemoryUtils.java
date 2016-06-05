package vn.com.vng.zalopay.utils;

/**
 * Created by huuhoa on 6/5/16.
 * Methods for working with memory
 */
public class MemoryUtils {
    // Helper method to extract bytes from byte array.
    public static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    public static short extractShort(byte[] scanRecord, int start) {
        long shortValue = scanRecord[start] & 0xFF;
        shortValue += (scanRecord[start + 1] & 0xFF) << 8;
        return (short)shortValue;
    }

    public static long extractLong(byte[] scanRecord, int start) {
        long longValue = scanRecord[start] & 0xFF;
        longValue += (scanRecord[start + 1] & 0xFF) << 8;
        longValue += (scanRecord[start + 2] & 0xFF) << 16;
        longValue += (scanRecord[start + 3] & 0xFF) << 24;
        return longValue;
    }
}
