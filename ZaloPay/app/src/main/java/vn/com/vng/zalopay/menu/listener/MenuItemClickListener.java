package vn.com.vng.zalopay.menu.listener;

import vn.com.vng.zalopay.menu.model.MenuItem;

/**
 * Created by longlv on 04/05/2016.
 */
public interface MenuItemClickListener {

    void onMenuItemClick(MenuItem menuItem);

    void onMenuHeaderClick(MenuItem menuItem);

    void onProfileClick();
}
