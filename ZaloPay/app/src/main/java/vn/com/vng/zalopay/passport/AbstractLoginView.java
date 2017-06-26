package vn.com.vng.zalopay.passport;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 6/25/17.
 * *
 */

interface AbstractLoginView extends ILoadDataView {
    void gotoHomePage();

    void finish();
}
