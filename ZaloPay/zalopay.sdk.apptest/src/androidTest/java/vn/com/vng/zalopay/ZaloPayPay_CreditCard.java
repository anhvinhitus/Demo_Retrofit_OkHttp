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

public class ZaloPayPay_CreditCard extends ZaloPayBaseTesting
{
    /***
     * pay app 3 by cc card
     * load website
     */
    @Test
    public void app_3_citibank_load_3ds()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        //onView(withId(R.id.editTextPrice)).perform(replaceText("20000000"),closeSoftKeyboard());
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select cc channel
        onRow(CC_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER_CITYBANK));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE_CITYBANK));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARD_CVV_CITYBANK));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME_CITYBANK));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(60000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
    }

    /***
     * testing app 3
     * hsbc load 3ds
     */
    @Test
    public void app_3_hsbc_load_3ds()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.editTextPrice)).perform(replaceText("20000000"),closeSoftKeyboard());
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select cc channel
        onRow(CC_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER_HSBC_THANH));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE_HSBC_THANH));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARDCVV_HSBC_THANH));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME_HSBC_THANH));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(60000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
    }

    /***
     * testing app 3
     * sacombank load 3ds
     */
    @Test
    public void app_3_sacombank_load_3ds()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(APP_TEST_ID));
        onView(withId(R.id.editTextPrice)).perform(replaceText("20000000"),closeSoftKeyboard());
        onView(withId(R.id.btn)).perform(click());
        SystemClock.sleep(1000);

        //select cc channel
        onRow(CC_CHANNEL_NAME).perform(click());
        SystemClock.sleep(1000);

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER_SACOM_CUONG));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE_SACOM_CUONG));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARDCVV_SACOM_CUONG));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME_SACOM_CUONG));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(60000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
    }
}
