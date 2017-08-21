package vn.com.zalopay;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.entity.config.OtpRule;
import vn.com.zalopay.wallet.helper.OtpHelper;

/*
 * Created by chucvv on 8/8/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BankOtpTest {
    String senderVTB = "VietinBank";
    String senderSCB = "Sacombank";
    String senderVCB = "Vietcombank";
    String senderExim = "Eximbank";
    String senderSGCB = "SCB";
    String senderBidv = "BIDV";

    @Before
    public void setup() {
    }

    @After
    public void cleanup() {
    }

    private List<OtpRule> createVTBOtpRules() {
        List<OtpRule> otpRules = new ArrayList<>();

        OtpRule otpRule1 = new OtpRule();
        otpRule1.length = 6;
        otpRule1.bankcode = CardType.PVTB;
        otpRule1.begin = true;
        otpRule1.start = 0;
        otpRule1.sender = senderVTB;

        OtpRule otpRule2 = new OtpRule();
        otpRule2.length = 6;
        otpRule2.bankcode = CardType.PVTB;
        otpRule2.start = 0;
        otpRule2.sender = senderVTB;

        otpRules.add(otpRule1);
        otpRules.add(otpRule2);

        return otpRules;
    }

    private List<OtpRule> createSacombankOtpRules() {
        List<OtpRule> otpRules = new ArrayList<>();

        OtpRule otpRule1 = new OtpRule();
        otpRule1.length = 7;
        otpRule1.bankcode = CardType.PSCB;
        otpRule1.begin = true;
        otpRule1.start = 0;
        otpRule1.sender = senderSCB;

        OtpRule otpRule2 = new OtpRule();
        otpRule2.length = 6;
        otpRule2.bankcode = CardType.PSCB;
        otpRule2.begin = true;
        otpRule2.start = 0;
        otpRule2.sender = senderSCB;

        otpRules.add(otpRule1);
        otpRules.add(otpRule2);

        return otpRules;
    }

    private List<OtpRule> createVCBOtpRules() {
        List<OtpRule> otpRules = new ArrayList<>();

        OtpRule otpRule1 = new OtpRule();
        otpRule1.length = 10;
        otpRule1.bankcode = CardType.PVCB;
        otpRule1.start = 0;
        otpRule1.sender = senderVCB;
        otpRule1.isdigit = false;

        otpRules.add(otpRule1);

        return otpRules;
    }

    private List<OtpRule> createSGCBOtpRules() {
        List<OtpRule> otpRules = new ArrayList<>();

        OtpRule otpRule1 = new OtpRule();
        otpRule1.length = 6;
        otpRule1.bankcode = CardType.PSGCB;
        otpRule1.start = 0;
        otpRule1.sender = senderSGCB;
        otpRule1.isdigit = false;

        otpRules.add(otpRule1);

        return otpRules;
    }

    private List<OtpRule> createBIDVOtpRules() {
        List<OtpRule> otpRules = new ArrayList<>();

        OtpRule otpRule1 = new OtpRule();
        otpRule1.length = 6;
        otpRule1.bankcode = CardType.PBIDV;
        otpRule1.start = 24;
        otpRule1.begin = true;
        otpRule1.sender = senderBidv;

        otpRules.add(otpRule1);

        return otpRules;
    }

    /**
     * flow call authen payer api
     */
    @Test
    public void testVTBType1() {
        String otp = "611311 la OTP xac nhan ban da DONG Y gan The voi Vi dien tu ZaloPay ***5002. Het han xac nhan vao 21/08 13:42. Ma GD 170821823613";
        List<OtpRule> otpRules = createVTBOtpRules();
        String clearOtp = OtpHelper.parseOtp(otpRules, senderVTB, otp);
        Assert.assertEquals("611311", clearOtp);
    }

    /**
     * flow parse website bank flow
     */
    @Test
    public void testVTBype2() {
        String otp = "Giao dich truc tuyen VietinBank. Ma giao dich: 2387 Mat khau: 110205 ";
        List<OtpRule> otpRules = createVTBOtpRules();
        String clearOtp = OtpHelper.parseOtp(otpRules, senderVTB, otp);
        Assert.assertEquals("110205", clearOtp);
    }

    @Test
    public void testVTBType3() {
        String otp = "QK dang thuc hien lien ket the 970415*******8156 voi tai khoan ***5002 cua vi dien tu ZALOPAY. Nhap Ma OTP 218542 ung voi ma GD 1708211137174";
        List<OtpRule> otpRules = createVTBOtpRules();
        String clearOtp = OtpHelper.parseOtp(otpRules, senderVTB, otp);
        Assert.assertEquals(null, clearOtp);
    }

    @Test
    public void testBidvOtp() {
        String otp = "So OTP cua quy khach la 874966. Quy khach dang thuc hien giao dich lien ket tai khoan tai Kenh thanh toan truc tuyen BIDV.";
        List<OtpRule> otpRules = createBIDVOtpRules();
        String clearOtp = OtpHelper.parseOtp(otpRules, senderBidv, otp);
        Assert.assertEquals("874966", clearOtp);
    }

    @Test
    public void testVCBOtp() {
        String otp = "Quy khach dang thuc hien gd Dang ky su dung dich vu vi dien tu tren VCB-iBanking. Ma giao dich cua Quy khach la 5dc2e17795";
        List<OtpRule> otpRules = createVCBOtpRules();
        String clearOtp = OtpHelper.parseOtp(otpRules, senderVCB, otp);
        Assert.assertEquals("5dc2e17795", clearOtp);
    }

    @Test
    public void testSacombank() {
        String otp = "348694 la ma xac thuc (OTP) giao dich truc tuyen, thoi han 5 phut. Vui long KHONG cung cap OTP cho bat ki ai";
        List<OtpRule> otpRules = createSacombankOtpRules();
        String clearOtp = OtpHelper.parseOtp(otpRules, senderSCB, otp);
        Assert.assertEquals("348694", clearOtp);
    }

    @Test
    public void testSGCBLink() {
        String otp = "Ma xac thuc thong tin the cua khach hang qua SMS luc 13:43:12 ngay 11/08/2017 la: OKGYXB";
        List<OtpRule> otpRules = createSGCBOtpRules();
        String clearOtp = OtpHelper.parseOtp(otpRules, senderSGCB, otp);
        Assert.assertEquals("OKGYXB", clearOtp);
    }

    /*@Test
    public void testSGCBPay() {
        String otp = "Ma xac thuc giao dich ONLINE cua the SCB x2639 la: MZTFQB. Neu giao dich KHONG do Quy khach thuc hien, vui long goi ngay Hotline SCB 1800545438";
    }*/

   /* @Test
    void testExim() {
        String otp = "Vui long nhạp OTP 23028378 de hoan tat giao dich VND 524800.. Neu khong phai QK thuc hien GD nay, LH 18001199.";
    }*/
}
