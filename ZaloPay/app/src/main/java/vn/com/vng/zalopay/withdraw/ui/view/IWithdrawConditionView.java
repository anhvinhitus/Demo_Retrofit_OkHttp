package vn.com.vng.zalopay.withdraw.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 11/08/2016.
 *
 */
public interface IWithdrawConditionView extends ILoadDataView {
    Activity getActivity();
    void updateUserInfo(User user);
    void hideUpdateProfile();
    void showUserNote();
    void hideUserNote();
}
