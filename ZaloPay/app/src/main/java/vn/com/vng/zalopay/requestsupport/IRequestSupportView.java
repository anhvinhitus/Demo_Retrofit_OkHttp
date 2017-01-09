package vn.com.vng.zalopay.requestsupport;

import android.net.Uri;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

public interface IRequestSupportView extends ILoadDataView {
    void insertScreenshot(Uri uri);
}