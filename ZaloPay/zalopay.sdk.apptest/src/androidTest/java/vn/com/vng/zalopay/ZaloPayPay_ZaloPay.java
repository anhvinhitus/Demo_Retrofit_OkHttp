package vn.com.vng.zalopay;

import android.os.SystemClock;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.startsWith;
import static vn.com.vng.zalopay.ContantTest.APP_TEST_ID;
import static vn.com.vng.zalopay.ContantTest.ATM_CHANNEL_NAME;
import static vn.com.vng.zalopay.ContantTest.BUTTON_CLOSE;
import static vn.com.vng.zalopay.ContantTest.CC_CARDCVV_HSBC_THANH;
import static vn.com.vng.zalopay.ContantTest.CC_CARDCVV_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_CITYBANK;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_HSBC_THANH;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNAME_CITYBANK;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNAME_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNAME_HSBC_THANH;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNAME_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_CITYBANK;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_HSBC_THANH;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARD_CVV_CITYBANK;
import static vn.com.vng.zalopay.ContantTest.CC_CCV_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CHANNEL_NAME;
import static vn.com.vng.zalopay.ContantTest.FAKE_OTP;
import static vn.com.vng.zalopay.ContantTest.FAKE_PIN;
import static vn.com.vng.zalopay.ContantTest.RIGHT_PIN;
import static vn.com.vng.zalopay.ContantTest.ZALOPAY_CHANNEL_NAME;

public class ZaloPayPay_ZaloPay extends ZaloPayBaseTesting
{
    /***
     * test payment app 3
     * select zalopay channel
     * wrong pin flow
     */
    @Test
    public void app_3_WRONG_PIN()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select zalopay channel
        onRow(ZALOPAY_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);
        //cick confirm
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(500);
        //input pin
        onView(withId(R.id.inputView)).perform(typeText(FAKE_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.inputView)).perform(typeText(FAKE_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.inputView)).perform(typeText(FAKE_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.inputView)).perform(typeText(FAKE_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    /***
     * buy app 3 success
     */
    @Test
    public void app_3_HAPPY_CASE()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select zalopay channel
        onRow(ZALOPAY_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);
        //cick confirm
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(500);

        //input pin
        onView(withId(R.id.inputView)).perform(typeText(RIGHT_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.zpw_rippleview_continue)).perform(click());
        SystemClock.sleep(5000);
    }

    /***
     * buy app 3 success with fingerprint
     */
    @Test
    public void app_3_fingerprint_HAPPY_CASE()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select zalopay channel
        onRow(ZALOPAY_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);
        //cick confirm
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpw_rippleview_continue)).perform(click());
        SystemClock.sleep(5000);
    }

    @Test
    public void testSupportView()
    {
        SystemClock.sleep(3000);
        onView(withId(R.id.editTextUsername)).perform(replaceText(ContantTest.USER_ID_TRANSFER));
        onView(withId(R.id.chkTransfer)).perform(click());
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.ZALOPAYAPP_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select zalopay channel
        onRow(ContantTest.ZALOPAY_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        //cick confirm
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.inputView)).perform(typeText(ContantTest.FAKE_PIN));
        SystemClock.sleep(500);

        onView(withId(R.id.inputView)).perform(typeText(ContantTest.FAKE_PIN));
        SystemClock.sleep(500);

        onView(withId(R.id.inputView)).perform(typeText(ContantTest.FAKE_PIN));
        SystemClock.sleep(500);

        onView(withId(R.id.inputView)).perform(typeText(ContantTest.FAKE_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.zpw_submit_support)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.cancel_button)).perform(click());
        SystemClock.sleep(1000);

    }
}
