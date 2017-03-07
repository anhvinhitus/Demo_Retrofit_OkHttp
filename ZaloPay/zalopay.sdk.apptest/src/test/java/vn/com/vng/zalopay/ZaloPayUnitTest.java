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
        assertTrue(CardValidation.validCardName("VO  **"));
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
    public void testVCBUtils_getVcbType() {
        assertEquals(VcbUtils.getVcbType("Quý khách vui lòng nhập tên truy cập!"), EVCBType.EMPTY_USERNAME);
        assertEquals(VcbUtils.getVcbType("Quý khách vui lòng nhập mật khẩu!"), EVCBType.EMPTY_PASSWORD);
        assertEquals(VcbUtils.getVcbType("Quý khách vui lòng nhập mã xác nhận!"), EVCBType.EMPTY_CAPCHA);
        assertEquals(VcbUtils.getVcbType("Tên truy cập hoặc mật khẩu không chính xác. Quý khách lưu ý, dịch vụ bị tạm khóa nếu Quý khách nhập sai mật khẩu quá 5 lần."), EVCBType.WRONG_USERNAME_PASSWORD);
        assertEquals(VcbUtils.getVcbType("Quý khách đã nhập sai mật khẩu quá 5 lần. Dịch vụ đã bị khóa. Để mở lại dịch vụ, Quý khách vui lòng liên hệ Trung tâm dịch vụ khách hàng Vietcombank tại số 1900 54 54 13 hoặc Quầy giao dịch Vietcombank gần nhất để được trợ giúp"), EVCBType.ACCOUNT_LOCKED);
        assertEquals(VcbUtils.getVcbType("Mã kiểm tra không chính xác! Quý khách vui lòng kiểm tra lại!"), EVCBType.WRONG_CAPTCHA);
    }

    @Test
    public void testGetFirstStringWithSize() {
        assertEquals(StringUtil.getFirstStringWithSize("0412000416723", 4), "0412");
        assertEquals(StringUtil.getFirstStringWithSize("0412000416723", 6), "041200");
        assertEquals(StringUtil.getFirstStringWithSize("0412000416723", 0), "");
    }

    @Test
    public void testGetLastStringWithSize() {
        assertEquals(StringUtil.getLastStringWithSize("0412000416723", 4), "6723");
        assertEquals(StringUtil.getLastStringWithSize("0412000416723", 6), "416723");
        assertEquals(StringUtil.getLastStringWithSize("0412000416723", 0), "");
    }

    @Test
    public void testOtpUtils() {
        assertEquals(OtpUtils.getOtp("Quy khach dang thuc hien gd Dang ky su dung dich vu vi dien tu tren VCB-iBanking. Ma giao dich cua Quy khach la 565c9c6ffd", "VCB-iBanking", "Ma giao dich cua Quy khach la\u0020", 10), "565c9c6ffd");
    }
}
