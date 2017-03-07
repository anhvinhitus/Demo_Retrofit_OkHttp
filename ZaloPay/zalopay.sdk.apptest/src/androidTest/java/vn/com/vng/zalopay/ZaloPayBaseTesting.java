package vn.com.vng.zalopay;


import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DPaymentChannelView;

import static android.support.test.espresso.Espresso.onData;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ZaloPayBaseTesting
{
    /***
     * this is run to init main activity
     */
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    /***
     * init neccesary things before testing
     */
    @Before
    public void beforeTest()
    {

    }

    /***
     * release all things after finishing testing
     */
    @After
    public void afterTest()
    {

    }

    public static DataInteraction onRow(String str)
    {
        return onData(withContent(str));
    }

    public static Matcher<Object> withContent(final String content)
    {
        return new BoundedMatcher<Object, DPaymentChannelView>(DPaymentChannelView.class)
        {
            @Override
            public boolean matchesSafely(DPaymentChannelView myObj)
            {
                return myObj.pmcname.equals(content);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with content '" + content + "'");
            }
        };
    }

    public static Matcher<View> withCustomHint(final Matcher<String> stringMatcher)
    {
        return new BaseMatcher<View>() {
            @Override
            public void describeTo(Description description) {
            }

            @Override
            public boolean matches(Object item) {
                try {
                    Method method = item.getClass().getMethod("getHint");
                    return stringMatcher.matches(method.invoke(item));
                } catch (NoSuchMethodException e) {
                } catch (InvocationTargetException e) {
                } catch (IllegalAccessException e) {
                }
                return false;
            }
        };
    }

    Matcher<View> isEditTextValueEqualTo(final String content)
    {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Match Edit Text Value with View ID Value : :  " + content);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView) && !(view instanceof EditText)) {
                    return false;
                }
                if (view != null) {
                    String text;
                    if (view instanceof TextView) {
                        text =((TextView) view).getText().toString();
                    } else {
                        text =((EditText) view).getText().toString();
                    }

                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };
    }

    @Test
    public void testIsOwnContext()
    {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("zalopay.vng.com.vn.wallettest", appContext.getPackageName());
    }
}
