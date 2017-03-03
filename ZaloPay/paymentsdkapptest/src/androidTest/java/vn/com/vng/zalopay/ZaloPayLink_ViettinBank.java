package vn.com.vng.zalopay;

import android.os.SystemClock;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
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

public class ZaloPayLink_ViettinBank extends ZaloPayBaseTesting
{
    /***
     * test link card viettinbank
     * wrong otp flow
     */
    @Test
    public void test_otp_flow_WRONG_OTP()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.CARDNUMBER_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_issue_date)).perform(typeText(ContantTest.CARDDATE_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(ContantTest.CARDNAME_LYTM));

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

    /***
     * test link card
     * card has an error
     */
    @Test
    public void input_card_ERROR()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CARDNUMBER_CHUCVV));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_issue_date)).perform(typeText(CARDDATE_CHUCVV));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CARDNAME_CHUCVV));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    @Test
    public void otp_flow_SUCCESS()
    {
        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CARDNUMBER_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_issue_date)).perform(typeText(CARDDATE_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CARDNAME_LYTM));

        //submit
        onView(withId(R.id.next)).perform(click());

        //waiting for otp comming
        SystemClock.sleep(30000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }


    @Test
    public void input_card_LINK_EXIST()
    {
        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CARDNUMBER_LYTM));

        onView(withId(R.id.textlayout_localcard_number)).check(matches(withCustomHint(is(mActivityRule.getActivity().getResources().getString(R.string.zpw_link_card_existed)))));
    }

}
