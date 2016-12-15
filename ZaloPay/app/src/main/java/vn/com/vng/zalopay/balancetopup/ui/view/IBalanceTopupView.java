package vn.com.vng.zalopay.balancetopup.ui.view;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 10/05/2016.
 * *
 */
public interface IBalanceTopupView  extends ILoadDataView {
    Activity getActivity();
    Fragment getFragment();
}
