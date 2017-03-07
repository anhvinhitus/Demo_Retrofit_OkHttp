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
 * link credit card test class
 */
public class ZaloPayLink_CreditCard extends ZaloPayBaseTesting
{
    /***
     * Test link card cc no 3ds
     * success
     */
    @Test
    public void non_3ds_SUCCESS()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARD_CVV));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    /***
     * Test input card Visa link card
     * success
     */
    @Test
    public void input_card_number()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER));
        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(replaceText(""));
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER_1));

        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(replaceText(""));
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER_2));


        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(replaceText(""));
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER_3));

        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(replaceText(""));
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER_4));

        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(replaceText(""));


        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER));
        SystemClock.sleep(500);
        onView(withId(R.id.next)).perform(click());
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARD_CVV));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    /***
     * Test input card Visa link card
     * success
     */
    @Test
    public void input_card_date()
    {

        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE));

        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        //onView(withId(R.id.edittext_localcard_number)).);

        //Fail ko clear text dc
        onView(withId(R.id.CreditCardExpiredDate)).perform(clearText());
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE_1));
        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        onView(withId(R.id.CreditCardExpiredDate)).perform(clearText());
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE_2));
        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        onView(withId(R.id.CreditCardExpiredDate)).perform(clearText());
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE_3));
        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        onView(withId(R.id.CreditCardExpiredDate)).perform(clearText());
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE_4));
        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        onView(withId(R.id.CreditCardExpiredDate)).perform(clearText());
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARD_CVV));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }
    /***
     * Test input card Visa link card
     * success
     */
    @Test
    public void testInputNameCreditCard()
    {

        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARD_CVV));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText("ABC"));
        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_name)).perform(clearText());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText("@#$"));
        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_name)).perform(clearText());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    /**
     * Test Switch card CC and ATM
     */
    @Test
    public void switch_atm_creditcard()
    {
        SystemClock.sleep(3000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

        //fill card info
        SystemClock.sleep(1000);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER));
        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        onView(withId(R.id.edittext_localcard_number)).perform(clearText());
        //fill card info
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CARDNUMBER_LYTM));
        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        onView(withId(R.id.edittext_localcard_number)).perform(clearText());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_MASTER_CARDNUMBER));

        SystemClock.sleep(500);
        onView(withId(R.id.previous)).perform(click());
        onView(withId(R.id.edittext_localcard_number)).perform(clearText());
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_number)).perform(typeText(CC_CARDNUMBER));
        SystemClock.sleep(500);
        onView(withId(R.id.next)).perform(click());
        onView(withId(R.id.CreditCardExpiredDate)).perform(typeText(CC_CARDDATE));
        SystemClock.sleep(500);
        onView(withId(R.id.CreditCardCVV)).perform(typeText(CC_CARD_CVV));
        SystemClock.sleep(500);
        onView(withId(R.id.edittext_localcard_name)).perform(typeText(CC_CARDNAME));

        //submit
        onView(withId(R.id.next)).perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
        SystemClock.sleep(5000);
    }

    /***
     * sacombank load 3ds
     */
    @Test
    public void sacombank_has_3ds()
    {
        SystemClock.sleep(5000);

        onView(withId(R.id.editTextAppID)).perform(replaceText(ZALOPAYAPP_ID));
        onView(withId(R.id.chkLinkCard)).perform(click());
        onView(withId(R.id.btn)).perform(click());

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
        SystemClock.sleep(600000);

        onView(withId(R.id.zpsdk_btn_submit)).perform(click());
    }
}
