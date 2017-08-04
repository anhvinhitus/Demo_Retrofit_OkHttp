package vn.com.vng.zalopay.zpc.ui.view;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.widget.AbsListView;

import java.util.List;

import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public interface IZaloFriendListView extends ILoadDataView {
    void swapCursor(Cursor cursor);

    void setRefreshing(boolean var);

    void checkIfEmpty();

    void setSubTitle(String subTitle);

    void setFavorite(List<FavoriteData> persons);

    void setMaxFavorite(int maxFavorite);

    void requestReadContactsPermission();

    void closeAllSwipeItems(AbsListView listView);

    void showNotificationDialog();

    void closeAllSwipeItems();

    Fragment getFragment();
}
