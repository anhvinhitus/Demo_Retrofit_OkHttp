package vn.com.vng.zalopay.data.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huuhoa on 9/2/16.
 */
public class CBORHelperTest {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
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

    @Test
    public void fieldNameTypeTest() throws Exception {
        String input = "{'type':1}";
        byte[] expected_output = new byte[] {(byte) 0xa1, 0x1, 0x01};

        runTest(input, expected_output);
    }

    @Test
    public void fieldNameUidTest() throws Exception {
        String input = "{'uid':160517000003001}";
        byte[] expected_output = new byte[] {(byte) 0xa1, (byte) 0x02, (byte) 0x1b, (byte) 0x00, (byte) 0x00, (byte) 0x91, (byte) 0xfd, (byte) 0x46, (byte) 0xc3, (byte) 0xfd, (byte) 0xb9};

        runTest(input, expected_output);
    }

    @Test
    public void fieldNameChecksumTest() throws Exception {
        String input = "{'checksum':'aabbccdd'}";
        byte[] expected_output = new byte[] {(byte) 0xa1, (byte) 0x03, (byte) 0x48, (byte) 0x61, (byte) 0x61, (byte) 0x62, (byte) 0x62, (byte) 0x63, (byte) 0x63, (byte) 0x64, (byte) 0x64};

        runTest(input, expected_output);
    }

    @Test
    public void fieldNameAmountTest() throws Exception {
        String input = "{'amount':200000}";
        byte[] expected_output = new byte[] {(byte) 0xa1, (byte) 0x04, (byte) 0x1a, (byte) 0x00, (byte) 0x03, (byte) 0x0d, (byte) 0x40};

        runTest(input, expected_output);
    }

    @Test
    public void fieldNameMessageTest() throws Exception {
        String input = "{'message':'bdhfhj'}";
        byte[] expected_output = new byte[] {(byte) 0xa1, (byte) 0x05, (byte) 0x46, (byte) 0x62, (byte) 0x64, (byte) 0x68, (byte) 0x66, (byte) 0x68, (byte) 0x6a};

        runTest(input, expected_output);
    }

    @Test
    public void fieldNameTypeUidTest() throws Exception {
        String input = "{'type':1,'uid':160517000003001}";
        byte[] expected_output = new byte[] {(byte) 0xa2, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x1b, (byte) 0x00, (byte) 0x00, (byte) 0x91, (byte) 0xfd, (byte) 0x46, (byte) 0xc3, (byte) 0xfd, (byte) 0xb9};

        runTest(input, expected_output);
    }

    @Test
    public void fieldNameTypeUidAmountMessageChecksumTest() throws Exception {
        String input = "{'checksum':'aabbccdd','amount':30000,'message':'abchdh','type':1,'uid':160517000003001}";
        byte[] expected_output = new byte[] {(byte) 0xa5, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x1b, (byte) 0x00, (byte) 0x00, (byte) 0x91, (byte) 0xfd, (byte) 0x46, (byte) 0xc3, (byte) 0xfd, (byte) 0xb9, (byte) 0x03, (byte) 0x48, (byte) 0x61, (byte) 0x61, (byte) 0x62, (byte) 0x62, (byte) 0x63, (byte) 0x63, (byte) 0x64, (byte) 0x64, (byte) 0x04, (byte) 0x19, (byte) 0x75, (byte) 0x30, (byte) 0x05, (byte) 0x46, (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x68, (byte) 0x64, (byte) 0x68};

        runTest(input, expected_output);
    }


    private void runTest(String input, byte[] expected) throws Exception {
        byte[] output = CBORHelper.jsonToCbor(input);
//        String hex = bytesToHex(output);
//        System.out.print(hex);
        Assert.assertArrayEquals(expected, output);

        String decoded = CBORHelper.cborToJson(output);
        assertJson(decoded, input);
    }

    private void assertJson(String expected, String actual) {
        JsonParser parser = new JsonParser();
        JsonObject objectActual = parser.parse(actual).getAsJsonObject();
        JsonObject objectExpected = parser.parse(expected).getAsJsonObject();

        Assert.assertEquals(objectExpected, objectActual);
    }
}