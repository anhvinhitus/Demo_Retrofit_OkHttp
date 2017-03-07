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
import static vn.com.vng.zalopay.ContantTest.BUTTON_CLOSE;
import static vn.com.vng.zalopay.ContantTest.CARDDATE_CHUCVV;
import static vn.com.vng.zalopay.ContantTest.CARDDATE_LYTM;
import static vn.com.vng.zalopay.ContantTest.CARDNAME_CHUCVV;
import static vn.com.vng.zalopay.ContantTest.CARDNAME_COMMERCIAL;
import static vn.com.vng.zalopay.ContantTest.CARDNAME_LYTM;
import static vn.com.vng.zalopay.ContantTest.CARDNUMBER_CHUCVV;
import static vn.com.vng.zalopay.ContantTest.CARDNUMBER_COMMERCIAL;
import static vn.com.vng.zalopay.ContantTest.CARDNUMBER_LYTM;
import static vn.com.vng.zalopay.ContantTest.CC_CARDCVV_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_1;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_2;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_3;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_4;
import static vn.com.vng.zalopay.ContantTest.CC_CARDDATE_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNAME;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNAME_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_1;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_2;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_3;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_4;
import static vn.com.vng.zalopay.ContantTest.CC_CARDNUMBER_SACOM_CUONG;
import static vn.com.vng.zalopay.ContantTest.CC_CARD_CVV;
import static vn.com.vng.zalopay.ContantTest.CC_MASTER_CARDNUMBER;
import static vn.com.vng.zalopay.ContantTest.FAKE_OTP;
import static vn.com.vng.zalopay.ContantTest.ZALOPAYAPP_ID;

/***
 * link sacombank card test
 */
public class ZaloPayLink_SacomBank extends ZaloPayBaseTesting
{
    /***
     * test link card saccombank
     * wrong otp flow
     */
    @Test
    public void otp_flow_WRONG_OTP()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.CARDNUMBER_SACOMBANK));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(ContantTest.CARDNAME_SACOMBANK));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        //fill fake otp
        onView(withId(R.id.edittext_otp)).perform(typeText(FAKE_OTP),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);

        //the first time retry
        onView(withId(R.id.confirm_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_otp)).perform(typeText(FAKE_OTP),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
        //the second retry
        onView(withId(R.id.confirm_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_otp)).perform(typeText(FAKE_OTP),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }
}
