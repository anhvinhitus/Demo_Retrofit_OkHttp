package vn.com.vng.zalopay.utils;

/**
 * Created by huuhoa on 6/5/16.
 * Debug utilities
 */
public class DebugUtils {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = 0x20;
        }
        return new String(hexChars);
    }
}
