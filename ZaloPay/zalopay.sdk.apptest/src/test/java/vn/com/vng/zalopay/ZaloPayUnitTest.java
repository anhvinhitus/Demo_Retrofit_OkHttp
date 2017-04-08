package vn.com.vng.zalopay;

import org.junit.Test;

import vn.com.zalopay.wallet.business.entity.enumeration.EVCBType;
import vn.com.zalopay.wallet.business.validation.CardValidation;
import vn.com.zalopay.wallet.utils.OtpUtils;
import vn.com.zalopay.wallet.utils.PaymentUtils;
import vn.com.zalopay.wallet.utils.StringUtil;
import vn.com.zalopay.wallet.utils.VcbUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZaloPayUnitTest {
    @Test
    public void testCardNumberWithSpecialCharacter() {
        assertTrue(CardValidation.validCardName("VO "));
    }

    @Test
    public void testCardNumberWithDigits() {
        assertTrue(CardValidation.validCardName("VO VAN CHUC 342 SF232"));
    }

    @Test
    public void testCardNumberWithSpace() {
        assertTrue(CardValidation.validCardName("VO VAN    CHUC"));
    }

    @Test
    public void clearCardNumberSpace() {
        assertEquals(PaymentUtils.clearCardName("VO      VAN    CHUC"), "VO VAN CHUC");
    }

    @Test
    public void testClearOTPWhiteSpace() {
        assertEquals(PaymentUtils.clearOTP("12345 67 "), "1234567");
    }

    @Test
    public void testClearOTP() {
        assertEquals(PaymentUtils.clearOTP("1234-5678"), "12345678");
    }

    @Test
    public void testOtpUtils() {
        assertEquals(OtpUtils.getOtp("Quy khach dang thuc hien gd Dang ky su dung dich vu vi dien tu tren VCB-iBanking. Ma giao dich cua Quy khach la 565c9c6ffd", "VCB-iBanking", "Ma giao dich cua Quy khach la\u0020", 10), "565c9c6ffd");
    }
}
