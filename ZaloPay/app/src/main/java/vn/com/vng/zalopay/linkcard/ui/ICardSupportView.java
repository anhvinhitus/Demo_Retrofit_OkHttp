package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by longlv on 10/22/16.
 * *
 */
interface ICardSupportView extends ILoadDataView {

    Activity getActivity();

    void onPreComplete();
}
