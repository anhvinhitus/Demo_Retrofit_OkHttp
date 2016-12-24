package vn.com.vng.zalopay.withdraw.ui.view;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 11/08/2016.
 */
public interface IWithdrawView extends ILoadDataView {
    Activity getActivity();

    Fragment getFragment();

    void showAmountError(String error);
}
