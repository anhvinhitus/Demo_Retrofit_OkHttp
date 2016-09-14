package vn.com.vng.zalopay.game.ui.view;

import android.app.Activity;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 14/09/2016.
 *
 */
public interface IWebView extends ILoadDataView {

    Activity getActivity();

    void showInputErrorDialog();

    void loadUrl(String urlPage);
}
