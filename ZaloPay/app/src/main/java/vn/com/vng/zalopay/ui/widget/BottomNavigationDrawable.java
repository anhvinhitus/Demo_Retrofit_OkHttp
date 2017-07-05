package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import com.shamanland.fonticon.FontIconDrawable;

import vn.com.vng.zalopay.R;

/**
 * Created by longlv on 4/3/17.
 * Icon font drawable with new icon at top right corner.
 */

class BottomNavigationDrawable extends FontIconDrawable {
    BottomNavigationDrawable(Context context,
                             @StringRes int icon,
                             @ColorRes int color) {

        super(context.getString(icon),
                ContextCompat.getColor(context, color),
                context.getResources().getDimension(R.dimen.font_size_tab_icon));
    }
}
