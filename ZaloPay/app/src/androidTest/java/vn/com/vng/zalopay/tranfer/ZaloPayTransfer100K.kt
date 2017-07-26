package vn.com.vng.zalopay.tranfer

import android.support.test.espresso.matcher.ViewMatchers.withId
import org.junit.Test
import vn.com.vng.zalopay.AbtractZaloPayTesting
import vn.com.vng.zalopay.R
import vn.com.vng.zalopay.sb.EZaloPayTransfer
import vn.com.vng.zalopay.sb.Info

/**
 * Created by cpu11843-local on 7/26/17.
 */
class ZaloPayTransfer100K : AbtractZaloPayTesting(), IZaloPayTransfer {
    override fun initTest() {
        super.initTest()
    }

    @Test
    fun transfer_100k_with_zalopayID_HAPPYCASE() {
        processInApp(EZaloPayTransfer.TRANSFER_MAIN_SCREEN, java.lang.Long.parseLong(getText(withId(R.id.tv_balance)).replace(".", "")), Info.AMOUNT_100K)
    }

    @Test
    fun transfer_verify_near_list_transaction_HAPPYCASE() {
        processInApp(EZaloPayTransfer.TRANSFER_VERIFY_LIST_NEAR_TRANS, 0, 0)
    }
}
