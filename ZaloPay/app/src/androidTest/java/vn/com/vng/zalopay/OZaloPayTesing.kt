package vn.com.vng.zalopay

import android.content.res.Resources
import android.support.annotation.CallSuper
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.DataInteraction
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.EditText
import android.widget.TextView
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import vn.com.vng.zalopay.ui.activity.SplashScreenActivity
import vn.com.zalopay.wallet.entity.gatewayinfo.MiniPmcTransType
import java.lang.reflect.InvocationTargetException


/**
 * Created by cpu11843-local on 7/26/17.
 */
@RunWith(AndroidJUnit4::class)
open class OZaloPayTesing {
    //this is run to init main activity
    @Rule @JvmField
    var mActivityRule: ActivityTestRule<SplashScreenActivity>? = ActivityTestRule(SplashScreenActivity::class.java)

    //invoke some resource such as string, color...
    protected var mResource: Resources? = null

    protected fun onRow(str: String): DataInteraction {
        return onData(withContent(str))
    }

    protected fun withContent(content: String): Matcher<Any> {
        return object : BoundedMatcher<Any, MiniPmcTransType>(MiniPmcTransType::class.java) {
            public override fun matchesSafely(myObj: MiniPmcTransType): Boolean {
                return myObj.pmcname == content
            }

            override fun describeTo(description: Description) {
                description.appendText("with content '$content'")
            }
        }
    }

    @CallSuper
    protected open fun initTest() {
        mResource = InstrumentationRegistry.getTargetContext().resources
    }

    @CallSuper
    protected fun releaseTest() {
        mResource = null
        mActivityRule = null
    }

    protected fun withCustomHint(stringMatcher: Matcher<String>): Matcher<View> {
        return object : BaseMatcher<View>() {
            override fun describeTo(description: Description) {}

            override fun matches(item: Any): Boolean {
                try {
                    val method = item.javaClass.getMethod("getHint")
                    return stringMatcher.matches(method.invoke(item))
                } catch (e: NoSuchMethodException) {
                } catch (e: InvocationTargetException) {
                } catch (e: IllegalAccessException) {
                }

                return false
            }
        }
    }

    /***
     * init neccesary things before testing
     */
    @Before
    fun beforeTest() {
        initTest()
    }

    /***
     * release all things after finishing testing
     */
    @After
    fun afterTest() {
        releaseTest()
    }

    internal fun isEditTextValueEqualTo(content: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Match Edit Text Value with View ID Value : :  " + content)
            }

            public override fun matchesSafely(view: View?): Boolean {
                if (view !is TextView && view !is EditText) {
                    return false
                }
                if (view != null) {
                    val text: String
                    if (view is TextView) {
                        text = view.text.toString()
                    } else {
                        text = (view as EditText).text.toString()
                    }
                    return text.equals(content, ignoreCase = true)
                }
                return false
            }
        }
    }

    /***
     * type pin
     */
    fun typePin(pin: String) {
        for (s in pin) {
            when (s) {
                '0' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_0)).perform(ViewActions.click())
                '1' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_1)).perform(ViewActions.click())
                '2' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_2)).perform(ViewActions.click())
                '3' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_3)).perform(ViewActions.click())
                '4' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_4)).perform(ViewActions.click())
                '5' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_5)).perform(ViewActions.click())
                '6' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_6)).perform(ViewActions.click())
                '7' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_7)).perform(ViewActions.click())
                '8' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_8)).perform(ViewActions.click())
                '9' -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_9)).perform(ViewActions.click())
                else -> Espresso.onView(ViewMatchers.withId(R.id.pin_code_button_clear)).perform(ViewActions.click())
            }
        }
    }

    /***
     *get get withID
     */
    fun getText(matcher: Matcher<View>): String {
        var stringHolder = ""
        onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun perform(uiController: UiController, view: View) {
                val tv = view as TextView //Save, because of check in getConstraints()
                stringHolder = tv.text.toString()
            }
        })
        return stringHolder
    }
}