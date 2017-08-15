package vn.com.vng.zalopay.zpc.ui;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 7/21/17.
 * *
 */

public interface SyncContactView extends ILoadDataView {

    void hideContactBookCount();

    void setContactBookCount(long count);

    void setFriendListCount(long count);

    void setLastTimeSyncContact(String timestamp);

    void showSyncContactSuccess();

    void showAvatarArrow();

    void hideAvatarArrow();
}
