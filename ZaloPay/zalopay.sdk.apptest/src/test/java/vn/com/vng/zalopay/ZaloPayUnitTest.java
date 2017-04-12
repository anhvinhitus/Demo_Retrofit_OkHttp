package vn.com.vng.zalopay;

import org.junit.Test;

import vn.com.zalopay.wallet.business.validation.CardValidation;
import vn.com.zalopay.wallet.utils.PaymentUtils;

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
}
