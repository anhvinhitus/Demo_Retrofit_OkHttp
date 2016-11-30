package vn.com.vng.zalopay.data;

import org.junit.Assert;
import org.junit.Test;

import vn.com.vng.zalopay.data.util.PhoneUtil;

/**
 * Created by longlv on 11/29/16.
 * Unit test for PhoneUtil
 */

public class PhoneUtilTest {


    @Test
    public void testFormatPhone() {
        //Valid <= 0
        long phoneNumber = -12345678998L;
        long phoneNumber1 = 0;

        //Phone format
        long phoneNumber2 = 1234567891L;
        long phoneNumber3 = 84988888888L;
        long phoneNumber4 = 841699999999L;
        long phoneNumber5 = 86888888;
        long phoneNumber6 = 88123456;
        long phoneNumber7 = 89123456;
        long phoneNumber8 = 69123456;
        long phoneNumber9 = 992123456;
        long phoneNumber10 = 8423242429988L;

        //Not phone format
        long phoneNumber20 = 1;
        long phoneNumber21 = 99212345634432432L;
        long phoneNumber22 = 992;

        Assert.assertEquals("", PhoneUtil.formatPhoneNumber(phoneNumber));
        Assert.assertEquals("", PhoneUtil.formatPhoneNumber(phoneNumber1));
        Assert.assertEquals("0"+ phoneNumber2, PhoneUtil.formatPhoneNumber(phoneNumber2));
        Assert.assertEquals("0988888888", PhoneUtil.formatPhoneNumber(phoneNumber3));
        Assert.assertEquals("01699999999", PhoneUtil.formatPhoneNumber(phoneNumber4));
        Assert.assertEquals("0"+ phoneNumber5, PhoneUtil.formatPhoneNumber(phoneNumber5));
        Assert.assertEquals("0"+ phoneNumber6, PhoneUtil.formatPhoneNumber(phoneNumber6));
        Assert.assertEquals("0"+ phoneNumber7, PhoneUtil.formatPhoneNumber(phoneNumber7));
        Assert.assertEquals("0"+ phoneNumber8, PhoneUtil.formatPhoneNumber(phoneNumber8));
        Assert.assertEquals("0"+ phoneNumber9, PhoneUtil.formatPhoneNumber(phoneNumber9));
        Assert.assertEquals("023242429988", PhoneUtil.formatPhoneNumber(phoneNumber10));

        Assert.assertEquals(String.valueOf(phoneNumber20), PhoneUtil.formatPhoneNumber(phoneNumber20));
        Assert.assertEquals(String.valueOf(phoneNumber21), PhoneUtil.formatPhoneNumber(phoneNumber21));
        Assert.assertEquals(String.valueOf(phoneNumber22), PhoneUtil.formatPhoneNumber(phoneNumber22));
    }
}
