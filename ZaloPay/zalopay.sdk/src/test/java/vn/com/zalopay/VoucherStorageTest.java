package vn.com.zalopay;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.repository.SharedPreferencesManager;

/*
 * Created by chucvv on 8/8/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class VoucherStorageTest {
    SharedPreferencesManager sharedPreferencesManager;

    String userId = "user_id";

    @Before
    public void setup() {
        sharedPreferencesManager = new SharedPreferencesManager(RuntimeEnvironment.application);
    }

    @After
    public void cleanup() {
        sharedPreferencesManager = null;
    }

    private VoucherInfo getVoucherInfo() {
        VoucherInfo voucherInfo = new VoucherInfo();
        voucherInfo.campaignid = 123;
        voucherInfo.discountamount = 10000;
        voucherInfo.vouchersig = "vouchersig";
        voucherInfo.usevouchertime = System.currentTimeMillis();
        return voucherInfo;
    }

    private VoucherInfo getVoucherInfoApp11() {
        VoucherInfo voucherInfo = getVoucherInfo();
        voucherInfo.vouchercode = "HAPPY178";
        return voucherInfo;
    }

    private VoucherInfo getVoucherInfoApp3() {
        VoucherInfo voucherInfo = getVoucherInfo();
        voucherInfo.vouchercode = "HAPPY179";
        return voucherInfo;
    }

    private void insertVoucherApp(VoucherInfo voucherInfo) {
        try {
            sharedPreferencesManager.setRevertVoucher(userId, voucherInfo.vouchercode, GsonUtils.toJsonString(voucherInfo));
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void assertVoucherEqual(VoucherInfo voucherInfo1, VoucherInfo voucherInfo2) {
        Assert.assertEquals(voucherInfo1.campaignid, voucherInfo2.campaignid);
        Assert.assertEquals(voucherInfo1.vouchercode, voucherInfo2.vouchercode);
        Assert.assertEquals(voucherInfo1.vouchersig, voucherInfo2.vouchersig);
        Assert.assertEquals(voucherInfo1.usevouchertime, voucherInfo2.usevouchertime);
        Assert.assertEquals(voucherInfo1.discountamount, voucherInfo2.discountamount);
    }

    @Test
    public void testSet1Voucher() {
        try {
            VoucherInfo voucherInfo = getVoucherInfoApp3();
            insertVoucherApp(voucherInfo);
            String[] vouchers = sharedPreferencesManager.getVouchers(userId);
            Assert.assertEquals(1, vouchers.length);

            VoucherInfo voucherInfoTest = GsonUtils.fromJsonString(vouchers[0], VoucherInfo.class);
            assertVoucherEqual(voucherInfo, voucherInfoTest);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Test
    public void testSet2Voucher() {
        try {
            VoucherInfo voucherInfo11 = getVoucherInfoApp11();
            VoucherInfo voucherInfo3 = getVoucherInfoApp3();

            insertVoucherApp(voucherInfo11);
            insertVoucherApp(voucherInfo3);

            String[] vouchers = sharedPreferencesManager.getVouchers(userId);
            Assert.assertEquals(2, vouchers.length);

            VoucherInfo voucherInfo11Test = GsonUtils.fromJsonString(vouchers[0], VoucherInfo.class);
            VoucherInfo voucherInfo3Test = GsonUtils.fromJsonString(vouchers[1], VoucherInfo.class);

            assertVoucherEqual(voucherInfo11, voucherInfo11Test);
            assertVoucherEqual(voucherInfo3, voucherInfo3Test);

        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Test
    public void testSetSameCodeVoucher() {
        try {
            VoucherInfo voucherInfo11 = getVoucherInfoApp11();
            VoucherInfo voucherInfo3 = getVoucherInfoApp3();

            insertVoucherApp(voucherInfo11);
            insertVoucherApp(voucherInfo3);

            insertVoucherApp(voucherInfo11);
            insertVoucherApp(voucherInfo3);
            insertVoucherApp(voucherInfo3);

            String[] vouchers = sharedPreferencesManager.getVouchers(userId);
            Assert.assertEquals(2, vouchers.length);

            VoucherInfo voucherInfo11Test = GsonUtils.fromJsonString(vouchers[0], VoucherInfo.class);
            VoucherInfo voucherInfo3Test = GsonUtils.fromJsonString(vouchers[1], VoucherInfo.class);

            assertVoucherEqual(voucherInfo11, voucherInfo11Test);
            assertVoucherEqual(voucherInfo3, voucherInfo3Test);

        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Test
    public void testSetNULLVoucher() {
        try {
            insertVoucherApp(null);
            String[] vouchers = sharedPreferencesManager.getVouchers(userId);
            Assert.assertEquals(0, vouchers.length);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Test
    public void testClear1Voucher() {
        try {
            testSet1Voucher();

            String voucherCode = "HAPPY179";
            sharedPreferencesManager.clearVouchers(userId, voucherCode);

            String[] vouchers = sharedPreferencesManager.getVouchers(userId);
            Assert.assertEquals(0, vouchers.length);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Test
    public void testClear2Voucher() {
        try {
            testSet2Voucher();

            String voucherCode1 = "HAPPY179";
            String voucherCode2 = "HAPPY178";
            sharedPreferencesManager.clearVouchers(userId, voucherCode1);
            sharedPreferencesManager.clearVouchers(userId, voucherCode2);

            String[] vouchers = sharedPreferencesManager.getVouchers(userId);
            Assert.assertEquals(0, vouchers.length);
        } catch (Exception e) {
            Timber.d(e);
        }
    }
}
