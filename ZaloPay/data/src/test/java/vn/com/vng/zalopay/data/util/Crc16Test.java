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

        crc = Crc16.crcb(
                (byte) 0x07, (byte) 0xa8, (byte) 0x61, (byte) 0x00, (byte) 0x00
                , (byte) 0x10, (byte) 0x00, (byte) 0xf9, (byte) 0xa7, (byte) 0xce
                , (byte) 0xa4, (byte) 0x67, (byte) 0x8c, (byte) 0xf0, (byte) 0x73
                , (byte) 0x75, (byte) 0xe5, (byte) 0x97, (byte) 0x75, (byte) 0xd0
                , (byte) 0x84, (byte) 0xbb, (byte) 0xca, (byte) 0x0a, (byte) 0x7f);
        output = 32522;


//        Assert.assertEquals(crc, output);

        crc = Crc16.crcb(
                (byte) 0xa8, (byte) 0x61, (byte) 0x00, (byte) 0x00, (byte) 0x10,
                (byte) 0x00, (byte) 0xf1, (byte) 0x4f, (byte) 0xe9, (byte) 0xb5,
                (byte) 0xc9, (byte) 0xd4, (byte) 0xdb, (byte) 0xca, (byte) 0x09,
                (byte) 0xa2, (byte) 0x84, (byte) 0xe8, (byte) 0xc5, (byte) 0x60,
                (byte) 0xc4, (byte) 0x2a);
        output = 0xe640;
//        Assert.assertEquals(crc, output);
    }

    @Test
    public void testCrcb() throws Exception {
        int crc = Crc16.crc("123456789");
        int output = 0x8921;

        Assert.assertEquals(crc, output);

        crc = Crc16.crc("1");
        System.out.println(String.format("crc16 of [1] = [%d]", crc));
        crc = Crc16.crc("12");
        System.out.println(String.format("crc16 of [12] = [%d]", crc));

        crc = Crc16.crcb((byte) 0x01);
        System.out.println(String.format("crc16 of [0x01] = [%d]", crc));

        crc = Crc16.crcb((byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04);
        System.out.println(String.format("crc16 of [0x01, 0x02, 0x03, 0x04] = [%d]", crc));

    }
}