package vn.com.vng.zalopay.transfer.ui;

import de.greenrobot.dao.query.LazyList;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 11/06/2016.
 */
public interface IZaloContactView extends ILoadDataView {
    void showGetZFriendFromServerError();
    void showGetZFriendFromServerTimeout();
    void updateZFriendList(LazyList<ZaloFriendGD> items);
    String getTextSearch();
    void showRefreshView();
    void hideRefreshView();
}
