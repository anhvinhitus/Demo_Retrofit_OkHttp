package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 2/10/17.
 * *
 */

public interface IPaymentDataView extends ILoadDataView {
    Activity getActivity();
    Fragment getFragment();
}
