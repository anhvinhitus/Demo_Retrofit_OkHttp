package vn.com.vng.zalopay.transfer.ui.view;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 11/06/2016.
 */
public interface IZaloContactView extends ILoadDataView {
    void onGetZaloFriendError();
    void onZaloFriendUpdated();
    void onGetZaloFriendFinish();
    void onGetZaloFriendTimeout();
}
