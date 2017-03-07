package vn.com.vng.zalopay;

import android.os.SystemClock;
import android.widget.EditText;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;
import static vn.com.vng.zalopay.ContantTest.WITHDRAWAPP_ID;

public class ZaloPayWithDraw_SGCB extends ZaloPayBaseTesting
{
    @Test
    public void withdraw_WRONG_PIN()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(WITHDRAWAPP_ID));
        onView(withId(R.id.chkWithDraw)).perform(click());
        onView(withId(R.id.editTextUsername)).perform(replaceText(((EditText)mActivityRule.getActivity().findViewById(R.id.editTextZaloUserID)).getText().toString()));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onView(withText(endsWith(ContantTest.SGCB_MAPCARD_ACCOUNTTEST))).perform(click());
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
    public void withdraw_HAPPYCASE()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(WITHDRAWAPP_ID));
        onView(withId(R.id.chkWithDraw)).perform(click());
        onView(withId(R.id.editTextUsername)).perform(replaceText(((EditText)mActivityRule.getActivity().findViewById(R.id.editTextZaloUserID)).getText().toString()));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onView(withText(endsWith(ContantTest.SGCB_MAPCARD_ACCOUNTTEST))).perform(click());
        SystemClock.sleep(1000);

        //cick confirm
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.inputView)).perform(typeText(ContantTest.RIGHT_PIN));
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }
}
