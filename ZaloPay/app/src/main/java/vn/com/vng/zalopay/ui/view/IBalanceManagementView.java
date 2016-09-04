package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 13/06/2016.
 */
public interface IBalanceManagementView extends ILoadDataView {
    Activity getActivity();
    void updateBalance(long balance);
    void updateUserInfo(User user);
}
