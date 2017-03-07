package vn.com.vng.zalopay;

import android.os.SystemClock;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by cpu11843-local on 1/9/17.
 */

public class ZaloPayLinkAccTest extends ZaloPayBaseTesting {
    /***
     * test link acc with VCB
     */
    @Test
    public void testLinkAcc_VCB_Link(){
        SystemClock.sleep(3000);

        onView(withId(R.id.chkLinkAcc)).perform(click());
        onView(withId(R.id.radio_link)).perform(click());
        SystemClock.sleep(100);
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.edt_login_username)).perform(typeText(ContantTest.VCB_ACCOUNT_USERNAME));
        onView(withId(R.id.edt_login_password)).perform(typeText(ContantTest.VCB_ACCOUNT_PASSWORD));
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
    }

    /***
     * test unlink acc with VCB
     */
    @Test
    public void testLinkAcc_VCB_Unlink(){
        SystemClock.sleep(3000);

        onView(withId(R.id.chkLinkAcc)).perform(click());
        onView(withId(R.id.radio_unlink)).perform(click());
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(5000);
    }
}
