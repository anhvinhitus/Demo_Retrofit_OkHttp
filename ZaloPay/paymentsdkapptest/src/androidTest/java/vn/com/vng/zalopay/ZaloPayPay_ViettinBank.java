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

public class ZaloPayPay_ViettinBank extends ZaloPayBaseTesting
{
    /***
     * test pay app 3,atm
     * parse web fail
     */
    @Test
    public void app_3_input_card_FAIL_WEB()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(ATM_CHANNEL_NAME).perform(click());
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
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(50000);
    }

    /***
     * test pay 3 app
     * viettinbank auto fill otp
     */
    @Test
    public void app_3_input_card_HAPPY_CASE()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select zalopay channel
        onRow(ATM_CHANNEL_NAME).perform(click());
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
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(50000);
    }
    /**
     * pay app 3 with ATM Vietinbank  fail otp
     */
    @Test
    public void app_3_otp_flow_WRONG_OTP()
    {
        SystemClock.sleep(3000);
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select zalopay channel
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

    /**
     * pay app 3 with ATM Vietinbank  fail CapCha 3 times
     */
    @Test
    public void app_3_input_card_WRONG_CAPTCHA()
    {
        SystemClock.sleep(3000);
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select zalopay channel

        onRow(ATM_CHANNEL_NAME).perform(click());

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
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_captchar_ctl)).perform(typeText(ContantTest.FAKE_CAPCHA),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(2000);
        //the first time retry
        onView(withText(startsWith(ContantTest.BUTTON_CLOSE))).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_captchar_ctl)).perform(typeText(ContantTest.FAKE_CAPCHA),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(2000);
        //the first time retry
        onView(withText(startsWith(ContantTest.BUTTON_CLOSE))).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_captchar_ctl)).perform(typeText(ContantTest.FAKE_CAPCHA),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
        //the first time retry

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(2000);

    }
    @Test
    public void app_3_input_card_NO_INTERNET()
    {
        SystemClock.sleep(3000);
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(ATM_CHANNEL_NAME).perform(click());

        SystemClock.sleep(1000);

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.CARDNUMBER_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_issue_date)).perform(typeText(ContantTest.CARDDATE_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(ContantTest.CARDNAME_LYTM));
        SystemClock.sleep(10000);

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);
        onView(withText(startsWith(BUTTON_CLOSE))).perform(click());

    }
}
