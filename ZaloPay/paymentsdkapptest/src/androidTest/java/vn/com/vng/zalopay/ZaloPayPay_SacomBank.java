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

public class ZaloPayPay_SacomBank extends ZaloPayBaseTesting
{

    @Test
    public void app_3_input_card_SUCCESS()
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
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.CARDNUMBER_SACOMBANK));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(ContantTest.CARDNAME_SACOMBANK));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    @Test
    public void app_3_input_card_WRONG_OTP()
    {

        SystemClock.sleep(3000);
        onView(withId(R.id.editTextAppID)).perform(replaceText(ContantTest.APP_TEST_ID));
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        onRow(ATM_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(ContantTest.SCB_CARDNUMBER_CUONG));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(ContantTest.SCB_CARDNAME_CUONG));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        SystemClock.sleep(5000);
        onView(withId(R.id.edittext_otp)).perform(typeText(FAKE_OTP),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(1000);
        onView(withText(startsWith(BUTTON_CLOSE))).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_otp)).perform(clearText());


        onView(withId(R.id.edittext_otp)).perform(typeText(FAKE_OTP),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(1000);
        onView(withText(startsWith(BUTTON_CLOSE))).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_otp)).perform(clearText());

        onView(withId(R.id.edittext_otp)).perform(typeText(FAKE_OTP),closeSoftKeyboard());
        SystemClock.sleep(1000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
    }
}
