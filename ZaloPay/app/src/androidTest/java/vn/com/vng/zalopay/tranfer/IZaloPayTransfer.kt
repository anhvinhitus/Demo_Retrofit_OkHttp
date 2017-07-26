package vn.com.vng.zalopay.tranfer

import android.os.SystemClock
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.v7.widget.RecyclerView
import junit.framework.Assert
import junit.framework.Assert.fail
import org.hamcrest.CoreMatchers
import vn.com.vng.zalopay.IZaloPayTesing
import vn.com.vng.zalopay.R
import vn.com.vng.zalopay.sb.EZaloPayTransfer
import vn.com.vng.zalopay.sb.Info

/**
 * Created by cpu11843-local on 7/26/17.
 */
interface IZaloPayTransfer : IZaloPayTesing {
    /***
     * process In App
     * Route Screen
     */
    fun processInApp(mode: EZaloPayTransfer, balance: Long, amount: Long) {
        //TODO("Main Screen")
        SystemClock.sleep(3000)
        when (mode) {
            EZaloPayTransfer.TRANSFER_MAIN_SCREEN,
            EZaloPayTransfer.TRANSFER_VERIFY_LIST_NEAR_TRANS -> onView(ViewMatchers.withId(R.id.home_rcv_list_app)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click()))
            else -> {
            }
        }

        //TODO("Next Action")
        SystemClock.sleep(3000)
        when (mode) {
            EZaloPayTransfer.TRANSFER_MAIN_SCREEN -> processInTransferScreen(balance, amount)
            EZaloPayTransfer.TRANSFER_VERIFY_LIST_NEAR_TRANS -> verifyNearListTrans()
            else -> {
            }
        }
    }

    private fun processInTransferScreen(balance: Long, amount: Long) {
        // click item transfer by zalopay name
        onView(ViewMatchers.withId(R.id.layoutTransferViaAccount)).perform(ViewActions.click())
        // fill zalopay name to get transfer money
        onView(ViewMatchers.withId(R.id.edtAccountName)).perform(ViewActions.replaceText(Info.ZALOPAY_NAME_TRANSFER))
        // click button
        onView(ViewMatchers.withId(R.id.btnContinue)).perform(ViewActions.click())
        // delay for check user name
        SystemClock.sleep(500)
        // input amount transfer
        onView(ViewMatchers.withId(R.id.edtAmount)).perform(ViewActions.replaceText(amount.toString()))
        // input message transfer
        onView(ViewMatchers.withId(R.id.edtTransferMsg)).perform(ViewActions.replaceText(Info.MESSAGE_TRANSFER))
        // delay before press button
        SystemClock.sleep(500)
        // click button
        Espresso.onView(ViewMatchers.withId(R.id.btnContinue)).perform(ViewActions.click())
        // delay for call sdk
        SystemClock.sleep(500)
        // process in sdk
        processInSDK(balance, amount)
    }

    private fun processInSDK(balance: Long, amount: Long) {
        //delay for sdk check payment info
        SystemClock.sleep(2000)
        // check balance after transfer
        checkBalanceAfterTransfer(balance, amount)
    }

    private fun checkBalanceAfterTransfer(balance: Long, amount: Long) {
        try {
            onView(ViewMatchers.withId(R.id.pin_code_first_row)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            // type pin
            typePin(Info.PASSWORD)
            // delay for get status
            SystemClock.sleep(2000)
            // click button finish on result screen
            onView(ViewMatchers.withId(R.id.zpsdk_btn_submit)).perform(ViewActions.click())
            // delay for update balance and release payment resource
            SystemClock.sleep(1000)
            // asset balance again
            val updatedBalance = java.lang.Long.parseLong(getText(withId(R.id.tv_balance)).replace(".", ""))
            Assert.assertEquals(updatedBalance, balance - amount)
        } catch (e: NoMatchingViewException) {
            // select item 0 in list
            onView(ViewMatchers.withId(R.id.channel_list_recycler)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click()))
            // delay
            SystemClock.sleep(1000)
            // click confirm button
            onView(ViewMatchers.withId(R.id.confirm_button)).perform(ViewActions.click())
            //delay for wait to show keyboard
            SystemClock.sleep(2000)
            // call again
            checkBalanceAfterTransfer(balance, amount)
        }
    }

    /***
     * check
     */
    fun verifyNearListTrans() {
        try {
            onView(ViewMatchers.withId(R.id.list)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView(ViewMatchers.withId(R.id.tvPhone)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            SystemClock.sleep(1000)
            onView(CoreMatchers.allOf(ViewMatchers.withId(R.id.tvPhone), ViewMatchers.withText("Zalo Pay ID: ${Info.ZALOPAY_NAME_TRANSFER}"))).check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
        } catch (e: NoMatchingViewException) {
            fail(e.message)
        }
    }
}