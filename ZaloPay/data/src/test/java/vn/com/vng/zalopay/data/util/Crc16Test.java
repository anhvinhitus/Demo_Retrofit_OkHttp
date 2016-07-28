package vn.com.vng.zalopay.data.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huuhoa on 7/28/16.
 * Unit tests for Crc16
 */
public class Crc16Test {

    @Test
    public void testCrc() throws Exception {
        int crc = Crc16.crcb((byte) 0xFC, (byte) 5, (byte) 0x11);
        int output = 0x2756;

        Assert.assertEquals(crc, output);
    }

    @Test
    public void testCrcData() throws Exception {
        int crc = Crc16.crcb((byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09,
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09,
                (byte) 0x00, (byte) 0x01);

        int output = 0x79E1;
        Assert.assertEquals(crc, output);
    }

    @Test
    public void testCrcb() throws Exception {
        int crc = Crc16.crc("123456789");
        int output = 0x8921;

        Assert.assertEquals(crc, output);
    }
}