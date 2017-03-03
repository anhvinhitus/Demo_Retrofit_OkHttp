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
import static vn.com.vng.zalopay.ContantTest.APP_TEST_ID;
import static vn.com.vng.zalopay.ContantTest.ATM_CHANNEL_NAME;
import static vn.com.vng.zalopay.ContantTest.FAKE_PIN;
import static vn.com.vng.zalopay.ContantTest.RIGHT_PIN;
import static vn.com.vng.zalopay.ContantTest.ZALOPAY_CHANNEL_NAME;

public class ZaloPayPay_VietcomBank extends ZaloPayBaseTesting
{
    /***
     * test payment app 3
     * select zalopay channel
     * wrong pin flow
     */
    @Test
    public void app_3_select_bankaccount_flow_WRONG_PIN()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(mActivityRule.getActivity().getResources().getString(R.string.zpw_channelname_vietcombank_mapaccount)).perform(click());
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
    public void app_3_select_bankaccount_flow_HAPPY_CASE()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(mActivityRule.getActivity().getResources().getString(R.string.zpw_channelname_vietcombank_mapaccount)).perform(click());
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
     * user linked bank acccount
     * pay app 3 success
     */
    @Test
    public void app_3_input_card_info_has_link_account_HAPPY_CASE()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(ATM_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.VIETCOMBANK_CARDNUMBER_START));

        onRow(mActivityRule.getActivity().getResources().getString(R.string.zpw_channelname_vietcombank_mapaccount)).perform(click());
        //cick confirm
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(2000);

        //input pin
        onView(withId(R.id.inputView)).perform(typeText(RIGHT_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.zpw_rippleview_continue)).perform(click());
        SystemClock.sleep(5000);
    }

    /***
     * user has no link bank account
     * pay app 3
     */
    @Test
    public void app_3_input_card_info_has_no_link_account_HAPPY_CASE()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(ATM_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.VIETCOMBANK_CARDNUMBER_START));

        onView(withId(R.id.confirm_button)).perform(click());
        SystemClock.sleep(5000);
    }

    /***
     * user has no link bank account
     * pay app 3
     */
    @Test
    public void app_3_input_card_info_has_no_link_account_INPUT_OTHER_CARDNUMBER()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(ATM_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.VIETCOMBANK_CARDNUMBER_START));

        onView(withId(R.id.cancel_button)).perform(click());
        SystemClock.sleep(5000);
    }
}
