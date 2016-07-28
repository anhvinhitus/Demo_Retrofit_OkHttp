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

        crc = Crc16.crcb((byte) 0x07a, (byte) 0x86, (byte) 0x10, (byte) 0x00, (byte) 0x01
                , (byte) 0x00, (byte) 0x0f, (byte) 0x9a, (byte) 0x7c, (byte) 0xea
                , (byte) 0x46, (byte) 0x78, (byte) 0xcf, (byte) 0x07, (byte) 0x37
                , (byte) 0x5e, (byte) 0x59, (byte) 0x77, (byte) 0x5d, (byte) 0x08,
                (byte) 0x4b, (byte) 0xbc, (byte) 0xa0, (byte) 0xa7, (byte) 0xf);
        output = 32522;

        Assert.assertEquals(crc, output);
    }

    @Test
    public void testCrcb() throws Exception {
        int crc = Crc16.crc("123456789");
        int output = 0x8921;

        Assert.assertEquals(crc, output);
    }
}