package vn.com.vng.zalopay.zpc.ui;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.AbsListView;

import java.util.List;

import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public interface ContactListView extends ILoadDataView {
    void swapCursor(Cursor cursor);

    void setRefreshing(boolean var);

    void checkIfEmpty();

    void setTitle(String title);

    void setSubTitle(String subTitle);

    void setFavorite(List<FavoriteData> persons);

    void setMaxFavorite(int maxFavorite);

    void requestReadContactsPermission();

    void closeAllSwipeItems(AbsListView listView);

    void showNotificationDialog();

    void closeAllSwipeItems();

    Fragment getFragment();

    void focusEdtSearchView();

    void setProfileNotInZPC(@NonNull ZPProfile profile);

    void showDefaultProfileNotInZPC(@NonNull ZPProfile profile);

    void monitorTimingLoadZPCEnd();
}
