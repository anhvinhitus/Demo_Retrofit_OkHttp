package vn.com.vng.zalopay;

import android.content.res.Resources;
import android.support.annotation.CallSuper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
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
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import vn.com.vng.zalopay.ui.activity.SplashScreenActivity;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

/**
 * To ensure that espresso works as expected on your test device or emulator,
 * turn off animations on your device. Navigate to Settings -> Developer Options and turn all the following off under Drawing
 * Window animation scale
 * Transition animation scale
 * Animator duration scale
 * Created by chucvv on 6/7/17.
 */
@RunWith(AndroidJUnit4.class)
public abstract class AbtractZaloPayTesting {
    //this is run to init main activity
    @Rule
    public ActivityTestRule<SplashScreenActivity> mActivityRule = new ActivityTestRule<>(SplashScreenActivity.class);

    //invoke some resource such as string, color...
    protected Resources mResource;

    protected DataInteraction onRow(String str) {
        return onData(withContent(str));
    }

    protected Matcher<Object> withContent(final String content) {
        return new BoundedMatcher<Object, MiniPmcTransType>(MiniPmcTransType.class) {
            @Override
            public boolean matchesSafely(MiniPmcTransType myObj) {
                return myObj.pmcname.equals(content);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with content '" + content + "'");
            }
        };
    }

    @CallSuper
    protected void initTest() {
        mResource = InstrumentationRegistry.getTargetContext().getResources();
    }

    @CallSuper
    protected void releaseTest() {
        mResource = null;
        mActivityRule = null;
    }

    protected Matcher<View> withCustomHint(final Matcher<String> stringMatcher) {
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

    protected String getText(final Matcher<View> matcher) {
        final String[] stringHolder = {null};
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView) view; //Save, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        });
        return stringHolder[0];
    }

    /***
     *  init neccesary things before testing
     */
    @Before
    public void beforeTest() {
        initTest();
    }

    /***
     *  release all things after finishing testing
     */
    @After
    public void afterTest() {
        releaseTest();
    }

    Matcher<View> isEditTextValueEqualTo(final String content) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
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
                        text = ((TextView) view).getText().toString();
                    } else {
                        text = ((EditText) view).getText().toString();
                    }
                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };
    }
}
