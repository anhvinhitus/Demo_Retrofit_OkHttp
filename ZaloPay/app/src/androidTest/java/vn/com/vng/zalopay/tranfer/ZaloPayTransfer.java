package vn.com.vng.zalopay.tranfer;

import android.os.SystemClock;
import android.support.test.espresso.contrib.RecyclerViewActions;

import junit.framework.Assert;

import org.junit.Test;

import vn.com.vng.zalopay.AbtractZaloPayTesting;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.sb.Info;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Test tranfer money use zalopay channel
 * Make sure that you log in and app ready for testing payment
 * Created by chucvv on 6/7/17.
 */

public class ZaloPayTransfer extends AbtractZaloPayTesting {

    protected String tranfer_item_name;

    @Override
    protected void initTest() {
        super.initTest();
    }

    /***
     * tranfer use zalopay channel success
     */
    @Test
    public void tranfer_zalopayname_zalopay_password_HAPPYCASE() {
        //delay for app start
        SystemClock.sleep(10000);
        tranfer_item_name = mResource.getString(R.string.transfer_money);
        long balance = Long.parseLong(getText(withId(R.id.home_tv_balance)).replace(".",""));
        /*onView(withId(R.id.home_rcv_list_app))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(tranfer_item_name)), click()));*/
        //select tranfer
        onView(withId(R.id.home_rcv_list_app)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        //click item tranfer by zalopay name
        onView(withId(R.id.layoutTransferViaAccount)).perform(click());
        //fill zalopay name to get tranfer money
        onView(withId(R.id.edtAccountName)).perform(replaceText(Info.ZALOPAY_NAME_TRANFER));
        //click button
        onView(withId(R.id.btnContinue)).perform(click());
        //delay for check user name
        SystemClock.sleep(500);
        //input amount tranfer
        onView(withId(R.id.edtAmount)).perform(replaceText(String.valueOf(Info.AMOUNT_TRANFER)));
        //click button
        onView(withId(R.id.btnContinue)).perform(click());
        //delay for sdk check payment info
        SystemClock.sleep(1000);

        //USER enter SDK
        //select zalopay channel
        //onRow(Info.CHANNEL_ZALOPAY_NAME).perform(click());
        onView(withId(R.id.channel_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        //click confirm button
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        //password payment
        onView(withId(R.id.inputView)).perform(typeText(Info.PASSWORD));
        //delay for get status
        SystemClock.sleep(1000);
        //click button finish on result screen
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        //delay for update balance
        SystemClock.sleep(500);
        //asset balance again
        long updatedBalance = Long.parseLong(getText(withId(R.id.home_tv_balance)).replace(".",""));
        Assert.assertEquals(updatedBalance, balance - Info.AMOUNT_TRANFER);
        //home_tv_balance
    }
}
