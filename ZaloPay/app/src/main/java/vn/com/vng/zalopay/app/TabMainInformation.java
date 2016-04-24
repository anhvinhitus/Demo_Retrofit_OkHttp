package vn.com.vng.zalopay.app;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.SmartTabLayout;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.com.vng.zalopay.ui.widget.TabView;


/**
 * Created by AnhHieu on 4/11/16.
 */
public enum TabMainInformation {
    ZALOPAY(R.string.zalopay, R.drawable.tab_home_selector, ZaloPayFragment.class),
    TRANSACTION(R.string.transaction, R.drawable.tab_tranfer_selector, ZaloPayFragment.class),
    NOTIFICATION(R.string.notification, R.drawable.tab_notification_selector, ZaloPayFragment.class),
    ME(R.string.me, R.drawable.tab_me_selector, ZaloPayFragment.class);

    public final int titleResId;
    public final int iconResId;
    public final Class _class;

    TabMainInformation(int title, int iconRes, Class _class) {
        this.titleResId = title;
        this.iconResId = iconRes;
        this._class = _class;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public Class getClassFragment() {
        return _class;
    }


    public static final TabMainInformation getItem(int position) {
        if (position >= 0 && position < 4)
            return TabMainInformation.values()[position];
        return null;
    }


    public static class SimpleTabProvider implements SmartTabLayout.TabProvider {
        @Override
        public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {

            Timber.i("createTabView" + position);
            TabView tabView = new TabView(container.getContext());
            tabView.setTabInformation(TabMainInformation.getItem(position));
            return tabView;
        }
    }
}
