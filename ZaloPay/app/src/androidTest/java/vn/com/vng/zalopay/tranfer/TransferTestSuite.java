package vn.com.vng.zalopay.tranfer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by chucvv on 6/7/17.
 */
// Runs all tranfer tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({ZaloPayTransfer10K.class,
        ZaloPayTransfer100K.class,
        ZaloPayTransferSearch.class})
public class TransferTestSuite {
}
