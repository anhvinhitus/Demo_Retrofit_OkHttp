package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.database.Cursor;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

interface IZaloFriendListView extends ILoadDataView {
    void swapCursor(Cursor cursor);

    void setRefreshing(boolean var);

    void checkIfEmpty();
}
