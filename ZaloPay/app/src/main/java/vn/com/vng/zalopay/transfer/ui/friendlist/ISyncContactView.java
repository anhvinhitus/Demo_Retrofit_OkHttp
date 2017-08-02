package vn.com.vng.zalopay.transfer.ui.friendlist;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 7/21/17.
 * *
 */

interface ISyncContactView extends ILoadDataView {

    void setContactBookCount(long count);

    void setFriendListCount(long count);

    void setLastTimeSyncContact(String timestamp);

    void showSyncContactSuccess();
}
