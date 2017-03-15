package vn.com.vng.zalopay.utils;

import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * Created by longlv on 3/15/17.
 * Ref http://stackoverflow.com/questions/36032177/android-new-bottom-navigation-bar/36033640#36033640
 */

public class BottomNavigationViewHelper {

    /**
     * Function to show titles of all menu elements when bottomNavigationBar has 4 tabs.
     *
     * @param view BottomNavigationView
     */
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Timber.e(e, "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Timber.e(e, "Unable to change value of shift mode");
        }
    }
}
