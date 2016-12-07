package vn.com.vng.zalopay.webview.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 14/09/2016.
 *
 */
public interface IWebView extends ILoadDataView {

    Activity getActivity();

    Fragment getFragment();

    void showInputErrorDialog();

    void loadUrl(String urlPage);
}
