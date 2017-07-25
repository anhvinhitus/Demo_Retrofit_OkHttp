package vn.com.vng.zalopay.tranfer

import android.os.SystemClock
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.v7.widget.RecyclerView
import junit.framework.Assert
import org.junit.Test
import vn.com.vng.zalopay.AbtractZaloPayTesting
import vn.com.vng.zalopay.R
import vn.com.vng.zalopay.sb.Info

/**
 * Created by cpu11843-local on 7/25/17.
 */

class ZaloPayTransferKotlin : AbtractZaloPayTesting() {
    override fun initTest() {
        super.initTest()
    }

    @Test
    fun transfer_with_zalopayID_HAPPYCASE() {
        SystemClock.sleep(5000)
        var transfer_item_name = mResource.getString(R.string.transfer_money)
        val balance = java.lang.Long.parseLong(getText(withId(R.id.tv_balance)).replace(".", ""))
        //select tranfer
        onView(withId(R.id.home_rcv_list_app)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        //click item tranfer by zalopay name
        onView(withId(R.id.layoutTransferViaAccount)).perform(click())
        //fill zalopay name to get tranfer money
        onView(withId(R.id.edtAccountName)).perform(replaceText(Info.ZALOPAY_NAME_TRANFER))
        //click button
        onView(withId(R.id.btnContinue)).perform(click())
        //delay for check user name
        SystemClock.sleep(500)
        //input amount tranfer
        onView(withId(R.id.edtAmount)).perform(replaceText(Info.AMOUNT_TRANSFER.toString()))
        //input message tranfer
        onView(withId(R.id.edtTransferMsg)).perform(replaceText(Info.MESSAGE_TRANSFER))
        //click button
        onView(withId(R.id.btnContinue)).perform(click())

        transfer_happen_insdk(balance)
    }

    private fun transfer_happen_insdk(balance: Long) {
        //delay for sdk check payment info
        SystemClock.sleep(2000)
        try {
            inputPin(balance)
        } catch (e: NoMatchingViewException) {
            //onRow(Info.CHANNEL_ZALOPAY_NAME).perform(click());
            onView(withId(R.id.channel_list_recycler)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            SystemClock.sleep(2000)
            //click confirm button
            onView(withId(R.id.confirm_button)).perform(click())
            inputPin(balance)
        }
    }

    fun inputPin(balance: Long) {
        // check if show popup
        onView(withId(R.id.pin_code_button_0)).check(matches(isDisplayed()))
        // type pin
        typePin(Info.PASSWORD)
        //delay for get status
        SystemClock.sleep(5000)
        //click button finish on result screen
        onView(withId(R.id.zpsdk_btn_submit)).perform(click())
        //delay for update balance and release payment resource
        SystemClock.sleep(1000)
        //asset balance again
        val updatedBalance = java.lang.Long.parseLong(getText(withId(R.id.tv_balance)).replace(".", ""))
        Assert.assertEquals(updatedBalance, balance - Info.AMOUNT_TRANSFER)
    }

    private fun typePin(pin: String) {
        SystemClock.sleep(100)
        for (s in pin) {
            when (s) {
                '0' -> onView(withId(R.id.pin_code_button_0)).perform(click())
                '1' -> onView(withId(R.id.pin_code_button_1)).perform(click())
                '2' -> onView(withId(R.id.pin_code_button_2)).perform(click())
                '3' -> onView(withId(R.id.pin_code_button_3)).perform(click())
                '4' -> onView(withId(R.id.pin_code_button_4)).perform(click())
                '5' -> onView(withId(R.id.pin_code_button_5)).perform(click())
                '6' -> onView(withId(R.id.pin_code_button_6)).perform(click())
                '7' -> onView(withId(R.id.pin_code_button_7)).perform(click())
                '8' -> onView(withId(R.id.pin_code_button_8)).perform(click())
                '9' -> onView(withId(R.id.pin_code_button_9)).perform(click())
                else -> onView(withId(R.id.pin_code_button_clear)).perform(click())
            }
        }
    }

}
