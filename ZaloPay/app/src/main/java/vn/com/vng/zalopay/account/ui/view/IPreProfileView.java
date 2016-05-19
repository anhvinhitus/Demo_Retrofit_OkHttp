package vn.com.vng.zalopay.account.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.LoadDataView;

/**
 * Created by longlv on 19/05/2016.
 */
public interface IPreProfileView extends LoadDataView {
    Activity getActivity();
    void updateUserInfo(User user);
}

