package vn.com.vng.zalopay;

import android.os.SystemClock;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;

/**
 * Created by lytm on 09/01/2017.
 */
public class ZaloPayTransfer_ZaloPay extends ZaloPayBaseTesting {


    @Test
    public void WRONG_OTP()
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

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);

    }

    @Test
    public void HAPPYCASE()
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

        onView(withId(R.id.inputView)).perform(typeText(ContantTest.RIGHT_PIN));
        SystemClock.sleep(500);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);

    }
}
