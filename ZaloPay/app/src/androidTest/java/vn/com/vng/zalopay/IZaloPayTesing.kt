package vn.com.vng.zalopay

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.view.View
import android.widget.TextView
import org.hamcrest.Matcher


/**
 * Created by cpu11843-local on 7/26/17.
 */
interface IZaloPayTesing {
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