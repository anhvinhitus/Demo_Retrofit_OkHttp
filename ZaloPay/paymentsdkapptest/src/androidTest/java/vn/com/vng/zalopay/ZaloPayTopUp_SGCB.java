package vn.com.vng.zalopay;

import android.os.SystemClock;
import android.widget.EditText;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static vn.com.vng.zalopay.ContantTest.FAKE_PIN;
import static vn.com.vng.zalopay.ContantTest.RIGHT_PIN;
import static vn.com.vng.zalopay.ContantTest.SGCB_FAKE_OTP;

public class ZaloPayTopUp_SGCB extends ZaloPayBaseTesting
{
    @Test
    public void topup_input_card_HAPPYCASE()
    {
        SystemClock.sleep(3000);
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.ZALOPAYAPP_ID));
        onView(withId(R.id.editTextUsername)).perform(replaceText(((EditText)mActivityRule.getActivity().findViewById(R.id.editTextZaloUserID)).getText().toString()));

        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onView(withText(startsWith(ContantTest.ATM_CHANNEL_NAME))).perform(click());
        SystemClock.sleep(1000);

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.SGCB_CARDNUMBER_TRANG));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(ContantTest.SGCB_CARDNAME_TRANG));

        //submit
        onView(withId(R.id.next)).perform(click());
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());

        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_otp)).perform(typeText(ContantTest.SGCB_FAKE_OTP),closeSoftKeyboard());
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());

        SystemClock.sleep(5000);
    }

    @Test
    public void topup_mapcard_flow_cardtest_WRONG_PIN()
    {
        SystemClock.sleep(3000);
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.ZALOPAYAPP_ID));
        onView(withId(R.id.editTextUsername)).perform(replaceText(((EditText)mActivityRule.getActivity().findViewById(R.id.editTextZaloUserID)).getText().toString()));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onView(withText(endsWith(ContantTest.SGCB_MAPCARD_ACCOUNTTEST))).perform(click());
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

    @Test
    public void topup_mapcard_flow_cardtest_HAPPYCASE()
    {
        SystemClock.sleep(3000);
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.ZALOPAYAPP_ID));
        onView(withId(R.id.editTextUsername)).perform(replaceText(((EditText)mActivityRule.getActivity().findViewById(R.id.editTextZaloUserID)).getText().toString()));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onView(withText(endsWith(ContantTest.SGCB_MAPCARD_ACCOUNTTEST))).perform(click());
        //cick confirm
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(500);
        //input pin
        onView(withId(R.id.inputView)).perform(typeText(RIGHT_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

}
