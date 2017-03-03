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
import static org.hamcrest.Matchers.startsWith;
import static vn.com.vng.zalopay.ContantTest.BUTTON_CLOSE;

public class ZaloPayTopUp_ViettinBank extends ZaloPayBaseTesting
{
    @Test
    public void topup_input_card_otp_flow_WRONG_OTP()
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
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.CARDNUMBER_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_issue_date)).perform(typeText(ContantTest.CARDDATE_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(ContantTest.CARDNAME_LYTM));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(15000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_otp_ctl)).perform(typeText(ContantTest.FAKE_OTP),closeSoftKeyboard());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    @Test
    public void topup_input_card_MAINTENANCE()
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
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.CARDNUMBER_LYTM));

        onView(withId(R.id.confirm_button)).perform(click());
        SystemClock.sleep(5000);
    }

}
