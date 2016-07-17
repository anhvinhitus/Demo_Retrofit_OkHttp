package vn.com.vng.zalopay.account.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 19/05/2016.
 */
public interface IPinProfileView extends ILoadDataView {
    Activity getActivity();
    void updateProfileSuccess(String phone);
}

